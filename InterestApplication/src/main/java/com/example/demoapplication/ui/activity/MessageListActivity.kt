package com.example.demoapplication.ui.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ToastUtils
import com.example.demoapplication.R
import com.example.demoapplication.model.MessageListBean
import com.example.demoapplication.presenter.MessageListPresenter
import com.example.demoapplication.presenter.view.MessageListView
import com.example.demoapplication.ui.adapter.MessageListAdapter
import com.example.demoapplication.ui.adapter.MessageListFriensAdapter
import com.guanaj.easyswipemenulibrary.EasySwipeMenuLayout
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import kotlinx.android.synthetic.main.activity_message_list.*
import kotlinx.android.synthetic.main.fragment_square.*
import kotlinx.android.synthetic.main.headerview_label.view.*
import org.jetbrains.anko.startActivity
import java.util.*

/**
 * 总体消息列表
 */
class MessageListActivity : BaseMvpActivity<MessageListPresenter>(), MessageListView {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_list)

        initView()
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
            ToastUtils.showShort("电话本")
        }

        messageListRv.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        messageListRv.adapter = adapter
        adapter.bindToRecyclerView(messageListRv)
        adapter.setEmptyView(R.layout.empty_layout, messageListRv)
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
        val friendsView = LayoutInflater.from(this).inflate(R.layout.headerview_hi, squareDynamicRv, false)
        val linearLayoutManager = LinearLayoutManager(applicationContext, LinearLayoutManager.HORIZONTAL, false)
        friendsView.headRv.layoutManager = linearLayoutManager
        friendsView.headRv.adapter = hiAdapter
        hiAdapter.setOnItemClickListener { adapter, view, position ->
        }

        return friendsView
    }
}
