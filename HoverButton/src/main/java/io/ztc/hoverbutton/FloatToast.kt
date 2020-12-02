package io.ztc.hoverbutton

import android.content.Context
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import java.lang.reflect.Method

/**
 * 自定义 toast 方式，无需申请权限
 * 当前版本暂时用 TYPE_TOAST 代替，后续版本可能会再融入此方式
 */
internal class FloatToast(applicationContext: Context?) : FloatView() {
    private val toast: Toast = Toast(applicationContext)
    private var mTN: Any? = null
    private var show: Method? = null
    private var hide: Method? = null
    private var mWidth = 0
    private var mHeight = 0
    public override fun setSize(width: Int, height: Int) {
        mWidth = width
        mHeight = height
    }

    override fun setView(view: View?) {
        toast.view = view
        initTN()
    }

    public override fun setGravity(gravity: Int, xOffset: Int, yOffset: Int) {
        toast.setGravity(gravity, xOffset, yOffset)
    }

    public override fun init() {
        try {
            show!!.invoke(mTN)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    public override fun dismiss() {
        try {
            hide!!.invoke(mTN)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initTN() {
        try {
            val tnField = toast.javaClass.getDeclaredField("mTN")
            tnField.isAccessible = true
            mTN = tnField[toast]
            show = mTN?.javaClass?.getMethod("show")
            hide = mTN?.javaClass?.getMethod("hide")
            val tnParamsField = mTN?.javaClass?.getDeclaredField("mParams")
            if (tnParamsField != null) {
                tnParamsField.isAccessible = true
            }
            val params = (tnParamsField?.get(mTN) as WindowManager.LayoutParams)
            params.flags = (WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
            params.width = mWidth
            params.height = mHeight
            params.windowAnimations = 0
            val tnNextViewField = mTN?.javaClass?.getDeclaredField("mNextView")
            if (tnNextViewField != null) {
                tnNextViewField.isAccessible = true
            }
            tnNextViewField?.set(mTN, toast.view)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}