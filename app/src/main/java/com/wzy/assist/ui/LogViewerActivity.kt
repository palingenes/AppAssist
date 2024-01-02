package com.wzy.assist.ui

import android.content.Intent
import com.wzy.assist.base.BaseActivity
import com.wzy.assist.constant.Constants
import com.wzy.assist.databinding.ActivityLogViewerBinding

/**
 * @time 2024/01/02 15:09
 * @version V1.0.1
 * @author wzy
 * </>
 * @desc
 */
class LogViewerActivity : BaseActivity<ActivityLogViewerBinding>() {
    override fun initView() {

    }

    override fun initData() {
        val logText = intent.getStringExtra(Constants.TAG_CRASH)
        if (!logText.isNullOrEmpty()) {
            viewBinding.textLog.text = logText
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

}