package com.sdy.jitangapplication.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.View
import androidx.core.view.isVisible
import com.blankj.utilcode.util.SpanUtils
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.AddLabelResultBean
import com.sdy.jitangapplication.model.LabelQualityBean
import com.sdy.jitangapplication.presenter.LabelQualityPresenter
import com.sdy.jitangapplication.presenter.view.LabelQualityView
import com.sdy.jitangapplication.ui.dialog.LoadingDialog
import kotlinx.android.synthetic.main.activity_label_introduce.*
import kotlinx.android.synthetic.main.layout_actionbar.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.startActivityForResult

/**
 * 兴趣介绍
 * accid复制
 * token
 * tag_id [int]	是	标签id
 * type [int]	是	默认 1 新建或则 编辑 2直接复用以前的
 * describle [string]	是	兴趣介绍
 * intention [json]	是	标签意向json串
 * label_quality[json]	是	标签特质 json串
 */
class LabelIntroduceActivity : BaseMvpActivity<LabelQualityPresenter>(), LabelQualityView, View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_label_introduce)
        initView()

    }
    private fun initView() {
        mPresenter = LabelQualityPresenter()
        mPresenter.mView = this
        mPresenter.context = this
        hotT1.visibility = View.INVISIBLE
        divider.isVisible = false
        btnBack.setOnClickListener(this)
        rightBtn1.setOnClickListener(this)
        rightBtn1.isVisible = true
        rightBtn1.setBackgroundResource(R.drawable.selector_confirm_btn_25dp)
        rightBtn1.text = "完成"
        rightBtn1.isEnabled = false

        labelPurposeBtn.setOnClickListener(this)
        labelIntroduceModel.setOnClickListener(this)


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
            labelPurposeBtn -> {//标签意向
                startActivityForResult<MyIntentionActivity>(100, "from" to MyIntentionActivity.FROM_REGISTER)
            }
            rightBtn1 -> { //保存个人介绍
                mPresenter.saveRegisterInfo(
                    intention_id = intention?.id ?: null,
                    aboutme = labelIntroduceContent.text.toString()
                )
            }
            labelIntroduceModel -> {//个人介绍模板
                startActivity<ModelLabelIntroduceActivity>(
                    "tag_id" to intent.getIntExtra("tag_id", 0)
                )
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

    override fun getTagTraitInfoResult(result: Boolean, data: MutableList<LabelQualityBean>?) {
    }

    override fun addTagResult(result: Boolean, data: AddLabelResultBean?) {

    }

    override fun onSaveRegisterInfo(success: Boolean) {

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
}
