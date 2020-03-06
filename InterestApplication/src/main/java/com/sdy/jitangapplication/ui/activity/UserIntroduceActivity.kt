package com.sdy.jitangapplication.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.View
import androidx.core.view.isVisible
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.SpanUtils
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.LabelQualityBean
import com.sdy.jitangapplication.presenter.UserIntroducePresenter
import com.sdy.jitangapplication.presenter.view.UserIntroduceView
import com.sdy.jitangapplication.ui.dialog.LoadingDialog
import kotlinx.android.synthetic.main.activity_user_introduce.*
import kotlinx.android.synthetic.main.layout_actionbar.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.startActivityForResult

/**
 * 个人介绍
 */
class UserIntroduceActivity : BaseMvpActivity<UserIntroducePresenter>(), UserIntroduceView, View.OnClickListener {

    companion object {
        const val REGISTER = 1
        const val USERCENTER = 2
    }

    private val from by lazy { intent.getIntExtra("from", USERCENTER) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_introduce)
        initView()

    }

    override fun onPause() {
        super.onPause()
        KeyboardUtils.hideSoftInput(labelIntroduceContent)
    }

    override fun onResume() {
        super.onResume()
        labelIntroduceContent.postDelayed({ KeyboardUtils.showSoftInput(labelIntroduceContent) }, 200L)
    }

    private fun initView() {
        mPresenter = UserIntroducePresenter()
        mPresenter.mView = this
        mPresenter.context = this

        divider.isVisible = false
        setSwipeBackEnable(from != REGISTER)

        btnBack.setOnClickListener(this)
        rightBtn1.setOnClickListener(this)
        rightBtn1.isVisible = true
        rightBtn1.setBackgroundResource(R.drawable.selector_confirm_btn_25dp)
        rightBtn1.text = "完成"
        rightBtn1.isEnabled = false

        labelPurposeBtn.setOnClickListener(this)
        labelIntroduceModel.setOnClickListener(this)


        if (from == REGISTER) {
            btnBack.isVisible = false
            t1.isVisible = true
//            labelPurposeCl.isVisible = true
            hotT1.visibility = View.INVISIBLE
        } else {
            btnBack.isVisible = true
            t1.isVisible = false
//            labelPurposeCl.isVisible = false
            hotT1.visibility = View.VISIBLE
            hotT1.text = "关于我"
        }


        if (!intent.getStringExtra("content").isNullOrEmpty()) {
            labelIntroduceContent.setText(intent.getStringExtra("content"))
            labelIntroduceContent.setSelection(labelIntroduceContent.length())
            checkSaveEnable()
            labelIntroduceInputLength.text = SpanUtils.with(labelIntroduceInputLength)
                .append(labelIntroduceContent.length().toString())
                .setFontSize(14, true)
                .setForegroundColor(resources.getColor(R.color.colorOrange))
                .setBold()
                .append("/${(labelIntroduceContent.filters[0] as InputFilter.LengthFilter).max}")
                .setFontSize(10, true)
                .create()
        }

        labelIntroduceContent.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                checkSaveEnable()
                labelIntroduceInputLength.text = SpanUtils.with(labelIntroduceInputLength)
                    .append(labelIntroduceContent.length().toString())
                    .setFontSize(14, true)
                    .setForegroundColor(resources.getColor(R.color.colorOrange))
                    .setBold()
                    .append("/${(labelIntroduceContent.filters[0] as InputFilter.LengthFilter).max}")
                    .setFontSize(10, true)
                    .create()
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })

    }

    override fun onClick(view: View) {
        when (view) {
            btnBack -> {
                finish()
            }
            labelPurposeBtn -> {//兴趣意向
                startActivityForResult<MyIntentionActivity>(100, "from" to MyIntentionActivity.FROM_REGISTER)
            }
            rightBtn1 -> { //保存个人介绍
                if (from == REGISTER)
                    mPresenter.saveRegisterInfo(
                        intention_id = intention?.id ?: null,
                        aboutme = labelIntroduceContent.text.toString()
                    )
                else {
                    intent.putExtra("content", labelIntroduceContent.text.toString())
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                    KeyboardUtils.hideSoftInput(labelIntroduceContent)
                }
            }
            labelIntroduceModel -> {//个人介绍模板
                startActivity<ModelAboutMeActivity>()
            }
        }
    }


    fun checkSaveEnable() {
        rightBtn1.isEnabled = labelIntroduceContent.text.trim().isNotEmpty()
    }

    private val loading by lazy { LoadingDialog(this) }

    override fun showLoading() {
        loading.show()
    }

    override fun hideLoading() {
        loading.dismiss()
    }

    override fun onSaveRegisterInfo(success: Boolean) {
        if (success) {
            startActivity<AddLabelActivity>("from" to AddLabelActivity.FROM_REGISTER)
            finish()
        }
    }

    private var intention: LabelQualityBean? = null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 100) {
                if (data != null && data.getSerializableExtra("intention") != null) {
                    intention = data.getSerializableExtra("intention") as LabelQualityBean
                    if (intention != null)
                        labelPurposeBtn.text = intention!!.title
                }
            }
        }
    }

    override fun onBackPressed() {
        if (from != REGISTER)
            super.onBackPressed()
    }
}
