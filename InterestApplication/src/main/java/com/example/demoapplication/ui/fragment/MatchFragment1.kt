package com.example.demoapplication.ui.fragment


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
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.ToastUtils
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.example.demoapplication.R
import com.example.demoapplication.common.Constants
import com.example.demoapplication.event.RefreshEvent
import com.example.demoapplication.event.ShakeEvent
import com.example.demoapplication.event.UpdateHiCountEvent
import com.example.demoapplication.event.UpdateLabelEvent
import com.example.demoapplication.model.GreetBean
import com.example.demoapplication.model.MatchBean
import com.example.demoapplication.model.MatchListBean
import com.example.demoapplication.model.StatusBean
import com.example.demoapplication.nim.activity.ChatActivity
import com.example.demoapplication.nim.attachment.ChatHiAttachment
import com.example.demoapplication.presenter.MatchPresenter
import com.example.demoapplication.presenter.view.MatchView
import com.example.demoapplication.ui.activity.MatchDetailActivity
import com.example.demoapplication.ui.adapter.MatchUserAdapter
import com.example.demoapplication.ui.chat.MatchSucceedActivity
import com.example.demoapplication.ui.dialog.ChargeVipDialog
import com.example.demoapplication.utils.UserManager
import com.kennyc.view.MultiStateView
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.fragment.BaseMvpFragment
import com.netease.nim.uikit.business.session.module.Container
import com.netease.nim.uikit.business.session.module.ModuleProxy
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.msg.MessageBuilder
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.CustomMessageConfig
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.yuyakaido.android.cardstackview.*
import kotlinx.android.synthetic.main.error_layout.view.*
import kotlinx.android.synthetic.main.fragment_match1.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.toast

/**
 * 匹配页面(新版)
 */
