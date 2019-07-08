package com.example.demoapplication.ui.activity

import android.os.Bundle
import com.example.demoapplication.R
import com.example.demoapplication.presenter.PublishPresenter
import com.example.demoapplication.presenter.view.PublishView
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import kotlinx.android.synthetic.main.activity_publish.*

/**
 * 发现的发布内容页面
 */
class PublishActivity : BaseMvpActivity<PublishPresenter>(), PublishView {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_publish)
        initView()
    }

    private fun initView() {
        btnBack.onClick {
            finish()
        }

        mPresenter = PublishPresenter()
        mPresenter.mView = this
        mPresenter.context = this
    }
}
