package com.sdy.jitangapplication.ui.activity

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.widget.LinearLayout
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.SizeUtils
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseActivity
import com.sdy.jitangapplication.R
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
            hotT1.text= "关于我"
            changeHint.text = "说点什么吧"
            changeEt.filters = arrayOf(InputFilter.LengthFilter(200))
            val params = changeEt.layoutParams as LinearLayout.LayoutParams
            params.width = LinearLayout.LayoutParams.MATCH_PARENT
            params.height = SizeUtils.dp2px(60F)
//            changeEt.layoutParams = params
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
