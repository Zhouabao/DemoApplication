package com.sdy.jitangapplication.widgets

import android.content.Context

/**
 *    author : ZFM
 *    date   : 2020/4/2914:43
 *    desc   :
 *    version: 1.0
 */
class CustomScaleTransitionPagerTitleView(context: Context, val fromSquare: Boolean = false) :
    ScaleTransitionPagerTitleView(context) {


    override fun onSelected(index: Int, totalCount: Int) {
        super.onSelected(index, totalCount)
        paint.isFakeBoldText = true
    }

    override fun onDeselected(index: Int, totalCount: Int) {
        super.onDeselected(index, totalCount)
        paint.isFakeBoldText = false
    }


}