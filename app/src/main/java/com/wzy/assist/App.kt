package com.wzy.assist

import android.app.Application
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.wzy.assist.utils.CrashHandler


class App : Application() {

    override fun onCreate() {
        super.onCreate()
        app = this
        if (BuildConfig.DEBUG) {
            CrashHandler.instance.init()
        }
    }

    companion object {
        private val handler = Handler(Looper.getMainLooper())

        var vpn_is_running = false

        @JvmStatic
        lateinit var app: App
            private set

        val context: Context
            get() = app.applicationContext

        /**
         * 主线程执行
         */
        @JvmStatic
        fun runOnUiThread(runnable: Runnable?) {
            handler.post(runnable!!)
        }

        @JvmStatic
        fun runOnUiThread(runnable: Runnable?, delay: Long) {
            handler.postDelayed(runnable!!, delay)
        }
    }
}