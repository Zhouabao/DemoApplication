package com.sdy.jitangapplication.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ActivityUtils
import com.kennyc.view.MultiStateView
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.fragment.BaseMvpFragment
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.constant.RefreshState
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.event.UpdateFindByTagEvent
import com.sdy.jitangapplication.model.CheckBean
import com.sdy.jitangapplication.model.DatingBean
import com.sdy.jitangapplication.model.SquareTagBean
import com.sdy.jitangapplication.presenter.DatingPresenter
import com.sdy.jitangapplication.presenter.view.DatingView
import com.sdy.jitangapplication.ui.activity.TagDetailCategoryActivity
import com.sdy.jitangapplication.ui.adapter.DatingSquareAdapter
import com.sdy.jitangapplication.ui.adapter.TodayWantAdapter
import com.sdy.jitangapplication.ui.dialog.TouristDialog
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.error_layout.view.*
import kotlinx.android.synthetic.main.fragment_dating.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.support.v4.startActivity

/**
 * 约会
 */
class DatingFragment : BaseMvpFragment<DatingPresenter>(), DatingView,
    OnRefreshListener {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadData()
    }

    fun loadData() {
        initView()
    }


    //广场列表内容适配器
    private val adapter by lazy { DatingSquareAdapter() }

    val layoutManager by lazy { LinearLayoutManager(activity!!, RecyclerView.VERTICAL, false) }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dating, container, false)
    }


    private fun initView() {
        EventBus.getDefault().register(this)
        mPresenter = DatingPresenter()
        mPresenter.mView = this
        mPresenter.context = activity!!

        refreshDatingSquare.setOnRefreshListener(this)

        stateDatingSquare.retryBtn.onClick {
            stateDatingSquare.viewState = MultiStateView.VIEW_STATE_LOADING
//            这个地方还要默认设置选中第一个兴趣来更新数据
            mPresenter.getSquareList()
        }

        initDatingHeaderView()
        rvDatingSquare.layoutManager = layoutManager
        rvDatingSquare.adapter = adapter
        adapter.setHeaderAndEmpty(false)
        adapter.setEmptyView(R.layout.empty_friend_layout, rvDatingSquare)
        adapter.isUseEmpty(false)
        adapter.bindToRecyclerView(rvDatingSquare)

        adapter.setOnItemClickListener { _, view, position ->
            if (UserManager.touristMode) {
                TouristDialog(activity!!).show()
            } else
                if (!ActivityUtils.isActivityExistsInStack(TagDetailCategoryActivity::class.java))
                    startActivity<TagDetailCategoryActivity>(
                        "id" to adapter.data[position].id,
                        "type" to TagDetailCategoryActivity.TYPE_TAG
                    )
        }

        adapter.setOnItemChildClickListener { _, view, position ->
            if (UserManager.touristMode) {
                TouristDialog(activity!!).show()
            } else
                when (view.id) {

                }
        }

        mPresenter.getSquareList()
        mPresenter.getIntention()

    }

    /**
     * 初始化顶部约会项目数据
     */
    private val datingProjectAdapter by lazy { TodayWantAdapter(true) }
    private fun initDatingHeaderView() {
        headDatingRv.layoutManager =
            LinearLayoutManager(activity!!, RecyclerView.HORIZONTAL, false)
        headDatingRv.adapter = datingProjectAdapter
        datingProjectAdapter.setOnItemClickListener { _, view, position ->
            checkPosi = position
            for (data in datingProjectAdapter.data) {
                data.checked = data == datingProjectAdapter.data[position]
            }
            datingProjectAdapter.notifyDataSetChanged()
        }
    }


    private var topPosition = -1


    override fun onRefresh(refreshTagSquare: RefreshLayout) {
        refreshTagSquare.setNoMoreData(false)
        mPresenter.getSquareList()

    }


    override fun onGetSquareTagResult(data: MutableList<SquareTagBean>?, result: Boolean) {
        if (result) {
            if (refreshDatingSquare.state == RefreshState.Refreshing) {
                adapter.data.clear()
                adapter.notifyDataSetChanged()
                rvDatingSquare.scrollToPosition(0)
            }
            stateDatingSquare.viewState = MultiStateView.VIEW_STATE_CONTENT

            for (datas in data ?: mutableListOf()) {
                if (UserManager.getGender() == 1) {
                    if (datas.is_hot)
                        adapter.addData(DatingBean(datas.icon, type = DatingBean.TYPE_MAN))
                    else
                        adapter.addData(DatingBean(datas.icon, type = DatingBean.TYPE_WOMAN))
                } else {
                    adapter.addData(DatingBean(datas.icon, type = DatingBean.TYPE_MAN))
                }
            }

        } else {
            stateDatingSquare.viewState = MultiStateView.VIEW_STATE_ERROR
            stateDatingSquare.errorMsg.text = if (mPresenter.checkNetWork()) {
                activity!!.getString(R.string.retry_load_error)
            } else {
                activity!!.getString(R.string.retry_net_error)
            }
            adapter.notifyDataSetChanged()
        }
        refreshDatingSquare.finishRefresh(result)
    }


    override fun onGetMarkTagResult(result: Boolean) {
        refreshDatingSquare.autoRefresh()
    }

    private var checkWantId = -1
    private var checkPosi = 0
    override fun onGetIntentionResult(result: MutableList<CheckBean>?) {
        if (!result.isNullOrEmpty()) {
            var hasCheck = false
            if (checkWantId != -1) {
                for (data in result!!.withIndex()) {
                    if (data.value.id == checkWantId) {
                        data.value.checked = true
                        checkPosi = data.index
                        hasCheck = true
                        break
                    }
                }
            }
            if (!hasCheck) {
                result!![0].checked = true
                checkPosi = 0
            }
            datingProjectAdapter.setNewData(result)
        } else {
            adapter.removeAllHeaderView()
        }
    }

    override fun showLoading() {
        stateDatingSquare.viewState = MultiStateView.VIEW_STATE_LOADING
    }


    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateFindByTagEvent(eve: UpdateFindByTagEvent) {
        refreshDatingSquare.autoRefresh()
    }

}
