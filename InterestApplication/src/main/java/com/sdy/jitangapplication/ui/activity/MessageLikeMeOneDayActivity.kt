package com.sdy.jitangapplication.ui.activity

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kennyc.view.MultiStateView
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.netease.nim.uikit.business.session.module.Container
import com.netease.nim.uikit.business.session.module.ModuleProxy
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.msg.MessageBuilder
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.CustomMessageConfig
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.event.UpdateLikemeEvent
import com.sdy.jitangapplication.event.UpdateLikemeOnePosEvent
import com.sdy.jitangapplication.model.LikeMeOneDayBean
import com.sdy.jitangapplication.model.StatusBean
import com.sdy.jitangapplication.nim.activity.ChatActivity
import com.sdy.jitangapplication.nim.attachment.ChatHiAttachment
import com.sdy.jitangapplication.presenter.MessageLikeMeOneDayPresenter
import com.sdy.jitangapplication.presenter.view.MessageLikeMeOneDayView
import com.sdy.jitangapplication.ui.adapter.LikeMeOneDayGirdAdapter
import com.sdy.jitangapplication.ui.chat.MatchSucceedActivity
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.activity_message_like_me.btnBack
import kotlinx.android.synthetic.main.activity_message_like_me_one_day.*
import kotlinx.android.synthetic.main.activity_message_like_me_one_day.stateview
import kotlinx.android.synthetic.main.error_layout.view.*
import kotlinx.android.synthetic.main.item_like_me_one_day_header.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.startActivity

/**
 * 喜欢我的一天总数据
 */
class MessageLikeMeOneDayActivity : BaseMvpActivity<MessageLikeMeOneDayPresenter>(), MessageLikeMeOneDayView,
    OnRefreshListener, OnLoadMoreListener, ModuleProxy {


    private var clickPos = -1
    private var page = 1
    private val params by lazy {
        hashMapOf(
            "token" to UserManager.getToken(),
            "accid" to UserManager.getAccid(),
            "pagesize" to Constants.PAGESIZE,
            "page" to page,
            "date" to intent.getStringExtra("date")
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_like_me_one_day)
        EventBus.getDefault().register(this)
        initView()
        mPresenter.likeListsCategory(params)
    }

    private val adapter by lazy { LikeMeOneDayGirdAdapter() }
    private fun initView() {
        btnBack.onClick {
            finish()
        }

        mPresenter = MessageLikeMeOneDayPresenter()
        mPresenter.mView = this
        mPresenter.context = this

        refreshLayout.setOnRefreshListener(this)
        refreshLayout.setOnLoadMoreListener(this)
        refreshLayout.setEnableAutoLoadMore(true)

        stateview.retryBtn.onClick {
            stateview.viewState = MultiStateView.VIEW_STATE_LOADING
            mPresenter.likeListsCategory(params)
        }

        likeRv.layoutManager = GridLayoutManager(this, 3, RecyclerView.VERTICAL, false)
        likeRv.adapter = adapter
        adapter.bindToRecyclerView(likeRv)

        adapter.setEmptyView(R.layout.empty_layout, likeRv)
        adapter.addHeaderView(initHeadView())
        adapter.setHeaderAndEmpty(true)

        adapter.setOnItemClickListener { _, view, position ->
            if (UserManager.isUserVip())
                MatchDetailActivity.start(this, adapter.data[position].accid ?: "",position)
        }
        adapter.setOnItemChildClickListener { _, view, position ->
            when (view.id) {
                R.id.likeMeOneDayType -> {
                    clickPos = position
                    if (adapter.data[position].isfriend == 1) {
                        ChatActivity.start(this, adapter.data[position].accid ?: "")
                    } else {
                        mPresenter.likeUser(
                            position, hashMapOf(
                                "accid" to UserManager.getAccid(),
                                "token" to UserManager.getToken(),
                                "target_accid" to (adapter.data[position].accid ?: "")
//                                "tag_id" to (adapter.data[position].tag_title ?: "")
                            )
                        )
                    }
                }
            }
        }
        adapter.headerLayout.likeCount.text = "${intent.getIntExtra("count", 0)} 人对你感兴趣"
        adapter.headerLayout.likeDate.text = "${intent.getStringExtra("date")}"
        adapter.headerLayout.likeNew.isVisible = intent.getBooleanExtra("hasread", false)
    }

    private fun initHeadView(): View {
        return layoutInflater.inflate(R.layout.item_like_me_one_day_header, likeRv, false)
    }

    override fun onLoadMore(refreshLayout: RefreshLayout) {
        page++
        params["page"] = page
        mPresenter.likeListsCategory(params)

    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        page = 1
        params["page"] = page
        adapter.data.clear()
        refreshLayout.setNoMoreData(false)
        mPresenter.likeListsCategory(params)
    }

    override fun onLikeListResult(datas: MutableList<LikeMeOneDayBean>) {
        stateview.viewState = MultiStateView.VIEW_STATE_CONTENT
        if (datas.isNullOrEmpty()) {
            refreshLayout.finishLoadMoreWithNoMoreData()
        } else {
            refreshLayout.finishLoadMore(true)
        }
        adapter.addData(datas)
        refreshLayout.finishRefresh(true)
    }


    override fun onGetLikeResult(b: Boolean, statusBean: BaseResp<StatusBean?>, position: Int) {
        if (statusBean != null) {
            if (statusBean.data?.status == 2) {//匹配成功
                adapter.data[clickPos].isfriend = 1
                adapter.notifyItemChanged(clickPos)
                EventBus.getDefault().post(UpdateLikemeEvent(clickPos))
                sendChatHiMessage(ChatHiAttachment.CHATHI_MATCH)
            }
        }
    }


    override fun onError(text: String) {
        stateview.viewState = MultiStateView.VIEW_STATE_ERROR
        refreshLayout.finishLoadMore(false)
        stateview.errorMsg.text = if (mPresenter.checkNetWork()) {
            getString(R.string.retry_load_error)
        } else {
            getString(R.string.retry_net_error)
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)

    }

    /*--------------------------消息代理------------------------*/
    private fun sendChatHiMessage(position: Int) {
        val accid = adapter.data[position].accid ?: ""
        val container = Container(this, adapter.data[position].accid, SessionTypeEnum.P2P, this, true)
        val chatHiAttachment = ChatHiAttachment(UserManager.getGlobalLabelName(), ChatHiAttachment.CHATHI_MATCH)
        val message = MessageBuilder.createCustomMessage(
            accid,
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
                startActivity<MatchSucceedActivity>(
                    "avator" to adapter.data[clickPos].avatar,
                    "nickname" to adapter.data[clickPos].nickname,
                    "accid" to adapter.data[clickPos].accid
                )
            }

            override fun onFailed(code: Int) {
            }

            override fun onException(exception: Throwable) {
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


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateLikemeOnePosEvent(event: UpdateLikemeOnePosEvent) {
        if (event.parPos != -1 && event.childPos == -1) {
            adapter.data[event.parPos].isfriend  = 1
            adapter.notifyDataSetChanged()
        }
    }

}
