package com.sdy.jitangapplication.ui.activity

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseActivity
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.ui.adapter.MainPagerAdapter
import com.sdy.jitangapplication.ui.fragment.MyTodayVisitFragment
import com.sdy.jitangapplication.ui.fragment.MyVisitFragment
import com.sdy.jitangapplication.widgets.ScaleTransitionPagerTitleView
import kotlinx.android.synthetic.main.activity_my_visit.*
import kotlinx.android.synthetic.main.layout_actionbar.*
import net.lucode.hackware.magicindicator.ViewPagerHelper
import net.lucode.hackware.magicindicator.buildins.UIUtil
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator
import java.util.*

/**
 * 我的访客
 */
class MyVisitActivity : BaseActivity() {
    private val from by lazy { intent.getIntExtra("from", FROM_ME) }

    companion object {
        const val FROM_ME = 1
        const val FROM_TOP_RECOMMEND = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_visit)
        initView()
        initFragment()
    }

    private fun initView() {
        hotT1.text = "看过我的"
        btnBack.onClick {
            finish()
        }
        divider.isVisible = false
    }


    //fragment栈管理
    private val mStack = Stack<Fragment>()
    private val titles by lazy {
        if (from == FROM_TOP_RECOMMEND) arrayOf(
            "今天来过${intent.getIntExtra(
                "today",
                0
            )}", "所有访客"
        ) else {
            arrayOf("所有访客")
        }
    }

    private fun initIndicator() {
        tabMyVisit.setBackgroundColor(Color.WHITE)
        val commonNavigator = CommonNavigator(this)
        commonNavigator.adapter = object : CommonNavigatorAdapter() {
            override fun getCount(): Int {
                return mStack.size
            }

            override fun getTitleView(context: Context, index: Int): IPagerTitleView {
                val simplePagerTitleView = ScaleTransitionPagerTitleView(context)
                simplePagerTitleView.text = titles[index]
                simplePagerTitleView.minScale = 0.88F
                simplePagerTitleView.textSize = 18F
                simplePagerTitleView.width =
                    (ScreenUtils.getScreenWidth() - SizeUtils.dp2px(40F)) / 2
                simplePagerTitleView.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                simplePagerTitleView.normalColor = Color.parseColor("#FF7E8183")
                simplePagerTitleView.selectedColor = resources.getColor(R.color.colorBlack)
                simplePagerTitleView.onClick {
                    vpMyVisit.currentItem = index
                }
                return simplePagerTitleView
            }

            override fun getIndicator(context: Context): IPagerIndicator {
                val indicator = LinePagerIndicator(context)
                indicator.mode = LinePagerIndicator.MODE_EXACTLY
                indicator.lineHeight = UIUtil.dip2px(context, 4.0).toFloat()
                indicator.lineWidth = UIUtil.dip2px(context, 25.0).toFloat()
                indicator.roundRadius = UIUtil.dip2px(context, 2.0).toFloat()
                indicator.startInterpolator = AccelerateInterpolator()
                indicator.endInterpolator = DecelerateInterpolator(1.0f)
                indicator.setColors(resources.getColor(R.color.colorOrange))
                return indicator
            }
        }
        tabMyVisit.navigator = commonNavigator
        ViewPagerHelper.bind(tabMyVisit, vpMyVisit)
    }

    /*
      初始化Fragment栈管理
      view.visitTodayCount.text = "今日来访：${intent.getIntExtra("today", 0)}"
view.visitAllCount.text = "总来访：${intent.getIntExtra("all", 0)}"
intent.getBooleanExtra("freeShow", false)
   */
    private fun initFragment() {

        if (from == FROM_TOP_RECOMMEND)
            mStack.add(
                MyTodayVisitFragment(
                    intent.getIntExtra("todayExplosure", 0),
                    intent.getBooleanExtra("freeShow", false)
                )
            )
        //今日来访
        mStack.add(
            MyVisitFragment(
                intent.getBooleanExtra("freeShow", false),
                from,
                intent.getIntExtra("today", 0),
                intent.getIntExtra("all", 0)
            )
        )
        //所有来访
        vpMyVisit.adapter = MainPagerAdapter(supportFragmentManager, mStack, titles)
        if (from == FROM_TOP_RECOMMEND) {
            vpMyVisit.offscreenPageLimit = 2
            initIndicator()
            tabMyVisit.isVisible = true
        } else {
            vpMyVisit.offscreenPageLimit = 1
            tabMyVisit.isVisible = false
        }
        vpMyVisit.currentItem = 0
    }


}

