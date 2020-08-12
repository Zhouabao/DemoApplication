package com.sdy.jitangapplication.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.SizeUtils
import com.kennyc.view.MultiStateView
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.fragment.BaseMvpFragment
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.constant.RefreshState
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.event.UpdateFindByTagEvent
import com.sdy.jitangapplication.model.SquareTagBean
import com.sdy.jitangapplication.presenter.TagSquarePresenter
import com.sdy.jitangapplication.presenter.view.TagSquareView
import com.sdy.jitangapplication.ui.activity.TagDetailCategoryActivity
import com.sdy.jitangapplication.ui.adapter.TagSquareAdapter
import com.sdy.jitangapplication.ui.dialog.TouristDialog
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.error_layout.view.*
import kotlinx.android.synthetic.main.fragment_tag_square.*
import kotlinx.android.synthetic.main.popupwindow_square_filter_tag_top.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.support.v4.startActivity

/**
 * 兴趣广场
 */
class TagSquareFragment : BaseMvpFragment<TagSquarePresenter>(), TagSquareView,
    OnRefreshListener {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadData()
    }
    fun loadData() {
        initView()
    }

    private val filterPopupWindow by lazy {
        PopupWindow(activity!!).apply {
            contentView =
                LayoutInflater.from(activity!!)
                    .inflate(R.layout.popupwindow_square_filter_tag_top, null, false)
            width = ViewGroup.LayoutParams.WRAP_CONTENT
            height = ViewGroup.LayoutParams.WRAP_CONTENT
            setBackgroundDrawable(null)
            isFocusable = true
            isOutsideTouchable = true
            contentView.tagTop.onClick {
                //位置类型 0 没有操作 1置顶 2置底
                mPresenter.markTag(
                    adapter.data[topPosition].id,
                    if (adapter.data[topPosition].place_type == 1) {
                        0
                    } else {
                        1
                    }
                )
                dismiss()

            }
            contentView.tagBottom.onClick {
                mPresenter.markTag(
                    adapter.data[topPosition].id, if (adapter.data[topPosition].place_type == 2) {
                        0
                    } else {
                        2
                    }
                )
                dismiss()

            }
        }
    }

    //广场列表内容适配器
    private val adapter by lazy { TagSquareAdapter() }

    val layoutManager by lazy { LinearLayoutManager(activity!!, RecyclerView.VERTICAL, false) }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tag_square, container, false)
    }


    private fun initView() {
        EventBus.getDefault().register(this)
        mPresenter = TagSquarePresenter()
        mPresenter.mView = this
        mPresenter.context = activity!!

        refreshTagSquare.setOnRefreshListener(this)

        stateTagSquare.retryBtn.onClick {
            stateTagSquare.viewState = MultiStateView.VIEW_STATE_LOADING
//            这个地方还要默认设置选中第一个兴趣来更新数据
            mPresenter.getSquareList()
        }

        rvTagSquare.layoutManager = layoutManager
        rvTagSquare.adapter = adapter
        adapter.setHeaderAndEmpty(false)
        adapter.setEmptyView(R.layout.empty_friend_layout, rvTagSquare)
        adapter.isUseEmpty(false)
        adapter.bindToRecyclerView(rvTagSquare)

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
                    R.id.btnTagMore -> {
                        topPosition = position
                        filterPopupWindow.showAsDropDown(view, 0, SizeUtils.dp2px(-15F))
                        filterPopupWindow.contentView.tagTop.text =
                            if (adapter.data[position].place_type == 1) {
                                "取消置顶"
                            } else {
                                "置于顶部"
                            }
                        filterPopupWindow.contentView.tagBottom.text =
                            if (adapter.data[position].place_type == 2) {
                                "取消置底"
                            } else {
                                "置于底部"
                            }
                    }
                    R.id.rvTagSquareImg -> {
                        if (!ActivityUtils.isActivityExistsInStack(TagDetailCategoryActivity::class.java))
                            startActivity<TagDetailCategoryActivity>(
                                "id" to adapter.data[position].id,
                                "type" to TagDetailCategoryActivity.TYPE_TAG
                            )
                    }
                }
        }

        mPresenter.getSquareList()

    }

    private var topPosition = -1


    override fun onRefresh(refreshTagSquare: RefreshLayout) {
        refreshTagSquare.setNoMoreData(false)
        mPresenter.getSquareList()

    }


    override fun onGetSquareTagResult(data: MutableList<SquareTagBean>?, result: Boolean) {
        if (result) {
            if (refreshTagSquare.state == RefreshState.Refreshing) {
                adapter.data.clear()
                adapter.notifyDataSetChanged()
                rvTagSquare.scrollToPosition(0)
            }
            stateTagSquare.viewState = MultiStateView.VIEW_STATE_CONTENT
            adapter.addData(data ?: mutableListOf())

        } else {
            stateTagSquare.viewState = MultiStateView.VIEW_STATE_ERROR
            stateTagSquare.errorMsg.text = if (mPresenter.checkNetWork()) {
                activity!!.getString(R.string.retry_load_error)
            } else {
                activity!!.getString(R.string.retry_net_error)
            }
            adapter.notifyDataSetChanged()
        }
        refreshTagSquare.finishRefresh(result)
    }


    override fun onGetMarkTagResult(result: Boolean) {
        refreshTagSquare.autoRefresh()
    }

    override fun showLoading() {
        stateTagSquare.viewState = MultiStateView.VIEW_STATE_LOADING
    }


    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateFindByTagEvent(eve: UpdateFindByTagEvent) {
        refreshTagSquare.autoRefresh()
    }

}
