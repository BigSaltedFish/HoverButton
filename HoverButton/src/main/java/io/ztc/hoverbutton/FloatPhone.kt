package io.ztc.hoverbutton

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.View
import android.view.WindowManager
import io.ztc.hoverbutton.FloatActivity.Companion.request

/**
 */
internal class FloatPhone(private val mContext: Context, private val mPermissionListener: PermissionListener?) : FloatView() {
    private val mWindowManager: WindowManager = mContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val mLayoutParams: WindowManager.LayoutParams = WindowManager.LayoutParams()
    private var mView: View? = null
    private var mX = 0
    private var mY = 0
    private var isRemove = false
    public override fun setSize(width: Int, height: Int) {
        mLayoutParams.width = width
        mLayoutParams.height = height
    }

    public override fun setView(view: View?) {
        mView = view
    }

    public override fun setGravity(gravity: Int, xOffset: Int, yOffset: Int) {
        mLayoutParams.gravity = gravity
        mX = xOffset
        mLayoutParams.x = mX
        mY = yOffset
        mLayoutParams.y = mY
    }

    public override fun init() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            req()
        } else if (Miui.rom()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                req()
            } else {
                mLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE
                Miui.req(mContext, object : PermissionListener {
                    override fun onSuccess() {
                        mWindowManager.addView(mView, mLayoutParams)
                        mPermissionListener?.onSuccess()
                    }

                    override fun onFail() {
                        mPermissionListener?.onFail()
                    }
                })
            }
        } else {
            try {
                mLayoutParams.type = WindowManager.LayoutParams.TYPE_TOAST
                mWindowManager.addView(mView, mLayoutParams)
            } catch (e: Exception) {
                mWindowManager.removeView(mView)
                LogUtil.e("TYPE_TOAST 失败")
                req()
            }
        }
    }

    private fun req() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            mLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE
        }
        request(mContext, object : PermissionListener {
            override fun onSuccess() {
                mWindowManager.addView(mView, mLayoutParams)
                mPermissionListener?.onSuccess()
            }

            override fun onFail() {
                mPermissionListener?.onFail()
            }
        })
    }

    public override fun dismiss() {
        isRemove = true
        mWindowManager.removeView(mView)
    }

    public override fun updateXY(x: Int, y: Int) {
        if (isRemove) return
        mX = x
        mLayoutParams.x = mX
        mY = y
        mLayoutParams.y = mY
        mWindowManager.updateViewLayout(mView, mLayoutParams)
    }

    public override fun updateX(x: Int) {
        if (isRemove) return
        mX = x
        mLayoutParams.x = mX
        mWindowManager.updateViewLayout(mView, mLayoutParams)
    }

    public override fun updateY(y: Int) {
        if (isRemove) return
        mY = y
        mLayoutParams.y = mY
        mWindowManager.updateViewLayout(mView, mLayoutParams)
    }

    override fun getX(): Int {
        return mX
    }

    override fun getY(): Int {
        return mY
    }

    init {
        mLayoutParams.format = PixelFormat.RGBA_8888
        mLayoutParams.flags = (WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        mLayoutParams.windowAnimations = 0
    }
}