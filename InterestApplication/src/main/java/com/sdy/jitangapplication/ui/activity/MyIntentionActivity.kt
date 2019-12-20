package com.sdy.jitangapplication.ui.activity

import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kennyc.view.MultiStateView
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.sdy.baselibrary.utils.CustomClickListener
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.model.LabelQualityBean
import com.sdy.jitangapplication.presenter.MyIntentionPresenter
import com.sdy.jitangapplication.presenter.view.MyIntentionView
import com.sdy.jitangapplication.ui.adapter.MyIntentionAdapter
import kotlinx.android.synthetic.main.activity_my_intention.*
import kotlinx.android.synthetic.main.error_layout.view.*
import kotlinx.android.synthetic.main.layout_actionbar.*

/**
 * 我的意愿
 */
class MyIntentionActivity : BaseMvpActivity<MyIntentionPresenter>(), MyIntentionView {
    companion object {
        const val FROM_REGISTER = 1
        const val FROM_USERCENTER = 2
    }

    private val adapter by lazy { MyIntentionAdapter() }
    private val checkedId by lazy { intent.getIntExtra("id", -1) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_intention)
        initView()
        mPresenter.getIntentionList()
    }

    private fun initView() {
        mPresenter = MyIntentionPresenter()
        mPresenter.mView = this
        mPresenter.context = this

        hotT1.text = "我的意向"
        btnBack.onClick {
            finish()
        }
        rightBtn1.isVisible = intent.getIntExtra("from", FROM_USERCENTER) != FROM_REGISTER
        rightBtn1.text = "保存"
        rightBtn1.isEnabled = false
        rightBtn1.onClick(object : CustomClickListener() {
            override fun onSingleClick(view: View) {
                if (getCheckedIntentionId() == null) {
                    CommonFunction.toast("暂无可保存项")
                    return
                }

                if (intent.getIntExtra("from", FROM_USERCENTER) == FROM_REGISTER) {
                    if (getCheckedIntention() != null)
                        intent.putExtra("intention", getCheckedIntention())
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                } else
                    mPresenter.saveRegisterInfo(intention_id = getCheckedIntentionId())
            }


        })

        stateIntention.retryBtn.onClick {
            stateIntention.viewState = MultiStateView.VIEW_STATE_LOADING
            mPresenter.getIntentionList()
        }

        intentionRv.layoutManager = GridLayoutManager(this, 3, RecyclerView.VERTICAL, false)
        intentionRv.adapter = adapter

        adapter.setOnItemClickListener { _, view, position ->
            for (data in adapter.data.withIndex()) {
                if (data.index == position) {
                    data.value.isfuse = !data.value.isfuse
                    rightBtn1.isEnabled = data.value.isfuse
                } else {
                    data.value.isfuse = false
                }
            }
            adapter.notifyDataSetChanged()
            if (intent.getIntExtra("from", FROM_USERCENTER) == FROM_REGISTER) {
                if (getCheckedIntention() != null) {
                    intent.putExtra("intention", getCheckedIntention())
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
            }
        }
    }

    private fun getCheckedIntentionId(): Int? {
        var intention_id: Int? = null
        for (data in adapter.data) {
            if (data.isfuse) {
                intention_id = data.id
                break
            }
        }
        return intention_id
    }

    private fun getCheckedIntention(): LabelQualityBean? {
        var intention_id: LabelQualityBean? = null
        for (data in adapter.data) {
            if (data.isfuse) {
                intention_id = data
                break
            }
        }
        return intention_id
    }

    override fun onGetIntentionListResult(data: MutableList<LabelQualityBean>?) {
        if (data == null) {
            stateIntention.viewState = MultiStateView.VIEW_STATE_ERROR
        } else {
            stateIntention.viewState = MultiStateView.VIEW_STATE_CONTENT
            for (data1 in data) {
                if (data1.id == checkedId) {
                    data1.isfuse = true
                }
            }
            adapter.addData(data)
        }
    }

    override fun onSaveRegisterInfo(success: Boolean) {
        if (success) {
            CommonFunction.toast("保存成功!")
            intent.putExtra("intention", getCheckedIntention())
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

}