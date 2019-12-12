package com.sdy.jitangapplication.ui.activity

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.flyco.tablayout.listener.OnTabSelectListener
import com.kotlin.base.ui.activity.BaseActivity
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.event.UpdateEditModeEvent
import com.sdy.jitangapplication.model.TabEntity
import com.sdy.jitangapplication.ui.adapter.MainPagerAdapter
import com.sdy.jitangapplication.ui.fragment.MyInterestLabelFragment
import com.sdy.jitangapplication.ui.fragment.MyLabelFragment
import kotlinx.android.synthetic.main.activity_my_label.*
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

    private val titles = arrayOf("兴趣", "感兴趣")
    private val fragments by lazy { mutableListOf<Fragment>(MyLabelFragment(), MyInterestLabelFragment()) }
    private fun initView() {
        btnBack.setOnClickListener(this)
        rightBtn.setOnClickListener(this)
        rightBtn.text = "删除"
        rightBtn.setTextColor(Color.parseColor("#FF191919"))

        vpLabel.setScrollable(false)
        tabTopLabel.setTabData(arrayListOf(TabEntity(titles[0]), TabEntity(titles[1])))
        tabTopLabel.setOnTabSelectListener(object : OnTabSelectListener {
            override fun onTabSelect(position: Int) {
                vpLabel.currentItem = position
            }

            override fun onTabReselect(position: Int) {
            }
        })
        vpLabel.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                tabTopLabel.currentTab = position
                if (vpLabel.currentItem == 0) {
                    rightBtn.text = if (firstEditMode) {
                        "取消"
                    } else {
                        "删除"
                    }
                } else {
                    rightBtn.text = if (secondEditMode) {
                        "取消"
                    } else {
                        "删除"
                    }
                }
            }

        })
        vpLabel.adapter = MainPagerAdapter(supportFragmentManager, fragments, titles)
        vpLabel.currentItem = 0
        vpLabel.offscreenPageLimit = 2


    }

    private var firstEditMode = false
    private var secondEditMode = false
    override fun onClick(p0: View) {
        when (p0) {
            btnBack -> {
                finish()
            }
            rightBtn -> {
                if (vpLabel.currentItem == 0) {
                    firstEditMode = !firstEditMode
                    rightBtn.text = if (firstEditMode) {
                        "取消"
                    } else {
                        "删除"
                    }
                } else {
                    secondEditMode = !secondEditMode
                    rightBtn.text = if (secondEditMode) {
                        "取消"
                    } else {
                        "删除"
                    }
                }
                EventBus.getDefault().post(UpdateEditModeEvent(vpLabel.currentItem))
            }
        }
    }

}
