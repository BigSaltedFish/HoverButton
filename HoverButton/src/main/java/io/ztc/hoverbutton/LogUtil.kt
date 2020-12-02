package io.ztc.hoverbutton

import android.util.Log

/**
 */
internal object LogUtil {
    private const val TAG = "FloatWindow"
    @JvmStatic
    fun e(message: String?) {
        Log.e(TAG, message!!)
    }

    @JvmStatic
    fun d(message: String?) {
        Log.d(TAG, message!!)
    }
}