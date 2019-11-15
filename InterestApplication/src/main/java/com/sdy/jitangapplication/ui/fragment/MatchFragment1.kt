package com.sdy.jitangapplication.ui.fragment


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.RelativeLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.viewpager.widget.ViewPager
import com.blankj.utilcode.util.FragmentUtils
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
import com.sdy.jitangapplication.model.StatusBean
import com.sdy.jitangapplication.nim.attachment.ChatHiAttachment
import com.sdy.jitangapplication.presenter.MatchPresenter
import com.sdy.jitangapplication.presenter.view.MatchView
import com.sdy.jitangapplication.ui.activity.MainActivity
import com.sdy.jitangapplication.ui.activity.MatchDetailActivity
import com.sdy.jitangapplication.ui.adapter.MatchUserAdapter
import com.sdy.jitangapplication.ui.chat.MatchSucceedActivity
import com.sdy.jitangapplication.ui.dialog.ChargeVipDialog
import com.sdy.jitangapplication.ui.dialog.RightSlideOutdDialog
import com.sdy.jitangapplication.ui.dialog.SayHiDialog
import com.sdy.jitangapplication.utils.UserManager
import com.sdy.jitangapplication.widgets.GotoVerifyDialog
import com.yuyakaido.android.cardstackview.*
import kotlinx.android.synthetic.main.error_layout.*
import kotlinx.android.synthetic.main.fragment_match1.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.support.v4.startActivity

/**
 * 匹配页面(新版)
 */
class MatchFragment1 : BaseMvpLazyLoadFragment<MatchPresenter>(), MatchView, View.OnClickListener, CardStackListener {


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


    //请求广场的参数 TODO要更新tagid
    private val matchParams by lazy {
        hashMapOf(
            "accid" to SPUtils.getInstance(Constants.SPNAME).getString("accid"),
            "token" to SPUtils.getInstance(Constants.SPNAME).getString("token"),
            "_timestamp" to System.currentTimeMillis(),
            "tagid" to UserManager.getGlobalLabelId(),
            "lng" to UserManager.getlongtitude().toFloat(),
            "lat" to UserManager.getlatitude().toFloat(),
            "city_code" to UserManager.getCityCode()
        )

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
    }

    //    private val matchUserAdapter by lazy { CardAdapter() }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_match1, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }


