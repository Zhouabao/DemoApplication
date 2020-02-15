package com.sdy.jitangapplication.ui.fragment


import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Build
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.airbnb.lottie.LottieAnimationView
import com.blankj.utilcode.util.*
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.fragment.BaseMvpLazyLoadFragment
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.msg.MessageBuilder
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.CustomMessageConfig
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.sdy.baselibrary.utils.CustomClickListener
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.event.*
import com.sdy.jitangapplication.model.*
import com.sdy.jitangapplication.nim.attachment.ChatHiAttachment
import com.sdy.jitangapplication.presenter.MatchPresenter
import com.sdy.jitangapplication.presenter.view.MatchView
import com.sdy.jitangapplication.ui.activity.AddLabelActivity
import com.sdy.jitangapplication.ui.activity.MatchDetailActivity
import com.sdy.jitangapplication.ui.activity.MyLabelActivity
import com.sdy.jitangapplication.ui.adapter.MatchUserAdapter
import com.sdy.jitangapplication.ui.adapter.TagAdapter
import com.sdy.jitangapplication.ui.chat.MatchSucceedActivity
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

/**
 * 匹配页面(新版)
 */
class MatchFragment : BaseMvpLazyLoadFragment<MatchPresenter>(), MatchView, View.OnClickListener, CardStackListener {


    override fun loadData() {
        initTagsView()
        initView()
    }

    private val tags by lazy { mutableListOf<TagBean>() }
    private val tagAdapter by lazy { TagAdapter() }
    private fun initTagsView() {
        addTagBtn.onClick(object : CustomClickListener() {
            override fun onSingleClick(view: View) {
                val intent = Intent()
                intent.putExtra("from", AddLabelActivity.FROM_ADD_NEW)
                intent.setClass(activity!!, AddLabelActivity::class.java)
                startActivity(intent)
            }
        })

        matchTagRv.layoutManager = LinearLayoutManager(activity!!, RecyclerView.HORIZONTAL, false)
        matchTagRv.adapter = tagAdapter
        setTagData()
        tagAdapter.setOnItemClickListener { _, view, position ->
            preTagId = UserManager.getGlobalLabelId()
            for (tag in tagAdapter.data) {
                tag.cheked = tag == tagAdapter.data[position]
            }
            tagAdapter.notifyDataSetChanged()

            UserManager.saveGlobalLabelId(tagAdapter.data[position].id)
            matchParams["tag_id"] = tagAdapter.data[position].id
            mPresenter.getMatchList(matchParams)
            matchUserAdapter.data.clear()
            setViewState(LOADING)
        }

    }

    private fun setTagData() {
        tags.clear()
        tags.addAll(UserManager.getSpLabels())
//        tags.add(TagBean(-1))
        //初始化选中的tag
        if (UserManager.getGlobalLabelId() == 0) {
            if (tags.isNotEmpty()) {
                tags[0].cheked = true
                matchParams["tag_id"] = tags[0].id
                UserManager.saveGlobalLabelId(tags[0].id)
            }
        } else {
            var hasCheck = false
            for (tag in tags) {
                if (tag.id == UserManager.getGlobalLabelId()) {
                    tag.cheked = true
                    matchParams["tag_id"] = tag.id
                    hasCheck = true
                    break
                }
            }
            if (!hasCheck) {
                tags[0].cheked = true
                matchParams["tag_id"] = tags[0].id
                UserManager.saveGlobalLabelId(tags[0].id)
            }
        }
        tagAdapter.setNewData(tags)
        preTagId = UserManager.getGlobalLabelId()
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
    private var is_human: Boolean = false

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
            "tag_id" to UserManager.getGlobalLabelId(),
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
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
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


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val param = customStatusBar.layoutParams as ConstraintLayout.LayoutParams
            param.height = BarUtils.getStatusBarHeight()
        } else {
            customStatusBar.isVisible = false
        }

        retryBtn.setOnClickListener(this)
        greetBtn.setOnClickListener(this)
        filterBtn.setOnClickListener(this)
        completeLabelBtn.setOnClickListener(this)
        dislikeBtn.setOnClickListener(this)
        likeBtn.setOnClickListener(this)
        likeBtn.setOnClickListener(this)

        initialize()

        updateLocation()

        mPresenter.getMatchList(matchParams)

