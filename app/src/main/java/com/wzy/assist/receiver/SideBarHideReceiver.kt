package com.wzy.assist.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.wzy.assist.widget.SideBarArrow

class SideBarHideReceiver : BroadcastReceiver() {

    private var mLeft: SideBarArrow? = null
    private var mRight: SideBarArrow? = null

    companion object{
      const val  ACTION_HIDE = "com.android.sidebar.ACTION_HIDE"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent!!.action == ACTION_HIDE) {
            if (null != mLeft || null != mRight) {
                mLeft!!.launcherInvisibleSideBar()
                mRight!!.launcherInvisibleSideBar()
            }
        }
    }
    fun setSideBar(left: SideBarArrow?, right: SideBarArrow?) {
        mLeft = left
        mRight = right
    }
}