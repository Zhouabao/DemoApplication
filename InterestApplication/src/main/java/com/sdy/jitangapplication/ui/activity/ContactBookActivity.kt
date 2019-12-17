package com.sdy.jitangapplication.ui.activity

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.KeyboardUtils
import com.kennyc.view.MultiStateView
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.event.UpdateContactBookEvent
import com.sdy.jitangapplication.model.ContactBean
import com.sdy.jitangapplication.model.ContactDataBean
import com.sdy.jitangapplication.model.LetterComparator
import com.sdy.jitangapplication.model.SquareBean
import com.sdy.jitangapplication.nim.activity.ChatActivity
import com.sdy.jitangapplication.presenter.ContactBookPresenter
import com.sdy.jitangapplication.presenter.view.ContactBookView
import com.sdy.jitangapplication.ui.adapter.ContactAdapter
import com.sdy.jitangapplication.ui.adapter.ContactStarAdapter
import com.sdy.jitangapplication.ui.dialog.ShareToFriendsDialog
import com.sdy.jitangapplication.utils.UserManager
import com.sdy.jitangapplication.widgets.sortcontacts.Cn2Spell
import com.sdy.jitangapplication.widgets.sortcontacts.PinnedHeaderDecoration
import com.sdy.jitangapplication.widgets.sortcontacts.WaveSideBarView
import kotlinx.android.synthetic.main.activity_contact_book.*
import kotlinx.android.synthetic.main.error_layout.view.*
import kotlinx.android.synthetic.main.headerview_contact.view.*
import kotlinx.android.synthetic.main.item_contact_book.view.*
import kotlinx.android.synthetic.main.layout_actionbar.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.startActivity
import java.util.*

/**
 * 通讯录
 * 包括转发到好友也是在这里面操作
 */
class ContactBookActivity : BaseMvpActivity<ContactBookPresenter>(), ContactBookView {
    private val params by lazy {
        hashMapOf(
            "token" to UserManager.getToken(),
            "accid" to UserManager.getAccid()
        )
    }

    private var sqauareBean: SquareBean? = null

    companion object {
        fun start(context: Context, squareBean: SquareBean) {
            context.startActivity<ContactBookActivity>("square" to squareBean)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_book)
        EventBus.getDefault().register(this)
        initView()
        mPresenter.getContactLists(params)
    }

