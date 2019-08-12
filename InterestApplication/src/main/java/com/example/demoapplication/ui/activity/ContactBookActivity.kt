package com.example.demoapplication.ui.activity

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.KeyboardUtils
import com.example.demoapplication.R
import com.example.demoapplication.model.ContactDataBean
import com.example.demoapplication.model.LetterComparator
import com.example.demoapplication.model.SquareBean
import com.example.demoapplication.nim.activity.ChatActivity
import com.example.demoapplication.presenter.ContactBookPresenter
import com.example.demoapplication.presenter.view.ContactBookView
import com.example.demoapplication.ui.adapter.ContactAdapter
import com.example.demoapplication.ui.adapter.ContactStarAdapter
import com.example.demoapplication.ui.dialog.ShareToFriendsDialog
import com.example.demoapplication.utils.UserManager
import com.example.demoapplication.widgets.sortcontacts.Cn2Spell
import com.example.demoapplication.widgets.sortcontacts.PinnedHeaderDecoration
import com.example.demoapplication.widgets.sortcontacts.WaveSideBarView
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import kotlinx.android.synthetic.main.activity_contact_book.*
import kotlinx.android.synthetic.main.headerview_label.view.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
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
        initView()
        mPresenter.getContactLists(params)
    }

    private val adapter by lazy { ContactAdapter() }
    private val searchAdapter by lazy { ContactStarAdapter(false) }
    private fun initView() {
        btnBack.onClick {
            finish()
        }

        mPresenter = ContactBookPresenter()
        mPresenter.mView = this
        mPresenter.context = this

        val manager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        contactsRv.layoutManager = manager

        val decoration = PinnedHeaderDecoration()
        decoration.registerTypePinnedHeader(1) { _, _ ->
            true
        }
        contactsRv.addItemDecoration(decoration)
        contactsRv.adapter = adapter
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
            chatOrShare(position)
        }

        searchContactsRv.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        searchContactsRv.adapter = searchAdapter
        searchAdapter.setOnItemClickListener { adapter, view, position ->
            //todo 聊天
            toast("${position}")
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
        var datas = adapter.data
        datas.addAll(headAdapter.data)
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
            //todo  跳转到聊天界面
            chatOrShare(position)
        }

        return headView
    }

    private fun chatOrShare(position: Int) {
        if (intent != null && intent.getSerializableExtra("square") != null) {
            sqauareBean = intent.getSerializableExtra("square") as SquareBean
            ShareToFriendsDialog(
                this@ContactBookActivity,
                adapter.data[position].avatar,
                adapter.data[position].nickname,
                adapter.data[position].accid,
                sqauareBean!!
            ).show()
        } else {
            ChatActivity.start(this, adapter.data[position].accid ?: "")
        }
    }


    /**
     * 这里只是为了做演示，实际上数据应该从服务器获取
     */
    override fun onGetContactListResult(data: ContactDataBean?) {
        if (data != null) {
            if (!data.list.isNullOrEmpty()) {
                for (data in data.list!!) {
                    data.index = Cn2Spell.getPinYinFirstLetter(data.nickname)
                }

                adapter.addData(data.list!!)
                Collections.sort(adapter.data, LetterComparator())
                adapter.notifyDataSetChanged()
            }
            if (!data.asterisk.isNullOrEmpty()) {
                for (data in data.asterisk!!) {
                    data.index = Cn2Spell.getPinYinFirstLetter(data.nickname)
                }
                Collections.sort(data.asterisk, LetterComparator())
                headAdapter.addData(data.asterisk!!)
            }
        }

    }
}
