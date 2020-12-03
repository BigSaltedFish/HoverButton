package io.ztc.hoverbutton

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.annotation.RequiresApi
import java.util.*

/**
 * 用于在内部自动申请权限
 */
class FloatActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestAlertWindowPermission()
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private fun requestAlertWindowPermission() {
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
        intent.data = Uri.parse("package:$packageName")
        startActivityForResult(intent, 756232212)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 756232212) {
            if (PermissionUtil.hasPermissionOnActivityResult(this)) {
                mPermissionListener!!.onSuccess()
            } else {
                mPermissionListener!!.onFail()
            }
        }
        finish()
    }

    companion object {
        private var mPermissionListenerList: MutableList<PermissionListener>? = null
        private var mPermissionListener: PermissionListener? = null
        @JvmStatic
        @Synchronized
        fun request(context: Context, permissionListener: PermissionListener) {
            if (PermissionUtil.hasPermission(context)) {
                permissionListener.onSuccess()
                return
            }
            if (mPermissionListenerList == null) {
                mPermissionListenerList = ArrayList()
                mPermissionListener = object : PermissionListener {
                    override fun onSuccess() {
                        for (listener in mPermissionListenerList as ArrayList<PermissionListener>) {
                            listener.onSuccess()
                        }
                        (mPermissionListenerList as ArrayList<PermissionListener>).clear()
                    }

                    override fun onFail() {
                        for (listener in mPermissionListenerList as ArrayList<PermissionListener>) {
                            listener.onFail()
                        }
                        (mPermissionListenerList as ArrayList<PermissionListener>).clear()
                    }
                }
                val intent = Intent(context, FloatActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            }
            mPermissionListenerList!!.add(permissionListener)
        }
    }
}