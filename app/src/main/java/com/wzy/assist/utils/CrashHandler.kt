package com.wzy.assist.utils

import android.os.Build
import android.os.Process
import com.drake.logcat.LogCat.e
import com.wzy.assist.App
import com.wzy.assist.BuildConfig
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * @author wzy
 * >
 * @version V1.0.1
 * @time 2023/04/04 18:24
 * @desc 异常捕获 （debug使用，方便定位测试发现的问题）
 */
class CrashHandler private constructor() : Thread.UncaughtExceptionHandler {

    private var phoneInfo: String? = null
    private var mDefaultCrashHandler: Thread.UncaughtExceptionHandler? = null
    fun init() {
        mDefaultCrashHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(this)
        phoneInfo = phoneInformation
    }

    /**
     * 这个是最关键的函数，当程序中有未被捕获的异常，系统将会自动调用uncaughtException方法
     * thread为出现未捕获异常的线程，ex为未捕获的异常，有了这个ex，我们就可以得到异常信息
     */
    override fun uncaughtException(thread: Thread, ex: Throwable) {
        try {
            dumpExceptionToSDCard(ex) // 导出异常信息到SD卡中
            uploadExceptionToServer() // 上传异常信息到服务器
        } catch (e: IOException) {
            e.printStackTrace()
        }
        ex.printStackTrace()

        //  如果系统提供默认的异常处理器，则交给系统去结束程序，否则就由自己结束自己
        if (mDefaultCrashHandler != null) {
            mDefaultCrashHandler!!.uncaughtException(thread, ex)
        } else {
            try {
                Thread.sleep(2000) // 延迟2秒杀进程
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            Process.killProcess(Process.myPid())
        }
    }

    @Throws(IOException::class)
    private fun dumpExceptionToSDCard(ex: Throwable) {
        // 如果SD卡不存在或无法使用，则无法把异常信息写入SD卡
        val dir = File(PATH)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val current = System.currentTimeMillis()
        val time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(Date(current))
        val file = File(PATH + FILE_NAME + time + FILE_NAME_SUFFIX)
        try {
            val pw = PrintWriter(BufferedWriter(FileWriter(file)))
            pw.println("------ $time ------")
            pw.println(phoneInfo)
            pw.println()
            ex.printStackTrace(pw)
            pw.close()
            e("dump crash info success!")
        } catch (e: Exception) {
            e(e.message)
            e("dump crash info failed")
        }
    }

    private fun uploadExceptionToServer() {
        // 将异常信息发送到服务器
    }

    private val phoneInformation: String
        get() {
            val sb = StringBuilder()
            sb.append("App version name:")
                .append("\t").append(BuildConfig.VERSION_NAME)
                .append(", version code:")
                .append(BuildConfig.VERSION_CODE).append("\n")
            // Android版本号
            sb.append("\t").append("OS Version: ")
                .append(Build.VERSION.RELEASE)
                .append("_")
                .append(Build.VERSION.SDK_INT).append("\n")
            // 手机制造商
            sb.append("\t").append("Vendor: ")
                .append(Build.MANUFACTURER).append("\n")
            // 手机型号
            sb.append("\t").append("Model: ").append(Build.MODEL).append("\n")
            // CPU架构
            sb.append("\t").append("CPU ABI:").append("\n")
            val abiArr = Build.SUPPORTED_ABIS
            for (i in abiArr.indices) {
                sb.append("\t")
                sb.append(if (i == 0) "  * " else "\t")
                sb.append(abiArr[i])
                if (i != abiArr.size - 1) sb.append("\n")
            }
            return sb.toString()
        }

    companion object {
        // 自定义存储的目录
        private val PATH = (App.context.filesDir.absolutePath + File.separator
                + "tmp_crash" + File.separator)
        private const val FILE_NAME = "crash"
        private const val FILE_NAME_SUFFIX = ".txt"
        val instance = CrashHandler()
    }
}
