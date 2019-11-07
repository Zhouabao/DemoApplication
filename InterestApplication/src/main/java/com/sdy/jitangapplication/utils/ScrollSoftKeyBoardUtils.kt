package com.sdy.jitangapplication.utils

import android.graphics.Rect
import android.view.View
import com.blankj.utilcode.util.ScreenUtils

/**
 *    author : ZFM
 *    date   : 2019/11/714:51
 *    desc   :
 *    version: 1.0
 */
object ScrollSoftKeyBoardUtils {


    /**
     * 弹起软键盘
     */
    fun addLayoutListener(main: View, scroll: View) {
        main.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = Rect()
            main.getWindowVisibleDisplayFrame(rect)
            val screenHeight = ScreenUtils.getScreenHeight()
            val softHeight = screenHeight - rect.bottom
            val scrollDistance = softHeight - (screenHeight - scroll.bottom)
            if (scrollDistance > 0) {
                main.scrollTo(0, scrollDistance + 60)
            } else {
                main.scrollTo(0, 0)
            }
        }
    }

}