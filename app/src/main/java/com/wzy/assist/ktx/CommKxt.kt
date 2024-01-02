package com.wzy.assist.ktx

import android.app.Activity
import android.content.Intent
import androidx.fragment.app.Fragment

/**
 * @time 2023/11/13 15:35
 * @version V1.0.1
 * @author wzy
 * </>
 * @desc
 */
fun <T : Any, R : Any> whenAllNotNull(vararg options: T?, block: (List<T>) -> R) {
    if (options.all { it != null }) {
        block(options.filterNotNull())
    }
}

fun <T : Any, R : Any> whenAnyNotNull(vararg options: T?, block: (List<T>) -> R) {
    if (options.any { it != null }) {
        block(options.filterNotNull())
    }
}


inline fun <reified T : Activity> Activity.startActivity() {
    startActivity(Intent(this, T::class.java))
}

inline fun <reified T : Activity> Fragment.startActivity() {
    startActivity(Intent(requireActivity(), T::class.java))
}


fun body(vararg value: Any): String {
    val size = value.size
    if (size % 2 != 0) {
        throw RuntimeException("参数必须为偶数->key,value形式")
    }

    val sb = StringBuilder()
    value.forEachIndexed { index, s ->
        sb.append(s)
        if (index % 2 == 0) {
            sb.append("=")
        } else if (index < size - 1) {
            sb.append("&")
        }
    }
    return sb.toString()
}

fun timestamp(): Long {
    return System.currentTimeMillis() / 1000
}

