package com.sdy.jitangapplication.ui.activity

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SizeUtils
import com.kennyc.view.MultiStateView
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.AddLabelBean
import com.sdy.jitangapplication.model.MyLabelBean
import com.sdy.jitangapplication.presenter.AddLabelPresenter
import com.sdy.jitangapplication.presenter.view.AddLabelView
import com.sdy.jitangapplication.ui.adapter.AddLabelAdapter
import com.sdy.jitangapplication.ui.adapter.MatchLabelAdapter
import com.sdy.jitangapplication.widgets.CenterLayoutManager
import com.sdy.jitangapplication.widgets.DividerItemDecoration
import kotlinx.android.synthetic.main.activity_add_label.*
import kotlinx.android.synthetic.main.error_layout.view.*
import kotlinx.android.synthetic.main.layout_actionbar.*

/**
 * 添加标签activity
 */
class AddLabelActivity : BaseMvpActivity<AddLabelPresenter>(), AddLabelView, View.OnClickListener {
    companion object {
        const val FROM_REGISTER = 1//注册流程进入
        const val FROM_EDIT = 2//主页编辑已有标签
        const val FROM_ADD_NEW = 3//主页添加新标签
        const val FROM_PUBLISH = 4//发布界面进入
        const val FROM_USERCENTER = 5//个人中心进入
    }

    private val from by lazy { intent.getIntExtra("from", FROM_EDIT) }
    private var usingLabels: MutableList<MyLabelBean> = mutableListOf()
    private var removedLabels: MutableList<MyLabelBean> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_label)
        initView()
        mPresenter.tagClassifyList()
    }

    //标签标题适配器
    private val labelMenuAdapter: MatchLabelAdapter by lazy { MatchLabelAdapter(this) }
    //标签列表适配器
    private val labelListAdapter: AddLabelAdapter by lazy { AddLabelAdapter() }

    private var menuClick = false
    private fun initView() {
        mPresenter = AddLabelPresenter()
        mPresenter.context = this
        mPresenter.mView = this

        if (intent.getSerializableExtra("is_using") != null) {
            usingLabels = intent.getSerializableExtra("is_using") as MutableList<MyLabelBean>
        }
        if (intent.getSerializableExtra("is_removed") != null) {
            removedLabels = intent.getSerializableExtra("is_removed") as MutableList<MyLabelBean>
            labelListAdapter.removedLabels = removedLabels
        }


        setSwipeBackEnable(from != FROM_REGISTER)

        hotT1.text = "添加你的兴趣"
        divider.isVisible = false
        btnBack.isVisible = from != FROM_REGISTER
        btnBack.setOnClickListener(this)

        stateAddLabel.retryBtn.onClick {
            stateAddLabel.viewState = MultiStateView.VIEW_STATE_LOADING
            mPresenter.tagClassifyList()
        }

        //标签种类
        labelClassRv.layoutManager = CenterLayoutManager(this, RecyclerView.HORIZONTAL, false)
        LinearSnapHelper().attachToRecyclerView(labelClassRv)
        labelClassRv.adapter = labelMenuAdapter

        //标签列表
        labelsRv.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        labelsRv.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.HORIZONTAL_LIST,
                SizeUtils.dp2px(10F),
                resources.getColor(R.color.colorWhite)
            )
        )
        labelsRv.adapter = labelListAdapter
        labelListAdapter.from = from


        labelMenuAdapter.setOnItemClickListener { _, view, position ->
            menuClick = true
            for (data in labelMenuAdapter.data.withIndex()) {
                data.value.checked = data.index == position
            }
            labelMenuAdapter.notifyDataSetChanged()

            (labelsRv.layoutManager as LinearLayoutManager).smoothScrollToPosition(
                labelsRv,
                RecyclerView.State(),
                position
            )
        }

        labelsRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE)
                    menuClick = false
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!menuClick) {
                    val first = (labelsRv.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                    for (data in labelMenuAdapter.data.withIndex()) {
                        data.value.checked = data.index == first
                    }
                    labelMenuAdapter.notifyDataSetChanged()
                }
            }
        })
    }


    override fun onClick(view: View) {
        when (view) {
            btnBack -> {
                finish()
            }
        }

    }

    override fun onTagClassifyListResult(result: Boolean, data: AddLabelBean?) {
        if (result) {
            stateAddLabel.viewState = MultiStateView.VIEW_STATE_CONTENT
            if (data!!.menu.isNotEmpty()) {
                data.menu[0].checked = true
            }
            labelMenuAdapter.setNewData(data.menu)

            //遍历所有，标记已选
            for (tdata in data.list) {
                for (tdata1 in tdata.son) {
                    for (tdata2 in usingLabels)
                        if (tdata1.id == tdata2.tag_id) {
                            tdata1.checked = true
                        }
                }
            }

            //遍历所有,标记已删除
            for (tdata in data.list) {
                for (tdata1 in tdata.son) {
                    for (tdata2 in removedLabels)
                        if (tdata1.id == tdata2.tag_id) {
                            tdata1.removed = true
                        }
                }
            }

            labelListAdapter.setNewData(data.list)

        } else {
            stateAddLabel.viewState = MultiStateView.VIEW_STATE_ERROR
        }
    }
}
