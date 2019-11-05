package com.sdy.jitangapplication.ui.activity

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kotlin.base.ext.onClick
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.ModelAboutBean
import kotlinx.android.synthetic.main.activity_model_about_me_detail.*
import kotlinx.android.synthetic.main.layout_actionbar.*

class ModelAboutMeDetailActivity : AppCompatActivity() {
private val modelBean by lazy { intent.getSerializableExtra("content") as ModelAboutBean }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_model_about_me_detail)
        initView()
    }

    private fun initView() {
        btnBack.onClick {
            finish()
        }
        hotT1.text = "${modelBean.title}"
        modelContent.text = "${modelBean.content}"
        confirmBtn.onClick {
            setResult(Activity.RESULT_OK,intent.putExtra("content",modelBean.content))
            finish()
        }
    }
}