class MatchFragment1 : BaseMvpFragment<MatchPresenter>(), MatchView, View.OnClickListener, CardStackListener,
    ModuleProxy {

    private var hasMore = false


    //用户适配器
    private val matchUserAdapter: MatchUserAdapter by lazy { MatchUserAdapter(mutableListOf()) }


    //当前请求页
    var page = 1
    //请求广场的参数 TODO要更新tagid
    private val matchParams by lazy {
        hashMapOf(
            "accid" to SPUtils.getInstance(Constants.SPNAME).getString("accid"),
            "token" to SPUtils.getInstance(Constants.SPNAME).getString("token"),
            "page" to page,
            "pagesize" to Constants.PAGESIZE,
            "_timestamp" to System.currentTimeMillis(),
            "tagid" to SPUtils.getInstance(Constants.SPNAME).getInt("globalLabelId")
        )
    }


    //    private val matchUserAdapter by lazy { CardAdapter() }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_match1, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
    }


    private val manager by lazy { CardStackLayoutManager(activity!!, this) }

    private fun initView() {
        EventBus.getDefault().register(this)

        mPresenter = MatchPresenter()
        mPresenter.mView = this
        mPresenter.context = activity!!

        stateview.retryBtn.onClick {
            stateview.viewState = MultiStateView.VIEW_STATE_LOADING
            mPresenter.getMatchList(matchParams)
        }
        btnChat.setOnClickListener(this)

        initialize()
        mPresenter.getMatchList(matchParams)

        matchUserAdapter.setOnItemChildClickListener { _, view, position ->
            val item = matchUserAdapter.data[manager.topPosition]
            when (view.id) {
                R.id.v1 -> {
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
            "tag_id" to SPUtils.getInstance(Constants.SPNAME).getInt("globalLabelId")
        )
    }


    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnChat -> {
                card_stack_view.swipe()
//                mPresenter.greetState(
//                    UserManager.getToken(),
//                    UserManager.getAccid(),
//                    (matchUserAdapter.data[manager.topPosition].accid ?: ""),
//                    matchUserAdapter.data[manager.topPosition]
//                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }


    /**
     *  点击聊天
     *    未打过招呼 判断招呼剩余次数
     *
     *         2.2.1 有次数 直接打招呼
     *
     *         2.2.2 无次数 其他操作--如:请求充值会员
     */
    override fun onGreetStateResult(greetBean: GreetBean?, matchBean: MatchBean) {
        if (greetBean != null) {
            if (greetBean.lightningcnt > 0) {
                mPresenter.greet(
                    UserManager.getToken(),
                    UserManager.getAccid(),
                    (matchBean.accid ?: ""),
                    UserManager.getGlobalLabelId()
                )
            } else {
                card_stack_view.rewind()
                if (UserManager.isUserVip()) {
                    //TODO 会员充值
                    ToastUtils.showShort("次数用尽，请充值。")

                } else {
                    ChargeVipDialog(activity!!).show()
                }
            }
        } else {
            card_stack_view.rewind()
            ToastUtils.showShort("请求失败，请重试")
        }
    }

    /**
     * 打招呼结果（先请求服务器）
     */
    override fun onGreetSResult(greetBean: Boolean) {
        if (greetBean) {
            sendChatHiMessage(ChatHiAttachment.CHATHI_HI)
        } else {
            card_stack_view.rewind()
            ToastUtils.showShort("打招呼失败，重新试一次吧")
        }
    }

    /**
     * 匹配列表数据
     */
    override fun onGetMatchListResult(success: Boolean, matchBeans: MatchListBean?) {
        if (success) {
            hasMore = true
            hasMore = (matchBeans!!.list ?: mutableListOf<MatchBean>()).size == Constants.PAGESIZE
            if (matchBeans!!.list.isNullOrEmpty() && matchUserAdapter.data.isNullOrEmpty()) {
                stateview.viewState = MultiStateView.VIEW_STATE_EMPTY
                btnChat.isVisible = false
                tvLeftChatTime.isVisible = false
            } else {
                stateview.viewState = MultiStateView.VIEW_STATE_CONTENT
                btnChat.isVisible = true
                tvLeftChatTime.isVisible = true
            }
            matchUserAdapter.addData(matchBeans!!.list ?: mutableListOf<MatchBean>())
            //保存剩余招呼次数
            UserManager.saveLightingCount(matchBeans.lightningcnt ?: 0)
            //保存 VIP信息
            UserManager.saveUserVip(matchBeans.isvip)
            //保存认证信息
            UserManager.saveUserVerify(matchBeans.isfaced)
            tvLeftChatTime.text = "${UserManager.getLightingCount()}"
        } else {
            stateview.viewState = MultiStateView.VIEW_STATE_ERROR
            stateview.errorMsg.text = if (mPresenter.checkNetWork()) {
                activity!!.getString(R.string.retry_load_error)
            } else {
                activity!!.getString(R.string.retry_net_error)
            }
        }
    }

    /**
     * 左滑不喜欢结果
     */
    override fun onGetDislikeResult(success: Boolean) {
    }

    //status :1.喜欢成功  2.匹配成功
    override fun onGetLikeResult(success: Boolean, data: StatusBean?) {
        if (success) {
            switch = false
            if (data != null && data.status == 2) {
                sendChatHiMessage(ChatHiAttachment.CHATHI_MATCH)
                startActivity<MatchSucceedActivity>("matchBean" to matchUserAdapter.data[matchUserAdapter.data.size - 1])
            }

        }
    }


    override fun onError(text: String) {
        Log.d("error", text)
    }


    /*---------------------事件总线--------------------------------*/

    /**
     * 通过全局的标签来更新数据
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateLabelEvent(event: UpdateLabelEvent) {
        stateview.viewState = MultiStateView.VIEW_STATE_LOADING

        params["tag_id"] = event.label.id
        matchUserAdapter.data.clear()
        page = 1
        matchParams["page"] = page
        hasMore = false
        matchParams["tagid"] = event.label.id
        //这个地方还要默认设置选中第一个标签来更新数据
        mPresenter.getMatchList(matchParams)
    }


    /**
     * 通过本地的筛选条件类更新数据
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRefreshEvent(event: RefreshEvent) {
        stateview.viewState = MultiStateView.VIEW_STATE_LOADING

        matchUserAdapter.data.clear()
        page = 1
        matchParams["page"] = page
        hasMore = false
        val params = UserManager.getFilterConditions()
        params.forEach {
            matchParams[it.key] = it.value
        }
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
        tvLeftChatTime.text = "${UserManager.getLightingCount()}"
    }

    /*---------------------卡片参数和方法------------------------------*/

    private fun initialize() {
        //卡片排列方式
        manager.setStackFrom(StackFrom.Bottom)
        //最大可见数量
        manager.setVisibleCount(3)
        //两个卡片之间的间隔
        manager.setTranslationInterval(13.0f)
        //最大的缩放间隔
        manager.setScaleInterval(0.95f)
        //卡片滑出飞阈值
        manager.setSwipeThreshold(0.6f)
        //横向纵向的旋转角度
        manager.setMaxDegree(5F)
        //滑动的方向
        manager.setDirections(mutableListOf(Direction.Left, Direction.Right, Direction.Top))
        manager.setCanScrollHorizontal(true)
        manager.setCanScrollVertical(true)
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

    var switch = false
    private val chargeVipDialog by lazy {
        ChargeVipDialog(activity!!).apply {
            setOnDismissListener {
                switch = false
            }
        }
    }

    override fun onCardDragging(direction: Direction, ratio: Float) {
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
            Direction.Top -> {
                //重置左边、上边的距离
                val paramsDisLike = animation_dislike.layoutParams as RelativeLayout.LayoutParams
                paramsDisLike.width = 0
                paramsDisLike.height = 0
                animation_dislike.layoutParams = paramsDisLike
                animation_dislike.alpha = 0F

                val paramsLike = animation_like.layoutParams as RelativeLayout.LayoutParams
                paramsLike.width = 0
                paramsLike.height = 0
                animation_like.layoutParams = paramsLike
                animation_like.alpha = 0F




                animation_chathi.alpha = ratio
                val params = animation_chathi.layoutParams as RelativeLayout.LayoutParams
//                params.width = (ScreenUtils.getScreenWidth() / 2F * ratio).toInt()
//                params.height = (ScreenUtils.getScreenWidth() / 2F * ratio).toInt()
                params.width = (SizeUtils.dp2px(50F) + SizeUtils.dp2px(50f) * ratio).toInt()
                params.height = (SizeUtils.dp2px(50F) + SizeUtils.dp2px(50f) * ratio).toInt()
                params.topMargin =
                    ((ScreenUtils.getScreenHeight() / 2F * ratio) - SizeUtils.dp2px(126F) - params.height / 2F).toInt()
                Log.d(
                    "CardStackView",
                    "topMargin= ${params.topMargin}, getScreenHeight = ${ScreenUtils.getScreenHeight()}"
                )

                animation_chathi.layoutParams = params

            }

        }
        Log.d("CardStackView", "onCardDragging: d = ${direction.name}, r = $ratio")
    }

    //此时已经飞出去了
    //todo 放开注释
    override fun onCardSwiped(direction: Direction?) {
        resetAnimation()
        if (direction == Direction.Left) {//左滑不喜欢
            toast("不喜欢${matchUserAdapter.data[manager.topPosition - 1].nickname}")
//            params["target_accid"] = matchUserAdapter.data[manager.topPosition - 1].accid ?: ""
//            mPresenter.dislikeUser(params)
        } else if (direction == Direction.Right) {//右滑喜欢
            toast("喜欢${matchUserAdapter.data[manager.topPosition - 1].nickname}")
//            params["target_accid"] = matchUserAdapter.data[manager.topPosition - 1].accid ?: ""
//            mPresenter.likeUser(params)
        } else if (direction == Direction.Top) {//上滑打招呼
            mPresenter.greetState(
                UserManager.getToken(),
                UserManager.getAccid(),
                (matchUserAdapter.data[manager.topPosition - 1].accid ?: ""),
                matchUserAdapter.data[manager.topPosition - 1]
            )
        }

        //如果已经只剩5张了就请求数据(预加载)
        if (hasMore && manager.topPosition == matchUserAdapter.itemCount - 5) {
            page++
            matchParams["page"] = page
            mPresenter.getMatchList(matchParams)
        } else if (!hasMore && manager.topPosition == matchUserAdapter.itemCount) {
            stateview.viewState = MultiStateView.VIEW_STATE_EMPTY
            btnChat.isVisible = false
            tvLeftChatTime.isVisible = false
        }
    }

    override fun onCardCanceled() {
        resetAnimation()
        Log.d("CardStackView", "onCardCanceled: ${manager.topPosition}")

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

    override fun onCardAppeared(view: View?, position: Int) {
        Log.d("CardStackView", "onCardAppeared: ($position)")

    }

    override fun onCardRewound() {
        Log.d("CardStackView", "onCardRewound: ${manager.topPosition}")

    }


    /*--------------------------消息代理------------------------*/

    private fun sendChatHiMessage(type: Int) {
        val matchBean = matchUserAdapter.data[manager.topPosition - 1]
        Log.d("OkHttp", matchBean.accid ?: "")
        val container = Container(activity!!, matchBean?.accid, SessionTypeEnum.P2P, this, true)
        val chatHiAttachment = ChatHiAttachment(UserManager.getGlobalLabelName(), type)
        val message = MessageBuilder.createCustomMessage(
            matchBean?.accid,
            SessionTypeEnum.P2P,
            "",
            chatHiAttachment,
            CustomMessageConfig()
        )
        container.proxy.sendMessage(message)
    }


    override fun sendMessage(msg: IMMessage): Boolean {
        NIMClient.getService(MsgService::class.java).sendMessage(msg, false).setCallback(object :
            RequestCallback<Void?> {
            override fun onSuccess(param: Void?) {
                ChatActivity.start(activity!!, matchUserAdapter.data[manager.topPosition - 1]?.accid ?: "")
                /*manager.topPosition*/
                //打招呼成功，就减少招呼次数
                if (msg.attachment is ChatHiAttachment && (msg.attachment as ChatHiAttachment).showType == ChatHiAttachment.CHATHI_HI) {
                    UserManager.saveLightingCount(UserManager.getLightingCount() - 1)
                    tvLeftChatTime.text = "${UserManager.getLightingCount()}"
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

    override fun onInputPanelExpand() {

    }

    override fun shouldCollapseInputPanel() {

    }

    override fun isLongClickEnabled(): Boolean {
        return false
    }

    override fun onItemFooterClick(message: IMMessage?) {

    }

}
