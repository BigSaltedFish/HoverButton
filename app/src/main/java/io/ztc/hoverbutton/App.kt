package io.ztc.hoverbutton

import android.app.Application
import android.content.Intent
import android.util.Log
import android.view.View
import android.view.animation.BounceInterpolator
import android.widget.ImageView

/**
 */
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        val imageView = ImageView(applicationContext)
        imageView.setImageResource(R.drawable.icon)
        FloatWindow
            .with(applicationContext)
            .setView(imageView)
            .setWidth(Screen.width, 0.15f) //设置悬浮控件宽高
            .setHeight(Screen.width, 0.15f)
            .setX(Screen.width, 0.8f)
            .setY(Screen.height, 0.3f)
            .setMoveType(MoveType.slide, -70, -70)
            .setMoveStyle(500, BounceInterpolator())
            .setFilter(true, A_Activity::class.java)
            .setViewStateListener(mViewStateListener)
            .setPermissionListener(mPermissionListener)
            .setDesktopShow(true)
            .build()
        imageView.setOnClickListener {
            val mIntent = Intent(applicationContext, A_Activity::class.java)
            mIntent.addCategory(Intent.CATEGORY_LAUNCHER)
            mIntent.action = Intent.ACTION_MAIN
            mIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
            startActivity(mIntent)
        }
    }

    private val mPermissionListener: PermissionListener = object : PermissionListener {
        override fun onSuccess() {
            Log.d(TAG, "onSuccess")
        }

        override fun onFail() {
            Log.d(TAG, "onFail")
        }
    }
    private val mViewStateListener: ViewStateListener = object : ViewStateListener {
        override fun onPositionUpdate(x: Int, y: Int) {
            Log.d(TAG, "onPositionUpdate: x=$x y=$y")
        }

        override fun onShow() {
            Log.d(TAG, "onShow")
        }

        override fun onHide() {
            Log.d(TAG, "onHide")
        }

        override fun onDismiss() {
            Log.d(TAG, "onDismiss")
        }

        override fun onMoveAnimStart() {
            Log.d(TAG, "onMoveAnimStart")
        }

        override fun onMoveAnimEnd() {
            Log.d(TAG, "onMoveAnimEnd")
        }

        override fun onBackToDesktop() {
            Log.d(TAG, "onBackToDesktop")
        }
    }

    companion object {
        private const val TAG = "FloatWindow"
    }
}