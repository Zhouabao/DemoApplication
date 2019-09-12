package com.sdy.jitangapplication.ui.activity

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseActivity
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.nim.activity.ChatActivity
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
        contactUs.onClick {
            ChatActivity.start(this, com.sdy.jitangapplication.common.Constants.ASSISTANT_ACCID)
        }

        versionTip.text = "for Android V${getAppVersionName(this)}"
    }

    //获取当前版本号
    private fun getAppVersionName(context: Context): String {
        var versionName = ""
        try {
            val packageManager = context.packageManager
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            versionName = packageInfo.versionName
            if (TextUtils.isEmpty(versionName)) {
                return ""
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return versionName
    }
}
