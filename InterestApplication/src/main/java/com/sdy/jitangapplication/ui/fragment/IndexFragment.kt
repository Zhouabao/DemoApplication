package com.sdy.jitangapplication.ui.fragment


import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.blankj.utilcode.util.SizeUtils
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.fragment.BaseMvpLazyLoadFragment
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.event.ShowNearCountEvent
import com.sdy.jitangapplication.event.TopCardEvent
import com.sdy.jitangapplication.event.UpdateTodayWantEvent
import com.sdy.jitangapplication.presenter.IndexPresenter
import com.sdy.jitangapplication.presenter.view.IndexView
import com.sdy.jitangapplication.ui.adapter.MainPagerAdapter
import com.sdy.jitangapplication.ui.dialog.FilterUserDialog
import com.sdy.jitangapplication.ui.dialog.TodayWantDialog
import com.sdy.jitangapplication.ui.dialog.TopCardDialog
import com.sdy.jitangapplication.ui.dialog.TouristDialog
import com.sdy.jitangapplication.utils.UserManager
import com.sdy.jitangapplication.widgets.CustomScaleTransitionPagerTitleView
import kotlinx.android.synthetic.main.fragment_index.*
import net.lucode.hackware.magicindicator.ViewPagerHelper
import net.lucode.hackware.magicindicator.buildins.UIUtil
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

/**
 * 首页fragment
 */
class IndexFragment : BaseMvpLazyLoadFragment<IndexPresenter>(), IndexView {


    private val fragments by lazy { Stack<Fragment>() }
    private val titles by lazy { arrayOf("推荐", "附近") }
    //    private val matchFragment by lazy { MatchFragment() }
    private val recommendFragment by lazy { PeopleNearbyFragment(PeopleNearbyFragment.TYPE_RECOMMEND) }
    //    private val findByTagFragment by lazy { FindByTagFragment() }
    private val sameCityFragment by lazy { PeopleNearbyFragment(PeopleNearbyFragment.TYPE_SAMECITY) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_index, container, false)
    }

    //    val titleAdapter by lazy { IndexSwitchAdapter() }
    private val todayWantDialog by lazy { TodayWantDialog(activity!!, null) }

    override fun loadData() {
        EventBus.getDefault().register(this)


        initFragments()

        filterBtn.clickWithTrigger {
            if (UserManager.touristMode)
                TouristDialog(activity!!).show()
            else
                FilterUserDialog(activity!!).show()
        }

        //选择今日意向
        todayWantCl.clickWithTrigger {
            if (UserManager.touristMode)
                TouristDialog(activity!!).show()
            else
                todayWantDialog.show()
        }

        //置顶卡片
        topCardBtn.clickWithTrigger {
            TopCardDialog(activity!!).show()
//            TouristDialog(activity!!).show()
        }

    }

    private fun initFragments() {


        fragments.add(recommendFragment)
        fragments.add(sameCityFragment)

        vpIndex.setScrollable(!UserManager.touristMode)
        vpIndex.offscreenPageLimit = 2
        vpIndex.adapter = MainPagerAdapter(activity!!.supportFragmentManager, fragments)
        vpIndex.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                if (position == 1 && UserManager.touristMode) {
                    TouristDialog(activity!!).show()
                    vpIndex.currentItem = 0
                } else {
                    if (position == 0) {
                        EventBus.getDefault().post(ShowNearCountEvent())
                    }
                }
            }
        })

        initIndicator()
        vpIndex.currentItem = 0
    }

    private fun initIndicator() {
        val commonNavigator = CommonNavigator(activity!!)
        commonNavigator.adapter = object : CommonNavigatorAdapter() {
            override fun getCount(): Int {
                return fragments.size
            }

            override fun getTitleView(context: Context, index: Int): IPagerTitleView {
                val simplePagerTitleView = CustomScaleTransitionPagerTitleView(context)
                simplePagerTitleView.text = titles[index]
                simplePagerTitleView.minScale = 0.66F
                simplePagerTitleView.textSize = 24F
                simplePagerTitleView.normalColor = Color.parseColor("#191919")
                simplePagerTitleView.selectedColor = Color.parseColor("#FF6318")
                simplePagerTitleView.setPadding(SizeUtils.dp2px(5F), 0, 0, 0)
                simplePagerTitleView.onClick {
                    if (UserManager.touristMode && index == 1) {//游客模式不能查看附近的人
                        TouristDialog(activity!!).show()
                        vpIndex.currentItem = 0
                    } else {
                        vpIndex.currentItem = index
                    }
                }
                return simplePagerTitleView
            }

            override fun getIndicator(context: Context): IPagerIndicator {
                val indicator = LinePagerIndicator(context)
                indicator.mode = LinePagerIndicator.MODE_EXACTLY
                indicator.lineHeight = UIUtil.dip2px(context, 4.0).toFloat()
                indicator.lineWidth = UIUtil.dip2px(context, 16.0).toFloat()
                indicator.setPadding(SizeUtils.dp2px(15F), 0, 0, 0)
                indicator.roundRadius = UIUtil.dip2px(context, 2.0).toFloat()
                indicator.startInterpolator = AccelerateInterpolator()
                indicator.endInterpolator = DecelerateInterpolator(1.0f)
                indicator.setColors(resources.getColor(R.color.colorOrange))
                return indicator
            }
        }
        titleIndex.navigator = commonNavigator
        ViewPagerHelper.bind(titleIndex, vpIndex)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateTodayWantEvent(event: UpdateTodayWantEvent) {
        if (event.todayWantBean != null) {
            todayWantContent.text = event.todayWantBean.title
//            GlideUtil.loadCircleImg(activity!!, event.todayWantBean.icon, todayWantIcon)
        } else {
            todayWantContent.text = "选择意向"
            todayWantIcon.setImageResource(R.drawable.icon_today_want_heart)
        }
    }


    /**
     * @param event showTop用户是否是钻石会员
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTopCardEvent(event: TopCardEvent) {
//        在用户为黄金会员（有门槛）或普通用户时（无门槛）
        if ((UserManager.registerFileBean?.threshold == true && UserManager.isUserVip()) || UserManager.registerFileBean?.threshold == false) {
            topCardBtn.isVisible = !event.showTop
        } else {
            topCardBtn.isVisible = false
        }
    }
}