        matchUserAdapter.setOnItemChildClickListener { _, view, position ->
            val item = matchUserAdapter.data[manager.topPosition]
            val itemView = manager.topView
            when (view.id) {
                R.id.v1 -> {
                    (itemView.findViewById<ConstraintLayout>(R.id.v1)).isEnabled = false
                    if ((matchUserAdapter.data[manager.topPosition].accid ?: "") != UserManager.getAccid())
                        MatchDetailActivity.start(activity!!, matchUserAdapter.data[manager.topPosition].accid)
                    (itemView.findViewById<ConstraintLayout>(R.id.v1)).isEnabled = true
                }
                R.id.btnHiLottieView,
                R.id.btnHi -> {
                    CommonFunction.commonGreet(
                        activity!!,
                        matchUserAdapter.data[manager.topPosition].isfriend == 1,
                        matchUserAdapter.data[manager.topPosition].greet_switch,
                        matchUserAdapter.data[manager.topPosition].greet_state,
                        matchUserAdapter.data[manager.topPosition].accid,
                        matchUserAdapter.data[manager.topPosition].nickname ?: "",
                        view = itemView.findViewById(R.id.btnHi)
                    )
                }
                R.id.nextImgBtn -> {
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
            "tag_id" to UserManager.getGlobalLabelId(),
            "type" to 1
        )
    }


    override fun onClick(view: View) {
        when (view.id) {
            R.id.completeLabelBtn -> {//完善标签
                startActivity<MyLabelActivity>()
            }
            R.id.greetBtn -> {
                CommonFunction.commonGreet(
                    activity!!, matchUserAdapter.data[manager.topPosition].isfriend == 1,
                    matchUserAdapter.data[manager.topPosition].greet_switch,
                    matchUserAdapter.data[manager.topPosition].greet_state,
                    matchUserAdapter.data[manager.topPosition].accid,
                    matchUserAdapter.data[manager.topPosition].nickname ?: "",
                    view = greetBtn
                )
            }
            R.id.likeBtn -> {
                val setting = SwipeAnimationSetting.Builder()
                    .setDirection(Direction.Right)
                    .setDuration(Duration.Normal.duration)
                    .setInterpolator(AccelerateInterpolator())
                    .build()
                manager.setSwipeAnimationSetting(setting)
                card_stack_view.swipe()
            }
            R.id.dislikeBtn -> {
                val setting = SwipeAnimationSetting.Builder()
                    .setDirection(Direction.Left)
                    .setDuration(Duration.Normal.duration)
                    .setInterpolator(AccelerateInterpolator())
                    .build()
                manager.setSwipeAnimationSetting(setting)
                card_stack_view.swipe()
            }
            R.id.retryBtn -> {
                setViewState(LOADING)
                updateLocation()
                mPresenter.getMatchList(matchParams)
            }
            R.id.filterBtn -> {
                FilterUserDialog(activity!!).show()
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
            if (matchBeans != null) {
                hasMore = (matchBeans!!.list ?: mutableListOf<MatchBean>()).size == PAGESIZE

                matchUserAdapter.addData(matchBeans.list ?: mutableListOf<MatchBean>())
                matchUserAdapter.my_tags_quality = matchBeans.mytags ?: mutableListOf<Newtag>()
                paramsLastFiveIds = matchBeans.exclude ?: mutableListOf()

                //保存没有标签的用户的滑动次数，为了提醒其去更新标签内容
                completeLabelBtn.isVisible = !matchBeans.is_full
                if (matchBeans.is_full) {
                    UserManager.saveCompleteLabelCount(matchBeans.interest_times)
                }

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
                //保存倒计时时间
                UserManager.saveCountDownTime(matchBeans.countdown)

                //保存引导次数
                UserManager.motion = matchBeans.motion
                my_percent_complete = matchBeans.my_percent_complete
                normal_percent_complete = matchBeans.normal_percent_complete
                myCount = matchBeans.my_like_times
                maxCount = matchBeans.total_like_times
                is_human = matchBeans.is_human
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
                tvLeftChatTime.text = "${UserManager.getLightingCount()}"
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
     * 通过全局的标签来更新数据
     */
    fun updateLabelEvent() {
        setViewState(LOADING)
        matchParams["type"] = 1
        matchUserAdapter.data.clear()
        hasMore = false
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
        setTagData()

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
        if (manager.topView != null)
            (manager.topView.findViewById<TextView>(R.id.btnHiLeftTime)).text = "${UserManager.getLightingCount()}"
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateCardEvent(event: GreetEvent) {
        if (event.success && FragmentUtils.getTopShow(fragmentManager!!) is MatchFragment) {
            val setting = SwipeAnimationSetting.Builder()
                .setDirection(Direction.Top)
                .setDuration(Duration.Normal.duration)
                .setInterpolator(AccelerateInterpolator())
                .build()
            manager.setSwipeAnimationSetting(setting)
            card_stack_view.swipe()
        } else {
//             card_stack_view.rewind()
        }
    }

    private var preTagId = 0
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun turnToLastLabelEvent(event: TurnToLastLabelEvent) {
        if (event.from == ChargeLabelDialog.FROM_INDEX) {
            UserManager.saveGlobalLabelId(0)
            onRefreshEvent(RefreshEvent(true))
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
        manager.setDirections(Direction.HORIZONTAL)
        manager.setCanScrollHorizontal(true)
        manager.setCanScrollVertical(false)
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

                animation_like.alpha = ratio
                val params = animation_like.layoutParams as RelativeLayout.LayoutParams
                params.width = (SizeUtils.dp2px(50F) + SizeUtils.dp2px(50f) * ratio).toInt()
                params.height = (SizeUtils.dp2px(50F) + SizeUtils.dp2px(50f) * ratio).toInt()
                params.rightMargin = ((ScreenUtils.getScreenWidth() / 2F * ratio) - params.width / 2F).toInt()
                animation_like.layoutParams = params
            }


        }
        Log.d("CardStackView", "onCardDragging: d = ${direction.name}, r = $ratio")
    }

    //此时已经飞出去了
    override fun onCardSwiped(direction: Direction?) {
        if (UserManager.slide_times != -1) {
            UserManager.slide_times++
            if (UserManager.motion == GotoVerifyDialog.TYPE_CHANGE_ABLUM && UserManager.slide_times == UserManager.perfect_times && !UserManager.getAlertChangeAlbum()) { //完善相册
                EventBus.getDefault().postSticky(ReVerifyEvent(GotoVerifyDialog.TYPE_CHANGE_ABLUM))
                UserManager.slide_times = 0
            } else if (UserManager.motion == GotoVerifyDialog.TYPE_CHANGE_AVATOR_PASS && UserManager.slide_times == UserManager.replace_times && !UserManager.getAlertChangeAvator()) {//引导替换
                EventBus.getDefault().postSticky(ReVerifyEvent(GotoVerifyDialog.TYPE_CHANGE_AVATOR_PASS))
                UserManager.slide_times = 0
            }
        }

        //非真人头像提示去修改头像
        if (!is_human && manager.topPosition - 1 == 0 && !UserManager.getAlertChangeRealMan()) {
            ChangeAvatarRealManDialog(activity!!).show()
        }

        resetAnimation()
        if (direction == Direction.Left) {//左滑不喜欢
            params["target_accid"] = matchUserAdapter.data[manager.topPosition - 1].accid ?: ""
            if (!matchUserAdapter.data[manager.topPosition - 1].newtags.isNullOrEmpty())
                params["tag_id"] = matchUserAdapter.data[manager.topPosition - 1].newtags!![0].id
            mPresenter.dislikeUser(params)
        } else if (direction == Direction.Right) {//右滑喜欢
            UserManager.saveSlideCount(UserManager.getSlideCount() + 1)
            //保存剩余滑动次数
            if (UserManager.isUserVip() || UserManager.getLeftSlideCount() > 0) {
                if (!UserManager.isUserVip() && UserManager.getLeftSlideCount() > 0) {
                    UserManager.saveLeftSlideCount(UserManager.getLeftSlideCount().minus(1))
                    EventBus.getDefault().post(UpdateSlideCountEvent())
                }
                //如果当前弹窗的滑动剩余次数为0并且没有显示过完善标签的弹窗，就弹窗
                if (UserManager.getCompleteLabelCount() != -1 && UserManager.getCompleteLabelCount() == UserManager.getSlideCount() - 1) {
                    CompleteLabelDialog(activity!!,UserManager.getGlobalLabelId()).show()
                }
                params["target_accid"] = matchUserAdapter.data[manager.topPosition - 1].accid
                if (!matchUserAdapter.data[manager.topPosition - 1].newtags.isNullOrEmpty())
                    params["tag_id"] = matchUserAdapter.data[manager.topPosition - 1].newtags!![0].id
                mPresenter.likeUser(params, matchUserAdapter.data[manager.topPosition - 1])
            } else {
                card_stack_view.postDelayed({ card_stack_view.rewind() }, 100)
                card_stack_view.isEnabled = false
                if (my_percent_complete <= normal_percent_complete)
                    RightSlideOutdDialog(activity!!, myCount, maxCount).show()
                else
                    ChargeVipDialog(ChargeVipDialog.INFINITE_SLIDE, activity!!).show()
            }
        }


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
                (view.findViewById<TextView>(R.id.btnHiLeftTime)).text = "${UserManager.getLightingCount()}"

                (view.findViewById<LottieAnimationView>(R.id.btnHiLottieView)).addAnimatorListener(object :
                    Animator.AnimatorListener {
                    override fun onAnimationEnd(animation: Animator?) {
                        (view.findViewById<ConstraintLayout>(R.id.btnHi)).visibility = View.VISIBLE
                        (view.findViewById<LottieAnimationView>(R.id.btnHiLottieView)).visibility = View.INVISIBLE


                        //透明度起始为1，结束时为0
                        val animator =
                            ObjectAnimator.ofFloat(view.findViewById<ImageView>(R.id.btnHiIv), "alpha", 1f, 0f)
                        val animator1 =
                            ObjectAnimator.ofFloat(view.findViewById<TextView>(R.id.btnHiLeftTime), "alpha", 0f, 1f)
                        val animator2 =
                            ObjectAnimator.ofFloat(view.findViewById<ImageView>(R.id.btnHiIv), "alpha", 0f, 1f)
                        val animator3 =
                            ObjectAnimator.ofFloat(view.findViewById<TextView>(R.id.btnHiLeftTime), "alpha", 1f, 0f)
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
                                (view.findViewById<ConstraintLayout>(R.id.btnHi)).visibility = View.INVISIBLE
                                (view.findViewById<LottieAnimationView>(R.id.btnHiLottieView)).isVisible = true

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
