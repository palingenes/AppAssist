package com.wzy.assist.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.wzy.assist.App
import com.wzy.assist.BuildConfig
import com.wzy.assist.R
import com.wzy.assist.base.BaseActivity
import com.wzy.assist.databinding.ActivityMainBinding
import com.wzy.assist.ktx.gone
import com.wzy.assist.ktx.startActivity
import com.wzy.assist.ktx.stringOf
import com.wzy.assist.ktx.visible
import com.wzy.assist.utils.PermissionUtil
import com.wzy.assist.utils.Tools

/**
 * @time 2024/01/02 14:50
 * @version V1.0.1
 * @author wzy
 * </>
 * @desc 主入口
 */
class MainActivity : BaseActivity<ActivityMainBinding>() {

    private lateinit var deviceLauncher: ActivityResultLauncher<Intent>
    private lateinit var deviceLauncher1: ActivityResultLauncher<Intent>

    override fun initView() {
        viewBinding.llContainer.postDelayed({
            viewBinding.llContainer.setBackgroundResource(R.color.color_333)
            viewBinding.llContainer.visible()
        }, 1500)

        deviceLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode != RESULT_OK) {
                    return@registerForActivityResult
                }
                floatWindowVisible()
            }
        deviceLauncher1 =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode != RESULT_OK) {
                    return@registerForActivityResult
                }
                accessibilityVisible()
            }

        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
            || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        )
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), 101
            )

        viewBinding.btnFloatWindow.setOnClickListener {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            deviceLauncher.launch(intent)
        }
        viewBinding.btnAccessibility.setOnClickListener {
            val accessibleIntent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            deviceLauncher1.launch(accessibleIntent)
        }
        viewBinding.isActionSupport.setOnClickListener {
            val action = "android.net.VpnService"
            val actionSupport = Tools.isActionSupport(action)
            if (actionSupport) {
                Tools.getPkgInfo()
            }
        }
        if (!BuildConfig.DEBUG) {
            viewBinding.isActionSupport.gone()
        }
    }

    override fun onResume() {
        super.onResume()
        floatWindowVisible()
    }

    /**
     * float button visible
     */
    private fun floatWindowVisible() {
        // > M,grant permission
        if (PermissionUtil.isCanDrawOverlays(this)) {
            // permission authorized,service go,button gone
            viewBinding.btnFloatWindow.gone()
            accessibilityVisible()
        } else {
            // permission unauthorized,button visible
            viewBinding.btnFloatWindow.visible()
            Toast.makeText(this, stringOf(R.string.permission_flatwindow_), Toast.LENGTH_SHORT)
                .show()
        }
    }

    /**
     * Accessibility button visible
     */
    private fun accessibilityVisible() {
        if (PermissionUtil.isAccessibilityServiceEnable(this)) {
            Toast.makeText(this, stringOf(R.string.permission_notice), Toast.LENGTH_SHORT).show()
            viewBinding.btnAccessibility.gone()

            if (App.vpn_is_running)  //  当前界面正在运行
                return
            startActivity<VpnActivity>()
            finish()
        } else {
            viewBinding.btnAccessibility.visible()
            Toast.makeText(this, stringOf(R.string.permission_accessibility_), Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun initData() {
    }
}