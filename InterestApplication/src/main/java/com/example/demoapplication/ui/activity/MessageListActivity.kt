package com.example.demoapplication.ui.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.demoapplication.R
import com.example.demoapplication.model.MessageListBean
import com.example.demoapplication.model.MessageListBean1
import com.example.demoapplication.presenter.MessageListPresenter
import com.example.demoapplication.presenter.view.MessageListView
import com.example.demoapplication.ui.adapter.MessageListAdapter
import com.example.demoapplication.ui.adapter.MessageListFriensAdapter
import com.example.demoapplication.ui.adapter.MessageListHeadAdapter
import com.example.demoapplication.utils.UserManager
import com.guanaj.easyswipemenulibrary.EasySwipeMenuLayout
import com.kennyc.view.MultiStateView
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import kotlinx.android.synthetic.main.activity_message_list.*
import kotlinx.android.synthetic.main.headerview_label.view.*
import org.jetbrains.anko.startActivity
import java.util.*

/**
 * 总体消息列表
 */
class MessageListActivity : BaseMvpActivity<MessageListPresenter>(), MessageListView {

    private val params by lazy { hashMapOf("token" to UserManager.getToken(), "accid" to UserManager.getAccid()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_list)

        initView()
        mPresenter.messageCensus(params)
    }

    private val adapter by lazy { MessageListAdapter() }
    private fun initView() {
        mPresenter = MessageListPresenter()
        mPresenter.mView = this
        mPresenter.context = this

        btnBack.onClick {
            finish()
        }
        contactBoockBtn.onClick {
            startActivity<ContactBookActivity>()
        }

        messageListRv.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        messageListRv.adapter = adapter
        adapter.bindToRecyclerView(messageListRv)
        adapter.setEmptyView(R.layout.empty_layout, messageListRv)
        adapter.addHeaderView(initHeadsView())

        adapter.setOnItemChildClickListener { _, view, position ->
            when (view.id) {
                R.id.menuTop -> {
                    Collections.swap(adapter.data, position, 0)
                    adapter.notifyDataSetChanged()
                    (adapter.getViewByPosition(position, R.id.msgSwipeMenu) as EasySwipeMenuLayout).resetStatus()
                }
                R.id.menuDetele -> {
                    adapter.remove(position)
                    (adapter.getViewByPosition(position, R.id.msgSwipeMenu) as EasySwipeMenuLayout).resetStatus()
                }
                R.id.content -> {
                    when (position) {
                        0 -> {
                            startActivity<MessageHiActivity>()
                        }
                        1 -> {
                            startActivity<MessageSquareActivity>()
                        }
                        2 -> {
                            startActivity<MessageLikeMeActivity>()
                        }
                    }
                }
            }
        }


        initData()

    }

    private fun initData() {
        val msgList = mutableListOf<MessageListBean>()
        msgList.add(MessageListBean("官方助手", "助手推送消息内容", 3, "2分钟前"))
        msgList.add(MessageListBean("发现", "javer 赞了你的动态", 3, "1小时前"))
        msgList.add(MessageListBean("对我感兴趣的", "有XX个人对你感兴趣，快来看看吧", 99, "昨天"))
        msgList.add(MessageListBean("xx", "javer 赞了你的动态", 3, "1小时前"))
        msgList.add(MessageListBean("javer", "有XX个人对你感兴趣，快来看看吧", 99, "昨天"))
        msgList.add(MessageListBean("sisi", "XX 发表了新动态，去看看吧", 3, "2分钟前"))
        msgList.add(MessageListBean("cll", "javer 赞了你的动态", 3, "1小时前"))
        msgList.add(MessageListBean("zfm", "有XX个人对你感兴趣，快来看看吧", 99, "昨天"))
        adapter.setNewData(msgList)
    }


    //创建打招呼好友布局
    private val hiAdapter by lazy { MessageListFriensAdapter() }

    private fun initFriendsView(): View {
        val friendsView = LayoutInflater.from(this).inflate(R.layout.headerview_hi, messageListRv, false)
        val linearLayoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        friendsView.headRv.layoutManager = linearLayoutManager
        friendsView.headRv.adapter = hiAdapter
        hiAdapter.setOnItemClickListener { adapter, view, position ->
        }

        return friendsView
    }


    /**
     * 创建头布局
     */
    private val headAdapter by lazy { MessageListHeadAdapter() }

    private fun initHeadsView(): View {
        val headView = LayoutInflater.from(this).inflate(R.layout.headerview_hi, messageListRv, false)
        headView.friendTv.visibility = View.GONE
        val linearLayoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        headView.headRv.layoutManager = linearLayoutManager
        headView.headRv.adapter = headAdapter
        headAdapter.setOnItemClickListener { adapter, view, position ->
            when (position) {
                0 -> {
                    startActivity<MessageHiActivity>()
                }
                1 -> {
                    startActivity<MessageSquareActivity>()
                }
                2 -> {
                    startActivity<MessageLikeMeActivity>()
                }
            }
        }

        return headView
    }


    override fun onMessageCensusResult(data: MessageListBean1?) {
        stateview.viewState = MultiStateView.VIEW_STATE_CONTENT
        val ass = MessageListBean("官方助手", "助手推送消息内容", 1, "2分钟前", R.drawable.icon_assistant)

        ////1广场点赞 2评论我的 3为我评论点赞的 4@我的列表
        val squa = MessageListBean(
            "发现", when (data?.square_type) {
                1 -> {
                    "${data.square_nickname}赞了你的动态"
                }
                2 -> {
                    "${data.square_nickname}评论了你的动态"
                }
                3 -> {
                    "${data.square_nickname}赞了你的评论"
                }
                4 -> {
                    "${data.square_nickname}@了你"
                }
                else -> {
                    ""
                }
            }, data?.square_cnt ?: 0, "${data?.square_time}", R.drawable.icon_square_msg
        )


        val like = MessageListBean(
            "对我感兴趣的",
            "有${data?.liked_cnt ?: 0}个人对你感兴趣",
            data?.liked_unread_cnt ?: 0,
            "${data?.liked_time}",
            R.drawable.icon_like_msg
        )

        headAdapter.addData(ass)
        headAdapter.addData(squa)
        headAdapter.addData(like)
    }

}
