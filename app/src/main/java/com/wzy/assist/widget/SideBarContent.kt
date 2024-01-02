package com.wzy.assist.widget

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Message
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.Toast
import com.wzy.assist.R
import com.wzy.assist.service.SideBarService
import com.wzy.assist.utils.PermissionUtil

class SideBarContent : View.OnClickListener {

    private var mContext: Context? = null
    private var mLeft = false
    private var mContentView: LinearLayout? = null
    private var mWindowManager: WindowManager? = null
    private var mArrowView: LinearLayout? = null
    private var mSideBarService: SideBarService? = null

    private var mControlBar: ControlConfigBar? = null
    private var mSeekBarView: LinearLayout? = null
    private var mAnotherArrowView: LinearLayout? = null
    private var mTagTemp = -1

    companion object {
        private const val COUNT_DOWN_TAG = 1
        private const val COUNT_DOWN_TIME = 5000
    }

    @SuppressLint("HandlerLeak")
    private var mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                COUNT_DOWN_TAG -> goNormal()
            }
        }
    }

    @SuppressLint("InflateParams")
    fun getView(
        context: Context,
        left: Boolean,
        windowManager: WindowManager,
        params: WindowManager.LayoutParams?,
        arrowView: LinearLayout,
        sideBarService: SideBarService,
        anotherArrowView: LinearLayout?
    ): LinearLayout? {
        mContext = context
        mLeft = left
        mWindowManager = windowManager
        mArrowView = arrowView
        mSideBarService = sideBarService
        mAnotherArrowView = anotherArrowView
        // get layout
        val inflater = LayoutInflater.from(context)
        mContentView = inflater.inflate(R.layout.layout_content, null) as LinearLayout
        // init click
        mContentView?.findViewById<View>(R.id.tv_brightness)?.setOnClickListener(this)
        mContentView?.findViewById<View>(R.id.tv_back)?.setOnClickListener(this)
        mContentView?.findViewById<View>(R.id.tv_config)?.setOnClickListener(this)
        mContentView?.findViewById<View>(R.id.tv_home)?.setOnClickListener(this)
        mContentView?.findViewById<View>(R.id.tv_annotation)?.setOnClickListener(this)
        mContentView?.findViewById<View>(R.id.tv_volume)?.setOnClickListener(this)
        mContentView?.findViewById<View>(R.id.tv_backstage)?.setOnClickListener(this)
        val root = mContentView?.findViewById<LinearLayout>(R.id.root)
        if (left) {
            root?.setPadding(15, 0, 0, 0)
        } else {
            root?.setPadding(0, 0, 15, 0)
        }
        mWindowManager?.addView(mContentView, params)
        return mContentView
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.tv_back -> {
                removeOrSendMsg(remove = true, send = true)
                clearSeekBar()
                mSideBarService!!.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
            }
            R.id.tv_home -> {
                removeOrSendMsg(remove = true, send = false)
                goNormal()
                mSideBarService!!.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)
            }
            R.id.tv_annotation -> {
                removeOrSendMsg(remove = true, send = false)
                goNormal()
                annotationGo()
            }
            R.id.tv_config -> {
                removeOrSendMsg(remove = true, send = true)
                brightnessOrVolume(2)
            }
            R.id.tv_volume -> {
                removeOrSendMsg(remove = true, send = true)
                brightnessOrVolume(1)
            }
            R.id.tv_brightness -> {
                removeOrSendMsg(remove = true, send = true)
                brightnessPermissionCheck()
            }
            R.id.tv_backstage -> {
                removeOrSendMsg(remove = true, send = false)
                goNormal()
                mSideBarService!!.performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS)
            }
        }
    }

    private fun brightnessOrVolume(tag: Int) {
        if (mTagTemp == tag) {
            if (null != mSeekBarView) {
                removeSeekBarView()
            } else {
                addSeekBarView(tag)
            }
            return
        }
        mTagTemp = tag
        if (null == mControlBar) {
            mControlBar = ControlConfigBar()
        }
        if (null == mSeekBarView) {
            addSeekBarView(tag)
        } else {
            removeSeekBarView()
            addSeekBarView(tag)
        }
    }

    private fun addSeekBarView(tag: Int) {
        mSeekBarView = mControlBar!!.getView(mContext!!, mLeft, tag, this)
        mWindowManager!!.addView(mSeekBarView, mControlBar!!.mParams)
    }

    private fun removeSeekBarView() {
        if (null != mSeekBarView) {
            mWindowManager!!.removeView(mSeekBarView)
            mSeekBarView = null
        }
    }

    private fun arrowsShow() {
        mContentView?.visibility = View.GONE
        mArrowView?.visibility = View.VISIBLE
        mAnotherArrowView?.visibility = View.VISIBLE
    }

    fun clearSeekBar() {
        if (null != mSeekBarView) {
            mWindowManager?.removeView(mSeekBarView)
            mSeekBarView = null
        }
    }

    private fun goNormal() {
        arrowsShow()
        clearSeekBar()
    }

    private fun annotationGo() {
        val intent = Intent()
        intent.component = ComponentName("com.android.notes", "com.android.notes.MainActivity")
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        try {
            mContext!!.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(
                mContext,
                mContext!!.getString(R.string.app_not_find),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun removeOrSendMsg(remove: Boolean, send: Boolean) {
        if (remove) {
            mHandler.removeMessages(COUNT_DOWN_TAG)
        }
        if (send) {
            mHandler.sendEmptyMessageDelayed(
                COUNT_DOWN_TAG,
                COUNT_DOWN_TIME.toLong()
            )
        }
    }

    fun removeOtherBar() {
        mHandler.removeMessages(COUNT_DOWN_TAG)
        mHandler.sendEmptyMessageDelayed(
            COUNT_DOWN_TAG,
            100
        )
    }

    /**
     * when AccessibilityService is forced closed
     */
    fun clearCallbacks() {
        mHandler.removeCallbacksAndMessages(null)
    }

    private fun brightnessPermissionCheck() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!PermissionUtil.isSettingsCanWrite(mContext)) {
                goNormal()
                val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                intent.data = Uri.parse("package:" + mContext!!.packageName)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                mContext!!.startActivity(intent)
                Toast.makeText(
                    mContext,
                    mContext!!.getString(R.string.setting_modify_toast),
                    Toast.LENGTH_LONG
                ).show()
            } else {
                brightnessOrVolume(0)
            }
        } else {
            brightnessOrVolume(0)
        }
    }
}