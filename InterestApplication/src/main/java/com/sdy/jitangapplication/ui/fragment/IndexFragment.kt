package com.sdy.jitangapplication.ui.fragment


import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.FragmentUtils
import com.kotlin.base.ui.fragment.BaseMvpFragment
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.event.*
import com.sdy.jitangapplication.model.CustomerMsgBean
import com.sdy.jitangapplication.model.IndexListBean
import com.sdy.jitangapplication.model.IndexTopBean
import com.sdy.jitangapplication.presenter.IndexPresenter
import com.sdy.jitangapplication.presenter.view.IndexView
import com.sdy.jitangapplication.ui.activity.MatchDetailActivity
import com.sdy.jitangapplication.ui.adapter.IndexHiAdater
import com.sdy.jitangapplication.ui.adapter.MainPagerAdapter
import com.sdy.jitangapplication.ui.adapter.PeopleRecommendTopAdapter
import com.sdy.jitangapplication.ui.dialog.ChoicenessOpenPtVipDialog
import com.sdy.jitangapplication.ui.dialog.FilterUserDialog
import com.sdy.jitangapplication.ui.dialog.TouristDialog
import com.sdy.jitangapplication.ui.dialog.VisitorsPayChatDialog
import com.sdy.jitangapplication.utils.UserManager
import com.sdy.jitangapplication.widgets.GreetHiView
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
    private val titles by lazy {
        arrayOf(
            getString(R.string.tab_recommend), getString(R.string.tab_nearby), getString(
                R.string.tab_sweet
            )
        )
    }

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
        mPresenter.context = requireContext()
        EventBus.getDefault().register(this)
        EventBus.getDefault().post(EnableRvScrollEvent(false))

        styleList = UserManager.isStyleList()
        changeListStyle(styleList)
        sweetHeartNew.isVisible = !UserManager.isShowSweetHeartNew()

        initHeadRecommendUser()
        initFragments()

        changeStyleBtn.clickWithTrigger {
            styleList = !styleList
            changeListStyle(styleList)
            EventBus.getDefault().post(ChangeListStyleEvent())
        }

        filterBtn.clickWithTrigger {
            if (UserManager.touristMode)
                TouristDialog(requireContext()).show()
            else
                FilterUserDialog(requireContext()).show()
        }


        GlideUtil.loadCircleImg(requireContext(), UserManager.getAvator(), topMyAvator)
        tobeChoicessBtn.clickWithTrigger {
            ChoicenessOpenPtVipDialog(requireContext()).show()
        }


        //加入甜心圈
        addToSweetBtn.clickWithTrigger {
            EventBus.getDefault().post(SweetAddClickEvent())
        }


        realMsgNotice.setDirection(GreetHiView.DIRECTION_LEFT)
        unrealMsgNotice.setDirection(GreetHiView.DIRECTION_RIGHT)
    }


    private fun initNewMessages(view1: GreetHiView, customerMsgBean: CustomerMsgBean) {
        view1.loadImg(customerMsgBean.avatar, customerMsgBean.accid)
        view1.isVisible = true
        view1.clickWithTrigger {
            if (UserManager.isUserFoot()) {
                //男性解锁聊天
                CommonFunction.checkChat(requireContext(), customerMsgBean.accid)
            } else {
                VisitorsPayChatDialog(requireActivity()).show()
            }

        }
        val animatorSet = AnimatorSet().apply {
            val scaleX = ObjectAnimator.ofFloat(view1, "scaleX", 0.8F, 1f, 0.8F)
            scaleX.repeatCount = 5
            scaleX.duration = 1000
            val scaleY = ObjectAnimator.ofFloat(view1, "scaleY", 0.8F, 1f, 0.8F)
            scaleY.repeatCount = 5
            scaleY.duration = 1000
            interpolator = AccelerateInterpolator()
            playTogether(scaleX, scaleY)
        }
        animatorSet.start()

        animatorSet.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {

            }

            override fun onAnimationEnd(animation: Animator?) {
                view1.isInvisible = true
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
            }

        })
    }


    private fun changeListStyle(styleList: Boolean) {
        UserManager.saveStyleList(styleList)
        if (styleList) {
            changeStyleBtn.setImageResource(R.drawable.icon_style_card)
        } else {
            changeStyleBtn.setImageResource(R.drawable.icon_style_list)
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
                        requireContext(),
                        peopleRecommendTopAdapter.data[position].accid
                    )
            } else {
                TouristDialog(requireContext()).show()
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
                if (position != 0 && UserManager.touristMode) {
                    TouristDialog(activity!!).show()
                    vpIndex.currentItem = 0
                }
                if (position == 2) {
                    UserManager.saveShowSweetHeartNew(true)
                    sweetHeartNew.isVisible = false
                }
                addToSweetBtnFl.isVisible = position == 2 && !isHoney && isInitialize

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
    fun onUpdateTodayWantEvent(event: UpdateNewMsgEvent) {
        if (!unrealMsgNotice.isVisible || unrealMsgNotice.accid == event.customerMsgBean.accid) {
            initNewMessages(unrealMsgNotice, event.customerMsgBean)
        } else {
            initNewMessages(realMsgNotice, event.customerMsgBean)
        }
    }


    /**
     * 刷新顶部推荐数据
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTopCardEvent(event: TopCardEvent) {
        mPresenter.indexTop()
    }

    /**
     * 刷新加入甜心圈显示
     */
    private var isHoney = false
    private var isInitialize = false

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTopCardEvent(event: RefreshSweetAddEvent) {
        isInitialize = true
        isHoney = event.isHoney
        if ((FragmentUtils.getTopShow(requireFragmentManager()) as PeopleNearbyFragment?)?.type == PeopleNearbyFragment.TYPE_SWEET_HEART)
            addToSweetBtnFl.isVisible = !isHoney
    }


    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onShowGuideChangeStyleEvent(event: ShowGuideChangeStyleEvent) {
        if (!UserManager.isGuideBrowseStyleCl()) {
            initGuideCl()
        } else {
            guideBrowseStyleCl.isVisible = false
        }

    }


    private fun initGuideCl() {
        BarUtils.setStatusBarColor(requireActivity(), resources.getColor(R.color.colorHalfBlack))
        guideBrowseStyleCl.isVisible = true
        nextBtn1.clickWithTrigger {
            iv1.isVisible = false
            iv2.isVisible = false
            tv1.isVisible = false
            nextBtn1.isVisible = false

            iv11.isVisible = true
            iv21.isVisible = true
            tv11.isVisible = true
            nextBtn11.isVisible = true
        }
        nextBtn11.clickWithTrigger {
            guideBrowseStyleCl.isVisible = false
            BarUtils.setStatusBarColor(requireActivity(), Color.WHITE)
            UserManager.saveGuideBrowseStyleCl(true)
        }
    }

    override fun indexTopResult(data: IndexListBean?) {
        if (data != null) {
            if (data.list.isNullOrEmpty()) {
                data.list = mutableListOf()
            }
            data.list.add(0, IndexTopBean(type = 0))
            peopleRecommendTopAdapter.setNewData(data.list)

            peopleRecommendTopAdapter.todayvisit = data.today_visit_cnt
            peopleRecommendTopAdapter.todayExplosure = data.today_exposure_cnt
            peopleRecommendTopAdapter.total_exposure_cnt = data.total_exposure_cnt
            peopleRecommendTopAdapter.free_show = data.free_show
            peopleRecommendTopAdapter.allvisit = data.total_visit_cnt
            peopleRecommendTopAdapter.mv_url = data.mv_url
            peopleRecommendTopAdapter.isplatinum = data.isplatinumvip

            UserManager.saveGender(data.gender)
            if ((data.gender == 1 && data.isplatinumvip) || (data.gender == 2 && data.mv_url)) {
                tobeChoicenessCl.isVisible = false
            } else {
                tobeChoicenessCl.isVisible = true
                recommendUsers.scrollToPosition(1)
            }
        }

    }


    override fun onDestroyView() {
        super.onDestroyView()
    }


}
