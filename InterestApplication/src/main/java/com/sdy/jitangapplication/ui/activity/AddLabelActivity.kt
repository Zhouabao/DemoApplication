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
        const val FROM_REGISTER = 1
        const val FROM_EDIT = 2
        const val FROM_ADD_NEW = 3
        const val FROM_PUBLISH = 4
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
        labelListAdapter.from = intent.getIntExtra("from", FROM_EDIT)


        labelMenuAdapter.setOnItemClickListener { _, view, position ->
            for (data in labelMenuAdapter.data.withIndex()) {
                if (data.value.checked && data.index != position) {
                    data.value.checked = false
                    labelMenuAdapter.notifyItemChanged(data.index)
                }
                if (data.index == position) {
                    data.value.checked = true
                    labelMenuAdapter.notifyItemChanged(data.index)
                }
            }


            (labelsRv.layoutManager as LinearLayoutManager).smoothScrollToPosition(
                labelsRv,
                RecyclerView.State(),
                position
            )

            (labelClassRv.layoutManager as CenterLayoutManager).smoothScrollToPosition(
                labelClassRv,
                RecyclerView.State(),
                position
            )
        }
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
