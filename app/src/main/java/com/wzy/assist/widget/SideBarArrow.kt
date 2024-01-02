package com.wzy.assist.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.*
import android.widget.LinearLayout
import com.wzy.assist.R
import com.wzy.assist.service.SideBarService

class SideBarArrow : View.OnClickListener {
    private var mParams: WindowManager.LayoutParams? = null
    private var mArrowView: LinearLayout? = null
    private var mContext: Context? = null
    private var mLeft = false
    private var mWindowManager: WindowManager? = null
    private var mSideBarService: SideBarService? = null
    private var mContentBar: SideBarContent? = null
    private var mContentBarView: LinearLayout? = null
    private var mAnotherArrowView: LinearLayout? = null


    @SuppressLint("InflateParams")
    fun getView(
        context: Context,
        left: Boolean,
        windowManager: WindowManager,
        sideBarService: SideBarService
    ): LinearLayout? {
        mContext = context
        mLeft = left
        mWindowManager = windowManager
        mSideBarService = sideBarService
        mParams = WindowManager.LayoutParams()
        // compatible
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mParams?.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            mParams?.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
        }
        // set bg transparent
        mParams?.format = PixelFormat.RGBA_8888
        // can not focusable
        mParams?.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        mParams?.x = 0
        mParams?.y = 0
        // window size
        mParams?.width = ViewGroup.LayoutParams.WRAP_CONTENT
        mParams?.height = ViewGroup.LayoutParams.WRAP_CONTENT
        // get layout
        val inflater = LayoutInflater.from(context)
        mArrowView = inflater.inflate(R.layout.layout_arrow, null) as LinearLayout
        val arrow: View = mArrowView!!.findViewById(R.id.arrow)
        arrow.setOnClickListener(this)
        if (left) {
            arrow.rotation = 90F
            mParams?.gravity = Gravity.START or Gravity.CENTER_VERTICAL
            mParams?.windowAnimations = R.style.LeftSeekBarAnim
        } else {
            arrow.rotation = 270F
            mParams?.gravity = Gravity.END or Gravity.CENTER_VERTICAL
            mParams?.windowAnimations = R.style.RightSeekBarAnim
        }
        mWindowManager?.addView(mArrowView, mParams)
        return mArrowView
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.arrow -> {
                mArrowView?.visibility = View.GONE
                mAnotherArrowView?.visibility = View.GONE
                if (null == mContentBar || null == mContentBarView) {
                    mContentBar = SideBarContent()
                    mContentBarView = mContentBar?.getView(
                        mContext!!, mLeft,
                        mWindowManager!!, mParams, mArrowView!!,
                        mSideBarService!!, mAnotherArrowView
                    )
                } else {
                    mContentBarView?.visibility = View.VISIBLE
                }
                mContentBar?.removeOrSendMsg(remove = false, send = true)
            }
        }
    }

    fun setAnotherArrowBar(anotherArrowBar: LinearLayout) {
        mAnotherArrowView = anotherArrowBar
    }

    fun launcherInvisibleSideBar() {
        mArrowView!!.visibility = View.VISIBLE
        if (null != mContentBar || null != mContentBarView) {
            mContentBarView!!.visibility = View.GONE
            mContentBar!!.removeOrSendMsg(remove = true, send = false)
            mContentBar!!.clearSeekBar()
        }
    }

    /**
     * when AccessibilityService is forced closed
     */
    fun clearAll() {
        mWindowManager!!.removeView(mArrowView)
        if (null != mContentBar || null != mContentBarView) {
            mWindowManager!!.removeView(mContentBarView)
            mContentBar!!.clearSeekBar()
            mContentBar!!.clearCallbacks()
        }
    }
}