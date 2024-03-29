package com.sdy.jitangapplication.ui.fragment

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.SizeUtils
import com.kennyc.view.MultiStateView
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.fragment.BaseMvpLazyLoadFragment
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.constant.RefreshState
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.event.*
import com.sdy.jitangapplication.model.CheckBean
import com.sdy.jitangapplication.model.NearBean
import com.sdy.jitangapplication.model.TodayFateBean
import com.sdy.jitangapplication.presenter.PeopleNearbyPresenter
import com.sdy.jitangapplication.presenter.view.PeopleNearbyView
import com.sdy.jitangapplication.ui.adapter.PeopleNearbyAdapter
import com.sdy.jitangapplication.ui.dialog.*
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.empty_friend_layout.view.*
import kotlinx.android.synthetic.main.error_layout.view.*
import kotlinx.android.synthetic.main.fragment_people_nearby.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 附近的人
 */
class PeopleNearbyFragment(var type: Int = TYPE_RECOMMEND) :
    BaseMvpLazyLoadFragment<PeopleNearbyPresenter>(), PeopleNearbyView,
    OnRefreshListener, OnLoadMoreListener {
    companion object {
        const val TYPE_RECOMMEND = 1
        const val TYPE_SAMECITY = 2
    }

    private val adapter by lazy { PeopleNearbyAdapter() }
    private var firstLoad = true
    private var ranking_level: Int = 0
    private var page = 1
    private var isLoadingMore = false
    private val params by lazy {
        hashMapOf(
            "lng" to UserManager.getlongtitude().toFloat(),
            "lat" to UserManager.getlatitude().toFloat(),
            "city_name" to UserManager.getCity(),
            "province_name" to UserManager.getProvince(),
            "page" to page,
            "pagesize" to Constants.PAGESIZE
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_people_nearby, container, false)
    }

    private val linearLayoutManager by lazy {
        LinearLayoutManager(
            activity!!,
            RecyclerView.VERTICAL,
            false
        )
    }

    override fun loadData() {


        EventBus.getDefault().register(this)

        mPresenter = PeopleNearbyPresenter()
        mPresenter.mView = this
        mPresenter.context = activity!!

        refreshPeopleNearby.setOnRefreshListener(this)
        refreshPeopleNearby.setOnLoadMoreListener(this)

        statePeopleNearby.retryBtn.onClick {
            statePeopleNearby.viewState = MultiStateView.VIEW_STATE_LOADING
            refreshPeopleNearby.autoRefresh()
        }
        changeAvatorCloseBtn.clickWithTrigger {

            EventBus.getDefault().post(UpdateShowTopAlert())
        }

        rvPeopleNearby.layoutManager = linearLayoutManager
        rvPeopleNearby.adapter = adapter
//        adapter.addHeaderView(initHeadView())
        adapter.setEmptyView(R.layout.empty_friend_layout, rvPeopleNearby)
        adapter.emptyView.emptyFriendTitle.text = "这里暂时没有人"
        adapter.emptyView.emptyFriendTip.text = "过会儿再来看看吧"
        adapter.emptyView.emptyImg.setImageResource(R.drawable.icon_empty_friend)
        adapter.setHeaderAndEmpty(true)

        rvPeopleNearby.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val lastVisible = linearLayoutManager.findLastVisibleItemPosition()
                val total = linearLayoutManager.itemCount
                if (lastVisible >= total - 5 && dy > 0) {
                    if (!isLoadingMore) {
                        onLoadMore(refreshPeopleNearby)
                        isLoadingMore = true
                    }
                }
            }
        })

        updateFilterParams()
        if (!UserManager.touristMode)
            mPresenter.todayRecommend()
        else
            mPresenter.nearlyIndex(params,type)

    }

    private fun showOpenVipCl(isvip: Boolean) {
        if (!isvip && type == TYPE_SAMECITY && UserManager.getGender() == 1) {
            openVipCl.isVisible = true
            t2.text = if (UserManager.getGender() == 1) {
                "在${UserManager.getCity()}共有${SPUtils.getInstance(Constants.SPNAME).getInt(
                    "people_amount",
                    0
                )}名糖宝女孩\n满足你的需求"
            } else {
                "${UserManager.getCity()}的${SPUtils.getInstance(Constants.SPNAME).getInt(
                    "people_amount",
                    0
                )}位${if (UserManager.getGender() == 1) {
                    "女"
                } else {
                    "男"
                }}性正等待你联络"
            }

            openVipBtn.text = if (UserManager.getGender() == 1) {
                "开通会员联系她们"
            } else {
                "开通会员"
            }
            GlideUtil.loadCircleImg(activity!!, UserManager.getAvator(), myAvator)
            openVipBtn.clickWithTrigger {
                ChargeVipDialog(ChargeVipDialog.LOOK_SAME_CITY, activity!!).show()
            }
            if (UserManager.getGender() == 1) {
                lottieMoreMatch.imageAssetsFolder = "images_boy"
                lottieMoreMatch.setAnimation("data_boy_more_match.json")
            } else {
                lottieMoreMatch.imageAssetsFolder = "images_girl"
                lottieMoreMatch.setAnimation("data_girl_more_match.json")
            }


            val scaleAnimationX = ObjectAnimator.ofFloat(myAvator, "scaleX", 0F, 1F)
            val scaleAnimationY = ObjectAnimator.ofFloat(myAvator, "scaleY", 0F, 1F)
            val alphaAnimation = ObjectAnimator.ofFloat(myAvator, "alpha", 0F, 1F)
            val animationSet = AnimatorSet()
            animationSet.duration = 500
            animationSet.playTogether(scaleAnimationX, scaleAnimationY, alphaAnimation)
            animationSet.start()

            animationSet.addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {

                }

                override fun onAnimationEnd(animation: Animator?) {
                    lottieMoreMatch.playAnimation()
                }

                override fun onAnimationCancel(animation: Animator?) {
                }

                override fun onAnimationStart(animation: Animator?) {
                }

            })


            lottieMoreMatch.addAnimatorListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {
                }

                override fun onAnimationEnd(animation: Animator?) {
                    lottieMoreMatchRipple1.postDelayed({
                        try {
                            lottieMoreMatchRipple1.playAnimation()
                        } catch (e: Exception) {
                        }
                    }, 2000L)

                    lottieMoreMatchRipple2.postDelayed({
                        try {
                            lottieMoreMatchRipple2.playAnimation()
                        } catch (e: Exception) {
                        }
                    }, 4000L)


                }

                override fun onAnimationCancel(animation: Animator?) {
                }

                override fun onAnimationStart(animation: Animator?) {
                    lottieMoreMatchRipple.playAnimation()
                }

            })

        } else {
            openVipCl.isVisible = false
        }
    }


    override fun onRefresh(refreshLayout: RefreshLayout) {
        page = 1
        params["page"] = page
        refreshPeopleNearby.resetNoMoreData()
        mPresenter.nearlyIndex(params, type)
    }

    override fun onLoadMore(refreshLayout: RefreshLayout) {
        page += 1
        params["page"] = page
        mPresenter.nearlyIndex(params, type)
    }


    override fun nearlyIndexResult(success: Boolean, nearBean: NearBean?) {
        if (success) {
            //如果没有显示过协议
            //否则直接判断有没有显示过引导页面
            //是否今日缘分
            //是否今日意向
            //资料完善度
            showOpenVipCl(nearBean?.isvip ?: false)
            if (!(UserManager.getAccountDanger() || UserManager.getAccountDangerAvatorNotPass()))
                if (!UserManager.getAlertProtocol()) {
                    PrivacyDialog(activity!!, nearBean, indexRecommends).show()
                } else if (nearBean?.iscompleteguide != true) {
                    GuideSendCandyDialog(activity!!, nearBean, indexRecommends).show()
                } else if (!indexRecommends?.list.isNullOrEmpty() && indexRecommends?.today_pull == false && !UserManager.showIndexRecommend) {
                    TodayFateDialog(activity!!, nearBean, indexRecommends).show()
                } else if (nearBean!!.today_find!!.id == -1 && !nearBean?.today_find_pull) {
                    TodayWantDialog(activity!!, nearBean).show()
                } else if (nearBean!!.complete_percent < nearBean!!.complete_percent_normal && !UserManager.showCompleteUserCenterDialog) {
                    //如果自己的完善度小于标准值的完善度，就弹出完善个人资料的弹窗
                    CompleteUserCenterDialog(activity!!).show()
                }

            if (nearBean?.today_find != null && !nearBean?.today_find?.title.isNullOrEmpty()) {
                EventBus.getDefault().post(
                    UpdateTodayWantEvent(
                        CheckBean(
                            icon = nearBean?.today_find?.icon ?: "",
                            title = nearBean?.today_find?.title ?: "",
                            checked = true,
                            id = nearBean!!.today_find!!.id
                        )
                    )
                )
            }
            statePeopleNearby.viewState = MultiStateView.VIEW_STATE_CONTENT
            if (refreshPeopleNearby.state == RefreshState.Refreshing) {
                adapter.data.clear()
                adapter.notifyDataSetChanged()
                rvPeopleNearby.scrollToPosition(0)
                refreshPeopleNearby.finishRefresh(success)
            }
            if (nearBean != null && nearBean.list?.size < Constants.PAGESIZE) {
                refreshPeopleNearby.finishLoadMoreWithNoMoreData()
            } else {
                refreshPeopleNearby.finishLoadMore(true)
            }

            //头像等级
            ranking_level = nearBean!!.ranking_level
            //保存 VIP信息
            UserManager.saveUserVip(nearBean.isvip)
            EventBus.getDefault().post(TopCardEvent(nearBean.isplatinum))
            onUpdateSameCityVipEvent(UpdateSameCityVipEvent())
            //保存认证信息
            UserManager.saveUserVerify(nearBean.isfaced)
            //保存是否进行过人脸验证
            UserManager.saveHasFaceUrl(nearBean.has_face_url)

            //第一次加载的时候就显示顶部提示条
            if (firstLoad) {
                if (ranking_level == 2) {//2 真人提示
                    (refreshPeopleNearby.layoutParams as FrameLayout.LayoutParams).topMargin =
                        SizeUtils.dp2px(41F)
                    lieAvatorLl.isVisible = true
                    lieAvatorContent.text = "当前头像非真实头像，替换后可获得首页推荐"
                    changeAvatorBtn.text = "立即替换"
                    changeAvatorCloseBtn.isVisible = false
                }
//                else if (!nearBean.is_full) {
//                    (refreshPeopleNearby.layoutParams as FrameLayout.LayoutParams).topMargin =
//                        SizeUtils.dp2px(41F)
//                    lieAvatorLl.isVisible = true
//                    lieAvatorContent.text = "当前有未完善兴趣，完善提升被打招呼几率"
//                    changeAvatorBtn.text = "立即完善"
//                    changeAvatorCloseBtn.isVisible = true
//                }
                else {
                    lieAvatorLl.isVisible = false
                }
                firstLoad = false
            }

            adapter.addData(nearBean?.list ?: mutableListOf())

            if (adapter.data.isNullOrEmpty()) {
                adapter.isUseEmpty(true)
            }
        } else {
            refreshPeopleNearby.finishLoadMore(false)
            refreshPeopleNearby.finishRefresh(false)
            statePeopleNearby.viewState = MultiStateView.VIEW_STATE_ERROR
        }
        isLoadingMore = false

    }

    private var indexRecommends: TodayFateBean? = null
    override fun onTodayRecommendResult(data: TodayFateBean?) {
        /**
         * 今日推荐获取结果
         */
        if (data != null) {
            indexRecommends = data
            mPresenter.nearlyIndex(params, type)
        } else {
            statePeopleNearby.viewState = MultiStateView.VIEW_STATE_ERROR
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onPause() {
        super.onPause()
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateNearPeopleParamsEvent(event: UpdateNearPeopleParamsEvent) {
        updateFilterParams()
        refreshPeopleNearby.autoRefresh()
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateSameCityVipEvent(event: UpdateSameCityVipEvent) {
        if (type == TYPE_SAMECITY)
            showOpenVipCl(true)
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateShowTopAlert(event: UpdateShowTopAlert) {
        (refreshPeopleNearby.layoutParams as FrameLayout.LayoutParams).topMargin = 0
        lieAvatorLl.isVisible = false
        firstLoad = false
    }

    private fun updateFilterParams() {
        //加入本地的筛选对话框的筛选条件
        if (params["audit_only"] != null)
            params.remove("audit_only")
//        if (params["local_only"] != null)
//            params.remove("local_only")
        val params1 = UserManager.getFilterConditions()
        params1.forEach {
            params[it.key] = it.value
        }
        if (params["lng"] == 0.0F) {
            params["lat"] = UserManager.getlongtitude().toFloat()
            params["lng"] = UserManager.getlatitude().toFloat()
            params["city_code"] = UserManager.getCityCode()
        }
    }

}
