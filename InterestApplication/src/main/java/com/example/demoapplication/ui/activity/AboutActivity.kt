package com.example.demoapplication.ui.activity

import android.os.Bundle
import com.example.demoapplication.R
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseActivity
import kotlinx.android.synthetic.main.layout_actionbar.*

class AboutActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        initView()
    }

    private fun initView() {
        btnBack.onClick {
            finish()
        }
        hotT1.text = "关于"
    }
}
