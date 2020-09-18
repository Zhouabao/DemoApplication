package com.sdy.jitangapplication.ui.activity

import android.os.Bundle
import com.blankj.utilcode.util.ActivityUtils
import com.kotlin.base.ui.activity.BaseActivity
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.clickWithTrigger
import kotlinx.android.synthetic.main.activity_sweet_heart_verifying.*
import kotlinx.android.synthetic.main.layout_actionbar.*

/**
 * 甜心圈认证中
 */
class SweetHeartVerifyingActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sweet_heart_verifying)

        initView()
    }

    private fun initView() {
        hotT1.text = "甜心圈认证"
        btnBack.clickWithTrigger { clearActivity() }
        okBtn.clickWithTrigger {
            clearActivity()
        }

    }

    private fun clearActivity() {
        if (ActivityUtils.isActivityExistsInStack(SweetHeartVerifyActivity::class.java)) {
            ActivityUtils.finishActivity(SweetHeartVerifyActivity::class.java)
        }
        if (ActivityUtils.isActivityExistsInStack(SweetHeartVerifyUploadActivity::class.java)) {
            ActivityUtils.finishActivity(SweetHeartVerifyUploadActivity::class.java)
        }
        if (ActivityUtils.isActivityExistsInStack(SweetHeartSquareUploadActivity::class.java)) {
            ActivityUtils.finishActivity(SweetHeartSquareUploadActivity::class.java)
        }

        finish()
    }
}