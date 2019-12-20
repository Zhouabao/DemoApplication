package com.sdy.jitangapplication.ui.fragment


import android.app.Activity
import android.content.Intent
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
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
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.event.*
import com.sdy.jitangapplication.model.*
import com.sdy.jitangapplication.nim.attachment.ChatHiAttachment
import com.sdy.jitangapplication.presenter.MatchPresenter
import com.sdy.jitangapplication.presenter.view.MatchView
import com.sdy.jitangapplication.ui.activity.MainActivity
import com.sdy.jitangapplication.ui.activity.MatchDetailActivity
import com.sdy.jitangapplication.ui.activity.MyIntentionActivity
import com.sdy.jitangapplication.ui.activity.MyLabelActivity
import com.sdy.jitangapplication.ui.adapter.MatchLabelAdapter
import com.sdy.jitangapplication.ui.adapter.MatchUserAdapter
import com.sdy.jitangapplication.ui.chat.MatchSucceedActivity
import com.sdy.jitangapplication.ui.dialog.*
import com.sdy.jitangapplication.utils.UserManager
import com.sdy.jitangapplication.widgets.CenterLayoutManager
import com.yuyakaido.android.cardstackview.*
import kotlinx.android.synthetic.main.empty_friend_layout.view.*
import kotlinx.android.synthetic.main.error_layout.*
import kotlinx.android.synthetic.main.fragment_match1.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.startActivityForResult

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

    companion object {
        private const val LOADING = 0
        private const val CONTENT = 1
        private const val ERROR = 2
        private const val EMPTY = 3
        private const val PAGESIZE = 20

        const val WANT_MATCH = 1
        const val SAME_MATCH = 2

    }


    //请求广场的参数 TODO要更新tagid
    private val matchParams by lazy {
        hashMapOf(
            "accid" to SPUtils.getInstance(Constants.SPNAME).getString("accid"),
            "token" to SPUtils.getInstance(Constants.SPNAME).getString("token"),
            "_timestamp" to System.currentTimeMillis(),
            "tagid" to UserManager.getGlobalLabelId(),
            "lng" to UserManager.getlongtitude().toFloat(),
            "lat" to UserManager.getlatitude().toFloat(),
            "city_code" to UserManager.getCityCode(),
            "type" to WANT_MATCH
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
        greetBtn.setOnClickListener(this)
        filterBtn.setOnClickListener(this)
        completeLabelBtn.setOnClickListener(this)
        manageLabel.setOnClickListener(this)
        findToTalkLl.setOnClickListener(this)
        dislikeBtn.setOnClickListener(this)
        likeBtn.setOnClickListener(this)
        likeBtn.setOnClickListener(this)

        initHeadView()

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

    //标签适配器
    private val labelAdapter: MatchLabelAdapter by lazy { MatchLabelAdapter(activity!!) }
    //标签数据源
    var labelList: MutableList<NewLabel> = mutableListOf()
    private val labelManager by lazy { CenterLayoutManager(activity!!, LinearLayoutManager.HORIZONTAL, false) }
    //标记当前选中的匹配类型  1想认识  2找同好
    private var checkedTitle = WANT_MATCH

    private fun initHeadView() {
        labelList.add(NewLabel(title = "想认识", checked = true))
        labelList.add(NewLabel(title = "找同好", checked = false))

        headRvLabels.layoutManager = labelManager
        LinearSnapHelper().attachToRecyclerView(headRvLabels)
        headRvLabels.adapter = labelAdapter
        labelAdapter.setNewData(labelList)
        labelAdapter.setOnItemClickListener { _, view, position ->
            if (labelAdapter.enable) {
                if (position == checkedTitle - 1) {
                    return@setOnItemClickListener
                } else {
                    for (index in 0 until labelAdapter.data.size) {
                        labelAdapter.data[index].checked = index == position
                        if (index == position)
                            labelManager.smoothScrollToPosition(headRvLabels, RecyclerView.State(), position)
                    }
                    labelAdapter.notifyDataSetChanged()
                    checkedTitle = position + 1
                    if (checkedTitle == WANT_MATCH) {
                        t1.text = "为你推荐${UserManager.getInterestLabelCount()}个标签"
                    } else {
                        t1.text = "根据我的标签推荐"
                    }
                    updateLabelEvent()
                }
            }
        }
    }


    val params by lazy {
        hashMapOf<String, Any>(
            "accid" to UserManager.getAccid(),
            "token" to UserManager.getToken(),
            "target_accid" to "",
            "type" to checkedTitle
        )
    }


    override fun onClick(view: View) {
        when (view.id) {
            R.id.filterBtn -> {//筛选
                FilterUserDialog(activity!!).show()
            }
            R.id.completeLabelBtn -> {//完善标签
                startActivity<MyLabelActivity>()
            }
            R.id.manageLabel -> {//标签管理
                startActivity<MyLabelActivity>("index" to checkedTitle - 1)
            }
            R.id.findToTalkLl -> {//找人说话
                startActivityForResult<MyIntentionActivity>(
                    100,
                    "id" to if (myIntention != null) {
                        myIntention!!.id
                    } else {
                        -1
                    }, "from" to MyIntentionActivity.FROM_USERCENTER
                )
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
    private var myIntention: LabelQualityBean? = null
    override fun onGetMatchListResult(success: Boolean, matchBeans: MatchListBean?) {
        labelAdapter.enable = true
        if (success) {
            hasMore = (matchBeans!!.list ?: mutableListOf<MatchBean>()).size == PAGESIZE


            if (matchBeans.intention != null && matchBeans.intention.id != 0) {
                myIntention = matchBeans.intention
                findToTalkTv.text = myIntention!!.title
                GlideUtil.loadImg(activity!!, myIntention!!.icon, findToTalkIv)
            } else {
                findToTalkTv.text = "选择意向"
                GlideUtil.loadImg(activity!!, R.drawable.icon_switch_gray, findToTalkIv)
            }
            matchUserAdapter.addData(matchBeans.list ?: mutableListOf<MatchBean>())
            matchUserAdapter.my_tags_quality = matchBeans.mytags ?: mutableListOf<Newtag>()
            UserManager.saveMyLabelCount((matchBeans.mytags ?: mutableListOf<MyLabelBean>()).size)
            if (checkedTitle == WANT_MATCH) {
                UserManager.saveInterestLabelCount(matchBeans.myinterest_count)
                t1.text = "为你推荐${UserManager.getInterestLabelCount()}个标签"
            }
            paramsLastFiveIds = matchBeans.exclude ?: mutableListOf()

            //保存没有标签的用户的滑动次数，为了提醒其去更新标签内容
            var noQuality = false//某个标签没有特质
            for (mytag in matchBeans.mytags ?: mutableListOf()) {
                if (mytag.label_quality.isNullOrEmpty()) {
                    noQuality = true
                    break
                }
            }
            if (matchBeans.mytags.isNullOrEmpty() || noQuality) {
                if (UserManager.isShowCompleteLabelDialog()) {
                    completeLabelBtn.isVisible = true
                } else {
                    UserManager.saveCompleteLabelCount(matchBeans.interest_times)
                    completeLabelBtn.isVisible = false
                }
            } else {
                completeLabelBtn.isVisible = false
                UserManager.saveIsShowCompleteLabelDialog(true)
            }


            //保存 VIP信息
            UserManager.saveUserVip(matchBeans.isvip)
            //保存认证信息
            UserManager.saveUserVerify(matchBeans.isfaced)

            //保存剩余滑动次数
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


            if (matchBeans!!.list.isNullOrEmpty() && matchUserAdapter.data.isNullOrEmpty()) {
                setViewState(EMPTY)
                btnChatCl.isVisible = false
                tvLeftChatTime.isVisible = false
            } else {
                setViewState(CONTENT)
                btnChatCl.isVisible = true
                tvLeftChatTime.isVisible = true
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
                if (checkedTitle == SAME_MATCH) {
                    if (UserManager.getMyLabelCount() > 0) {
                        emptyLayout.emptyFriendTitle.isVisible = false
                        emptyLayout.emptyFriendGoBtn.isVisible = false
                        emptyLayout.emptyImg.setImageResource(R.drawable.icon_empty_match)
                        emptyLayout.emptyFriendTip.text = "暂时没有人了"
                    } else {
                        emptyLayout.emptyFriendTitle.isVisible = true
                        emptyLayout.emptyFriendGoBtn.isVisible = true
                        emptyLayout.emptyImg.setImageResource(R.drawable.icon_empty_label)
                        emptyLayout.emptyFriendTitle.text = "标签未完善"
                        emptyLayout.emptyFriendGoBtn.text = "去看看"
                        emptyLayout.emptyFriendTip.text = "请先完善自身标签\n我们将根据您的标签为您推荐同好"
                        emptyLayout.emptyFriendGoBtn.onClick {
                            startActivity<MyLabelActivity>("from" to MyLabelActivity.MY_LABEL)
                        }
                    }
                } else {
                    if (UserManager.getInterestLabelCount() > 0) {
                        emptyLayout.emptyFriendTitle.isVisible = false
                        emptyLayout.emptyFriendGoBtn.isVisible = false
                        emptyLayout.emptyImg.setImageResource(R.drawable.icon_empty_match)
                        emptyLayout.emptyFriendTip.text = "暂时没有人了"
                    } else {
                        emptyLayout.emptyFriendTitle.isVisible = true
                        emptyLayout.emptyFriendGoBtn.isVisible = true
                        emptyLayout.emptyImg.setImageResource(R.drawable.icon_empty_label)
                        emptyLayout.emptyFriendTitle.text = "标签未完善"
                        emptyLayout.emptyFriendGoBtn.text = "去看看"
                        emptyLayout.emptyFriendTip.text = "请先完善自身标签\n我们将根据您的标签为您推荐同好"
                        emptyLayout.emptyFriendGoBtn.onClick {
                            startActivity<MyLabelActivity>("from" to MyLabelActivity.MY_INTEREST_LABEL)

                        }
                    }
                }
                contentLayout.isVisible = false
                errorLayout.isVisible = false
                loadingLayout.isVisible = false
            }
        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 100) {
                if (data != null && data.getSerializableExtra("intention") != null) {
                    myIntention = data.getSerializableExtra("intention") as LabelQualityBean
                    findToTalkTv.text = myIntention!!.title
                    GlideUtil.loadImg(activity!!, myIntention!!.icon, findToTalkIv)
                }
            }
        }
    }

    /*---------------------事件总线--------------------------------*/

    /**
     * 通过全局的标签来更新数据
     */
    fun updateLabelEvent() {
        setViewState(LOADING)
        matchParams["type"] = checkedTitle
        matchUserAdapter.data.clear()
        hasMore = false
        updateLocation()
        mPresenter.getMatchList(matchParams)
        labelAdapter.enable = false
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

    /**
     * 是否展示完善标签
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onShowCompleteLabelEvent(event: ShowCompleteLabelEvent) {
        if (event.show) {
            completeLabelBtn.isVisible = true
            UserManager.saveIsShowCompleteLabelDialog(true)
        } else {
            completeLabelBtn.isVisible = false
        }
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
                val setting = SwipeAnimationSetting.Builder()
                    .setDirection(Direction.Top)
                    .setDuration(Duration.Normal.duration)
                    .setInterpolator(AccelerateInterpolator())
                    .build()
                manager.setSwipeAnimationSetting(setting)
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

        resetAnimation()
        if (direction == Direction.Left) {//左滑不喜欢
            params["target_accid"] = matchUserAdapter.data[manager.topPosition - 1].accid ?: ""
            if (!matchUserAdapter.data[manager.topPosition - 1].newtags.isNullOrEmpty())
                params["tag_id"] = matchUserAdapter.data[manager.topPosition - 1].newtags!![0].id
            mPresenter.dislikeUser(params)
        } else if (direction == Direction.Right) {//右滑喜欢
            UserManager.saveSlideCount(UserManager.getSlideCount()+1)
            //保存剩余滑动次数
            if (UserManager.isUserVip() || UserManager.getLeftSlideCount() > 0) {
                if (!UserManager.isUserVip() && UserManager.getLeftSlideCount() > 0) {
                    UserManager.saveLeftSlideCount(UserManager.getLeftSlideCount().minus(1))
                    EventBus.getDefault().post(UpdateSlideCountEvent())
                }
                //如果当前弹窗的滑动剩余次数为0并且没有显示过完善标签的弹窗，就弹窗
                if (UserManager.getCompleteLabelCount() != -1
                    && UserManager.getCompleteLabelCount() == UserManager.getSlideCount()
                    && !UserManager.isShowCompleteLabelDialog()
                ) {
                    CompleteLabelDialog(activity!!).show()
                }
                params["target_accid"] = matchUserAdapter.data[manager.topPosition - 1].accid ?: ""
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
            btnChatCl.isVisible = false
            tvLeftChatTime.isVisible = false
        }
    }

    override fun onCardCanceled() {
        resetAnimation()
        Log.d("CardStackView", "onCardCanceled: ${manager.topPosition}")

    }


    override fun onCardAppeared(view: View?, position: Int) {
        Log.d("CardStackView", "onCardAppeared: ($position)")
        if (matchUserAdapter.data.size > 0 && matchUserAdapter.data.size > position) {
            btnChatCl.isEnabled = matchUserAdapter.data[position].greet_switch
            if (matchUserAdapter.data[position].greet_switch) {
                tvLeftChatTime.visibility = View.VISIBLE
                greetBtn.visibility = View.VISIBLE
            } else {
                tvLeftChatTime.visibility = View.INVISIBLE
                greetBtn.visibility = View.INVISIBLE
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
