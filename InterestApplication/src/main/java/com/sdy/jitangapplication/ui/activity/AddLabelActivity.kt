package com.sdy.jitangapplication.ui.activity

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SizeUtils
import com.google.gson.Gson
import com.kennyc.view.MultiStateView
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.event.RefreshEvent
import com.sdy.jitangapplication.event.UpdateMyLabelEvent
import com.sdy.jitangapplication.event.UserCenterEvent
import com.sdy.jitangapplication.model.AddLabelBean
import com.sdy.jitangapplication.model.TagBean
import com.sdy.jitangapplication.presenter.AddLabelPresenter
import com.sdy.jitangapplication.presenter.view.AddLabelView
import com.sdy.jitangapplication.ui.adapter.AddLabelAdapter
import com.sdy.jitangapplication.ui.adapter.MatchLabelAdapter
import com.sdy.jitangapplication.utils.UserManager
import com.sdy.jitangapplication.widgets.CenterLayoutManager
import com.sdy.jitangapplication.widgets.DividerItemDecoration
import kotlinx.android.synthetic.main.activity_add_label.*
import kotlinx.android.synthetic.main.error_layout.view.*
import kotlinx.android.synthetic.main.layout_actionbar.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.startActivity

/**
 * 添加兴趣activity
 */
class AddLabelActivity : BaseMvpActivity<AddLabelPresenter>(), AddLabelView, View.OnClickListener {

    companion object {
        const val FROM_REGISTER = 1//注册流程进入
        const val FROM_ADD_NEW = 3//主页添加新兴趣
    }

    private val from by lazy { intent.getIntExtra("from", FROM_ADD_NEW) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_label)
        initView()
        mPresenter.tagClassifyList(
            if (from == FROM_REGISTER) {
                1
            } else {
                2
            }
        )
    }

    //兴趣标题适配器
    private val labelMenuAdapter: MatchLabelAdapter by lazy { MatchLabelAdapter(this) }
    //兴趣列表适配器
    private val labelListAdapter: AddLabelAdapter by lazy { AddLabelAdapter() }

    private var menuClick = false
    private fun initView() {

        mPresenter = AddLabelPresenter()
        mPresenter.context = this
        mPresenter.mView = this


        setSwipeBackEnable(from != FROM_REGISTER)
        btnBack.isVisible = from != FROM_REGISTER
        hotT1.text = "选择兴趣"

        rightBtn1.isEnabled = true
        rightBtn1.text = "保存"
        rightBtn1.isVisible = true


        divider.isVisible = false
        btnBack.setOnClickListener(this)
        rightBtn1.setOnClickListener(this)
        stateAddLabel.retryBtn.onClick {
            stateAddLabel.viewState = MultiStateView.VIEW_STATE_LOADING
            mPresenter.tagClassifyList(
                if (from == FROM_REGISTER) {
                    1
                } else {
                    2
                }
            )
        }

        //兴趣种类
        labelClassRv.layoutManager = CenterLayoutManager(this, RecyclerView.HORIZONTAL, false)
        LinearSnapHelper().attachToRecyclerView(labelClassRv)
        labelClassRv.adapter = labelMenuAdapter

        //兴趣列表
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
        labelListAdapter.setOnItemChildClickListener { _, view, position ->
            when (view.id) {
                R.id.labelManagerBtn -> {
                    startActivity<MyLabelActivity>()
                }
            }
        }


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
            (labelClassRv.layoutManager as CenterLayoutManager).smoothScrollToPosition(
                labelClassRv,
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
                    (labelClassRv.layoutManager as CenterLayoutManager).smoothScrollToPosition(
                        labelClassRv,
                        RecyclerView.State(),
                        first
                    )
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
            rightBtn1 -> {
                val tag_ids = mutableListOf<Int>()
                for (index in labelListAdapter.data) {
                    if (!(index.ishot || index.ismine))
                        for (tdata in index.son) {
                            if (tdata.checked) {
                                tag_ids.add(tdata.id)
                            }
                        }
                }
                if (tag_ids.isNotEmpty()) {
                    mPresenter.saveInterestTag(Gson().toJson(tag_ids))
                } else {
                    CommonFunction.toast("暂无选中的兴趣可保存")
                }
            }
        }

    }

    override fun onTagClassifyListResult(result: Boolean, data: AddLabelBean?) {
        if (result) {
            stateAddLabel.viewState = MultiStateView.VIEW_STATE_CONTENT
            UserManager.saveMaxMyLabelCount(data!!.limit_count)
            if (data!!.menu.isNotEmpty()) {
                data.menu[0].checked = true
            }
            labelMenuAdapter.setNewData(data.menu)

            for (data1 in data.list) {
                for (data2 in data1.son) {
                    if (data2.state == 2 || data2.state == 10) {
                        data2.checked = true
                    }
                }
            }
            labelListAdapter.setNewData(data.list)

        } else {
            stateAddLabel.viewState = MultiStateView.VIEW_STATE_ERROR
        }
    }

    override fun saveMyTagResult(result: Boolean, data: MutableList<TagBean>?) {
        if (result) {
            EventBus.getDefault().post(RefreshEvent(true))
            EventBus.getDefault().post(UpdateMyLabelEvent())
            EventBus.getDefault().post(UserCenterEvent(true))
            if (from == FROM_REGISTER) {
                startActivity<MainActivity>()
                finish()
            }
            finish()
        }

    }

    override fun onBackPressed() {
        if (from != FROM_REGISTER)
            super.onBackPressed()
    }




}
