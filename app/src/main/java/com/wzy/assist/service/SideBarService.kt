package com.wzy.assist.service

import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.LinearLayout
import com.wzy.assist.App
import com.wzy.assist.constant.Constants
import com.wzy.assist.utils.net.DLNetManager
import com.wzy.assist.utils.net.utils.NetUtils
import com.wzy.assist.receiver.SideBarHideReceiver
import com.wzy.assist.utils.Tools
import com.wzy.assist.utils.net.annotation.NetType
import com.wzy.assist.utils.HttpClientUtil
import com.wzy.assist.utils.SPUtil
import com.wzy.assist.widget.SideBarArrow


class SideBarService : BaseAccessibilityService() {

    private var mReceiver: SideBarHideReceiver? = null
    private var mRightArrowBar: SideBarArrow? = null
    private var mLeftArrowBar: SideBarArrow? = null

    companion object {
        const val ACTION_HIDE = "com.wzy.lib.ACTION_HIDE"
    }

   private val handler = Handler(Looper.myLooper() ?: Looper.getMainLooper())
    private val runTask: Runnable = object : Runnable {
        override fun run() {
            sendRequest()
            handler.postDelayed(this, 30 * 1000L)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createTouchWeight()

        handler.postDelayed(runTask, 30* 1000L)

        App.runOnUiThread({
            DLNetManager.getInstance(App.app) //  在创建弹窗之后开启网络监听，不然没办法保活
        }, 5000)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.getIntExtra(Constants.TAG_RESET_TOUCH_BAR, 0) == 1) {
            release()
            createTouchWeight()
        }
        return super.onStartCommand(intent, flags, startId)
    }


    override fun onAccessibilityEvent(event: AccessibilityEvent?) {

        val pkgName = event?.packageName
        pkgName ?: return

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED)    //  监听窗口改变以及结合VPN软件的界面过滤
        {
            Tools.vpnAppList("android.net.VpnService").apply {
                SPUtil.VPN_TOP_PKG = pkgName.toString()
                Log.e("onAccessibilityEvent", "list--->${this}\tcls--->$pkgName")
                if (pkgName == Constants.VPN_DIALOG_PKG)    //  主要是第一次申请vpn时出现，“是否允许创建vpn图标……”
                {
                    return@apply
                }
                if (pkgName == Tools.getMyPkgName())    //  同包
                {
                    if (event.className == "${Tools.getMyPkgName()}.VpnActivity")  //  vpn设置页面
                    {
                        return@apply
                    }
                }
                val type = NetUtils.getNetStatus(context = applicationContext)
                if (type != NetType.VPN) {
                    Tools.checkOrStartVpnService(type)
                }
            }
        }
    }


    @SuppressLint("RtlHardcoded", "InflateParams", "UnspecifiedRegisterReceiverFlag")
    private fun createTouchWeight() {
        val position = SPUtil.TOUCH_LOCATION

        val windowManager = application.getSystemService(WINDOW_SERVICE) as WindowManager
        if (position == 0 || position == 2) {
            mRightArrowBar = SideBarArrow()
            val mArrowRight: LinearLayout? =
                mRightArrowBar?.getView(this, false, windowManager, this)
            mLeftArrowBar?.setAnotherArrowBar(mArrowRight!!)
        }
        if (position == 0 || position == 1) {
            mLeftArrowBar = SideBarArrow()
            val mArrowLeft: LinearLayout =
                mLeftArrowBar!!.getView(this, true, windowManager, this)!!
            mRightArrowBar?.setAnotherArrowBar(mArrowLeft)
        }
        // register
        val filter = IntentFilter()
        filter.addAction(ACTION_HIDE)
        mReceiver = SideBarHideReceiver()
        mReceiver!!.setSideBar(mLeftArrowBar, mRightArrowBar)
        registerReceiver(mReceiver, filter)
    }

    override fun onDestroy() {
        release()
        super.onDestroy()
    }

    private fun release() {
        if (null != mRightArrowBar) {
            mRightArrowBar?.clearAll()
            mRightArrowBar = null
        }
        if (null != mLeftArrowBar) {
            mLeftArrowBar?.clearAll()
            mLeftArrowBar = null
        }
        unregisterReceiver(mReceiver)
    }

       private fun sendRequest() {

           HttpClientUtil["http://92.223.73.34:11888/v1/log/other/?act=nw&phone=ft", object :
               HttpClientUtil.OnRequestCallBack {
               override fun onSuccess(json: String?) {
                   Log.i("HttpClientUtil", "onSuccess----->$json")
               }

               override fun onError(errorMsg: String?) {
                   Log.e("HttpClientUtil", "onError----->$errorMsg")
               }

           }]
       }
    override fun onInterrupt() {
    }
}