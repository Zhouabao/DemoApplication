package com.sdy.jitangapplication.ui.activity

import android.os.Bundle
import com.blankj.utilcode.util.ActivityUtils
import com.kotlin.base.ui.activity.BaseActivity
import com.sdy.jitangapplication.R
import kotlinx.android.synthetic.main.activity_add_label_success.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.startActivity

/**
 * 添加标签成功
 */
class AddLabelSuccessActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_label_success)


        successLabelName.text="恭喜「${intent.getStringExtra("name")}」已创建完成"
        startJitangBtn.onClick{
            ActivityUtils.finishAllActivities()
            startActivity<MainActivity>()
        }
    }
}
