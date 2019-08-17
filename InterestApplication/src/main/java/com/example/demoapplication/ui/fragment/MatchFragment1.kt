package com.example.demoapplication.ui.fragment


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DefaultItemAnimator
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.ToastUtils
import com.example.demoapplication.R
import com.example.demoapplication.common.Constants
import com.example.demoapplication.event.RefreshEvent
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
import kotlinx.android.synthetic.main.fragment_match.btnChat
import kotlinx.android.synthetic.main.fragment_match.stateview
import kotlinx.android.synthetic.main.fragment_match.tvLeftChatTime
import kotlinx.android.synthetic.main.fragment_match1.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.toast

/**
 * 匹配页面(新版)
 * //todo 探探是把用戶存在本地數據庫的
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
//        matchUserAdapter.setEmptyView(R.layout.empty_layout, card_stack_view)

        matchUserAdapter.setOnItemChildClickListener { _, view, position ->
            when (view.id) {
                R.id.v1 -> {
                    MatchDetailActivity.start(activity!!, (matchUserAdapter.data[manager.topPosition].accid ?: ""))
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


    private var lightCount = 0


    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnChat -> {
//                val matchBean = matchUserAdapter.data[manager.topPosition]

//                ChatActivity.start(activity!!, matchBean.accid ?: "")

                mPresenter.greetState(
                    hashMapOf(
                        "token" to UserManager.getToken(),
                        "accid" to UserManager.getAccid(),
                        "target_accid" to (matchUserAdapter.data[manager.topPosition].accid ?: "")
                    )
                )
            }
        }
    }


    /**
     *  点击聊天
     *  1. 好友 直接聊天 已经匹配过了 ×
     *
     *  2. 不是好友 判断是否打过招呼
     *
     *     2.1 打过招呼 且没有过期  直接直接聊天
     *
     *     2.2 未打过招呼 判断招呼剩余次数
     *
     *         2.2.1 有次数 直接打招呼
     *
     *         2.2.2 无次数 其他操作--如:请求充值会员
     */
    override fun onGreetStateResult(greetBean: GreetBean?) {
        if (greetBean != null) {
            if (greetBean.isfriend || (!greetBean.isfriend && greetBean.isgreet)) {
                ChatActivity.start(activity!!, matchUserAdapter.data[manager.topPosition].accid ?: "")
            } else {
                if (!greetBean.isgreet) {
                    if (greetBean.lightningcnt > 0) {
                        mPresenter.greet(
                            hashMapOf(
                                "token" to UserManager.getToken(),
                                "accid" to UserManager.getAccid(),
                                "target_accid" to (matchUserAdapter.data[manager.topPosition].accid ?: "")
                            )
                        )
                    } else {
                        if (UserManager.isUserVip()) {
                            ToastUtils.showShort("次数用尽，请充值。")
                        } else {
                            ChargeVipDialog(activity!!).show()
                        }
                    }
                }
            }
        } else {
            ToastUtils.showShort("请求失败，请重试")
        }
    }


    /**
     * 打招呼结果（先请求服务器）
     */
    override fun onGreetSResult(greetBean: Boolean) {
        if (greetBean) {
            sendChatHiMessage()
        } else {
            ToastUtils.showShort("打招呼失败，重新试一次吧")
        }
    }


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
            lightCount = matchBeans.lightningcnt ?: 0
            UserManager.saveUserVip(matchBeans.isvip)
            UserManager.saveUserVerify(matchBeans.isfaced)
            tvLeftChatTime.text = "${matchBeans.lightningcnt}"
        } else {
            stateview.viewState = MultiStateView.VIEW_STATE_ERROR
            stateview.errorMsg.text = if (mPresenter.checkNetWork()) {
                activity!!.getString(R.string.retry_load_error)
            } else {
                activity!!.getString(R.string.retry_net_error)
            }
        }
    }


    override fun onGetDislikeResult(success: Boolean) {
        if (success) {
//            matchUserAdapter.remove(manager.topPosition - 1)

//            matchUserAdapter.data.removeAt(manager.topPosition - 1)
//            matchUserAdapter.notifyItemRemoved(manager.topPosition - 1)
        } else {
//            val data = matchUserAdapter.data[manager.topPosition - 1]
//            matchUserAdapter.remove(manager.topPosition - 1)
//            matchUserAdapter.addData(manager.topPosition - 1, data)

//            matchUserAdapter.data.add(manager.topPosition - 1, matchUserAdapter.data.removeAt(manager.topPosition - 1))
//            matchUserAdapter.notifyItemChanged(manager.topPosition)
        }
    }

    //todo  这里应该还要传参数
    //status :1.喜欢成功  2.匹配成功
    override fun onGetLikeResult(success: Boolean, data: StatusBean?) {
        if (success) {
            switch = false
            if (data != null && data.status == 2) {
                startActivity<MatchSucceedActivity>("matchBean" to matchUserAdapter.data[matchUserAdapter.data.size - 1])
            }
//            matchUserAdapter.remove(manager.topPosition - 1)

//            matchUserAdapter.remove(matchUserAdapter.data.size - 1)
        } else {
//            val data = matchUserAdapter.data[manager.topPosition - 1]
//            matchUserAdapter.remove(manager.topPosition - 1)
//            matchUserAdapter.addData(manager.topPosition - 1, data)

//            matchUserAdapter.data.add(manager.topPosition - 1, matchUserAdapter.data.removeAt(manager.topPosition - 1))
//            matchUserAdapter.notifyItemChanged(manager.topPosition - 1)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onResume() {
        super.onResume()
    }


    /**
     * 通过全局的标签来更新数据
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateLabelEvent(event: UpdateLabelEvent) {
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


    override fun onError(text: String) {
        Log.d("error", text)
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
        manager.setSwipeThreshold(0.3f)
        //横向纵向的旋转角度
        manager.setMaxDegree(5F)
        //滑动的方向
        manager.setDirections(Direction.HORIZONTAL)
        manager.setCanScrollHorizontal(true)
        manager.setCanScrollVertical(true)
        manager.setSwipeableMethod(SwipeableMethod.AutomaticAndManual)
        manager.setOverlayInterpolator(LinearInterpolator())
        card_stack_view.layoutManager = manager
        card_stack_view.adapter = matchUserAdapter
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
        if (direction == Direction.Top && ratio > 0.5F && !switch) {
            switch = true
            if (!UserManager.isUserVip()) {
                chargeVipDialog.show()
            } else {
                val setting = SwipeAnimationSetting.Builder()
                    .setDirection(Direction.Right)
                    .setDuration(Duration.Normal.duration)
                    .setInterpolator(AccelerateInterpolator())
                    .build()
                manager.setSwipeAnimationSetting(setting)
                card_stack_view.swipe()
//                params["target_accid"] = matchUserAdapter.data[manager.topPosition - 1].accid ?: ""
//                mPresenter.likeUser(params)
                switch = false
            }
        }
        Log.d("CardStackView", "onCardDragging: d = ${direction.name}, r = $ratio")
    }

    //此时已经飞出去了
    override fun onCardSwiped(direction: Direction?) {
        switch = false
        Log.d("CardStackView", "onCardSwiped: p = ${manager.topPosition}, d = $direction")
        if (direction == Direction.Left) {
            toast("不喜欢${matchUserAdapter.data[manager.topPosition - 1].nickname}")
//            params["target_accid"] = matchUserAdapter.data[manager.topPosition - 1].accid ?: ""
//            mPresenter.dislikeUser(params)
        } else if (direction == Direction.Right) {
            toast("喜欢${matchUserAdapter.data[manager.topPosition - 1].nickname}")
//            params["target_accid"] = matchUserAdapter.data[manager.topPosition - 1].accid ?: ""
//            mPresenter.likeUser(params)
        }

        //如果已经只剩5张了就请求数据
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
        Log.d("CardStackView", "onCardCanceled: ${manager.topPosition}")

    }

    override fun onCardAppeared(view: View?, position: Int) {
        Log.d("CardStackView", "onCardAppeared: ($position)")

    }

    override fun onCardRewound() {
        Log.d("CardStackView", "onCardRewound: ${manager.topPosition}")

    }


    /*--------------------------消息代理------------------------*/

    private fun sendChatHiMessage() {
        val matchBean = matchUserAdapter.data[manager.topPosition]
        Log.d("OkHttp",matchBean.accid?:"")
        val container = Container(activity!!, matchBean?.accid, SessionTypeEnum.P2P, this, true)
//        val chatHiAttachment = ChatMatchAttachment(
//            UserManager.getGlobalLabelName(),
//            matchBean?.tags ?: mutableListOf(),
//            matchBean?.avatar ?: ""
//        )
        val chatHiAttachment = ChatHiAttachment(
            UserManager.getGlobalLabelName(),
            ChatHiAttachment.CHATHI_HI
        )
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
                ChatActivity.start(activity!!, matchUserAdapter.data[manager.topPosition]?.accid ?: "")

            }

            override fun onFailed(code: Int) {
                toast("$code")
            }

            override fun onException(exception: Throwable) {
                toast(exception.message ?: "")
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
