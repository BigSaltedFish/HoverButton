package io.ztc.hoverbutton

import android.animation.*
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.annotation.SuppressLint
import android.os.Build
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewConfiguration
import android.view.animation.DecelerateInterpolator
import io.ztc.hoverbutton.FloatWindow.B

/**
 */
class IFloatWindowImpl : IFloatWindow {
    private var mB: B? = null
    private var mFloatView: FloatView? = null
    private var mFloatLifecycle: FloatLifecycle? = null
    override var isShowing = false
        private set
    private var once = true
    private var mAnimator: ValueAnimator? = null
    private var mDecelerateInterpolator: TimeInterpolator? = null
    private var downX = 0f
    private var downY = 0f
    private var upX = 0f
    private var upY = 0f
    private var mClick = false
    private var mSlop = 0

    private constructor() {}
    internal constructor(b: B) {
        mB = b
        if (mB!!.mMoveType == MoveType.fixed) {
            mFloatView = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                FloatPhone(b.mApplicationContext!!, mB!!.mPermissionListener)
            } else {
                FloatToast(b.mApplicationContext)
            }
        } else {
            mFloatView = FloatPhone(b.mApplicationContext!!, mB!!.mPermissionListener)
            initTouchEvent()
        }
        mFloatView!!.setSize(mB!!.mWidth, mB!!.mHeight)
        mFloatView!!.setGravity(mB!!.gravity, mB!!.xOffset, mB!!.yOffset)
        mFloatView!!.setView(mB!!.mView)
        mFloatLifecycle = FloatLifecycle(
            mB!!.mApplicationContext!!,
            mB!!.mShow,
            mB!!.mActivities,
            object : LifecycleListener {
                override fun onShow() {
                    show()
                }

                override fun onHide() {
                    hide()
                }

                override fun onBackToDesktop() {
                    if (!mB!!.mDesktopShow) {
                        hide()
                    }
                    if (mB!!.mViewStateListener != null) {
                        mB!!.mViewStateListener!!.onBackToDesktop()
                    }
                }
            })
    }

    override fun show() {
        if (once) {
            mFloatView!!.init()
            once = false
            isShowing = true
        } else {
            if (isShowing) {
                return
            }
            view!!.visibility = View.VISIBLE
            isShowing = true
        }
        if (mB!!.mViewStateListener != null) {
            mB!!.mViewStateListener!!.onShow()
        }
    }

    override fun hide() {
        if (once || !isShowing) {
            return
        }
        view!!.visibility = View.INVISIBLE
        isShowing = false
        if (mB!!.mViewStateListener != null) {
            mB!!.mViewStateListener!!.onHide()
        }
    }

    override fun dismiss() {
        mFloatView!!.dismiss()
        isShowing = false
        if (mB!!.mViewStateListener != null) {
            mB!!.mViewStateListener!!.onDismiss()
        }
    }

    override fun updateX(x: Int) {
        checkMoveType()
        mB!!.xOffset = x
        mFloatView!!.updateX(x)
    }

    override fun updateY(y: Int) {
        checkMoveType()
        mB!!.yOffset = y
        mFloatView!!.updateY(y)
    }

    override fun updateX(screenType: Int, ratio: Float) {
        checkMoveType()
        mB!!.xOffset =
            ((if (screenType == Screen.width) Util.getScreenWidth(mB!!.mApplicationContext) else Util.getScreenHeight(
                mB!!.mApplicationContext
            )) * ratio).toInt()
        mFloatView!!.updateX(mB!!.xOffset)
    }

    override fun updateY(screenType: Int, ratio: Float) {
        checkMoveType()
        mB!!.yOffset =
            ((if (screenType == Screen.width) Util.getScreenWidth(mB!!.mApplicationContext) else Util.getScreenHeight(
                mB!!.mApplicationContext
            )) * ratio).toInt()
        mFloatView!!.updateY(mB!!.yOffset)
    }

    override val x: Int
        get() = mFloatView!!.getX()
    override val y: Int
        get() = mFloatView!!.getY()
    override val view: View?
        get() {
            mSlop = ViewConfiguration.get(mB!!.mApplicationContext).scaledTouchSlop
            return mB!!.mView
        }

    private fun checkMoveType() {
        require(mB!!.mMoveType != MoveType.fixed) { "FloatWindow of this tag is not allowed to move!" }
    }

    private fun initTouchEvent() {
        when (mB!!.mMoveType) {
            MoveType.inactive -> {
            }
            else -> view!!.setOnTouchListener(object : OnTouchListener {
                var lastX = 0f
                var lastY = 0f
                var changeX = 0f
                var changeY = 0f
                var newX = 0
                var newY = 0

                @SuppressLint("ClickableViewAccessibility")
                override fun onTouch(v: View, event: MotionEvent): Boolean {
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            downX = event.rawX
                            downY = event.rawY
                            lastX = event.rawX
                            lastY = event.rawY
                            cancelAnimator()
                        }
                        MotionEvent.ACTION_MOVE -> {
                            changeX = event.rawX - lastX
                            changeY = event.rawY - lastY
                            newX = (mFloatView!!.getX() + changeX).toInt()
                            newY = (mFloatView!!.getY() + changeY).toInt()
                            mFloatView!!.updateXY(newX, newY)
                            if (mB!!.mViewStateListener != null) {
                                mB!!.mViewStateListener!!.onPositionUpdate(newX, newY)
                            }
                            lastX = event.rawX
                            lastY = event.rawY
                        }
                        MotionEvent.ACTION_UP -> {
                            upX = event.rawX
                            upY = event.rawY
                            mClick = Math.abs(upX - downX) > mSlop || Math.abs(upY - downY) > mSlop
                            when (mB!!.mMoveType) {
                                MoveType.slide -> {
                                    val startX = mFloatView!!.getX()
                                    val endX = if (startX * 2 + v.width > Util.getScreenWidth(
                                            mB!!.mApplicationContext
                                        )
                                    ) Util.getScreenWidth(
                                        mB!!.mApplicationContext
                                    ) - v.width - mB!!.mSlideRightMargin else mB!!.mSlideLeftMargin
                                    mAnimator = ObjectAnimator.ofInt(startX, endX)
                                    mAnimator?.addUpdateListener(AnimatorUpdateListener { animation ->
                                        val x = animation.animatedValue as Int
                                        mFloatView!!.updateX(x)
                                        if (mB!!.mViewStateListener != null) {
                                            mB!!.mViewStateListener!!.onPositionUpdate(
                                                x,
                                                upY.toInt()
                                            )
                                        }
                                    })
                                    startAnimator()
                                }
                                MoveType.back -> {
                                    val pvhX = PropertyValuesHolder.ofInt(
                                        "x",
                                        mFloatView!!.getX(),
                                        mB!!.xOffset
                                    )
                                    val pvhY = PropertyValuesHolder.ofInt(
                                        "y",
                                        mFloatView!!.getY(),
                                        mB!!.yOffset
                                    )
                                    mAnimator = ObjectAnimator.ofPropertyValuesHolder(pvhX, pvhY)
                                    mAnimator?.addUpdateListener(AnimatorUpdateListener { animation ->
                                        val x = animation.getAnimatedValue("x") as Int
                                        val y = animation.getAnimatedValue("y") as Int
                                        mFloatView!!.updateXY(x, y)
                                        if (mB!!.mViewStateListener != null) {
                                            mB!!.mViewStateListener!!.onPositionUpdate(x, y)
                                        }
                                    })
                                    startAnimator()
                                }
                                else -> {
                                }
                            }
                        }
                        else -> {
                        }
                    }
                    return mClick
                }
            })
        }
    }

    private fun startAnimator() {
        if (mB!!.mInterpolator == null) {
            if (mDecelerateInterpolator == null) {
                mDecelerateInterpolator = DecelerateInterpolator()
            }
            mB!!.mInterpolator = mDecelerateInterpolator
        }
        mAnimator!!.interpolator = mB!!.mInterpolator
        mAnimator!!.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                mAnimator!!.removeAllUpdateListeners()
                mAnimator!!.removeAllListeners()
                mAnimator = null
                if (mB!!.mViewStateListener != null) {
                    mB!!.mViewStateListener!!.onMoveAnimEnd()
                }
            }
        })
        mAnimator!!.setDuration(mB!!.mDuration).start()
        if (mB!!.mViewStateListener != null) {
            mB!!.mViewStateListener!!.onMoveAnimStart()
        }
    }

    private fun cancelAnimator() {
        if (mAnimator != null && mAnimator!!.isRunning) {
            mAnimator!!.cancel()
        }
    }
}