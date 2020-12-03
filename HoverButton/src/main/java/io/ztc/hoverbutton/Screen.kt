package io.ztc.hoverbutton

import androidx.annotation.IntDef

/**
 */
object Screen {
    const val width = 0
    const val height = 1

    @IntDef(width, height)
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    internal annotation class screenType
}