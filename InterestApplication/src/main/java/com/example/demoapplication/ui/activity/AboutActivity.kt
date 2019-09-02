package com.example.demoapplication.ui.activity

import android.os.Bundle
import com.example.demoapplication.R
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseActivity
import kotlinx.android.synthetic.main.activity_about.*
import kotlinx.android.synthetic.main.layout_actionbar.*
import org.jetbrains.anko.startActivity

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
        privacyPolicy.onClick {
            startActivity<ProtocolActivity>("type" to 1)
        }
        userAgreement.onClick {
            startActivity<ProtocolActivity>("type" to 2)
        }
    }
}
