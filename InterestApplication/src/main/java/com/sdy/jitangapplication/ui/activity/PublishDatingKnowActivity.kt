package com.sdy.jitangapplication.ui.activity

import android.os.Bundle
import android.widget.CompoundButton
import com.kotlin.base.ui.activity.BaseActivity
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.clickWithTrigger
import kotlinx.android.synthetic.main.activity_publish_dating_know.*
import org.jetbrains.anko.startActivity

/**
 * 活动发布提示
 */
class PublishDatingKnowActivity : BaseActivity(), CompoundButton.OnCheckedChangeListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_publish_dating_know)

        initView()
    }

    private fun initView() {
        backBtn.clickWithTrigger {
            finish()
        }

        publishDatingBtn.clickWithTrigger {
            startActivity<ChooseDatingTypeActivity>()
            finish()
        }

        switchReliable.setOnCheckedChangeListener(this)
        switchObeyLine.setOnCheckedChangeListener(this)
        switchActive.setOnCheckedChangeListener(this)
        switchLimit.setOnCheckedChangeListener(this)
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        checkPublishEnable()
    }

    private fun checkPublishEnable() {
        publishDatingBtn.isEnabled = switchReliable.isChecked
                && switchObeyLine.isChecked
                && switchActive.isChecked
                && switchLimit.isChecked
    }
}