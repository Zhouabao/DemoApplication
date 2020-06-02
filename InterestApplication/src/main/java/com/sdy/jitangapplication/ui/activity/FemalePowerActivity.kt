package com.sdy.jitangapplication.ui.activity

import android.graphics.Color
import android.os.Bundle
import androidx.constraintlayout.widget.ConstraintLayout
import com.blankj.utilcode.util.ScreenUtils
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseActivity
import com.sdy.jitangapplication.R
import kotlinx.android.synthetic.main.activity_female_power.*
import kotlinx.android.synthetic.main.layout_actionbar.*

/**
 * 女性个人权益
 */
class FemalePowerActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_female_power)
        initView()
    }

    private fun initView() {
        llTitle.setBackgroundColor(Color.TRANSPARENT)
        hotT1.text = "个人权益"
        btnBack.setImageResource(R.drawable.icon_back_white)
        btnBack.onClick {
            finish()
        }


        val params = powerBg.layoutParams as ConstraintLayout.LayoutParams
        params.width = ScreenUtils.getScreenWidth()
        params.height = (210 * ScreenUtils.getScreenWidth() / 350F).toInt()
        powerBg.layoutParams = params


    }
}
