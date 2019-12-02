package com.sdy.jitangapplication.ui.activity

import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.kotlin.base.ext.onClick
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.LabelQualityBean
import kotlinx.android.synthetic.main.activity_model_about_me_detail.*
import kotlinx.android.synthetic.main.layout_actionbar.*

class ModelAboutMeDetailActivity : AppCompatActivity(), View.OnClickListener {


    private val modelBean by lazy { intent.getSerializableExtra("content") as LabelQualityBean }
    private val from by lazy { intent.getIntExtra("from", ModelAboutMeActivity.FROM_ME) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_model_about_me_detail)
        initView()
    }

    private fun initView() {
        btnBack.onClick {
            finish()
        }

        when (from) {
            ModelAboutMeActivity.FROM_ME -> {
                confirmBtn.isVisible = true
                confirmBtn.setOnClickListener(this)
                rightBtn1.isVisible = false
                t1.isVisible = false
                hotT1.isVisible = true
                hotT1.text = "${modelBean.title}"
            }
            ModelAboutMeActivity.FROM_LABEL -> {
                rightBtn1.setBackgroundResource(R.drawable.shape_rectangle_orange_25dp)
                rightBtn1.text = "使用该范本"
                confirmBtn.isVisible = false
                rightBtn1.isVisible = true
                rightBtn1.isEnabled = true
                rightBtn1.setOnClickListener(this)
                hotT1.isVisible = false
                t1.isVisible = true
                t1.text = "${modelBean.title}"
            }
        }

        modelContent.text = "${modelBean.content}"
    }

    override fun onClick(view: View) {
        when (view) {
            confirmBtn,rightBtn1->{
                setResult(Activity.RESULT_OK, intent.putExtra("content", modelBean.content))
                finish()
            }
        }
    }
}
