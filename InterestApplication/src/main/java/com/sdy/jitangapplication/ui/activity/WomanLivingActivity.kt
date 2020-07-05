package com.sdy.jitangapplication.ui.activity

import android.os.Bundle
import com.kotlin.base.ui.activity.BaseActivity
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.model.MoreMatchBean
import kotlinx.android.synthetic.main.activity_woman_living.*
import org.jetbrains.anko.startActivity

/**
 * 女性提前活体认证
 */
class WomanLivingActivity : BaseActivity() {
    private val morematchbean by lazy { intent.getSerializableExtra("morematchbean") as MoreMatchBean? }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_woman_living)

        setSwipeBackEnable(false)
        beginLivingBtn.clickWithTrigger {
            startActivity<IDVerifyActivity>(
                "type" to IDVerifyActivity.TYPE_LIVE_CAPTURE,
                "morematchbean" to morematchbean
            )
        }
    }

    override fun onBackPressed() {

    }
}