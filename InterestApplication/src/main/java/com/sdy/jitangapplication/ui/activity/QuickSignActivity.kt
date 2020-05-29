package com.sdy.jitangapplication.ui.activity

import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.OnLazyClickListener
import com.sdy.jitangapplication.model.LabelQualityBean
import com.sdy.jitangapplication.presenter.ModelAboutMePresenter
import com.sdy.jitangapplication.presenter.view.ModelAboutMeView
import com.sdy.jitangapplication.ui.adapter.QuickSignAdapter
import kotlinx.android.synthetic.main.activity_quick_sign.*
import kotlinx.android.synthetic.main.layout_actionbar.*

/**
 * 快速签名
 */
class QuickSignActivity : BaseMvpActivity<ModelAboutMePresenter>(), ModelAboutMeView,
    OnLazyClickListener {
    private var page = 1
    private val adapter by lazy { QuickSignAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quick_sign)
        initView()
        mPresenter.getSignTemplate(page)
    }

    private fun initView() {
        mPresenter = ModelAboutMePresenter()
        mPresenter.context = this
        mPresenter.mView = this

        hotT1.text = "快速签名"
        btnBack.onClick {
            finish()
        }
        rightBtn1.text = "完成"
        rightBtn1.isVisible = true

        btnBack.setOnClickListener(this)
        rightBtn1.setOnClickListener(this)


        quickSignRv.layoutManager = LinearLayoutManager(this)
        quickSignRv.adapter = adapter
        adapter.setOnItemClickListener { _, view, position ->
            for (data in adapter.data.withIndex()) {
                if (data.index == position) {
                    data.value.cheked = !data.value.cheked
                } else
                    data.value.cheked = false
            }
            adapter.notifyDataSetChanged()
            checkCompleteEnable()
        }
    }


    /**
     * 检测完成按钮是否可用
     */
    private var checkPos = -1

    private fun checkCompleteEnable() {
        for (data in adapter.data.withIndex()) {
            if (data.value.cheked) {
                checkPos = data.index
                rightBtn1.isEnabled = true
                break
            } else {
                checkPos = -1
                rightBtn1.isEnabled = false
            }
        }
    }

    override fun onLazyClick(v: View) {
        when (v.id) {
            R.id.btnBack -> {
                finish()
            }
            R.id.rightBtn1 -> {
                setResult(
                    Activity.RESULT_OK,
                    intent.putExtra("quickSign", adapter.data[checkPos])
                )
                finish()
            }
        }

    }


    override fun getTagTraitInfoResult(b: Boolean, mutableList: MutableList<LabelQualityBean>?) {
        if (b) {
            adapter.addData(mutableList ?: mutableListOf())
        }
    }
}
