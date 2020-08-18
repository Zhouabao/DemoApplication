package com.sdy.jitangapplication.ui.activity

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import androidx.core.view.isVisible
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.SpanUtils
import com.kotlin.base.ui.activity.BaseActivity
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.clickWithTrigger
import kotlinx.android.synthetic.main.activity_write_dating_content.*
import kotlinx.android.synthetic.main.layout_actionbar.*

/**
 * 填写约会内容
 */
class WriteDatingContentActivity : BaseActivity() {
    private val datingType by lazy { intent.getStringExtra("dating_type") }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write_dating_content)

        initView()
    }

    companion object {
        const val MAX_DESCR_LENGTH = 10
    }

    private fun initView() {
        hotT1.text = "活动内容"
        btnBack.clickWithTrigger {
            finish()
        }
        rightBtn.text = "完成"
        rightBtn.setTextColor(resources.getColor(R.color.colorOrange))
        rightBtn.isVisible = true
        rightBtn.isEnabled = true
        rightBtn.clickWithTrigger {
            if (datingContentEt.text.trim().isNullOrBlank()) {
                CommonFunction.toast("请填写活动内容")
                return@clickWithTrigger
            }
            setResult(
                Activity.RESULT_OK,
                intent.putExtra("datingContent", datingContentEt.text.trim().toString())
            )
            finish()
        }


        datingContentEt.filters = arrayOf<InputFilter>(
            InputFilter.LengthFilter(
                MAX_DESCR_LENGTH
            )
        )
        SpanUtils.with(datingContentLength)
            .append(datingContentEt.length().toString())
            .setForegroundColor(resources.getColor(R.color.colorOrange))
            .append("/${MAX_DESCR_LENGTH}")
            .create()

        SpanUtils.with(contentTip)
            .append("其他用户预览样式如下，请妥善描述，")
            .append("发布违禁内容或导致封号及设备禁用")
            .setForegroundColor(Color.parseColor("#FFFF0D0D"))
            .create()


        SpanUtils.with(datingContentShow)
            .append("${datingType}·")
            .append("待描述")
            .setForegroundColor(resources.getColor(R.color.colorOrange))
            .create()

        datingContentEt.addTextChangedListener(object : TextWatcher {
            /************编辑内容监听**************/
            override fun afterTextChanged(p0: Editable) {
                SpanUtils.with(datingContentShow)
                    .append("${datingType}·")
                    .append(datingContentEt.text.trim().toString())
                    .setForegroundColor(resources.getColor(R.color.colorOrange))
                    .create()
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                SpanUtils.with(datingContentLength)
                    .append(datingContentEt.length().toString())
                    .setForegroundColor(resources.getColor(R.color.colorOrange))
                    .append("/${MAX_DESCR_LENGTH}")
                    .create()
            }

        })

    }


    override fun finish() {
        if (KeyboardUtils.isSoftInputVisible(this)) {
            KeyboardUtils.hideSoftInput(this)
        } else
            super.finish()
    }
}