package com.sdy.jitangapplication.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.SpanUtils
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseActivity
import com.sdy.jitangapplication.R
import kotlinx.android.synthetic.main.activity_about_me.*
import org.jetbrains.anko.startActivityForResult

/**
 * 关于我
 */
class AboutMeActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_me)
        initView()
    }

    private fun initView() {
        btnBack.onClick {
            finish()
        }
        modelMe.onClick {
            startActivityForResult<ModelAboutMeActivity>(105)
            KeyboardUtils.hideSoftInput(aboutMeContent)
        }

        aboutMeInputLength.text = SpanUtils.with(aboutMeInputLength)
            .append(aboutMeContent.length().toString())
            .setFontSize(14, true)
            .setForegroundColor(resources.getColor(R.color.colorOrange))
            .setBold()
            .append("/${(aboutMeContent.filters[0] as InputFilter.LengthFilter).max}")
            .setFontSize(10, true)
            .create()

        aboutMeContent.postDelayed({
            KeyboardUtils.showSoftInput(aboutMeContent)
        }, 200)

        aboutMeContent.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                saveBtn.isEnabled = !aboutMeContent.text.isNullOrEmpty()
                aboutMeContent.setSelection(aboutMeContent.length())
                aboutMeInputLength.text = SpanUtils.with(aboutMeInputLength)
                    .append(aboutMeContent.length().toString())
                    .setFontSize(14, true)
                    .setForegroundColor(resources.getColor(R.color.colorOrange))
                    .setBold()
                    .append("/${(aboutMeContent.filters[0] as InputFilter.LengthFilter).max}")
                    .setFontSize(10, true)
                    .create()
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })

        if (!intent.getStringExtra("content").isNullOrEmpty()) {
            aboutMeContent.setText(intent.getStringExtra("content"))
        }

        saveBtn.onClick {
            intent.putExtra("content",aboutMeContent.text.toString())
            setResult(Activity.RESULT_OK,intent)
            finish()
            KeyboardUtils.hideSoftInput(aboutMeContent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null) {
            aboutMeContent.setText(data.getStringExtra("content"))
            intent = data
        }
    }
}
