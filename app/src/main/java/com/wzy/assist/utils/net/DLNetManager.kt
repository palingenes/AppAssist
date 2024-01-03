package com.wzy.assist.utils.net

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkRequest
import com.wzy.assist.utils.net.impl.NetStatusCallBack

/**
 * 网络状态管理器
 *
 * @author D10NG
 * @date on 2019-10-21 16:04
 */
class DLNetManager(application: Application) {

    // 回调
    private val netStatusCallBack = NetStatusCallBack(application)

    companion object {

        @Volatile
        private var INSTANCE: DLNetManager? = null

        @JvmStatic
        fun getInstance(application: Application): DLNetManager {
            val temp = INSTANCE
            if (null != temp) {
                return temp
            }
            synchronized(this) {
                val instance = DLNetManager(application)
                INSTANCE = instance
                return instance
            }
        }
    }

    init {
        val request = NetworkRequest.Builder().build()
        val manager =
            application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        manager.registerNetworkCallback(request, netStatusCallBack)
    }

    /* fun register(activity: Any) {
         netStatusCallBack.register(activity)
     }

     fun unRegister(activity: Any) {
         netStatusCallBack.unRegister(activity)
     }

     fun unRegisterAll() {
         netStatusCallBack.unRegisterAll()
         val manager = application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
         manager.unregisterNetworkCallback(netStatusCallBack)
     }

     fun getNetType() = netStatusCallBack.getNetTypeLiveData().value?: NetType.NET_UNKNOWN

     fun getNetTypeLiveData() = netStatusCallBack.getNetTypeLiveData()*/
}