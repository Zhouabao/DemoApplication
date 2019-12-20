package com.sdy.jitangapplication.ui.activity

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.flyco.tablayout.listener.OnTabSelectListener
import com.kotlin.base.ui.activity.BaseActivity
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.event.UpdateEditModeEvent
import com.sdy.jitangapplication.event.UpdateEditShowEvent
import com.sdy.jitangapplication.model.TabEntity
import com.sdy.jitangapplication.ui.adapter.MainPagerAdapter
import com.sdy.jitangapplication.ui.fragment.MyInterestLabelFragment
import com.sdy.jitangapplication.ui.fragment.MyLabelFragment
import kotlinx.android.synthetic.main.activity_my_label.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


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

    private val titles = arrayOf("我的标签", "想认识")
    private val fragments by lazy { mutableListOf<Fragment>(MyLabelFragment(), MyInterestLabelFragment()) }
    private fun initView() {
        EventBus.getDefault().register(this)

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
        tabTopLabel.currentTab = 0
        vpLabel.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                tabTopLabel.currentTab = position
                rightBtn.text = if (editModes[vpLabel.currentItem]) {
                    "取消"
                } else {
                    "删除"
                }
                rightBtn.isVisible = editModesShow[vpLabel.currentItem]
            }

        })
        vpLabel.adapter = MainPagerAdapter(supportFragmentManager, fragments, titles)
        vpLabel.offscreenPageLimit = 2

        if (intent.getIntExtra("index", -1) != -1) {
            val index = intent.getIntExtra("index", 0)
            vpLabel.currentItem = if (index == 0) {
                1
            } else {
                0
            }
        } else {
            vpLabel.currentItem = 0
        }

    }

    private val editModes by lazy { mutableListOf(false, false) }
    private val editModesShow by lazy { mutableListOf(false, false) }
    override fun onClick(p0: View) {
        when (p0) {
            btnBack -> {
                finish()
            }
            rightBtn -> {
                editModes[vpLabel.currentItem] = !editModes[vpLabel.currentItem]
                rightBtn.text = if (editModes[vpLabel.currentItem]) {
                    "取消"
                } else {
                    "删除"
                }
                EventBus.getDefault().post(UpdateEditModeEvent(vpLabel.currentItem))
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateEditShowEvent(event: UpdateEditShowEvent) {
        editModesShow[event.position] = event.show
        if (vpLabel.currentItem == event.position)
            rightBtn.isVisible = editModesShow[event.position]
    }

}