    private fun updateLocation() {
        //加入本地的筛选对话框的筛选条件
        if (params["audit_only"] != null)
            params.remove("audit_only")
        if (params["local_only"] != null)
            params.remove("local_only")
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

    private val manager by lazy { CardStackLayoutManager(activity!!, this) }

    private fun initView() {

        mPresenter = MatchPresenter()
        mPresenter.mView = this
        mPresenter.context = activity!!

        retryBtn.setOnClickListener(this)
        btnChat.setOnClickListener(this)

        initialize()


        updateLocation()
        mPresenter.getMatchList(matchParams)

        matchUserAdapter.setOnItemChildClickListener { _, view, position ->
            val item = matchUserAdapter.data[manager.topPosition]
            when (view.id) {
                R.id.v1 -> {
                    if ((matchUserAdapter.data[manager.topPosition].accid ?: "") != UserManager.getAccid())
                        MatchDetailActivity.start(activity!!, (matchUserAdapter.data[manager.topPosition].accid ?: ""))
                }
                R.id.nextImgBtn -> {
                    val itemView = manager.topView
                    if (itemView != null) {
                        val vpPhotos = itemView.findViewById<ViewPager>(R.id.vpPhotos)
                        if (vpPhotos.currentItem < (item.photos ?: mutableListOf<MatchBean>()).size - 1) {
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
            "tag_id" to UserManager.getGlobalLabelId()
        )
    }


    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnChat -> {
                CommonFunction.commonGreet(
                    activity!!, matchUserAdapter.data[manager.topPosition].isfriend == 1,
                    matchUserAdapter.data[manager.topPosition].greet_switch,
                    matchUserAdapter.data[manager.topPosition].greet_state,
                    matchUserAdapter.data[manager.topPosition].accid,
                    matchUserAdapter.data[manager.topPosition].nickname ?: ""
                )

//                if (UserManager.getLightingCount() <= 0) {
//                    ChargeVipDialog(
//                        ChargeVipDialog.DOUBLE_HI, activity!!, if (UserManager.isUserVip()) {
//                            ChargeVipDialog.PURCHASE_GREET_COUNT
//                        } else {
//                            ChargeVipDialog.PURCHASE_VIP
//                        }
//                    ).show()
//                } else {
//                    card_stack_view.swipe()
//                    btnChat.isEnabled = false
//                }
            }
            R.id.retryBtn -> {
                setViewState(LOADING)
                updateLocation()
                mPresenter.getMatchList(matchParams)
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

    override fun onGetMatchListResult(success: Boolean, matchBeans: MatchListBean?) {
        if (success) {
            hasMore = (matchBeans!!.list ?: mutableListOf<MatchBean>()).size == PAGESIZE
            if (matchBeans!!.list.isNullOrEmpty() && matchUserAdapter.data.isNullOrEmpty()) {
                setViewState(EMPTY)
                btnChat.isVisible = false
                tvLeftChatTime.isVisible = false
                llLeftSlideTime.isVisible = false
            } else {
                setViewState(CONTENT)
                btnChat.isVisible = true
                tvLeftChatTime.isVisible = true
                llLeftSlideTime.isVisible = matchBeans.isvip != 1
            }

            matchUserAdapter.addData(matchBeans.list ?: mutableListOf<MatchBean>())
            paramsLastFiveIds = matchBeans.exclude ?: mutableListOf()
            //保存剩余滑动次数
            UserManager.saveSlideCount(matchBeans.like_times)
            //保存提示剩余滑动次数
            UserManager.saveHighlightCount(matchBeans.highlight_times)
            //保存剩余招呼次数
            UserManager.saveLightingCount(matchBeans.lightningcnt ?: 0)
            //保存倒计时时间
            UserManager.saveCountDownTime(matchBeans.countdown)
            //保存 VIP信息
            UserManager.saveUserVip(matchBeans.isvip)
            //保存认证信息
            UserManager.saveUserVerify(matchBeans.isfaced)
            //保存引导次数
            UserManager.motion = matchBeans.motion
            my_percent_complete = matchBeans.my_percent_complete
            normal_percent_complete = matchBeans.normal_percent_complete
            myCount = matchBeans.my_like_times
            maxCount = matchBeans.total_like_times
            when (matchBeans.motion) {
                GotoVerifyDialog.TYPE_CHANGE_AVATOR_NOT_PASS -> {
                    EventBus.getDefault().postSticky(ReVerifyEvent(GotoVerifyDialog.TYPE_CHANGE_AVATOR_NOT_PASS))
                }
                GotoVerifyDialog.TYPE_CHANGE_AVATOR_PASS -> {
                    UserManager.replace_times = matchBeans.replace_times
                }
                GotoVerifyDialog.TYPE_CHANGE_ABLUM -> {
                    UserManager.perfect_times = matchBeans.perfect_times
                }
                else -> {
                    UserManager.cleanVerifyData()
                }
            }

            updateLeftCountStatus()

            tvLeftChatTime.text = "${UserManager.getLightingCount()}"

        } else {
            setViewState(ERROR)
            errorMsg.text = if (mPresenter.checkNetWork()) {
                activity!!.getString(R.string.retry_load_error)
            } else {
                activity!!.getString(R.string.retry_net_error)
            }
        }

        if (fragmentManager?.let { FragmentUtils.getTopShow(it) } == this)
            EventBus.getDefault().postSticky(EnableLabelEvent(true))
    }

    /**
     * 更新剩余滑动次数的状态
     */
    private fun updateLeftCountStatus() {
        if (UserManager.getSlideCount() <= UserManager.getHighlightCount()) {
            llLeftSlideTime.alpha = 1F
            llLeftSlideTime.setBackgroundResource(R.drawable.rectangle_white_3dp)
            tvLeftSlideTime.setTextColor(resources.getColor(R.color.colorOrange))
            ivLeftSlideTime.setImageResource(R.drawable.icon_smile_orange)
        } else {
            llLeftSlideTime.alpha = 0.3F
            llLeftSlideTime.setBackgroundResource(R.drawable.rectangle_black_3dp)
            tvLeftSlideTime.setTextColor(resources.getColor(R.color.colorWhite))
            ivLeftSlideTime.setImageResource(R.drawable.icon_smile_transparent)
        }
        tvLeftSlideTime.text = "${UserManager.getSlideCount()}"

    }

    /**
     * 左滑不喜欢结果
     * //405 封禁
     * //201 会员提醒
     */
    override fun onGetDislikeResult(success: Boolean, data: BaseResp<StatusBean?>) {

        if (data.code == 200) {
            if (data.data != null) {
                if (UserManager.getCurrentSurveyVersion().isEmpty()) {
                    UserManager.saveSlideSurveyCount(UserManager.getSlideSurveyCount().plus(1))
                    EventBus.getDefault().post(ShowSurveyDialogEvent(UserManager.getSlideSurveyCount()))
                }

                if (data.data!!.residue == 0) {
                    card_stack_view.rewind()
                    ChargeVipDialog(ChargeVipDialog.INFINITE_SLIDE, activity!!).show()
                    return
                }
            } else {
                CommonFunction.toast(data.msg)
                card_stack_view.rewind()
            }
        } else if (data.code == 201) {
            if (data.data!!.residue == 0) {
                card_stack_view.rewind()
                ChargeVipDialog(ChargeVipDialog.INFINITE_SLIDE, activity!!).show()
                return
            }
        } else if (data.code == 405) {
            CommonFunction.toast(data.msg)
            card_stack_view.rewind()
        }
    }

    //status :1.喜欢成功  2.匹配成功
    //201 不是会员
    //405 封禁
    override fun onGetLikeResult(success: Boolean, data: BaseResp<StatusBean?>, matchBean: MatchBean) {
        if (data.code == 200) {
            if (data.data != null) {
                if (UserManager.getCurrentSurveyVersion().isEmpty()) {
                    UserManager.saveSlideSurveyCount(UserManager.getSlideSurveyCount().plus(1))
                    EventBus.getDefault().post(ShowSurveyDialogEvent(UserManager.getSlideSurveyCount()))
                }
                if (data.data!!.residue == 10) {
                    CommonFunction.toast("剩余10次滑动机会")
                }
                if (data.data!!.residue == 0) {
                    card_stack_view.rewind()
                    if (!UserManager.isUserVip()) {
                        if (my_percent_complete <= normal_percent_complete)
                            RightSlideOutdDialog(activity!!, myCount, maxCount).show()
                        else
                            ChargeVipDialog(ChargeVipDialog.INFINITE_SLIDE, activity!!).show()
                    }
                    return
                }
                if (data.data!!.status == 2) {//status :1.喜欢成功  2.匹配成功
                    sendChatHiMessage(ChatHiAttachment.CHATHI_MATCH, matchBean)
                }
            } else {
                CommonFunction.toast(data.msg)
                card_stack_view.rewind()
            }

        } else if (data.code == 201) {
            if (data.data!!.residue == 0) {
                card_stack_view.rewind()
                ChargeVipDialog(ChargeVipDialog.INFINITE_SLIDE, activity!!).show()
                return
            }
        } else if (data.code == 405) {
            CommonFunction.toast(data.msg)
            card_stack_view.rewind()
        }
    }


    override fun onError(text: String) {
        Log.d("error", text)
    }


    companion object {
        private const val LOADING = 0
        private const val CONTENT = 1
        private const val ERROR = 2
        private const val EMPTY = 3
        private const val PAGESIZE = 20
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
                contentLayout.isVisible = false
                errorLayout.isVisible = false
                loadingLayout.isVisible = false
            }
        }

    }


    /*---------------------事件总线--------------------------------*/

    /**
     * 通过全局的标签来更新数据
     */
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onUpdateLabelEvent(event: UpdateLabelEvent) {
        setViewState(LOADING)

        params["tag_id"] = event.label.id
        matchUserAdapter.data.clear()
        hasMore = false
        matchParams["tagid"] = event.label.id
        //这个地方还要默认设置选中第一个标签来更新数据
        updateLocation()
        mPresenter.getMatchList(matchParams)
    }


    /**
     * 通过本地的筛选条件类更新数据
     */
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onRefreshEvent(event: RefreshEvent) {
//        matchStateview.viewState = MultiStateView.VIEW_STATE_LOADING
        setViewState(LOADING)

        matchUserAdapter.data.clear()
        hasMore = false

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
        tvLeftChatTime.text = "${UserManager.getLightingCount()}"
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateCardEvent(event: GreetEvent) {
        if (event.context is MainActivity)
            if (event.success) {
                card_stack_view.swipe()
            } else {
                // card_stack_view.rewind()
            }
    }
    /*---------------------卡片参数和方法------------------------------*/

    private fun initialize() {
        //卡片排列方式
        manager.setStackFrom(StackFrom.Bottom)
        //最大可见数量
        manager.setVisibleCount(3)
        //两个卡片之间的间隔
        manager.setTranslationInterval(15.0f)
        //最大的缩放间隔
        manager.setScaleInterval(0.95f)
        //卡片滑出飞阈值
        manager.setSwipeThreshold(0.3f)
        //横向纵向的旋转角度
        manager.setMaxDegree(5F)
        //滑动的方向
//        manager.setDirections(mutableListOf(Direction.Left, Direction.Right, Direction.Top))
        manager.setDirections(Direction.HORIZONTAL)
        manager.setCanScrollHorizontal(true)
        manager.setCanScrollVertical(false)
        manager.setSwipeableMethod(SwipeableMethod.AutomaticAndManual)
        manager.setOverlayInterpolator(LinearInterpolator())

        //向上的动画设置
        val swipeTopSetting = SwipeAnimationSetting.Builder()
            .setDirection(Direction.Top)
            .setDuration(Duration.Fast.duration)
            .setInterpolator(AccelerateInterpolator())
            .build()
        manager.setSwipeAnimationSetting(swipeTopSetting)

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
//        matchUserAdapter.setEmptyView(R.layout.loading_layout_match, card_stack_view)
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

                animation_chathi.alpha = 0F
                val paramsChathi = animation_chathi.layoutParams as RelativeLayout.LayoutParams
                paramsChathi.width = 0
                paramsChathi.height = 0
                animation_chathi.layoutParams = paramsChathi



                animation_dislike.alpha = ratio
                val params = animation_dislike.layoutParams as RelativeLayout.LayoutParams
//                params.width = (ScreenUtils.getScreenWidth() / 2F * ratio).toInt()
//                params.height = (ScreenUtils.getScreenWidth() / 2F * ratio).toInt()
                params.width = (SizeUtils.dp2px(50F) + SizeUtils.dp2px(50f) * ratio).toInt()
                params.height = (SizeUtils.dp2px(50F) + SizeUtils.dp2px(50f) * ratio).toInt()
                params.leftMargin = ((ScreenUtils.getScreenWidth() / 2F * ratio) - params.width / 2F).toInt()
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


                val paramsChathi = animation_chathi.layoutParams as RelativeLayout.LayoutParams
                paramsChathi.width = 0
                paramsChathi.height = 0
                animation_chathi.layoutParams = paramsChathi
                animation_chathi.alpha = 0F



                animation_like.alpha = ratio
                val params = animation_like.layoutParams as RelativeLayout.LayoutParams
//                params.width = (ScreenUtils.getScreenWidth() / 2F * ratio).toInt()
//                params.height = (ScreenUtils.getScreenWidth() / 2F * ratio).toInt()
                params.width = (SizeUtils.dp2px(50F) + SizeUtils.dp2px(50f) * ratio).toInt()
                params.height = (SizeUtils.dp2px(50F) + SizeUtils.dp2px(50f) * ratio).toInt()
                params.rightMargin = ((ScreenUtils.getScreenWidth() / 2F * ratio) - params.width / 2F).toInt()
                animation_like.layoutParams = params
            }
            //上滑时加载动画
//            Direction.Top -> {
//                //重置左边、上边的距离
//                val paramsDisLike = animation_dislike.layoutParams as RelativeLayout.LayoutParams
//                paramsDisLike.width = 0
//                paramsDisLike.height = 0
//                animation_dislike.layoutParams = paramsDisLike
//                animation_dislike.alpha = 0F
//
//                val paramsLike = animation_like.layoutParams as RelativeLayout.LayoutParams
//                paramsLike.width = 0
//                paramsLike.height = 0
//                animation_like.layoutParams = paramsLike
//                animation_like.alpha = 0F
//
//
//
//
//                animation_chathi.alpha = ratio
//                val params = animation_chathi.layoutParams as RelativeLayout.LayoutParams
////                params.width = (ScreenUtils.getScreenWidth() / 2F * ratio).toInt()
////                params.height = (ScreenUtils.getScreenWidth() / 2F * ratio).toInt()
//                params.width = (SizeUtils.dp2px(50F) + SizeUtils.dp2px(50f) * ratio).toInt()
//                params.height = (SizeUtils.dp2px(50F) + SizeUtils.dp2px(50f) * ratio).toInt()
//                params.topMargin =
//                    ((ScreenUtils.getScreenHeight() / 2F * ratio) - SizeUtils.dp2px(126F) - params.height / 2F).toInt()
//                Log.d(
//                    "CardStackView",
//                    "topMargin= ${params.topMargin}, getScreenHeight = ${ScreenUtils.getScreenHeight()}"
//                )
//
//                animation_chathi.layoutParams = params
//
//            }

        }
        Log.d("CardStackView", "onCardDragging: d = ${direction.name}, r = $ratio")
    }

    //此时已经飞出去了
    //todo 放开注释
    override fun onCardSwiped(direction: Direction?) {
        if (UserManager.slide_times != -1) {
            UserManager.slide_times++
            if (UserManager.motion == GotoVerifyDialog.TYPE_CHANGE_ABLUM && UserManager.slide_times == UserManager.perfect_times && !UserManager.getAlertChangeAlbum()) { //完善相册
//            if (UserManager.motion == GotoVerifyDialog.TYPE_CHANGE_ABLUM && UserManager.slide_times == 5) { //完善相册
                EventBus.getDefault().postSticky(ReVerifyEvent(GotoVerifyDialog.TYPE_CHANGE_ABLUM))
                UserManager.slide_times = 0
            } else if (UserManager.motion == GotoVerifyDialog.TYPE_CHANGE_AVATOR_PASS && UserManager.slide_times == UserManager.replace_times && !UserManager.getAlertChangeAvator()) {//引导替换
//            } else if (UserManager.motion == GotoVerifyDialog.TYPE_CHANGE_AVATOR_PASS && UserManager.slide_times == 5) {//引导替换
                EventBus.getDefault().postSticky(ReVerifyEvent(GotoVerifyDialog.TYPE_CHANGE_AVATOR_PASS))
                UserManager.slide_times = 0
            }
        }

        resetAnimation()
        if (direction == Direction.Left) {//左滑不喜欢
            params["target_accid"] = matchUserAdapter.data[manager.topPosition - 1].accid ?: ""
            mPresenter.dislikeUser(params)
        } else if (direction == Direction.Right) {//右滑喜欢
            //保存剩余滑动次数
            if (UserManager.isUserVip()) {
                params["target_accid"] = matchUserAdapter.data[manager.topPosition - 1].accid ?: ""
                mPresenter.likeUser(params, matchUserAdapter.data[manager.topPosition - 1])
            } else {
                if (UserManager.getSlideCount() > 0) {
                    UserManager.saveSlideCount(UserManager.getSlideCount().minus(1))
                    params["target_accid"] = matchUserAdapter.data[manager.topPosition - 1].accid ?: ""
                    mPresenter.likeUser(params, matchUserAdapter.data[manager.topPosition - 1])
                } else {
                    card_stack_view.postDelayed({ card_stack_view.rewind() }, 100)
                    card_stack_view.isEnabled = false

                    if (my_percent_complete <= normal_percent_complete)
                        RightSlideOutdDialog(activity!!, myCount, maxCount).show()
                    else
                        ChargeVipDialog(ChargeVipDialog.INFINITE_SLIDE, activity!!).show()
                }
                updateLeftCountStatus()

            }
        }


        //如果已经只剩5张了就请求数据(预加载).
        if (hasMore && manager.topPosition == matchUserAdapter.itemCount - 5) {
            updateLocation()
            mPresenter.getMatchList(matchParams, paramsLastFiveIds)
        } else if (!hasMore && manager.topPosition == matchUserAdapter.itemCount) {
            setViewState(EMPTY)
            btnChat.isVisible = false
            tvLeftChatTime.isVisible = false
        }
    }

    override fun onCardCanceled() {
        resetAnimation()
        Log.d("CardStackView", "onCardCanceled: ${manager.topPosition}")

    }


    override fun onCardAppeared(view: View?, position: Int) {
        Log.d("CardStackView", "onCardAppeared: ($position)")
        btnChat.isEnabled = matchUserAdapter.data[position].greet_switch
        if (matchUserAdapter.data[position].greet_switch) {
            tvLeftChatTime.visibility = View.VISIBLE
            ivChatTime.visibility = View.VISIBLE
        } else {
            tvLeftChatTime.visibility = View.INVISIBLE
            ivChatTime.visibility = View.INVISIBLE
        }

    }

    override fun onCardRewound() {
        if (UserManager.slide_times > 0) {
            UserManager.slide_times--
        }

    }

    private fun resetAnimation() {
        val params = animation_chathi.layoutParams
        params.width = 0
        params.height = 0
        animation_chathi.alpha = 0F
        animation_chathi.layoutParams = params

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
        val chatHiAttachment = ChatHiAttachment(
            if (type == ChatHiAttachment.CHATHI_MATCH) {
                UserManager.getGlobalLabelName()
            } else {
                null
            }, type
        )
        val message = MessageBuilder.createCustomMessage(
            matchBean?.accid,
            SessionTypeEnum.P2P,
            "",
            chatHiAttachment,
            CustomMessageConfig()
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
                } else {//招呼成功跳转到招呼
                    SayHiDialog(
                        matchBean.accid,
                        matchBean.nickname ?: "",
                        activity!!
                    ).show()
                    //打招呼成功，就减少招呼次数
                    if (msg.attachment is ChatHiAttachment && (msg.attachment as ChatHiAttachment).showType == ChatHiAttachment.CHATHI_HI) {
                        UserManager.saveLightingCount(UserManager.getLightingCount() - 1)
                        tvLeftChatTime.text = "${UserManager.getLightingCount()}"
                    }
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
