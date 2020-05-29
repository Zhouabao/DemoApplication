package com.sdy.jitangapplication.ui.activity

import android.graphics.Color
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseActivity
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.ui.adapter.AllVipPowerAdapter
import com.sdy.jitangapplication.utils.StatusBarUtil
import kotlinx.android.synthetic.main.activity_vip_power1.*
import kotlinx.android.synthetic.main.layout_actionbar.*
import java.util.*

/**
 * 会员权益
 */
class VipPowerActivity : BaseActivity() {
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


        initVp()
        initVp2()
    }

    private val adapter by lazy { AllVipPowerAdapter() }
    private fun initVp2() {
        vpPower.adapter = adapter
        adapter.setOnItemChildClickListener { _, view, position ->

        }
    }

    private fun initVp() {
//        mStack.add(vipPowerFragment)
//        mStack.add(ptVipPowerFragment)
//        vpPower.adapter = MainPagerAdapter(supportFragmentManager, mStack)
//        vpPower.currentItem = 0
//        vpPower.offscreenPageLimit = 2
//        vpPower.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
//            override fun onPageScrollStateChanged(state: Int) {
//
//            }
//
//            override fun onPageScrolled(
//                position: Int,
//                positionOffset: Float,
//                positionOffsetPixels: Int
//            ) {
//                EventBus.getDefault()
//                    .post(UpdateOffsetEvent(abs(positionOffsetPixels) < SizeUtils.dp2px(10F)))
//            }
//
//            override fun onPageSelected(position: Int) {
//            }
//
//        })
    }

}
