package com.sdy.jitangapplication.ui.activity

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.kotlin.base.ui.activity.BaseActivity
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.event.UpdateEditModeEvent
import com.sdy.jitangapplication.ui.adapter.MainPagerAdapter
import com.sdy.jitangapplication.ui.fragment.MyLabelFragment
import kotlinx.android.synthetic.main.activity_my_label.*
import kotlinx.android.synthetic.main.layout_actionbar.*
import org.greenrobot.eventbus.EventBus


/**
 * 管理我的标签
 */
class MyLabelActivity : BaseActivity(), View.OnClickListener {
    companion object {
        val MY_LABEL = 0
        val MY_INTEREST_LABEL = 1
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_label)
        initView()
    }

    private val fragments by lazy { mutableListOf<Fragment>(MyLabelFragment()) }
    private fun initView() {

        btnBack.setOnClickListener(this)
        rightBtn.setOnClickListener(this)
        hotT1.text = "我的标签"
        rightBtn.isVisible = true
        rightBtn.text = "编辑"
        divider.isVisible = false
        rightBtn.setTextColor(Color.parseColor("#FF191919"))
        vpLabel.setScrollable(false)
        vpLabel.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                rightBtn.text = if (editModes) {
                    "完成"
                } else {
                    "编辑"
                }
            }

        })
        vpLabel.adapter = MainPagerAdapter(supportFragmentManager, fragments)
        vpLabel.offscreenPageLimit = 1
        vpLabel.currentItem = 0
    }

    private var editModes = false
    override fun onClick(p0: View) {
        when (p0) {
            btnBack -> {
                finish()
            }
            rightBtn -> {
                editModes = !editModes
                rightBtn.text = if (editModes) {
                    "完成"
                } else {
                    "编辑"
                }
                EventBus.getDefault().post(UpdateEditModeEvent(vpLabel.currentItem))
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }


}
