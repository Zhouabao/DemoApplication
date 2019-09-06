package com.sdy.jitangapplication.ui.activity

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import com.blankj.utilcode.util.KeyboardUtils
import com.sdy.jitangapplication.R
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseActivity
import kotlinx.android.synthetic.main.activity_nick_name.*
import kotlinx.android.synthetic.main.activity_publish.btnBack

/**
 * 修改昵稱和簽名
 * //1昵称 2签名
 */
class NickNameActivity : BaseActivity() {

    val type by lazy { intent.getIntExtra("type", 0) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nick_name)

        btnBack.onClick { onBackPressed() }

        if (type == 1) {
            hotT1.text= "更改昵称"
            changeHint.text = "好名字可以让你的朋友更容易记住你"
            changeEt.filters = arrayOf(InputFilter.LengthFilter(10))
        } else {
            hotT1.text= "更改签名"
            changeHint.text = "说点什么吧"
            changeEt.filters = arrayOf(InputFilter.LengthFilter(50))
        }

        changeEt.setText(intent.getStringExtra("content"))
        changeEt.setSelection(changeEt.text.length)
        changeEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                saveChangeBtn.isEnabled = !changeEt.text.isNullOrEmpty()
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })
        saveChangeBtn.onClick {
            intent.putExtra("content", changeEt.text.toString())
            setResult(Activity.RESULT_OK,intent)
            onBackPressed()
        }
    }

    override fun onStart() {
        super.onStart()
        changeEt.postDelayed({ KeyboardUtils.showSoftInput(changeEt) }, 100)
    }

    override fun onBackPressed() {
        KeyboardUtils.hideSoftInput(changeEt)
        super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        KeyboardUtils.hideSoftInput(changeEt)
    }

    override fun finish() {
        super.finish()
        KeyboardUtils.hideSoftInput(changeEt)
    }

}
