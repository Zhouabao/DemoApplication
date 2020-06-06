package com.sdy.jitangapplication.ui.activity

import android.graphics.Color
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseActivity
import com.sdy.jitangapplication.R
import com.sdy.baselibrary.utils.StatusBarUtil
import kotlinx.android.synthetic.main.layout_actionbar.*
import java.util.*

/**
 * 会员权益
 */
class VipPowerActivity1 : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vip_power1)
        initView()
    }


    private val vipPowerFragment by lazy {
        VipPowerFragment(
            VipPowerFragment.TYPE_VIP
        )
    }
    private val ptVipPowerFragment by lazy {
        VipPowerFragment(
            VipPowerFragment.TYPE_PT_VIP
        )
    }
    private val mStack by lazy { Stack<Fragment>() }

    private fun initView() {
        StatusBarUtil.immersive(this)
        llTitle.setBackgroundColor(Color.TRANSPARENT)
//        llTitle.setBackgroundColor(Color.parseColor("#FF1D1F21"))
        btnBack.setImageResource(R.drawable.icon_back_white)
        divider.isVisible = false
        hotT1.setTextColor(resources.getColor(R.color.colorWhite))
        hotT1.text = "会员权益"
        btnBack.onClick { finish() }


        mStack.add(vipPowerFragment)
        mStack.add(ptVipPowerFragment)
//        vpPower.adapter = MainPagerAdapter(supportFragmentManager, mStack)
//        vpPower.currentItem = 0
//        vpPower.offscreenPageLimit = 2
    }

}
