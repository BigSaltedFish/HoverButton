package io.ztc.hoverbutton

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.View
import android.view.WindowManager
import io.ztc.hoverbutton.FloatLifecycle.Companion.setResumedListener
import io.ztc.hoverbutton.LogUtil.d
import io.ztc.hoverbutton.LogUtil.e
import java.util.*

/**
 *
 *
 * 需要清楚：一个MIUI版本对应小米各种机型，基于不同的安卓版本，但是权限设置页跟MIUI版本有关
 * 测试TYPE_TOAST类型：
 * 7.0：
 * 小米      5        MIUI8         -------------------- 失败
 * 小米   Note2       MIUI9         -------------------- 失败
 * 6.0.1
 * 小米   5                         -------------------- 失败
 * 小米   红米note3                  -------------------- 失败
 * 6.0：
 * 小米   5                         -------------------- 成功
 * 小米   红米4A      MIUI8         -------------------- 成功
 * 小米   红米Pro     MIUI7         -------------------- 成功
 * 小米   红米Note4   MIUI8         -------------------- 失败
 *
 *
 * 经过各种横向纵向测试对比，得出一个结论，就是小米对TYPE_TOAST的处理机制毫无规律可言！
 * 跟Android版本无关，跟MIUI版本无关，addView方法也不报错
 * 所以最后对小米6.0以上的适配方法是：不使用 TYPE_TOAST 类型，统一申请权限
 */
internal object Miui {
    private const val miui = "ro.miui.ui.version.name"
    private const val miui5 = "V5"
    private const val miui6 = "V6"
    private const val miui7 = "V7"
    private const val miui8 = "V8"
    private const val miui9 = "V9"
    private var mPermissionListenerList: MutableList<PermissionListener>? = null
    private var mPermissionListener: PermissionListener? = null
    fun rom(): Boolean {
        d(" Miui  : $prop")
        return Build.MANUFACTURER == "Xiaomi"
    }

    private val prop: String?
        private get() = Rom.getProp(miui)

    /**
     * Android6.0以下申请权限
     */
    fun req(context: Context, permissionListener: PermissionListener) {
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
            req_(context)
        }
        mPermissionListenerList!!.add(permissionListener)
    }

    private fun req_(context: Context) {
        when (prop) {
            miui5 -> reqForMiui5(context)
            miui6, miui7 -> reqForMiui67(context)
            miui8, miui9 -> reqForMiui89(context)
            else ->  reqForMiui89(context)
        }
        setResumedListener (object :ResumedListener{
            override fun onResumed() {
                if (PermissionUtil.hasPermission(context)) {
                    mPermissionListener!!.onSuccess()
                } else {
                    mPermissionListener!!.onFail()
                }
            }
        })
    }

    private fun reqForMiui5(context: Context) {
        val packageName = context.packageName
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        if (Rom.isIntentAvailable(intent, context)) {
            context.startActivity(intent)
        } else {
            e("intent is not available!")
        }
    }

    private fun reqForMiui67(context: Context) {
        val intent = Intent("miui.intent.action.APP_PERM_EDITOR")
        intent.setClassName(
            "com.miui.securitycenter",
            "com.miui.permcenter.permissions.AppPermissionsEditorActivity"
        )
        intent.putExtra("extra_pkgname", context.packageName)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        if (Rom.isIntentAvailable(intent, context)) {
            context.startActivity(intent)
        } else {
            e("intent is not available!")
        }
    }

    private fun reqForMiui89(context: Context) {
        var intent = Intent("miui.intent.action.APP_PERM_EDITOR")
        intent.setClassName(
            "com.miui.securitycenter",
            "com.miui.permcenter.permissions.PermissionsEditorActivity"
        )
        intent.putExtra("extra_pkgname", context.packageName)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        if (Rom.isIntentAvailable(intent, context)) {
            context.startActivity(intent)
        } else {
            intent = Intent("miui.intent.action.APP_PERM_EDITOR")
            intent.setPackage("com.miui.securitycenter")
            intent.putExtra("extra_pkgname", context.packageName)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            if (Rom.isIntentAvailable(intent, context)) {
                context.startActivity(intent)
            } else {
                e("intent is not available!")
            }
        }
    }

    /**
     * 有些机型在添加TYPE-TOAST类型时会自动改为TYPE_SYSTEM_ALERT，通过此方法可以屏蔽修改
     * 但是...即使成功显示出悬浮窗，移动的话也会崩溃
     */
    private fun addViewToWindow(wm: WindowManager, view: View, params: WindowManager.LayoutParams) {
        setMiUI_International(true)
        wm.addView(view, params)
        setMiUI_International(false)
    }

    private fun setMiUI_International(flag: Boolean) {
        try {
            val BuildForMi = Class.forName("miui.os.Build")
            val isInternational = BuildForMi.getDeclaredField("IS_INTERNATIONAL_BUILD")
            isInternational.isAccessible = true
            isInternational.setBoolean(null, flag)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}