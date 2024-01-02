package com.wzy.assist.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.wzy.assist.ui.MainActivity
import com.wzy.assist.service.SideBarService
import com.wzy.assist.utils.PermissionUtil

class BootBroadcastReceiver : BroadcastReceiver() {

    private val actionBoot = "android.intent.action.BOOT_COMPLETED"

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent!!.action == actionBoot) {
            if (PermissionUtil.isCanDrawOverlays(context)
                && PermissionUtil.isAccessibilityServiceEnable(context!!)
            ) {
                launchAccessibility(context)
            } else {
                mainPageGo(context!!)
            }
        }
    }

    private fun mainPageGo(context: Context) {
        val launch = Intent(context, MainActivity::class.java)
        launch.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(launch)
    }

    private fun launchAccessibility(context: Context) {
        val intent = Intent(context, SideBarService::class.java)
        context.startService(intent)
    }
}