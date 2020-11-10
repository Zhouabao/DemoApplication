package com.sdy.jitangapplication.ui.activity

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kennyc.view.MultiStateView
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.model.DatingOptionsBean
import com.sdy.jitangapplication.presenter.ChooseDatingTypePresenter
import com.sdy.jitangapplication.presenter.view.ChooseDatingTypeView
import com.sdy.jitangapplication.ui.adapter.TodayWantAdapter
import kotlinx.android.synthetic.main.activity_choose_dating_type.*
import kotlinx.android.synthetic.main.error_layout.*
import kotlinx.android.synthetic.main.layout_actionbar.*
import org.jetbrains.anko.startActivity

/**
 * 选择约会类型
 */
class ChooseDatingTypeActivity : BaseMvpActivity<ChooseDatingTypePresenter>(),
    ChooseDatingTypeView {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_dating_type)

        initView()
        mPresenter.getIntention()
    }

    private val adapter by lazy {
        TodayWantAdapter()
    }

    private var checkWantId = -1

    /**
     * 添加今日意向
     */
    private var checkPosi = -1

    private fun initView() {
        mPresenter = ChooseDatingTypePresenter()
        mPresenter.context = this
        mPresenter.mView = this

        hotT1.text = getString(R.string.dating_title)
        btnBack.clickWithTrigger {
            finish()
        }
        rightBtn1.isVisible = true
        rightBtn1.text = getString(R.string.next)

        retryBtn.clickWithTrigger {
            stateDatingType.viewState = MultiStateView.VIEW_STATE_LOADING
            mPresenter.getIntention()
        }

        rightBtn1.clickWithTrigger {
            if (checkPosi != -1)
                startActivity<CompleteDatingInfoActivity>("dating_type" to adapter.data[checkPosi])
        }


        todayWantList.layoutManager = GridLayoutManager(this, 3, RecyclerView.VERTICAL, false)
        todayWantList.adapter = adapter
        adapter.setOnItemClickListener { _, view, position ->
            checkPosi = position
            checkWantId = adapter.data[position].id
            for (data in adapter.data) {
                data.checked = data == adapter.data[position]
            }
            adapter.notifyDataSetChanged()
        }
    }

    override fun onGetIntentionResult(result: DatingOptionsBean?) {
        if (result != null) {
            stateDatingType.viewState = MultiStateView.VIEW_STATE_CONTENT
            var hasCheck = false
            if (checkWantId != -1) {
                for (data in result!!.dating_type.withIndex()) {
                    if (data.value.id == checkWantId) {
                        data.value.checked = true
                        checkPosi = data.index
                        checkWantId = data.value.id
                        hasCheck = true
                        break
                    }
                }
            }
            if (!hasCheck) {
                result.dating_type[0].checked = true
                checkPosi = 0
                checkWantId = result.dating_type[0].id
                rightBtn1.isEnabled = true
            }
            adapter.setNewData(result.dating_type)
        } else {
            stateDatingType.viewState = MultiStateView.VIEW_STATE_ERROR
        }
    }

}