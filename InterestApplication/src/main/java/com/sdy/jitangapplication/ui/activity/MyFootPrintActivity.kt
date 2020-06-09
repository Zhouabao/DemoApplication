package com.sdy.jitangapplication.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
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
import com.sdy.jitangapplication.event.NotifyEvent
import com.sdy.jitangapplication.ui.adapter.MainPagerAdapter
import com.sdy.jitangapplication.ui.fragment.MyCollectionAndLikeFragment
import com.sdy.jitangapplication.ui.fragment.MyCommentFragment
import com.sdy.jitangapplication.widgets.ScaleTransitionPagerTitleView
import com.umeng.socialize.UMShareAPI
import kotlinx.android.synthetic.main.activity_my_foot_print.*
import kotlinx.android.synthetic.main.layout_actionbar.*
import net.lucode.hackware.magicindicator.ViewPagerHelper
import net.lucode.hackware.magicindicator.buildins.UIUtil
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator
import org.greenrobot.eventbus.EventBus
import java.util.*

/**
 * 我的足迹：我的点赞 、评论、收藏
 */
class MyFootPrintActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_foot_print)
        initView()
        initFragment()
    }

    private fun initView() {
        hotT1.text = "我的足迹"
        btnBack.onClick {
            finish()
        }
        divider.isVisible = false
    }


    //fragment栈管理
    private val mStack = Stack<Fragment>()
    private val titles = arrayOf("点赞", "评论", "收藏")

    private fun initIndicator() {
        tabMyFootprint.setBackgroundColor(Color.WHITE)
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
                    (ScreenUtils.getScreenWidth() - SizeUtils.dp2px(40F)) / 4
                simplePagerTitleView.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                simplePagerTitleView.normalColor = Color.parseColor("#FF7E8183")
                simplePagerTitleView.selectedColor = resources.getColor(R.color.colorBlack)
                simplePagerTitleView.onClick {
                    vpMyFootPrint.currentItem = index
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
        tabMyFootprint.navigator = commonNavigator
        ViewPagerHelper.bind(tabMyFootprint, vpMyFootPrint)
    }

    /*
      初始化Fragment栈管理
   */
    private fun initFragment() {
        mStack.add(MyCollectionAndLikeFragment(MyCollectionAndLikeFragment.TYPE_LIKE))   //我的点赞
        mStack.add(MyCommentFragment())   //我的评论
        mStack.add(MyCollectionAndLikeFragment(MyCollectionAndLikeFragment.TYPE_COLLECT))//我的收藏
//        mStack.add(MyLikedFragment())//我喜欢的
        vpMyFootPrint.adapter = MainPagerAdapter(supportFragmentManager, mStack, titles)
        vpMyFootPrint.offscreenPageLimit = 3
        initIndicator()
        vpMyFootPrint.currentItem = 0
    }


    override fun finish() {
        setResult(Activity.RESULT_OK, intent)
        super.finish()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK)
            if (requestCode == SquarePlayDetailActivity.REQUEST_CODE) {
                EventBus.getDefault()
                    .post(
                        NotifyEvent(
                            data!!.getIntExtra("position", -1),
                            data!!.getIntExtra("type", 0)
                        )
                    )
            }
    }

}
