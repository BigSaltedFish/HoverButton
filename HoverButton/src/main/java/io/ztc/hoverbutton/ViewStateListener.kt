package io.ztc.hoverbutton

/**
 */
interface ViewStateListener {
    fun onPositionUpdate(x: Int, y: Int)
    fun onShow()
    fun onHide()
    fun onDismiss()
    fun onMoveAnimStart()
    fun onMoveAnimEnd()
    fun onBackToDesktop()
}