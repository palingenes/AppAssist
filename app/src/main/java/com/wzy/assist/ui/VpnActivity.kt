package com.wzy.assist.ui

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.net.VpnService
import android.text.Html
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.wzy.assist.App
import com.wzy.assist.base.BaseActivity
import com.wzy.assist.databinding.ActivityVpnBinding
import com.wzy.assist.ktx.checkIsDestroyed
import com.wzy.assist.utils.Tools
import com.wzy.assist.utils.vpn.LocalVPNService

/**
 * @time 2024/01/02 14:50
 * @version V1.0.1
 * @author wzy
 * </>
 * @desc 打开Vpn，以此来控制上网
 */
class VpnActivity : BaseActivity<ActivityVpnBinding>() {

    private var waitingForVPNStart = false
    private lateinit var deviceLauncher: ActivityResultLauncher<Intent>

    companion object {
        const val VPN_REQUEST_CODE = 0x0F
    }

    private val vpnStateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (LocalVPNService.BROADCAST_VPN_STATE == intent.action) {
                if (intent.getBooleanExtra("running", false)) waitingForVPNStart = false
            }
        }
    }


    override fun initView() {
        waitingForVPNStart = false

        LocalBroadcastManager.getInstance(this).registerReceiver(
            vpnStateReceiver,
            IntentFilter(LocalVPNService.BROADCAST_VPN_STATE)
        )

        deviceLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode != RESULT_OK) {
                    return@registerForActivityResult
                }
                refreshStatus()
            }

        App.runOnUiThread({
            startVPN()
        }, 60)

        checkConnect()
    }

    @SuppressLint("SetTextI18n")
    private fun checkConnect() {
        viewBinding.btnCheckConnect.setOnClickListener {
            viewBinding.btnCheckConnect.text = "检查中……"
            viewBinding.btnCheckConnect.isEnabled = false
            Thread {
                val online = Tools.isOnline()
                App.runOnUiThread {
                    val hint = if (online) {
                        "可上网，请重新配置或打开vpn"
                    } else {
                        "已拦截网络，调试请打开vpn"
                    }
                    viewBinding.btnCheckConnect.text = hint
                    viewBinding.btnCheckConnect.isEnabled = true
                    Toast.makeText(App.context, hint, Toast.LENGTH_SHORT).show()
                }
            }.start()
            App.runOnUiThread({
                if (checkIsDestroyed()) return@runOnUiThread
                viewBinding.btnCheckConnect.text = "Check Connect"
            }, 5000)
        }
    }

    @Suppress("DEPRECATION")
    private fun startVPN() {
        val vpnIntent = VpnService.prepare(this)
        if (vpnIntent != null)
            deviceLauncher.launch(vpnIntent)
        else
            onActivityResult(VPN_REQUEST_CODE, RESULT_OK, null)
    }

    override fun onResume() {
        super.onResume()
        try {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        } catch (exc: Exception) {
            exc.printStackTrace()
        }
        enableButton(!waitingForVPNStart && !LocalVPNService.isRunning)
        App.vpn_is_running = true
    }

    override fun onPause() {
        super.onPause()
        App.vpn_is_running = false
    }

    @SuppressLint("SetTextI18n")
    private fun enableButton(enable: Boolean) {
        viewBinding.llVpn.removeAllViews()

        if (enable) {
            viewBinding.btnVpn.isEnabled = true
            viewBinding.btnVpn.text = "Start VPN"
            descText(
                Html.fromHtml(
                    "<font color=\"0xcf2020\">通过打开VPN来达到防止应用上网的目的</font>",
                    0
                )
            )
            descText(Html.fromHtml("<font color=\"0xff0000\">请务必开启！！！</font>", 0))
        } else {
            viewBinding.btnVpn.isEnabled = false
            viewBinding.btnVpn.text = "Stop from Notification Bar"

            descText(Html.fromHtml("<font color=\"0x4aec4a\">已限制所有应用上网</font>", 0))
            descText(
                Html.fromHtml(
                    "<font color=\"0x4aec4a\">请先经过测试再进行实际操作</font>",
                    0
                )
            )
            descText(
                Html.fromHtml(
                    "<font color=\"0x33ec33\">不能上网后可打开正式的</font><font color=\"0xff00ff\">代理软件</font><font color=\"0x33ec33\">进行工作</font>",
                    0
                )
            )
            Toast.makeText(this, "网络监听已开启！", Toast.LENGTH_LONG).show()
            Toast.makeText(this, "已限制所有应用上网！", Toast.LENGTH_LONG).show()
            Toast.makeText(this, "请务必确认悬浮窗及VPN处于开启状态！", Toast.LENGTH_LONG).show()
            Toast.makeText(this, "遇到问题请联系开发！", Toast.LENGTH_LONG).show()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VPN_REQUEST_CODE && resultCode == RESULT_OK) {
            refreshStatus()
        }
    }

    private fun refreshStatus() {
        waitingForVPNStart = true
        startService(Intent(this, LocalVPNService::class.java))
        enableButton(false)
    }

    private fun descText(desc: CharSequence) {
        val wrapContent = ViewGroup.LayoutParams.WRAP_CONTENT
        val params = LinearLayout.LayoutParams(wrapContent, wrapContent)
        val textView = TextView(this)
        textView.textSize = 16F
        textView.text = desc
        textView.layoutParams = params
        viewBinding.llVpn.addView(textView)
    }

    override fun initData() {
    }
}