    private val adapter by lazy { ContactAdapter() }
    private val searchAdapter by lazy { ContactStarAdapter(false) }
    private fun initView() {
        btnBack.onClick {
            finish()
        }
        if (intent != null && intent.getSerializableExtra("square") != null) {
            hotT1.text = "选择好友"
        } else {
            hotT1.text = "通讯录"
        }
        mPresenter = ContactBookPresenter()
        mPresenter.mView = this
        mPresenter.context = this

        stateview.retryBtn.onClick {
            stateview.viewState = MultiStateView.VIEW_STATE_LOADING
            mPresenter.getContactLists(params)
        }

        val manager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        contactsRv.layoutManager = manager

        val decoration = PinnedHeaderDecoration()
        decoration.registerTypePinnedHeader(1) { _, _ ->
            true
        }
        contactsRv.addItemDecoration(decoration)
        contactsRv.adapter = adapter
        adapter.addHeaderView(initAssistantView())
        adapter.addHeaderView(initHeadsView())
        indexBar.setOnSelectIndexItemListener(object : WaveSideBarView.OnSelectIndexItemListener {
            override fun onSelectIndexItem(letter: String) {
                if (letter.equals("☆")) {
                    (contactsRv.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(0, 0)
                    return
                }

                for (data in adapter.data.withIndex()) {
                    if (data.value.index.equals(letter)) {
                        (contactsRv.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(data.index + 1, 0)
                        return
                    }
                }
            }

        })
        adapter.setOnItemClickListener { _, view, position ->
            chatOrShare(adapter.data[position])
        }

        searchContactsRv.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        searchContactsRv.adapter = searchAdapter
        searchAdapter.setOnItemClickListener { _, view, position ->
            chatOrShare(searchAdapter.data[position])
            searchContactsRv.visibility = View.GONE
            KeyboardUtils.hideSoftInput(searchView)
            searchView.clearFocus()
        }



        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchResult(query)
                KeyboardUtils.hideSoftInput(searchView)
                searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                searchResult(newText)
                return true
            }

        })
    }

    private fun searchResult(query: String?) {
        //每次改变输入就清空数据重新查询
        searchAdapter.data.clear()
        searchAdapter.notifyDataSetChanged()
        for (data in adapter.data) {
            val pinyin = Cn2Spell.getPinYin(data.nickname ?: "")
            if (!query.isNullOrEmpty() && (pinyin.contains(query) || (data.nickname ?: "").contains(query))) {
                searchAdapter.addData(data)
            }
        }
        if (searchAdapter.data.size > 0) {
            searchContactsRv.visibility = View.VISIBLE
        } else {
            searchContactsRv.visibility = View.GONE
        }
    }


    /**
     * 创建头布局
     */
    private val headAdapter by lazy { ContactStarAdapter() }

    private fun initHeadsView(): View {
        val headView = LayoutInflater.from(this).inflate(R.layout.headerview_contact, contactsRv, false)
        val linearLayoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        headView.headRv.layoutManager = linearLayoutManager
        headView.headRv.adapter = headAdapter
        headAdapter.setOnItemClickListener { _, view, position ->
            chatOrShare(headAdapter.data[position])
        }

        return headView
    }

    private fun initAssistantView(): View {
        val headView = LayoutInflater.from(this).inflate(R.layout.item_contact_book, contactsRv, false)
        headView.tv_index.isVisible = false
        headView.friendDivider.isVisible = false
        headView.friendIcon.setImageResource(R.drawable.icon_assistant)
        headView.friendName.text = "官方小助手"
        headView.setOnClickListener {
            chatOrShare(ContactBean(nickname = "官方小助手",accid = Constants.ASSISTANT_ACCID))
        }
        return headView
    }

    private fun chatOrShare(squareBean: ContactBean) {
        if (intent != null && intent.getSerializableExtra("square") != null) {
            hotT1.text = "选择好友"
            sqauareBean = intent.getSerializableExtra("square") as SquareBean
            ShareToFriendsDialog(
                this@ContactBookActivity,
                squareBean.avatar,
                squareBean.nickname,
                squareBean.accid,
                sqauareBean!!
            ).show()
        } else {
            ChatActivity.start(this, squareBean.accid ?: "")
        }
    }


    /**
     * 这里只是为了做演示，实际上数据应该从服务器获取
     */
    override fun onGetContactListResult(data: ContactDataBean?) {
        stateview.viewState = MultiStateView.VIEW_STATE_CONTENT
        if (data != null) {
            if (!data.list.isNullOrEmpty()) {
                for (data in data.list!!) {
                    data.index = Cn2Spell.getPinYinFirstLetter(
                        if (data.nickname.isNullOrEmpty()) {
                            "#"
                        } else {
                            data.nickname
                        }
                    )
                }

                adapter.setNewData(data.list!!)
                Collections.sort(adapter.data, LetterComparator())
                adapter.notifyDataSetChanged()
            } else {
                adapter.notifyDataSetChanged()
            }
            if (!data.asterisk.isNullOrEmpty()) {
                for (data in data.asterisk!!) {
                    data.index = Cn2Spell.getPinYinFirstLetter(
                        if (data.nickname.isNullOrEmpty()) {
                            "#"
                        } else {
                            data.nickname
                        }
                    )
                }
                Collections.sort(data.asterisk, LetterComparator())
                headAdapter.setNewData(data.asterisk!!)
            } else {
                headAdapter.notifyDataSetChanged()
            }
        } else {
            adapter.notifyDataSetChanged()
            headAdapter.notifyDataSetChanged()
        }
    }

    override fun onError(text: String) {
        stateview.viewState = MultiStateView.VIEW_STATE_ERROR
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun updateContactEvent(event: UpdateContactBookEvent) {
        headAdapter.data.clear()
        adapter.data.clear()
        mPresenter.getContactLists(params)
    }
}
