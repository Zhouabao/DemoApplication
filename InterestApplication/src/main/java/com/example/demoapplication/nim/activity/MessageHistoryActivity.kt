package com.example.demoapplication.nim.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import com.example.demoapplication.R
import com.kotlin.base.ext.onClick
import com.netease.nim.uikit.business.session.module.Container
import com.netease.nim.uikit.business.session.module.ModuleProxy
import com.netease.nim.uikit.business.session.module.list.MessageListPanelEx
import com.netease.nim.uikit.common.activity.UI
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.IMMessage
import kotlinx.android.synthetic.main.activity_message_history.*
import org.jetbrains.anko.startActivity

/**
 * 消息历史查询界面
 */
class MessageHistoryActivity : UI(), ModuleProxy {
    companion object {
        private val EXTRA_DATA_ACCOUNT = "EXTRA_DATA_ACCOUNT"
        private val EXTRA_DATA_SESSION_TYPE = "EXTRA_DATA_SESSION_TYPE"
        @JvmStatic
        fun start(context: Context, sessionId: String, sessionTypeEnum: SessionTypeEnum) {
            context.startActivity<MessageHistoryActivity>(
                EXTRA_DATA_ACCOUNT to sessionId,
                EXTRA_DATA_SESSION_TYPE to sessionTypeEnum
            )
        }
    }

    // context
    private var sessionType: SessionTypeEnum? = null
    private var account: String? = null // 对方帐号
    private var messageListPanel: MessageListPanelEx? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val rootView = LayoutInflater.from(this).inflate(R.layout.activity_message_history, null)

        setContentView(rootView)

        initView()


        val container = Container(this, account, sessionType, this)
        messageListPanel = MessageListPanelEx(container, rootView, true, true)
    }

    private fun initView() {
        btnBack.onClick {
            finish()
        }

        account = intent.getStringExtra(EXTRA_DATA_ACCOUNT)
        sessionType = intent.getSerializableExtra(EXTRA_DATA_SESSION_TYPE) as SessionTypeEnum

    }


    override fun sendMessage(msg: IMMessage?): Boolean {
        return false
    }

    override fun onInputPanelExpand() {
    }

    override fun shouldCollapseInputPanel() {
    }

    override fun isLongClickEnabled(): Boolean {
        return true
    }

    override fun onItemFooterClick(message: IMMessage?) {
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (messageListPanel != null)
            messageListPanel!!.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (messageListPanel != null)
            messageListPanel!!.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (messageListPanel != null) {
            messageListPanel!!.onActivityResult(requestCode, resultCode, data)
        }
    }
}
