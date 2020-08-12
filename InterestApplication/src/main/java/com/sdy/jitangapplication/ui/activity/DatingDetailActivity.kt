package com.sdy.jitangapplication.ui.activity

import android.os.Bundle
import android.view.View
import com.blankj.utilcode.util.VibrateUtils
import com.kotlin.base.ui.activity.BaseActivity
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.OnLazyClickListener
import com.sdy.jitangapplication.common.clickWithTrigger
import kotlinx.android.synthetic.main.activity_dating_detail.*
import kotlinx.android.synthetic.main.layout_actionbar.*

/**
 * 约会详情
 */
class DatingDetailActivity : BaseActivity(), OnLazyClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dating_detail)

        initView()
    }

    private var zan = false
    private fun initView() {
        hotT1.text = "约会详情"
        btnBack.clickWithTrigger {
            finish()
        }
        datingDianzanAni.setOnClickListener(this)
        datingZanCnt.setOnClickListener(this)


    }

    override fun onLazyClick(v: View) {
        when (v.id) {
            R.id.datingDianzanAni, R.id.datingZanCnt -> {
                zan = !zan
                if (zan) {
                    datingDianzanAni.playAnimation()
                    VibrateUtils.vibrate(50L)
                } else {
                    datingDianzanAni.progress = 0F
                }
            }
        }

    }
}