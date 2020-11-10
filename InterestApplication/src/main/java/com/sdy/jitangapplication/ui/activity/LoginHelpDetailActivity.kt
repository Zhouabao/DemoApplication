package com.sdy.jitangapplication.ui.activity

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SizeUtils
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseActivity
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.LoginHelpBean
import com.sdy.jitangapplication.ui.adapter.LoginHelpDetailAdapter
import com.sdy.jitangapplication.widgets.DividerItemDecoration
import kotlinx.android.synthetic.main.activity_login_help_detail.*
import kotlinx.android.synthetic.main.layout_actionbar.*

/**
 * 登录帮助详情
 */
class LoginHelpDetailActivity : BaseActivity() {
    private val adapter by lazy { LoginHelpDetailAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_help_detail)

        btnBack.onClick { finish() }
        hotT1.text = getString(R.string.help)

        loginHelpTitle.text = (intent.getSerializableExtra("data") as LoginHelpBean).title
        loginHelpContentRv.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        loginHelpContentRv.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.HORIZONTAL_LIST,
                SizeUtils.dp2px(10F),
                resources.getColor(R.color.colorWhite)
            )
        )
        loginHelpContentRv.adapter = adapter
        adapter.setNewData((intent.getSerializableExtra("data") as LoginHelpBean).content)

    }
}
