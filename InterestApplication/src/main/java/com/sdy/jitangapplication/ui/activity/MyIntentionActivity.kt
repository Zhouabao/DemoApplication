package com.sdy.jitangapplication.ui.activity

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.SizeUtils
import com.kennyc.view.MultiStateView
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.baselibrary.utils.CustomClickListener
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.model.LabelQualityBean
import com.sdy.jitangapplication.presenter.MyIntentionPresenter
import com.sdy.jitangapplication.presenter.view.MyIntentionView
import com.sdy.jitangapplication.ui.adapter.MyIntentionAdapter
import com.sdy.jitangapplication.utils.UserManager
import com.sdy.jitangapplication.widgets.DividerItemDecoration
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


        BarUtils.setStatusBarLightMode(this, false)
        val params = titleView.layoutParams as ConstraintLayout.LayoutParams
        params.topMargin = BarUtils.getStatusBarHeight()
        titleView.layoutParams = params

        btnBack.onClick {
            finish()
        }
        titleView.setBackgroundColor(Color.TRANSPARENT)
        hotT1.isVisible = false
        divider.isVisible = false
        btnBack.setImageResource(R.drawable.icon_back_white)
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

        GlideUtil.loadCircleImg(this, UserManager.getAvator(), userAvator)

        stateIntention.retryBtn.onClick {
            stateIntention.viewState = MultiStateView.VIEW_STATE_LOADING
            mPresenter.getIntentionList()
        }

        intentionRv.layoutManager = GridLayoutManager(this, 2, RecyclerView.VERTICAL, false)
        intentionRv.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.BOTH_SET,
                SizeUtils.dp2px(15F),
                Color.TRANSPARENT
            )
        )
        intentionRv.adapter = adapter

        adapter.setOnItemClickListener { _, view, position ->
            for (data in adapter.data.withIndex()) {
                if (data.index == position) {
                    data.value.isfuse = !data.value.isfuse
                    rightBtn1.isEnabled = data.value.isfuse
                    if (data.value.isfuse) {
                        GlideUtil.loadImg(this, data.value.icon, intentionIcon)
                    } else {
                        GlideUtil.loadImg(this, R.drawable.icon_matching_default, intentionIcon)
                    }
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


        switchDistance.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                switchDistanceContentOpen.isVisible = true
                switchDistanceContentClose.isVisible = false
            } else {
                switchDistanceContentOpen.isVisible = false
                switchDistanceContentClose.isVisible = true
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
                    GlideUtil.loadImg(this, data1.icon, intentionIcon)
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
