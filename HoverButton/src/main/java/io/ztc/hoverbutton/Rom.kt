package io.ztc.hoverbutton

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

/**
 * Created by yhao on 2017/12/30.
 * https://github.com/yhaolpz
 */
internal object Rom {
    @SuppressLint("QueryPermissionsNeeded")
    fun isIntentAvailable(intent: Intent?, context: Context): Boolean {
        return intent != null && context.packageManager.queryIntentActivities(
            intent, PackageManager.MATCH_DEFAULT_ONLY
        ).size > 0
    }

    fun getProp(name: String): String? {
        var input: BufferedReader? = null
        return try {
            val p = Runtime.getRuntime().exec("getprop $name")
            input = BufferedReader(InputStreamReader(p.inputStream), 1024)
            val line = input.readLine()
            input.close()
            line
        } catch (ex: IOException) {
            null
        } finally {
            if (input != null) {
                try {
                    input.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }
}