package com.sdy.jitangapplication.ui.activity

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kennyc.view.MultiStateView
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.sdy.jitangapplication.R
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
        rightBtn.setOnClickListener(this)
        hotT1.text = getString(R.string.label_publish_someone)
        rightBtn.isVisible = true
        rightBtn.text = getString(R.string.skip)
        rightBtn.textSize = 17F
        rightBtn.setTextColor(Color.parseColor("#FFE1E1E3"))

        stateChooseLabel.retryBtn.onClick {
            stateChooseLabel.viewState = MultiStateView.VIEW_STATE_LOADING
            mPresenter.getSquareTag()
        }

        rvMyLabels.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        rvMyLabels.adapter = adapter
        adapter.setOnItemClickListener { _, view, position ->
            val dataChecked = adapter.data[position].checked
            if (!dataChecked) {
                rightBtn.setTextColor(Color.parseColor("#FFFF6318"))
                rightBtn.text = getString(R.string.publish)
                mylabelBean = adapter.data[position]
            } else {
                rightBtn.setTextColor(Color.parseColor("#FFE1E1E3"))
                rightBtn.text = getString(R.string.skip)
                mylabelBean = null
            }
//            rightBtn
            for (label in adapter.data) {
                if (label.id == adapter.data[position].id)
                    label.checked = !dataChecked
                else
                    label.checked = false
            }
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
                        adapter.addData(SquareLabelBean(title = getString(R.string.label_frequently_use), type = SquareLabelBean.TITLE))
                        for (data in datas.used_list) {
                            data.type = SquareLabelBean.CONTENT
                        }
                        adapter.addData(datas.used_list)
                    }
                    adapter.addData(SquareLabelBean(title = getString(R.string.label_all), type = SquareLabelBean.TITLE))
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

            rightBtn -> {
                if (mylabelBean != null)
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
