package com.example.demoapplication.ui.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.KeyboardUtils
import com.example.demoapplication.R
import com.example.demoapplication.model.ContactBean
import com.example.demoapplication.model.ContactDataBean
import com.example.demoapplication.model.LetterComparator
import com.example.demoapplication.presenter.ContactBookPresenter
import com.example.demoapplication.presenter.view.ContactBookView
import com.example.demoapplication.ui.adapter.ContactAdapter
import com.example.demoapplication.ui.adapter.ContactStarAdapter
import com.example.demoapplication.utils.UserManager
import com.example.demoapplication.widgets.sortcontacts.Cn2Spell
import com.example.demoapplication.widgets.sortcontacts.PinnedHeaderDecoration
import com.example.demoapplication.widgets.sortcontacts.WaveSideBarView
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import kotlinx.android.synthetic.main.activity_contact_book.*
import kotlinx.android.synthetic.main.headerview_label.view.*
import org.jetbrains.anko.toast
import java.util.*

/**
 * 通讯录
 */
class ContactBookActivity : BaseMvpActivity<ContactBookPresenter>(), ContactBookView {
    private val params by lazy {
        hashMapOf(
            "token" to UserManager.getToken(),
            "accid" to UserManager.getAccid()
        )
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
        headAdapter.setOnItemClickListener { adapter, view, position ->
            //todo  跳转到聊天界面
        }

        return headView
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
                adapter.addData(data.list!!)
                adapter.addData(data.list!!)
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


    /**
     * 这里只是为了做演示，实际上数据应该从服务器获取
     */
    private fun getContacts(): MutableList<ContactBean> {
        val contacts = mutableListOf<ContactBean>()
        contacts.add(ContactBean("Andy", index = "☆"))
        contacts.add(ContactBean("阿姨"))
        contacts.add(ContactBean("爸爸"))
        contacts.add(ContactBean("Bear", index = "☆"))
        contacts.add(ContactBean("BiBi"))
        contacts.add(ContactBean("CiCi"))
        contacts.add(ContactBean("刺猬"))
        contacts.add(ContactBean("Dad"))
        contacts.add(ContactBean("弟弟"))
        contacts.add(ContactBean("妈妈", index = "☆"))
        contacts.add(ContactBean("哥哥"))
        contacts.add(ContactBean("姐姐"))
        contacts.add(ContactBean("奶奶"))
        contacts.add(ContactBean("嗯哼"))
        contacts.add(ContactBean("鹅毛"))
        contacts.add(ContactBean("爷爷"))
        contacts.add(ContactBean("哈哈"))
        contacts.add(ContactBean("测试"))
        contacts.add(ContactBean("自己"))
        contacts.add(ContactBean("You"))
        contacts.add(ContactBean("NearLy"))
        contacts.add(ContactBean("Hear"))
        contacts.add(ContactBean("Where"))
        contacts.add(ContactBean("怕"))
        contacts.add(ContactBean("嘻嘻"))
        contacts.add(ContactBean("123"))
        contacts.add(ContactBean("1508022"))
        contacts.add(ContactBean("2251"))
        contacts.add(ContactBean("****"))
        contacts.add(ContactBean("####"))
        contacts.add(ContactBean("w asad "))
        contacts.add(ContactBean("我爱你"))
        contacts.add(ContactBean("一百二十二"))
        contacts.add(ContactBean("壹"))
        contacts.add(ContactBean("I"))
        contacts.add(ContactBean("肆"))
        contacts.add(ContactBean("王八蛋"))
        contacts.add(ContactBean("zzz"))
        contacts.add(ContactBean("呵呵哒"))
        contacts.add(ContactBean("叹气"))
        contacts.add(ContactBean("南尘"))
        contacts.add(ContactBean("欢迎关注"))
        contacts.add(ContactBean("西西"))
        contacts.add(ContactBean("东南"))
        contacts.add(ContactBean("成都"))
        contacts.add(ContactBean("四川"))
        contacts.add(ContactBean("爱上学"))
        contacts.add(ContactBean("爱吖校推"))

        Collections.sort<ContactBean>(contacts, LetterComparator())
        return contacts
    }

}
