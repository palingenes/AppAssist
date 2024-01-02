package com.wzy.assist.utils

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.util.Log
import com.wzy.assist.App
import com.wzy.assist.constant.Constants
import com.wzy.assist.utils.net.annotation.NetType
import com.wzy.assist.service.RSSPullService
import java.io.IOException
import java.net.MalformedURLException
import java.net.NetworkInterface
import java.net.URL
import java.util.*


object Tools {

    /* private val mWifiManager by lazy {
         App.getInstance().applicationContext?.getSystemService(Context.WIFI_SERVICE) as WifiManager
     }

     fun openWifi() {
         mWifiManager.isWifiEnabled = true
     }

     fun closeWifi() {
         mWifiManager.isWifiEnabled = false
     }

     fun wifiEnable(): Boolean {
         return mWifiManager.isWifiEnabled
     }

     fun wifiState(): Int {
         return mWifiManager.wifiState
     }*/

    private fun isDeviceInVPN() {
        try {
            val all: List<NetworkInterface> =
                Collections.list(NetworkInterface.getNetworkInterfaces())
            for (nif in all) {
                if (nif.name.equals("tun0") || nif.name.equals("ppp0")) {
                    Log.e("TAG", "isDeviceInVPN  current device is in VPN.")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun networkCheck() {
        try {
            val connectivityManager =
                App.app.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network =
                connectivityManager.activeNetwork
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
            Log.i("TAG", "networkCapabilities -> $networkCapabilities")
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    fun isOnline(): Boolean {
        val url: URL
        try {
            url = URL("http://www.baidu.com")
            url.openStream()
            return true
        } catch (e: MalformedURLException) {
        } catch (e: IOException) {
        }
        return false
    }

    /**
     * 判断是否包含对应action
     */
    fun isActionSupport(action: String): Boolean {
        val packageManager = App.app.packageManager
        val queryIntentServices =
            packageManager.queryIntentServices(Intent(action), PackageManager.GET_RESOLVED_FILTER)
        return queryIntentServices.size > 0
    }

    fun getPkgInfo() {
        val packageManager = App.app.packageManager
        val packages = packageManager.getInstalledPackages(0)
        for (pkgInfo in packages) {
            // 判断系统/非系统应用
            if (pkgInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0) // 非系统应用
            {
                val applicationLabel = packageManager.getApplicationLabel(
                    packageManager.getApplicationInfo(
                        pkgInfo.packageName,
                        PackageManager.GET_META_DATA
                    )
                ).toString()
                Log.e("getAppList", " pkgInfo = $applicationLabel")
            }
        }
    }

    fun vpnAppList(action: String): ArrayList<String> {
        val packageManager = App.app.packageManager
        val services =
            packageManager.queryIntentServices(Intent(action), PackageManager.GET_RESOLVED_FILTER)

//        val array = ArrayList<AppInfoEntity>()
//        val pkgNames = StringBuilder()
        val pkgNames = ArrayList<String>()

        for (service in services) {
            val appInfo = service.serviceInfo.applicationInfo
            /*  val loadIcon = packageManager.getApplicationLabel(
                  packageManager.getApplicationInfo(
                      packageName,
                      PackageManager.GET_META_DATA
                  )
              ).toString()*/
            val packageName = appInfo.packageName
            /*          val appName = appInfo.loadLabel(packageManager).toString()
                      val appIcon = appInfo.loadIcon(packageManager)
                      array.add(AppInfoEntity(pkgName = packageName, appName = appName, icon = appIcon))*/
//            pkgNames.append(packageName).append(",")
            pkgNames.add(packageName)
        }

//        if (pkgNames.isNotEmpty()&& pkgNames.contains(",")) {
//            val removeSuffix = pkgNames.removeSuffix(",")
//            SPUtil.VPN_PKG_ARRAY = removeSuffix.toString()
//        }
        SPUtil.VPN_PKG_LIST = pkgNames
        return pkgNames
    }

    /**
     * 两个场景调用：
     * 1. 网络变化的时候
     * 2. 界面变化（不同app切换）
     */
    fun checkOrStartVpnService(type: @NetType String) {
        if (type == NetType.NET_UNKNOWN || type == NetType.NONE) return
        if (type == NetType.VPN) return

        val topPkg = SPUtil.VPN_TOP_PKG
        val vpnPkgList = SPUtil.VPN_PKG_LIST

        val tmpIgnore = if (vpnPkgList.isNotEmpty() && topPkg.isNotEmpty()) {
            vpnPkgList.contains(topPkg)
        } else {
            false
        }

        if (tmpIgnore) return
        SPUtil.VPN_TOP_PKG = "" //  置空
        val intent = Intent(App.app, RSSPullService::class.java)
        intent.putExtra(Constants.TAG_START_VPN_ACTIVITY, 1)
        App.app.startService(intent)
    }

    fun getMyPkgName(): String {
        try {
            val packageInfo =
                App.app.packageManager.getPackageInfo(App.app.packageName, 0)
            return packageInfo.packageName
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return "com.wzy.assist"
    }
}
/**
 * [
 * Transports: WIFI|VPN
 * Capabilities: NOT_METERED&INTERNET&NOT_RESTRICTED&TRUSTED&VALIDATED&NOT_ROAMING&FOREGROUND&NOT_CONGESTED&NOT_SUSPENDED
 * LinkUpBandwidth>=1048576Kbps
 * LinkDnBandwidth>=1048576Kbps
 * ]
 * -------------------------------------------------------------------------------------------------
 * [
 * Transports: WIFI
 * Capabilities: NOT_METERED&INTERNET&NOT_RESTRICTED&TRUSTED&NOT_VPN&VALIDATED&NOT_ROAMING&FOREGROUND&NOT_CONGESTED&NOT_SUSPENDED
 * LinkUpBandwidth>=1048576Kbps
 * LinkDnBandwidth>=1048576Kbps
 * SignalStrength: -60
 * ]
 *
 * 断开wifi（无手机卡）后，networkCapabilities = null
 */