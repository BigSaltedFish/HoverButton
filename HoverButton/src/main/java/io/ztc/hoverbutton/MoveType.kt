package io.ztc.hoverbutton

import androidx.annotation.IntDef

/**
 */
object MoveType {
    const val fixed = 0
    const val inactive = 1
    const val active = 2
    const val slide = 3
    const val back = 4

    @IntDef(fixed, inactive, active, slide, back)
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    internal annotation class MOVE_TYPE
}