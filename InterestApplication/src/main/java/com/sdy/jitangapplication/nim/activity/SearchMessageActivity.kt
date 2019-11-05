package com.sdy.jitangapplication.nim.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.AbsListView
import android.widget.AdapterView
import androidx.appcompat.widget.SearchView
import com.kotlin.base.common.AppManager
import com.kotlin.base.ext.onClick
import com.netease.nim.uikit.business.uinfo.UserInfoHelper
import com.netease.nim.uikit.common.activity.UI
import com.netease.nim.uikit.common.ui.listview.AutoRefreshListView
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.RequestCallbackWrapper
import com.netease.nimlib.sdk.msg.MessageBuilder
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.sdy.baselibrary.widgets.swipeback.SwipeBackLayout
import com.sdy.baselibrary.widgets.swipeback.Utils
import com.sdy.baselibrary.widgets.swipeback.app.SwipeBackActivityBase
import com.sdy.baselibrary.widgets.swipeback.app.SwipeBackActivityHelper
import com.sdy.jitangapplication.R
import kotlinx.android.synthetic.main.activity_search_message.*
import kotlinx.android.synthetic.main.layout_actionbar.*
import org.jetbrains.anko.startActivity

/**
 * 查找聊天记录
 */
class SearchMessageActivity : UI(),SwipeBackActivityBase {
    private lateinit var account: String

    companion object {
        private const val INTENT_EXTRA_UID = "intent_extra_uid"
        private const val INTENT_EXTRA_SESSION_TYPE = "intent_extra_session_type"
        private const val SEARCH_COUNT = 20


        @JvmStatic
        fun start(context: Context, sessionId: String, sessionTypeEnum: SessionTypeEnum) {
            context.startActivity<SearchMessageActivity>(
                INTENT_EXTRA_UID to sessionId,
                INTENT_EXTRA_SESSION_TYPE to sessionTypeEnum
            )
        }
    }

    private val searchResultListData = mutableListOf<IMMessage?>()
    private lateinit var adapter: SearchMessageAdapter
    private var searching: Boolean = false
    private var pendingText: String? = null
    private var emptyMsg: IMMessage? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_message)



        AppManager.instance.addActivity(this)
        mHelper = SwipeBackActivityHelper(this)
        mHelper.onActivityCreate()
        swipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT)




        initView()
        initSearchListView()
        handleIntent()
        hotT1.text = UserInfoHelper.getUserName(account)

    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent()
    }


   private fun handleIntent() {
        account = intent.getStringExtra(INTENT_EXTRA_UID) ?: ""
        reset()
    }

    private fun initView() {
        btnBack.onClick {
            finish()
        }
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                searchByKeyword(query)
                showKeyboard(false)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                searchByKeyword(newText)
                return true
            }

        })
    }


    private fun initSearchListView() {
        searchResultList.setMode(AutoRefreshListView.Mode.END)
        searchResultList.visibility = View.GONE
        searchResultList.emptyView = LayoutInflater.from(this).inflate(R.layout.empty_layout, null)
        searchResultList.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val anchor = searchResultList.adapter.getItem(position) as IMMessage
            DisplayMessageActivity.start(this@SearchMessageActivity, anchor)

            showKeyboard(false)
        }
        searchResultList.addOnScrollListener(object : AbsListView.OnScrollListener {
            override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {
                showKeyboard(false)
            }

            override fun onScroll(
                view: AbsListView, firstVisibleItem: Int,
                visibleItemCount: Int, totalItemCount: Int
            ) {
            }
        })
        searchResultList.setOnRefreshListener(object : AutoRefreshListView.OnRefreshListener {
            override fun onRefreshFromStart() {}

            override fun onRefreshFromEnd() {
                showKeyboard(false)
                loadMoreSearchResult()
            }
        })

        adapter = SearchMessageAdapter(this, searchResultListData)
        searchResultList.adapter = adapter
    }

    private fun loadMoreSearchResult() {
        doSearch(searchView.query.toString(), searchResultListData.size > 0)
    }


    private fun pend(query: String, append: Boolean): Boolean {
        if (searching && !append) {
            pendingText = query
        }
        return searching
    }

    private fun onPend(): Boolean {
        var reset = false
        if (pendingText != null) {
            if (pendingText!!.isEmpty()) {
                reset()
                reset = true
            } else {
                doSearch(pendingText!!, false)
            }
            pendingText = null
        }

        return reset
    }


    private fun reset() {
        searchResultListData.clear()
        adapter.notifyDataSetChanged()

        emptyMsg = MessageBuilder.createEmptyMessage(account, SessionTypeEnum.P2P, 0)
    }

    private fun searchByKeyword(keyword: String) {
        when {
            TextUtils.isEmpty(keyword) -> {
                searchResultList.visibility = View.GONE
                reset()
            }
            TextUtils.isEmpty(keyword.trim()) -> {
                searchResultListData.clear()
                adapter.notifyDataSetChanged()
                searchResultList.visibility = View.VISIBLE
            }
            else -> doSearch(keyword, false)
        }
    }


    private fun doSearch(keyword: String, append: Boolean) {
        if (pend(keyword, append)) {
            return
        }
        searching = true
        val anchor = if (append) searchResultListData[searchResultListData.size - 1] else emptyMsg

        NIMClient.getService(MsgService::class.java)
            .searchMessageHistory(keyword, arrayListOf(), anchor, SEARCH_COUNT)
            .setCallback(object : RequestCallbackWrapper<MutableList<IMMessage?>?>() {
                override fun onResult(p0: Int, p1: MutableList<IMMessage?>?, p2: Throwable?) {
                }

                override fun onSuccess(result: MutableList<IMMessage?>?) {
                    searching = false
                    if (result != null) {
                        searchResultList.onRefreshComplete(result.size, SEARCH_COUNT, true)

                        if (!onPend()) {
                            if (!append) {
                                searchResultListData.clear()
                            }
                            searchResultListData.addAll(result)
                            adapter.setKeyword(keyword)
                            adapter.notifyDataSetChanged()
                            searchResultList.visibility = View.VISIBLE
                        }
                    }
                }

                override fun onException(p0: Throwable?) {
                    super.onException(p0)
                }

                override fun onFailed(p0: Int) {
                    super.onFailed(p0)
                }
            })

    }

    override fun onDestroy() {
        super.onDestroy()
        AppManager.instance.finishActivity(this)
    }


    /*------------------------侧滑退出-----------------*/
    private lateinit var mHelper: SwipeBackActivityHelper

    override fun getSwipeBackLayout(): SwipeBackLayout {
        return mHelper.swipeBackLayout
    }

    override fun setSwipeBackEnable(enable: Boolean) {
        swipeBackLayout.setEnableGesture(enable)
    }

    override fun scrollToFinishActivity() {
        Utils.convertActivityToTranslucent(this)
        swipeBackLayout.scrollToFinishActivity()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        mHelper.onPostCreate()

    }
}
