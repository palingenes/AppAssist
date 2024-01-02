package com.wzy.assist.utils.vpn

import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.wzy.assist.App
import com.wzy.assist.R
import com.wzy.assist.ui.VpnActivity
import java.io.Closeable
import java.io.FileDescriptor
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.Selector
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class LocalVPNService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null
    private var deviceToNetworkUDPQueue: ConcurrentLinkedQueue<Packet>? = null
    private var deviceToNetworkTCPQueue: ConcurrentLinkedQueue<Packet>? = null
    private var networkToDeviceQueue: ConcurrentLinkedQueue<ByteBuffer>? = null
    private lateinit var executorService: ExecutorService
    private var udpSelector: Selector? = null
    private var tcpSelector: Selector? = null

    companion object {
        private val TAG = LocalVPNService::class.java.simpleName
        const val BROADCAST_VPN_STATE = "com.wzy.assist.VPN_STATE"
        const val BAD_IP_ADDRESS = "127.0.0.1"
        const val BAD_IP_PORT = 500

        private const val VPN_ADDRESS = "10.0.0.2" // Only IPv4 support for now
        private const val VPN_ROUTE = "0.0.0.0" // Intercept everything
        private const val DNS_SERVER = "8.8.8.8" // Intercept everything
        var isRunning = false
            private set

        private fun closeResources(vararg resources: Closeable) {
            for (resource in resources) {
                try {
                    resource.close()
                } catch (e: IOException) {
                    // Ignore
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        setupVPN()
        try {
            udpSelector = Selector.open()
            tcpSelector = Selector.open()
            deviceToNetworkUDPQueue = ConcurrentLinkedQueue<Packet>()
            deviceToNetworkTCPQueue = ConcurrentLinkedQueue<Packet>()
            networkToDeviceQueue = ConcurrentLinkedQueue()
            executorService = Executors.newFixedThreadPool(5)
            executorService.submit(UDPInput(networkToDeviceQueue, udpSelector))
            executorService.submit(UDPOutput(deviceToNetworkUDPQueue!!, udpSelector, this))
            executorService.submit(TCPInput(networkToDeviceQueue, tcpSelector))
            executorService.submit(
                TCPOutput(
                    deviceToNetworkTCPQueue,
                    networkToDeviceQueue,
                    tcpSelector,
                    this
                )
            )

            vpnInterface?.fileDescriptor?.let {
                executorService.submit(
                    VPNRunnable(
                        it,
                        deviceToNetworkUDPQueue,
                        deviceToNetworkTCPQueue,
                        networkToDeviceQueue!!
                    )
                )
            }

            LocalBroadcastManager.getInstance(this)
                .sendBroadcast(Intent(BROADCAST_VPN_STATE).putExtra("running", true))
            Log.i(TAG, "Started")
        } catch (e: IOException) {
            Log.e(TAG, "Error starting service", e)
            cleanup()
        }
    }

    private fun setupVPN() {
        if (vpnInterface == null) {
            val builder = Builder()
            builder.addAddress(VPN_ADDRESS, 32)
            builder.addRoute(VPN_ROUTE, 0)
            builder.addDnsServer(DNS_SERVER)

            val pendingIntent = PendingIntent.getActivity(
                App.app,
                100,
                Intent(this, VpnActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE
            )
            vpnInterface = builder.setSession(getString(R.string.app_name))
                .setConfigureIntent(pendingIntent)
                .establish()
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        executorService.shutdownNow()
        cleanup()
        Log.i(TAG, "Stopped")
    }

    private fun cleanup() {
        deviceToNetworkTCPQueue = null
        deviceToNetworkUDPQueue = null
        networkToDeviceQueue = null
        ByteBufferPool.clear()
        closeResources(udpSelector!!, tcpSelector!!, vpnInterface!!)
    }

    private class VPNRunnable(
        private val vpnFileDescriptor: FileDescriptor,
        private val deviceToNetworkUDPQueue: ConcurrentLinkedQueue<Packet>?,
        private val deviceToNetworkTCPQueue: ConcurrentLinkedQueue<Packet>?,
        private val networkToDeviceQueue: ConcurrentLinkedQueue<ByteBuffer>
    ) : Runnable {

        companion object {
            private val TAG = VPNRunnable::class.java.simpleName
        }

        override fun run() {
            Log.i(TAG, "Started")
            val vpnInput = FileInputStream(
                vpnFileDescriptor
            ).channel
            val vpnOutput = FileOutputStream(
                vpnFileDescriptor
            ).channel
            try {
                var bufferToNetwork: ByteBuffer? = null
                var dataSent = true
                var dataReceived: Boolean
                while (!Thread.interrupted()) {
                    if (dataSent)
                        bufferToNetwork =
                            ByteBufferPool.acquire()
                    else
                        bufferToNetwork!!.clear()

                    // TODO: Block when not connected
                    val readBytes = vpnInput.read(bufferToNetwork)
                    if (readBytes > 0) {
                        dataSent = true
                        bufferToNetwork?.flip()
                        val packet = Packet(bufferToNetwork)
                        when {
                            packet.isUDP -> {
                                deviceToNetworkUDPQueue!!.offer(packet)
                            }

                            packet.isTCP -> {
                                deviceToNetworkTCPQueue!!.offer(packet)
                            }

                            else -> {
                                Log.w(TAG, "Unknown packet type")
                                Log.w(TAG, packet.ip4Header.toString())
                                dataSent = false
                            }
                        }
                    } else {
                        dataSent = false
                    }
                    val bufferFromNetwork = networkToDeviceQueue.poll()
                    if (bufferFromNetwork != null) {
                        bufferFromNetwork.flip()
                        while (bufferFromNetwork.hasRemaining()) vpnOutput.write(bufferFromNetwork)
                        dataReceived = true
                        ByteBufferPool.release(bufferFromNetwork)
                    } else {
                        dataReceived = false
                    }

                    // TODO: Sleep-looping is not very battery-friendly, consider blocking instead
                    // Confirm if throughput with ConcurrentQueue is really higher compared to BlockingQueue
                    if (!dataSent && !dataReceived) Thread.sleep(10)
                }
            } catch (e: InterruptedException) {
                Log.i(TAG, "Stopping")
            } catch (e: IOException) {
                Log.w(TAG, e.toString(), e)
            } finally {
                closeResources(vpnInput, vpnOutput)
            }
        }
    }

}
