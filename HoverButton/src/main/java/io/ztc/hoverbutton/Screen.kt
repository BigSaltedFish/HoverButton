package io.ztc.hoverbutton

import androidx.annotation.IntDef

/**
 * Created by yhao on 2017/12/23.
 * https://github.com/yhaolpz
 */
object Screen {
    const val width = 0
    const val height = 1

    @IntDef(width, height)
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    internal annotation class screenType
}