package com.sdy.jitangapplication.ui.fragment


import android.animation.Animator
import android.animation.ObjectAnimator
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.viewpager.widget.ViewPager
import com.airbnb.lottie.LottieAnimationView
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ui.fragment.BaseMvpLazyLoadFragment
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.msg.MessageBuilder
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.CustomMessageConfig
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.event.*
import com.sdy.jitangapplication.model.MatchBean
import com.sdy.jitangapplication.model.MatchListBean
import com.sdy.jitangapplication.model.Newtag
import com.sdy.jitangapplication.model.StatusBean
import com.sdy.jitangapplication.nim.attachment.ChatHiAttachment
import com.sdy.jitangapplication.presenter.MatchPresenter
import com.sdy.jitangapplication.presenter.view.MatchView
import com.sdy.jitangapplication.ui.activity.MatchDetailActivity
import com.sdy.jitangapplication.ui.activity.MatchSucceedActivity
import com.sdy.jitangapplication.ui.activity.MyLabelActivity
import com.sdy.jitangapplication.ui.activity.NewUserInfoSettingsActivity
import com.sdy.jitangapplication.ui.adapter.MatchUserAdapter
import com.sdy.jitangapplication.ui.dialog.*
import com.sdy.jitangapplication.utils.UserManager
import com.yuyakaido.android.cardstackview.*
import kotlinx.android.synthetic.main.empty_friend_layout.view.*
import kotlinx.android.synthetic.main.error_layout.*
import kotlinx.android.synthetic.main.fragment_match.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.startActivityForResult

/**
 * 匹配页面(新版)
 */
