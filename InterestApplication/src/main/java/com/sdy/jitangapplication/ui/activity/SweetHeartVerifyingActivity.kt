package com.sdy.jitangapplication.ui.activity

import android.os.Bundle
import androidx.core.view.isInvisible
import com.blankj.utilcode.util.ActivityUtils
import com.kotlin.base.ui.activity.BaseActivity
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.event.RefreshSweetEvent
import kotlinx.android.synthetic.main.activity_sweet_heart_verifying.*
import kotlinx.android.synthetic.main.layout_actionbar.*
import org.greenrobot.eventbus.EventBus

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
        setSwipeBackEnable(false)
        hotT1.text = "甜心圈认证"
        btnBack.isInvisible = true
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
        EventBus.getDefault().post(RefreshSweetEvent())
        finish()
    }
}