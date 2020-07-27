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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.blankj.utilcode.util.SizeUtils
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.fragment.BaseMvpFragment
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.event.EnableRvScrollEvent
import com.sdy.jitangapplication.event.TopCardEvent
import com.sdy.jitangapplication.event.UpdateTodayWantEvent
import com.sdy.jitangapplication.model.IndexListBean
import com.sdy.jitangapplication.model.IndexTopBean
import com.sdy.jitangapplication.presenter.IndexPresenter
import com.sdy.jitangapplication.presenter.view.IndexView
import com.sdy.jitangapplication.ui.activity.MatchDetailActivity
import com.sdy.jitangapplication.ui.adapter.MainPagerAdapter
import com.sdy.jitangapplication.ui.adapter.PeopleRecommendTopAdapter
import com.sdy.jitangapplication.ui.dialog.ChoicenessOpenPtVipDialog
import com.sdy.jitangapplication.ui.dialog.FilterUserDialog
import com.sdy.jitangapplication.ui.dialog.TodayWantDialog
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
class IndexFragment : BaseMvpFragment<IndexPresenter>(), IndexView {


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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadData()
        mPresenter.indexTop()
    }

    fun loadData() {
        mPresenter = IndexPresenter()
        mPresenter.mView = this
        mPresenter.context = activity!!
        EventBus.getDefault().register(this)
        EventBus.getDefault().post(EnableRvScrollEvent(false))
        initHeadRecommendUser()
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

        GlideUtil.loadCircleImg(activity!!, UserManager.getAvator(), topMyAvator)
        tobeChoicessBtn.clickWithTrigger {
            ChoicenessOpenPtVipDialog(activity!!).show()
        }

    }

    private var taged = false

    private val peopleRecommendTopAdapter by lazy { PeopleRecommendTopAdapter() }

    //初始化顶部推荐数据
    private fun initHeadRecommendUser() {
        recommendUsers.layoutManager = LinearLayoutManager(activity, RecyclerView.HORIZONTAL, false)
        recommendUsers.adapter = peopleRecommendTopAdapter
        peopleRecommendTopAdapter.setOnItemClickListener { adapter, view, position ->
            if (!UserManager.touristMode)
                MatchDetailActivity.start(
                    activity!!,
                    peopleRecommendTopAdapter.data[position].accid
                )
            else
                TouristDialog(activity!!).show()
        }

    }

    private fun initFragments() {

        fragments.add(recommendFragment)
        fragments.add(sameCityFragment)

        vpIndex.setScrollable(!UserManager.touristMode)
        vpIndex.offscreenPageLimit = 2
        vpIndex.adapter = MainPagerAdapter(childFragmentManager, fragments)
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
                simplePagerTitleView.selectedColor = Color.parseColor("#FF333333")
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
     * 刷新顶部推荐数据
     *  //todo 发送通知 上传视频成功 或者充值高级会员，更新首页
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTopCardEvent(event: TopCardEvent) {
        mPresenter.indexTop()
    }

    override fun indexTopResult(data: IndexListBean?) {
        if (data != null) {
            if (data?.list.isNullOrEmpty()) {
                data?.list = mutableListOf()
            }
            data?.list.add(0, IndexTopBean(type = 0))
            peopleRecommendTopAdapter.setNewData(data?.list)

            peopleRecommendTopAdapter.todayvisit = data!!.today_visit_cnt
            peopleRecommendTopAdapter.todayExplosure = data!!.today_exposure_cnt
            peopleRecommendTopAdapter.total_exposure_cnt = data!!.total_exposure_cnt
            peopleRecommendTopAdapter.free_show = data!!.free_show
            peopleRecommendTopAdapter.allvisit = data!!.total_visit_cnt
            peopleRecommendTopAdapter.mv_url = data!!.mv_url
            peopleRecommendTopAdapter.isplatinum = data!!.isplatinumvip

            if ((data!!.gender == 1 && data!!.isplatinumvip) || (data.gender == 2 && data!!.mv_url)) {
                tobeChoicenessCl.isVisible = false
            } else {
                tobeChoicenessCl.isVisible = true
                recommendUsers.scrollToPosition(1)
            }
        }

    }


}
