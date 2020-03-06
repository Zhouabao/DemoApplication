package com.sdy.jitangapplication.ui.activity

import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kennyc.view.MultiStateView
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.model.SquareLabelBean
import com.sdy.jitangapplication.model.SquareLabelsBean
import com.sdy.jitangapplication.presenter.ChooseLabelPresenter
import com.sdy.jitangapplication.presenter.view.ChooseLabelView
import com.sdy.jitangapplication.ui.adapter.ChooseLabelAdapter
import kotlinx.android.synthetic.main.activity_choose_label.*
import kotlinx.android.synthetic.main.error_layout.view.*
import kotlinx.android.synthetic.main.layout_actionbar.*

/**
 * 发布选择兴趣
 */
class ChooseLabelActivity : BaseMvpActivity<ChooseLabelPresenter>(), ChooseLabelView, View.OnClickListener {
    private val adapter by lazy { ChooseLabelAdapter() }
    private var mylabelBean: SquareLabelBean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_label)

        initView()
        mPresenter.getSquareTag()
    }

    private fun initView() {
        mPresenter = ChooseLabelPresenter()
        mPresenter.mView = this
        mPresenter.context = this

        btnBack.setOnClickListener(this)
        rightBtn1.setOnClickListener(this)
        hotT1.text = "发布到哪个兴趣"
        rightBtn1.isVisible = true
        rightBtn1.text = "发布"
        rightBtn1.isEnabled = false

        stateChooseLabel.retryBtn.onClick {
            stateChooseLabel.viewState = MultiStateView.VIEW_STATE_LOADING
            mPresenter.getSquareTag()
        }

        rvMyLabels.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        rvMyLabels.adapter = adapter
        adapter.setOnItemClickListener { _, view, position ->
            for (label in adapter.data) {
                label.checked = label == adapter.data[position]
            }
            mylabelBean = adapter.data[position]
            rightBtn1.isEnabled = true
            adapter.notifyDataSetChanged()
        }
    }

    override fun getSquareTagListResult(result: Boolean, datas: SquareLabelsBean?) {
        if (result) {
            stateChooseLabel.viewState = MultiStateView.VIEW_STATE_CONTENT

            if (datas != null) {
                if (datas.all_list.isNullOrEmpty() && datas.used_list.isNullOrEmpty()) {
                    stateChooseLabel.viewState = MultiStateView.VIEW_STATE_EMPTY
                } else {
                    if (!datas.used_list.isNullOrEmpty()) {
                        adapter.addData(SquareLabelBean(title = "常用兴趣", type = SquareLabelBean.TITLE))
                        for (data in datas.used_list) {
                            data.type = SquareLabelBean.CONTENT
                        }
                        adapter.addData(datas.used_list)
                    }
                    adapter.addData(SquareLabelBean(title = "全部兴趣", type = SquareLabelBean.TITLE))
                    for (data in datas.all_list) {
                        data.type = SquareLabelBean.CONTENT
                    }
                    adapter.addData(datas.all_list)
                }
            }
        } else {
            stateChooseLabel.viewState = MultiStateView.VIEW_STATE_ERROR
        }
    }

    override fun onClick(v: View) {
        when (v) {
            btnBack -> {
                finish()
            }

            rightBtn1 -> {
                if (mylabelBean == null) {
                    CommonFunction.toast("兴趣为必选项")
                    return
                }
                intent.putExtra("label", mylabelBean)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
    }


}