class MatchFragment : BaseMvpLazyLoadFragment<MatchPresenter>(), MatchView, View.OnClickListener,
    CardStackListener {


    override fun loadData() {
        initView()
    }


    private var hasMore = false

    //用户适配器
    private val matchUserAdapter: MatchUserAdapter by lazy { MatchUserAdapter(mutableListOf()) }
    //我的资料完整度
    private var my_percent_complete: Int = 0//（我的资料完整度）
    //标准完整度
    private var normal_percent_complete: Int = 0//（标准完整度）
    private var myCount: Int = 0//当前滑动次数
    private var maxCount: Int = 0//最大滑动次数
    private var ranking_level: Int = 0
    private var isShowChangeAvatorRealMan: Boolean = false


    companion object {
        private const val LOADING = 0
        private const val CONTENT = 1
        private const val ERROR = 2
        private const val EMPTY = 3
        private const val PAGESIZE = 20

    }


    //请求广场的参数 TODO要更新tagid
    private val matchParams by lazy {
        hashMapOf(
            "accid" to SPUtils.getInstance(Constants.SPNAME).getString("accid"),
            "token" to SPUtils.getInstance(Constants.SPNAME).getString("token"),
            "_timestamp" to System.currentTimeMillis(),
            "lng" to UserManager.getlongtitude().toFloat(),
            "lat" to UserManager.getlatitude().toFloat(),
            "city_code" to UserManager.getCityCode(),
            "type" to 1
        )

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
    }

    //    private val matchUserAdapter by lazy { CardAdapter() }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_match, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }


    private fun updateLocation() {
        //加入本地的筛选对话框的筛选条件
        if (matchParams["audit_only"] != null)
            matchParams.remove("audit_only")
        if (matchParams["local_only"] != null)
            matchParams.remove("local_only")
        val params = UserManager.getFilterConditions()
        params.forEach {
            matchParams[it.key] = it.value
        }
        if (matchParams["lng"].toString().toFloat() == 0.0F) {
            matchParams["lat"] = UserManager.getlongtitude().toFloat()
            matchParams["lng"] = UserManager.getlatitude().toFloat()
            matchParams["city_code"] = UserManager.getCityCode()
        }
    }

    //    CompleteLabelDialog
    private val manager by lazy { CardStackLayoutManager(activity!!, this) }

    private fun initView() {
        mPresenter = MatchPresenter()
        mPresenter.mView = this
        mPresenter.context = activity!!

        retryBtn.setOnClickListener(this)
        changeAvatorCloseBtn.setOnClickListener(this)
        changeAvatorBtn.setOnClickListener(this)

        initialize()

        updateLocation()

        mPresenter.getMatchList(matchParams)

        matchUserAdapter.setOnItemChildClickListener { _, view, position ->
            val item = matchUserAdapter.data[manager.topPosition]
            val itemView = manager.topView
            when (view.id) {
                R.id.v1 -> {
                    (itemView.findViewById<ConstraintLayout>(R.id.v1)).isEnabled = false
                    if ((matchUserAdapter.data[manager.topPosition].accid
                            ?: "") != UserManager.getAccid()
                    )
                        MatchDetailActivity.start(
                            activity!!,
                            matchUserAdapter.data[manager.topPosition].accid
                        )
                    (itemView.findViewById<ConstraintLayout>(R.id.v1)).isEnabled = true
                }
                R.id.btnHiLottieView,
                R.id.btnHi -> {

                    CommonFunction.commonGreet(
                        activity!!,
                        matchUserAdapter.data[manager.topPosition].accid,
                        targetAvator = matchUserAdapter.data[manager.topPosition].avatar ?: "",
                        view = view, needSwipe = true
                    )
                }
                R.id.nextImgBtn -> {
                    if (itemView != null) {
                        val vpPhotos = itemView.findViewById<ViewPager>(R.id.vpPhotos)
                        if (vpPhotos.currentItem < (item.photos
                                ?: mutableListOf<MatchBean>()).size - 1
                        ) {
                            val index = vpPhotos.currentItem
                            vpPhotos.setCurrentItem(index + 1, true)
                        } else {
//                            EventBus.getDefault().post(ShakeEvent(true))
                            YoYo.with(Techniques.Shake)
                                .duration(300)
                                .repeat(0)
                                .playOn(itemView)
                        }

                    }
                }
                R.id.lastImgBtn -> {
                    val itemView = manager.topView
                    if (itemView != null) {
                        val vpPhotos = itemView.findViewById<ViewPager>(R.id.vpPhotos)
                        if (vpPhotos.currentItem > 0) {
                            val index = vpPhotos.currentItem
                            vpPhotos.setCurrentItem(index - 1, true)
                        } else {
                            YoYo.with(Techniques.Shake)
                                .duration(300)
                                .repeat(0)
                                .playOn(itemView)
//                            EventBus.getDefault().post(ShakeEvent(true))
                        }
                    }
                }
            }
        }

    }


    val params by lazy {
        hashMapOf<String, Any>(
            "accid" to UserManager.getAccid(),
            "token" to UserManager.getToken(),
            "target_accid" to "",
            "type" to 1
        )
    }


    override fun onClick(view: View) {
        when (view.id) {
            R.id.retryBtn -> {
                setViewState(LOADING)
                updateLocation()
                mPresenter.getMatchList(matchParams)
            }
            R.id.filterBtn -> {
                FilterUserDialog(activity!!).show()
            }
            R.id.changeAvatorCloseBtn -> {
                lieAvatorLl.isVisible = false
            }
            R.id.changeAvatorBtn -> { //强制替换头像
                if (ranking_level == 1 || ranking_level == 2) {
                    startActivityForResult<NewUserInfoSettingsActivity>(100)
                } else {
                    startActivity<MyLabelActivity>()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }


    /**
     * 匹配列表数据
     */
    private var paramsLastFiveIds = mutableListOf<Int>()
    private var firstLoad = true

    override fun onGetMatchListResult(success: Boolean, matchBeans: MatchListBean?) {
        if (success) {
            if (matchBeans != null) {

                hasMore = (matchBeans!!.list ?: mutableListOf<MatchBean>()).size == PAGESIZE

                matchUserAdapter.addData(matchBeans.list ?: mutableListOf<MatchBean>())
                matchUserAdapter.my_tags_quality = matchBeans.mytags ?: mutableListOf<Newtag>()
                paramsLastFiveIds = matchBeans.exclude ?: mutableListOf()

                //保存 VIP信息
                UserManager.saveUserVip(matchBeans.isvip)
                //保存认证信息
                UserManager.saveUserVerify(matchBeans.isfaced)

                UserManager.saveLeftSlideCount(matchBeans.like_times)
                EventBus.getDefault().post(UpdateSlideCountEvent())
                //保存提示剩余滑动次数
                UserManager.saveHighlightCount(matchBeans.highlight_times)
                //保存剩余招呼次数
                UserManager.saveLightingCount(matchBeans.lightningcnt ?: 0)

                //保存引导次数
                UserManager.motion = matchBeans.motion
                my_percent_complete = matchBeans.my_percent_complete
                normal_percent_complete = matchBeans.normal_percent_complete
                myCount = matchBeans.my_like_times
                maxCount = matchBeans.total_like_times
                //头像等级
                ranking_level = matchBeans.ranking_level

                //如果没有显示过协议，那就协议先出，再弹引导。否则直接判断有没有显示过引导页面
                if (!UserManager.getAlertProtocol()) {
                    PrivacyDialog(activity!!, matchBeans.iscompleteguide).show()
                } else if (!matchBeans.iscompleteguide) {
                    GuideDialog(activity!!).show()
                }

                //第一次加载的时候就显示顶部提示条
                if (firstLoad) {
                    if (ranking_level == 2) {//2 真人提示
                        lieAvatorLl.isVisible = true
                        lieAvatorContent.text = "当前头像非真实头像，替换后可获得首页推荐"
                        changeAvatorBtn.text = "立即替换"
                        changeAvatorCloseBtn.isVisible = false
                    } else if (ranking_level == 1) {//1 审核中ing      管
                        lieAvatorLl.isVisible = true
                        lieAvatorContent.text = "头像审核中，可替换头像加速审核"
                        changeAvatorBtn.text = "立即替换"
                        changeAvatorCloseBtn.isVisible = true
                    } else if (!matchBeans.is_full) {
                        lieAvatorLl.isVisible = true
                        lieAvatorContent.text = "当前有未完善兴趣，完善提升被打招呼几率"
                        changeAvatorBtn.text = "立即完善"
                        changeAvatorCloseBtn.isVisible = true
                    } else {
                        lieAvatorLl.isVisible = false
                    }
                    firstLoad = false
                }
                when (matchBeans.motion) {
                    GotoVerifyDialog.TYPE_CHANGE_AVATOR_NOT_PASS -> {//7头像违规替换
                        EventBus.getDefault()
                            .postSticky(ReVerifyEvent(GotoVerifyDialog.TYPE_CHANGE_AVATOR_NOT_PASS))
                    }
                    GotoVerifyDialog.TYPE_CHANGE_ABLUM -> {//3完善相册
                        UserManager.perfect_times = matchBeans.perfect_times
                    }
                    else -> {
                        UserManager.cleanVerifyData()
                    }
                }
            }

            if (matchBeans == null || (matchBeans!!.list.isNullOrEmpty() && matchUserAdapter.data.isNullOrEmpty())) {
                setViewState(EMPTY)
            } else {
                setViewState(CONTENT)
            }

        } else {
            setViewState(ERROR)
            errorMsg.text = if (mPresenter.checkNetWork()) {
                activity!!.getString(R.string.retry_load_error)
            } else {
                activity!!.getString(R.string.retry_net_error)
            }
        }
    }


    /**
     * 左滑不喜欢结果
     * //405 封禁
     * //201 会员提醒
     */
    override fun onGetDislikeResult(success: Boolean, data: BaseResp<StatusBean?>) {

        if (data.code == 200) {
            if (data.data != null) {
                if (data.data!!.residue == 0) {
                    card_stack_view.rewind()
                    ChargeVipDialog(ChargeVipDialog.INFINITE_SLIDE, activity!!).show()
                    return
                }
            } else {
                CommonFunction.toast(data.msg)
                card_stack_view.rewind()
            }
        }
    }


    //status :1.喜欢成功  2.匹配成功
    //201 不是会员
    //405 封禁
    override fun onGetLikeResult(
        success: Boolean,
        data: BaseResp<StatusBean?>,
        matchBean: MatchBean
    ) {
        if (data.code == 200) {
            if (data.data != null) {
                if (UserManager.getCurrentSurveyVersion().isEmpty()) {
                    UserManager.saveSlideSurveyCount(UserManager.getSlideSurveyCount().plus(1))
                    EventBus.getDefault()
                        .post(ShowSurveyDialogEvent(UserManager.getSlideSurveyCount()))
                }

                if (data.data!!.status == 2) {//status :1.喜欢成功  2.匹配成功
                    sendChatHiMessage(ChatHiAttachment.CHATHI_MATCH, matchBean)
                }
            } else {
                CommonFunction.toast(data.msg)
                card_stack_view.rewind()
            }

        } else if (data.code == 201) {
            card_stack_view.rewind()
            if (my_percent_complete <= normal_percent_complete)
                RightSlideOutdDialog(activity!!, myCount, maxCount).show()
            else
                ChargeVipDialog(ChargeVipDialog.INFINITE_SLIDE, activity!!).show()

        } else if (data.code == 405) {
            CommonFunction.toast(data.msg)
            card_stack_view.rewind()
        }
    }

    override fun onError(text: String) {
        Log.d("error", text)
    }


    /**
     * 改变当前页面的状态
     */
    private fun setViewState(state: Int) {
        when (state) {
            LOADING -> {
                loadingLayout.isVisible = true
                contentLayout.isVisible = false
                errorLayout.isVisible = false
                emptyLayout.isVisible = false
            }
            CONTENT -> {
                contentLayout.isVisible = true
                loadingLayout.isVisible = false
                errorLayout.isVisible = false
                emptyLayout.isVisible = false
            }
            ERROR -> {
                errorLayout.isVisible = true
                contentLayout.isVisible = false
                loadingLayout.isVisible = false
                emptyLayout.isVisible = false
            }
            EMPTY -> {
                emptyLayout.isVisible = true
                emptyLayout.emptyFriendTitle.isVisible = true
                emptyLayout.emptyFriendGoBtn.isVisible = false
                emptyLayout.emptyImg.setImageResource(R.drawable.icon_empty_match)
                emptyLayout.emptyFriendTitle.text = "暂时没有人了"
                emptyLayout.emptyFriendTip.text = "一会儿再回来看看吧"
                contentLayout.isVisible = false
                errorLayout.isVisible = false
                loadingLayout.isVisible = false
            }
        }

    }


    /*---------------------事件总线--------------------------------*/
    /**
     * 通过本地的筛选条件类更新数据
     */
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onRefreshEvent(event: RefreshEvent) {
        setViewState(LOADING)

        matchUserAdapter.data.clear()
        hasMore = false
        firstLoad = true

        updateLocation()
        mPresenter.getMatchList(matchParams)
    }

    /**
     * 震动动画
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onShakeEvent(event: ShakeEvent) {
        YoYo.with(Techniques.Shake)
            .duration(400)
            .repeat(0)
            .playOn(card_stack_view)
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateHiCountEvent(event: UpdateHiCountEvent) {
        if (UserManager.getLightingCount() < 0) {
            UserManager.saveLightingCount(0)
        }
        if (manager.topView != null)
            (manager.topView.findViewById<TextView>(R.id.btnHiLeftTime)).text =
                "${UserManager.getLightingCount()}"
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateGreetTopEvent(event: GreetTopEvent) {
        //请求成功并且在首页 卡片飞出去
        if (event.success && matchUserAdapter.data[manager.topPosition].accid == event.targetAccid) {
            val setting = SwipeAnimationSetting.Builder()
                .setDirection(Direction.Top)
                .setDuration(Duration.Normal.duration)
                .setInterpolator(AccelerateInterpolator())
                .build()
            manager.setSwipeAnimationSetting(setting)
            card_stack_view.swipe()
        }
    }


    /*---------------------卡片参数和方法------------------------------*/
    private fun initialize() {
        //卡片排列方式
        manager.setStackFrom(StackFrom.Bottom)
        //最大可见数量
        manager.setVisibleCount(3)
        //两个卡片之间的间隔
        manager.setTranslationInterval(0f)
        //卡片滑出飞阈值
        manager.setSwipeThreshold(0.3f)
        //横向纵向的旋转角度
//        manager.setMaxDegree(5F)
        //滑动的方向
//        manager.setDirections(mutableListOf(Direction.Left, Direction.Right, Direction.Top))
        manager.setDirections(Direction.FREEDOM)
//        manager.setCanScrollHorizontal(true)
//        manager.setCanScrollVertical(false)
        manager.setSwipeableMethod(SwipeableMethod.AutomaticAndManual)
        manager.setOverlayInterpolator(LinearInterpolator())

        //撤回的动画设置
        val setting = RewindAnimationSetting.Builder()
            .setDirection(Direction.Top)
            .setDuration(Duration.Normal.duration)
            .setInterpolator(DecelerateInterpolator())
            .build()
        manager.setRewindAnimationSetting(setting)
        card_stack_view.layoutManager = manager
        card_stack_view.adapter = matchUserAdapter
        matchUserAdapter.bindToRecyclerView(card_stack_view)
        card_stack_view.itemAnimator.apply {
            if (this is DefaultItemAnimator) {
                supportsChangeAnimations = false
            }
        }
    }


    override fun onCardDisappeared(view: View?, position: Int) {
        Log.d("CardStackView", "onCardDisappeared: ($position)")
    }


    override fun onCardDragging(direction: Direction, ratio: Float) {
        Log.d("CardStackView", "onCardDragging: ($direction)")

        //向上超级喜欢(会员就超级喜欢 否则弹起收费窗)
        when (direction) {
            //左滑时加载动画
            Direction.Left -> {
                //重置右边、上边的距离
                animation_like.alpha = 0F
                val paramsLike = animation_like.layoutParams as RelativeLayout.LayoutParams
                paramsLike.width = 0
                paramsLike.height = 0
                animation_like.layoutParams = paramsLike

                animation_dislike.alpha = ratio
                val params = animation_dislike.layoutParams as RelativeLayout.LayoutParams
                params.width = (SizeUtils.dp2px(50F) + SizeUtils.dp2px(50f) * ratio).toInt()
                params.height = (SizeUtils.dp2px(50F) + SizeUtils.dp2px(50f) * ratio).toInt()
                params.leftMargin =
                    ((ScreenUtils.getScreenWidth() / 2F * ratio) - params.width / 2F).toInt()
                animation_dislike.layoutParams = params

            }
            //右滑时加载动画
            Direction.Right -> {
                //重置左边、上边的距离
                val paramsLike = animation_dislike.layoutParams as RelativeLayout.LayoutParams
                paramsLike.width = 0
                paramsLike.height = 0
                animation_dislike.layoutParams = paramsLike
                animation_dislike.alpha = 0F

                animation_like.alpha = ratio
                val params = animation_like.layoutParams as RelativeLayout.LayoutParams
                params.width = (SizeUtils.dp2px(50F) + SizeUtils.dp2px(50f) * ratio).toInt()
                params.height = (SizeUtils.dp2px(50F) + SizeUtils.dp2px(50f) * ratio).toInt()
                params.rightMargin =
                    ((ScreenUtils.getScreenWidth() / 2F * ratio) - params.width / 2F).toInt()
                animation_like.layoutParams = params
            }


        }
        Log.d("CardStackView", "onCardDragging: d = ${direction.name}, r = $ratio")
    }

    //此时已经飞出去了
    override fun onCardSwiped(direction: Direction?) {
        //因为不要提示完善相册了，所以删除此段代码
        if (UserManager.slide_times != -1) {
            UserManager.slide_times++
            if (UserManager.motion == GotoVerifyDialog.TYPE_CHANGE_ABLUM && UserManager.slide_times == UserManager.perfect_times && !UserManager.getAlertChangeAlbum()) { //3补充照片库
                EventBus.getDefault().postSticky(ReVerifyEvent(GotoVerifyDialog.TYPE_CHANGE_ABLUM))
                UserManager.slide_times = 0
            }
        }
        resetAnimation()
        if (direction == Direction.Left) {//左滑不喜欢
            params["target_accid"] = matchUserAdapter.data[manager.topPosition - 1].accid ?: ""
            if (!matchUserAdapter.data[manager.topPosition - 1].newtags.isNullOrEmpty())
                params["tag_id"] = matchUserAdapter.data[manager.topPosition - 1].newtags!![0].id
            mPresenter.dislikeUser(params)
        } else if (direction == Direction.Right) {//右滑喜欢
            UserManager.saveSlideCount(UserManager.getSlideCount() + 1)
            if (UserManager.motion == ChangeAvatarRealManDialog.VERIFY_NEED_REAL_MAN && UserManager.getSlideCount() == UserManager.perfect_times) {//2非真人头像（头像审核不通过）
                ChangeAvatarRealManDialog(
                    activity!!,
                    ChangeAvatarRealManDialog.VERIFY_NEED_REAL_MAN,
                    matchBean = matchUserAdapter.data[manager.topPosition - 1],
                    view1 = view
                ).show()
            }
            //保存剩余滑动次数
            if (UserManager.isUserVip() || UserManager.getLeftSlideCount() > 0) {
                if (!UserManager.isUserVip() && UserManager.getLeftSlideCount() > 0) {
                    UserManager.saveLeftSlideCount(UserManager.getLeftSlideCount().minus(1))
                    EventBus.getDefault().post(UpdateSlideCountEvent())
                }

                params["target_accid"] = matchUserAdapter.data[manager.topPosition - 1].accid
                if (!matchUserAdapter.data[manager.topPosition - 1].newtags.isNullOrEmpty())
                    params["tag_id"] =
                        matchUserAdapter.data[manager.topPosition - 1].newtags!![0].id
                mPresenter.likeUser(params, matchUserAdapter.data[manager.topPosition - 1])
            } else {
                card_stack_view.postDelayed({ card_stack_view.rewind() }, 100)
                card_stack_view.isEnabled = false
                if (my_percent_complete < normal_percent_complete)
                    RightSlideOutdDialog(activity!!, myCount, maxCount).show()
                else
                    ChargeVipDialog(ChargeVipDialog.INFINITE_SLIDE, activity!!).show()
            }
        }
//        changeAvatorBtn


        //如果已经只剩5张了就请求数据(预加载).
        if (hasMore && manager.topPosition == matchUserAdapter.itemCount - 5) {
            updateLocation()
            mPresenter.getMatchList(matchParams, paramsLastFiveIds)
        } else if (!hasMore && manager.topPosition == matchUserAdapter.itemCount) {
            setViewState(EMPTY)
        }
    }

    override fun onCardCanceled() {
        resetAnimation()
        Log.d("CardStackView", "onCardCanceled: ${manager.topPosition}")

    }


    override fun onCardAppeared(view: View?, position: Int) {
        Log.d("CardStackView", "onCardAppeared: ($position)")
        if (view != null) {
            if (matchUserAdapter.data[manager.topPosition].greet_switch) {
                (view.findViewById<ConstraintLayout>(R.id.btnHi)).visibility = View.INVISIBLE
                (view.findViewById<LottieAnimationView>(R.id.btnHiLottieView)).isVisible = true
                (view.findViewById<TextView>(R.id.btnHiLeftTime)).text =
                    "${UserManager.getLightingCount()}"

                (view.findViewById<LottieAnimationView>(R.id.btnHiLottieView)).addAnimatorListener(
                    object :
                        Animator.AnimatorListener {
                        override fun onAnimationEnd(animation: Animator?) {
                            (view.findViewById<ConstraintLayout>(R.id.btnHi)).visibility =
                                View.VISIBLE
                            (view.findViewById<LottieAnimationView>(R.id.btnHiLottieView)).visibility =
                                View.INVISIBLE


                            //透明度起始为1，结束时为0
                            val animator =
                                ObjectAnimator.ofFloat(
                                    view.findViewById<ImageView>(R.id.btnHiIv),
                                    "alpha",
                                    1f,
                                    0f
                                )
                            val animator1 =
                                ObjectAnimator.ofFloat(
                                    view.findViewById<TextView>(R.id.btnHiLeftTime),
                                    "alpha",
                                    0f,
                                    1f
                                )
                            val animator2 =
                                ObjectAnimator.ofFloat(
                                    view.findViewById<ImageView>(R.id.btnHiIv),
                                    "alpha",
                                    0f,
                                    1f
                                )
                            val animator3 =
                                ObjectAnimator.ofFloat(
                                    view.findViewById<TextView>(R.id.btnHiLeftTime),
                                    "alpha",
                                    1f,
                                    0f
                                )
                            animator1.duration = animator.duration//时间1s
                            animator2.duration = animator.duration//时间1s
                            animator3.duration = animator.duration//时间1s
                            animator.addListener(object : Animator.AnimatorListener {
                                override fun onAnimationRepeat(animation: Animator?) {

                                }

                                override fun onAnimationEnd(animation: Animator?) {
                                    animator1.start()
                                }

                                override fun onAnimationCancel(animation: Animator?) {
                                }

                                override fun onAnimationStart(animation: Animator?) {
                                }

                            })
                            animator1.addListener(object : Animator.AnimatorListener {
                                override fun onAnimationRepeat(animation: Animator?) {

                                }

                                override fun onAnimationEnd(animation: Animator?) {
                                    view.postDelayed({
                                        animator2.start()
                                        animator3.start()
                                    }, 1000L)
                                }

                                override fun onAnimationCancel(animation: Animator?) {

                                }

                                override fun onAnimationStart(animation: Animator?) {
                                }

                            })
                            animator2.addListener(object : Animator.AnimatorListener {
                                override fun onAnimationRepeat(animation: Animator?) {

                                }

                                override fun onAnimationEnd(animation: Animator?) {
                                    (view.findViewById<ConstraintLayout>(R.id.btnHi)).visibility =
                                        View.INVISIBLE
                                    (view.findViewById<LottieAnimationView>(R.id.btnHiLottieView)).isVisible =
                                        true

                                    (view.findViewById<LottieAnimationView>(R.id.btnHiLottieView)).playAnimation()
                                }

                                override fun onAnimationCancel(animation: Animator?) {

                                }

                                override fun onAnimationStart(animation: Animator?) {
                                }

                            })

                            view.postDelayed({
                                animator.start()
                            }, 2000L)
                        }

                        override fun onAnimationCancel(animation: Animator?) {
                        }

                        override fun onAnimationStart(animation: Animator?) {
                        }

                        override fun onAnimationRepeat(animation: Animator?) {

                        }

                    })
                (view.findViewById<LottieAnimationView>(R.id.btnHiLottieView)).playAnimation()
            }
        }
    }

    override fun onCardRewound() {
        if (UserManager.slide_times > 0) {
            UserManager.slide_times--
        }

    }

    private fun resetAnimation() {

        val params1 = animation_like.layoutParams
        params1.width = 0
        params1.height = 0
        animation_like.alpha = 0F
        animation_like.layoutParams = params1

        val params2 = animation_dislike.layoutParams
        params2.width = 0
        params2.height = 0
        animation_dislike.alpha = 0F
        animation_dislike.layoutParams = params2
    }

    /*--------------------------消息代理------------------------*/

    private fun sendChatHiMessage(type: Int, matchBean: MatchBean) {
//        val matchBean = matchUserAdapter.data[manager.topPosition - 1]
        Log.d("OkHttp", matchBean.accid ?: "")
        val chatHiAttachment = ChatHiAttachment(type)
        val config = CustomMessageConfig()
        config.enablePush = false
        val message = MessageBuilder.createCustomMessage(
            matchBean?.accid,
            SessionTypeEnum.P2P,
            "",
            chatHiAttachment,
            config
        )
        sendMessage(message, matchBean)
    }


    fun sendMessage(msg: IMMessage, matchBean: MatchBean): Boolean {
        NIMClient.getService(MsgService::class.java).sendMessage(msg, false).setCallback(object :
            RequestCallback<Void?> {
            override fun onSuccess(param: Void?) {
                if (msg.attachment is ChatHiAttachment && (msg.attachment as ChatHiAttachment).showType == ChatHiAttachment.CHATHI_MATCH) { //匹配成功跳转到飞卡片
                    startActivity<MatchSucceedActivity>(
                        "avator" to matchBean.avatar,
                        "nickname" to matchBean.nickname,
                        "accid" to matchBean.accid
//                        "avator" to matchUserAdapter.data[manager.topPosition - 1].avatar,
//                        "nickname" to matchUserAdapter.data[manager.topPosition - 1].nickname,
//                        "accid" to matchUserAdapter.data[manager.topPosition - 1].accid
                    )
                }


            }

            override fun onFailed(code: Int) {
                card_stack_view.rewind()
            }

            override fun onException(exception: Throwable) {
                card_stack_view.rewind()
            }
        })
        return true
    }


}
