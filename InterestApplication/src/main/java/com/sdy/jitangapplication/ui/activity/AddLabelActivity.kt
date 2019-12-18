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
import com.sdy.jitangapplication.event.ShowCompleteLabelEvent
import com.sdy.jitangapplication.event.UpdateEditModeEvent
import com.sdy.jitangapplication.event.UpdateMyInterestLabelEvent
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
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.startActivity

/**
 * 添加标签activity
 */
class AddLabelActivity : BaseMvpActivity<AddLabelPresenter>(), AddLabelView, View.OnClickListener {

    companion object {
        const val FROM_REGISTER = 1//注册流程进入
        const val FROM_INTERSERT_LABEL = 6//我感兴趣的
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
        mPresenter.tagClassifyList(
            if (from == AddLabelActivity.FROM_REGISTER || from == AddLabelActivity.FROM_INTERSERT_LABEL) {
                1
            } else {
                2
            }
        )
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
        btnBack.isVisible = from != FROM_REGISTER
        hotT1.text = if (from == FROM_REGISTER || from == FROM_INTERSERT_LABEL) {
            "想认识的人"
        } else {
            "添加你的标签"
        }
        rightBtn1.isEnabled = true
        rightBtn1.isVisible = (from == FROM_REGISTER || from == FROM_INTERSERT_LABEL)
        rightBtn1.text = if (from == FROM_REGISTER) {
            "下一步"
        } else {
            "保存"
        }

        divider.isVisible = false
        btnBack.setOnClickListener(this)
        rightBtn1.setOnClickListener(this)

        stateAddLabel.retryBtn.onClick {
            stateAddLabel.viewState = MultiStateView.VIEW_STATE_LOADING
            mPresenter.tagClassifyList(
                if (from == AddLabelActivity.FROM_REGISTER || from == AddLabelActivity.FROM_INTERSERT_LABEL) {
                    1
                } else {
                    2
                }
            )
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
            rightBtn1 -> {
                val tag_ids = mutableListOf<Int>()
                for (data in labelListAdapter.data) {
                    for (tdata in data.son) {
                        if (tdata.checked) {
                            tag_ids.add(tdata.id)
                        }
                    }
                }
                if (tag_ids.isNotEmpty())
                    mPresenter.saveInterestTag(Gson().toJson(tag_ids))
                else
                    CommonFunction.toast("暂无选中的标签可保存")
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
                    if (tdata1.state == 1)
                        tdata1.checked = true
                }
            }

            //遍历所有,标记已删除
            for (tdata in data.list) {
                for (tdata1 in tdata.son) {
                    for (tdata2 in removedLabels)
                        if (tdata1.id == tdata2.tag_id) {
                            tdata1.removed = true
                        }
                    if (tdata1.state == 2)
                        tdata1.removed = true
                }
            }

            labelListAdapter.setNewData(data.list)

        } else {
            stateAddLabel.viewState = MultiStateView.VIEW_STATE_ERROR
        }
    }

    override fun saveInterestTagResult(result: Boolean) {
        if (result) {
            if (from == FROM_REGISTER) {
                startActivity<UserIntroduceActivity>("from" to UserIntroduceActivity.REGISTER)
            } else if (from == FROM_INTERSERT_LABEL) {
                EventBus.getDefault().post(UpdateMyInterestLabelEvent())
            } else {
                EventBus.getDefault().post(UpdateEditModeEvent(MyLabelActivity.MY_LABEL))
                EventBus.getDefault().post(ShowCompleteLabelEvent(false))
            }
            finish()
        }

    }

}
