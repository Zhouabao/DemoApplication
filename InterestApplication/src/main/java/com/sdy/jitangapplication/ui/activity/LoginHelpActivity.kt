package com.sdy.jitangapplication.ui.activity

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SizeUtils
import com.kennyc.view.MultiStateView
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.LoginHelpBean
import com.sdy.jitangapplication.presenter.LoginHelpPresenter
import com.sdy.jitangapplication.presenter.view.LoginHelpView
import com.sdy.jitangapplication.ui.adapter.LoginHelpAdapter
import com.sdy.jitangapplication.widgets.DividerItemDecoration
import kotlinx.android.synthetic.main.activity_login_help.*
import kotlinx.android.synthetic.main.error_layout.view.*
import kotlinx.android.synthetic.main.layout_actionbar.*
import org.jetbrains.anko.startActivity

/**
 * 登录帮助
 */
class LoginHelpActivity : BaseMvpActivity<LoginHelpPresenter>(), LoginHelpView {


    private val adapter by lazy { LoginHelpAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_help)
        initView()
        mPresenter.getHelpCenter()
    }

    private fun initView() {
        btnBack.onClick {
            finish()
        }
        hotT1.text = "帮助"

        mPresenter = LoginHelpPresenter()
        mPresenter.mView = this
        mPresenter.context = this
        stateLoginHelp.retryBtn.onClick {
            stateLoginHelp.viewState = MultiStateView.VIEW_STATE_LOADING
            mPresenter.getHelpCenter()
        }


        rvLoginHelp.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        rvLoginHelp.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.HORIZONTAL_LIST,
                SizeUtils.dp2px(1F),
                resources.getColor(R.color.colorDivider)
            )
        )
        rvLoginHelp.adapter = adapter
        adapter.setOnItemClickListener { _, _, position ->
            startActivity<LoginHelpDetailActivity>("data" to adapter.data[position])
        }
    }


    override fun getHelpCenterResult(success: Boolean, data: MutableList<LoginHelpBean>?) {
        if (success) {
            stateLoginHelp.viewState = MultiStateView.VIEW_STATE_CONTENT
            adapter.setNewData(data ?: mutableListOf<LoginHelpBean>())
        } else {
            stateLoginHelp.viewState = MultiStateView.VIEW_STATE_ERROR
        }

    }
}
