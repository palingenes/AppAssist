package com.wzy.assist.service

import android.app.IntentService
import android.content.Intent
import com.wzy.assist.App
import com.wzy.assist.constant.Constants
import com.wzy.assist.ui.VpnActivity

@Suppress("DEPRECATION")
class RSSPullService : IntentService(RSSPullService::class.simpleName) {

    @Deprecated("Deprecated in Java")
    override fun onHandleIntent(intent: Intent?) {
        if (intent == null) return
        if (App.vpn_is_running)  //  当前界面正在运行
            return
        val tag4Vpn = intent.getIntExtra(Constants.TAG_START_VPN_ACTIVITY, -1)
        if (tag4Vpn == 1) {
            val jumpIntent = Intent(this@RSSPullService, VpnActivity::class.java)
            jumpIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) //  防止出现多次activity
            jumpIntent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TASK //  清除栈顶
                        or Intent.FLAG_ACTIVITY_NEW_TASK    //  打开新栈
            )
            startActivity(jumpIntent)
        }
    }
}