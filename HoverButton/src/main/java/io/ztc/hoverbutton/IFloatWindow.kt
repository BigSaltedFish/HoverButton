package io.ztc.hoverbutton

import android.view.View
import io.ztc.hoverbutton.Screen.screenType

/**
 */
abstract class IFloatWindow {
    abstract fun show()
    abstract fun hide()
    abstract val isShowing: Boolean
    abstract val x: Int
    abstract val y: Int
    abstract fun updateX(x: Int)
    abstract fun updateX(@screenType screenType: Int, ratio: Float)
    abstract fun updateY(y: Int)
    abstract fun updateY(@screenType screenType: Int, ratio: Float)
    abstract val view: View?
    abstract fun dismiss()
}