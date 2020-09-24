package com.sdy.jitangapplication.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.fragment.BaseMvpFragment
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.event.DatingOnePlayEvent
import com.sdy.jitangapplication.event.DatingStopPlayEvent
import com.sdy.jitangapplication.event.UpdateMyDatingEvent
import com.sdy.jitangapplication.model.DatingBean
import com.sdy.jitangapplication.presenter.MyDatingPresenter
import com.sdy.jitangapplication.presenter.view.MyDatingView
import com.sdy.jitangapplication.ui.activity.DatingDetailActivity
import com.sdy.jitangapplication.ui.adapter.DatingSquareAdapter
import kotlinx.android.synthetic.main.empty_my_square_layout.view.*
import kotlinx.android.synthetic.main.fragment_my_tag.*
import kotlinx.android.synthetic.main.headerview_user_center_square.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 我的约会记录
 */
class MyDatingFragment : BaseMvpFragment<MyDatingPresenter>(), MyDatingView, OnRefreshListener {
    private val datingSquareAdapter by lazy { DatingSquareAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_tag, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadData()
    }

    fun loadData() {
        initView()
        mPresenter.myDating()
    }

    private fun initView() {
        mPresenter = MyDatingPresenter()
        mPresenter.context = activity!!
        mPresenter.mView = this
        EventBus.getDefault().register(this)

        refreshMyDating.setEnableRefresh(true)
        refreshMyDating.setOnRefreshListener(this)


        //用户标签
        val tagManager = LinearLayoutManager(activity!!,RecyclerView.VERTICAL,false)
        rvMyTag.layoutManager = tagManager
        rvMyTag.adapter = datingSquareAdapter
        //android 瀑布流
        datingSquareAdapter.setHeaderAndEmpty(false)
        datingSquareAdapter.setEmptyView(R.layout.empty_my_square_layout, rvMyTag)
        datingSquareAdapter.emptyView.emptyPublishBtn.text = "发布活动"
        datingSquareAdapter.emptyView.emptyImg.setImageResource(R.drawable.icon_empty_my_square)
        datingSquareAdapter.emptyView.emptyMySquareTip.text = "您还没有发布过活动\n快发布你的第一次活动吧"
        datingSquareAdapter.emptyView.emptyPublishBtn.onClick {
            CommonFunction.checkPublishDating(activity!!)
        }

        datingSquareAdapter.setOnItemClickListener { _, view, position ->
            DatingDetailActivity.start2Detail(activity!!, datingSquareAdapter.data[position].id)
        }
    }

    fun setTagData(datas: MutableList<DatingBean>) {
        datingSquareAdapter.setNewData(datas)
        if (datingSquareAdapter.data.size == 0) {
            datingSquareAdapter.isUseEmpty(true)
            refreshMyDating.finishLoadMoreWithNoMoreData()
        } else {
            datingSquareAdapter.removeAllHeaderView()
            datingSquareAdapter.addHeaderView(initHeadDating())
        }
        refreshMyDating.finishRefresh()

    }


    /**
     *头部banner
     */
    private fun initHeadDating(): View {
        val headDating = LayoutInflater.from(activity!!)
            .inflate(R.layout.headerview_user_center_square, rvMyTag, false)
        headDating.publishImg.setImageResource(R.drawable.icon_update_dating)
        headDating.publishBtn.text = "更新活动"
        headDating.publishCl.onClick {
            CommonFunction.checkPublishDating(activity!!)
        }
        return headDating
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateMyDatingEvent(event: UpdateMyDatingEvent) {
        refreshMyDating.autoRefresh()
    }

    override fun onGetMyDatingResult(data: MutableList<DatingBean>?) {
        setTagData(data ?: mutableListOf())
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        refreshLayout.resetNoMoreData()
        mPresenter.myDating()
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateFindByTagEvent(eve: DatingStopPlayEvent) {
        datingSquareAdapter.resetMyAudioViews()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDatingOnePlayEvent(eve: DatingOnePlayEvent) {
        datingSquareAdapter.notifySomeOneAudioView(eve.positionId)
    }

    override fun onStop() {
        super.onStop()
        datingSquareAdapter.resetMyAudioViews()
    }

    override fun onPause() {
        super.onPause()
        datingSquareAdapter.resetMyAudioViews()
    }

}
