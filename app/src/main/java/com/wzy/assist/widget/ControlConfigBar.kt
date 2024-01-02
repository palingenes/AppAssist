package com.wzy.assist.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.view.*
import android.widget.LinearLayout
import android.widget.RadioButton
import com.wzy.assist.App
import com.wzy.assist.R
import com.wzy.assist.constant.Constants
import com.wzy.assist.service.SideBarService
import com.wzy.assist.utils.DisplayUtil
import com.wzy.assist.utils.SPUtil

class ControlConfigBar : View.OnClickListener {

    var mParams: WindowManager.LayoutParams? = null
    private var mSideBarContent: SideBarContent? = null

    @SuppressLint("RtlHardcoded")
    fun getView(
        context: Context,
        left: Boolean,
        tag: Int,
        sideBarContent: SideBarContent
    ): LinearLayout {
        mParams = WindowManager.LayoutParams()
        mSideBarContent = sideBarContent

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mParams!!.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            mParams!!.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
        }
        mParams!!.format = PixelFormat.RGBA_8888
        mParams!!.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        mParams!!.width = ViewGroup.LayoutParams.WRAP_CONTENT
        mParams!!.height = ViewGroup.LayoutParams.WRAP_CONTENT
        mParams!!.gravity = Gravity.LEFT or Gravity.TOP
        val inflater = LayoutInflater.from(context)
        @SuppressLint("InflateParams") val layoutConfig =
            inflater.inflate(R.layout.layout_config, null) as LinearLayout
        val w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        val h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        layoutConfig.measure(w, h)

        mParams!!.x = DisplayUtil.widthPixels() / 2 - layoutConfig.measuredWidth / 2
        mParams!!.y = DisplayUtil.heightPixels() / 2 - DisplayUtil.dip2px( 50f)
        // left or right show view
        if (left) {
//            mParams!!.x = DisplayUtil.dp2px(context, 120) - layoutConfig.measuredWidth
//            mParams!!.y = DisplayUtil.getScreenHeight(context) - DisplayUtil.dp2px(context, 282)
            mParams!!.windowAnimations = R.style.LeftSeekBarAnim
        } else {
//            mParams!!.x = DisplayUtil.getScreenWidth(context) - DisplayUtil.dp2px(context, 120)
//            mParams!!.y = DisplayUtil.getScreenHeight(context) - DisplayUtil.dp2px(context, 282)
            mParams!!.windowAnimations = R.style.RightSeekBarAnim
        }
        val btn0 = layoutConfig.findViewById<RadioButton>(R.id.btn_0)
        val btn1 = layoutConfig.findViewById<RadioButton>(R.id.btn_1)
        val btn2 = layoutConfig.findViewById<RadioButton>(R.id.btn_2)
        btn0.setOnClickListener(this)
        btn1.setOnClickListener(this)
        btn2.setOnClickListener(this)

        /* val seekBar: VerticalSeekBar = layoutConfig.findViewById(R.id.sb_)
         val plus: AppCompatImageView = layoutConfig.findViewById(R.id.plus)
         val less: AppCompatImageView = layoutConfig.findViewById(R.id.less)
         if (tag == 0) {
             // brightness control
             plus.setImageDrawable(context.getDrawable(R.drawable.ic_brightness_plus_))
             less.setImageDrawable(context.getDrawable(R.drawable.ic_brightness_less_))
             // brightness range 0~255
             seekBar.setMax(255)
             seekBar.setProgress(SystemBrightness.getBrightness(context))
             seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                 override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                     SystemBrightness.setBrightness(context, progress)
                     sideBarContent.removeOrSendMsg(true, true)
                 }

                 override fun onStartTrackingTouch(seekBar: SeekBar) {}
                 override fun onStopTrackingTouch(seekBar: SeekBar) {}
             })
         } else if (tag == 1) {
             // volume control
             plus.setImageDrawable(context.getDrawable(R.drawable.ic_volume_plus_))
             less.setImageDrawable(context.getDrawable(R.drawable.ic_volume_less_))
             // volume range 0~15
             seekBar.setMax(15)
             seekBar.setProgress(SystemVolume.getVolume(context))
             seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                 override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                     SystemVolume.setVolume(context, progress)
                     sideBarContent.removeOrSendMsg(true, true)
                 }

                 override fun onStartTrackingTouch(seekBar: SeekBar) {}
                 override fun onStopTrackingTouch(seekBar: SeekBar) {}
             })
         }*/

        when (SPUtil.TOUCH_LOCATION) {
            0 -> {
                btn0.isChecked = true
                btn1.isChecked = false
                btn2.isChecked = false
            }
            1 -> {
                btn0.isChecked = false
                btn1.isChecked = true
                btn2.isChecked = false
            }
            2 -> {
                btn0.isChecked = false
                btn1.isChecked = false
                btn2.isChecked = true
            }
        }

        return layoutConfig
    }

    override fun onClick(v: View?) {
        if (v == null) return
        when (v.id) {
            R.id.btn_0 -> {
                resetBar(0)
            }
            R.id.btn_1 -> {
                resetBar(1)
            }
            R.id.btn_2 -> {
                resetBar(2)
            }
        }
    }

    private fun resetBar(index: Int) {
        val position = SPUtil.TOUCH_LOCATION
        if (position != index) {
            SPUtil.TOUCH_LOCATION = index
            val intent = Intent(App.app, SideBarService::class.java)
            intent.putExtra(Constants.TAG_RESET_TOUCH_BAR, 1)
            App.app.startService(intent)
        } else {
            mSideBarContent?.removeOtherBar()
        }
    }
}