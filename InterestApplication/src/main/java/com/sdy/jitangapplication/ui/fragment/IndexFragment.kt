package com.sdy.jitangapplication.ui.fragment


import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.blankj.utilcode.util.SizeUtils
import com.flyco.tablayout.listener.CustomTabEntity
import com.flyco.tablayout.listener.OnTabSelectListener
import com.kotlin.base.ui.fragment.BaseMvpFragment
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.event.*
import com.sdy.jitangapplication.model.IndexListBean
import com.sdy.jitangapplication.model.IndexTopBean
import com.sdy.jitangapplication.model.TabEntity
import com.sdy.jitangapplication.presenter.IndexPresenter
import com.sdy.jitangapplication.presenter.view.IndexView
import com.sdy.jitangapplication.ui.activity.MatchDetailActivity
import com.sdy.jitangapplication.ui.adapter.MainPagerAdapter
import com.sdy.jitangapplication.ui.adapter.PeopleRecommendTopAdapter
import com.sdy.jitangapplication.ui.dialog.ChoicenessOpenPtVipDialog
import com.sdy.jitangapplication.ui.dialog.FilterUserDialog
import com.sdy.jitangapplication.ui.dialog.TouristDialog
import com.sdy.jitangapplication.utils.UserManager
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.fragment_index.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

/**
 * 首页fragment
 */
class IndexFragment : BaseMvpFragment<IndexPresenter>(), IndexView {


    private val fragments by lazy { Stack<Fragment>() }
    private val titles by lazy { arrayOf("推荐", "附近", "甜心圈") }

    //    private val matchFragment by lazy { MatchFragment() }
    private val recommendFragment by lazy { PeopleNearbyFragment(PeopleNearbyFragment.TYPE_RECOMMEND) }

    //    private val findByTagFragment by lazy { FindByTagFragment() }
    private val sameCityFragment by lazy { PeopleNearbyFragment(PeopleNearbyFragment.TYPE_SAMECITY) }

    private val sweetHeartFragment by lazy { PeopleNearbyFragment(PeopleNearbyFragment.TYPE_SWEET_HEART) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_index, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadData()
        mPresenter.indexTop()
    }

    private var styleList: Boolean = true
    fun loadData() {
        mPresenter = IndexPresenter()
        mPresenter.mView = this
        mPresenter.context = activity!!
        EventBus.getDefault().register(this)
        EventBus.getDefault().post(EnableRvScrollEvent(false))

        styleList = UserManager.isStyleList()
        changeListStyle(styleList)
        sweetHeartNew.isVisible = !UserManager.isShowSweetHeartNew()

        initHeadRecommendUser()
        initFragments()

        changeStyleBtn.clickWithTrigger {
            //todo 改变列表的样式
            styleList = !styleList
            changeListStyle(styleList)
            EventBus.getDefault().post(ChangeListStyleEvent())
        }

        filterBtn.clickWithTrigger {
            if (UserManager.touristMode)
                TouristDialog(activity!!).show()
            else
                FilterUserDialog(activity!!).show()
        }

        //约会推荐点击
        todayWantCl.clickWithTrigger {
            if (UserManager.touristMode) {
                TouristDialog(activity!!).show()
            } else if (UserManager.getGender() == 1) {
                EventBus.getDefault().post(JumpToDatingEvent())
            } else {
                CommonFunction.checkPublishDating(activity!!)
            }
        }

        GlideUtil.loadCircleImg(activity!!, UserManager.getAvator(), topMyAvator)
        tobeChoicessBtn.clickWithTrigger {
            ChoicenessOpenPtVipDialog(activity!!).show()
        }


    }


    private fun changeListStyle(styleList: Boolean) {
        UserManager.saveStyleList(styleList)
        if (styleList) {
            changeStyleBtn.setImageResource(R.drawable.icon_style_card)
            //TODO 设置为列表样式
        } else {
            changeStyleBtn.setImageResource(R.drawable.icon_style_list)
            //TODO 设置为卡片样式
        }
    }

    private var taged = false

    private val peopleRecommendTopAdapter by lazy { PeopleRecommendTopAdapter() }

    //初始化顶部推荐数据
    private fun initHeadRecommendUser() {
        recommendUsers.layoutManager = LinearLayoutManager(activity, RecyclerView.HORIZONTAL, false)
        recommendUsers.adapter = peopleRecommendTopAdapter
        peopleRecommendTopAdapter.setOnItemClickListener { adapter, view, position ->
            if (!UserManager.touristMode) {
                if (peopleRecommendTopAdapter.data[position].type == 1)
                    MatchDetailActivity.start(
                        activity!!,
                        peopleRecommendTopAdapter.data[position].accid
                    )
            } else {
                TouristDialog(activity!!).show()
            }
        }

    }

    private fun initFragments() {

        fragments.add(recommendFragment)
        fragments.add(sameCityFragment)
        fragments.add(sweetHeartFragment)

        vpIndex.setScrollable(!UserManager.touristMode)
        vpIndex.offscreenPageLimit = fragments.size
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
                if (position == 2) {
                    UserManager.saveShowSweetHeartNew(true)
                    sweetHeartNew.isVisible = false
                }
            }
        })

        vpIndex.currentItem = 0
        titleIndex.setTitle(titles)
        titleIndex.setViewPager(vpIndex)
    }


    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateTodayWantEvent(event: UpdateTodayWantEvent) {
    }


    /**
     * 刷新顶部推荐数据
     *  //todo 发送通知 上传视频成功 或者充值黄金会员，更新首页
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

            UserManager.saveGender(data.gender)
            if ((data!!.gender == 1 && data!!.isplatinumvip) || (data.gender == 2 && data!!.mv_url)) {
                tobeChoicenessCl.isVisible = false
            } else {
                tobeChoicenessCl.isVisible = true
                recommendUsers.scrollToPosition(1)
            }
            //  setRecommendDatingView(data.dating_list)
        }

    }

    private fun setRecommendDatingView(datings: MutableList<String>) {
        todayWantVp.isVisible = datings.isNotEmpty()
        val params = todayWantContent.layoutParams as LinearLayout.LayoutParams
        if (todayWantVp.isVisible) {
            params.leftMargin = SizeUtils.dp2px(0F)
        } else {
            params.leftMargin = SizeUtils.dp2px(5F)
        }
        if (UserManager.getGender() == 1) {
            todayWantContent.text = "她想约你"
        } else {
            todayWantContent.text = "发布活动"
        }
        todayWantVp.removeAllViews()
        val images = mutableListOf<CircleImageView>()
        for (data in datings) {
            val v = CircleImageView(activity!!)
            v.borderColor = Color.WHITE
            v.borderWidth = SizeUtils.dp2px(1F)
            val params = ViewGroup.LayoutParams(SizeUtils.dp2px(23F), SizeUtils.dp2px(23F))
            v.layoutParams = params
            GlideUtil.loadCircleImg(activity!!, data, v)
            images.add(v)
        }
        todayWantVp.creatView(activity!!, images)
        if (images.size >= 3)
            todayWantVp.startLoop()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        todayWantVp.stopLoop()
    }


}
