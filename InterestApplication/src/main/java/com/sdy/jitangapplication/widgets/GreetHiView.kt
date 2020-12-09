package com.sdy.jitangapplication.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.blankj.utilcode.util.SizeUtils
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import kotlinx.android.synthetic.main.item_greet_hi_index_left.view.*

/**
 *    author : ZFM
 *    date   : 2020/12/816:22
 *    desc   :
 *    version: 1.0
 */
class GreetHiView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    init {
        LayoutInflater.from(context).inflate(R.layout.item_greet_hi_index_left, this)
    }


    /**
     * 设置泡泡方向
     */
    companion object {
        const val DIRECTION_LEFT = 1
        const val DIRECTION_RIGHT = 2
    }

    fun setDirection(direction: Int) {
        when (direction) {
            DIRECTION_LEFT -> {
                greetDirection.setImageResource(R.drawable.icon_hi_greet_right)
                val paramsIcon = greetIcon.layoutParams as LayoutParams
                paramsIcon.leftMargin = SizeUtils.dp2px(10F)

                val greetDirectionParams = greetDirection.layoutParams as LayoutParams
                greetDirectionParams.gravity = Gravity.LEFT

            }
            DIRECTION_RIGHT -> {
                greetDirection.setImageResource(R.drawable.icon_hi_greet_left)
                val paramsIcon = greetIcon.layoutParams as LayoutParams
                paramsIcon.rightMargin = SizeUtils.dp2px(10F)

                val greetDirectionParams = greetDirection.layoutParams as LayoutParams
                greetDirectionParams.gravity = Gravity.RIGHT
            }
        }

    }

    var url: String = ""
    var accid: String = ""
    fun loadImg(url: String, accid: String) {
        this.url = url
        this.accid = accid
        GlideUtil.loadAvatorImg(context, url, greetIcon)
    }

    fun initView() {

    }


}