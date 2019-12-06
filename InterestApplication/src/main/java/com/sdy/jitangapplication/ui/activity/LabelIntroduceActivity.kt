package com.sdy.jitangapplication.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.bigkoo.pickerview.builder.OptionsPickerBuilder
import com.bigkoo.pickerview.listener.OnOptionsSelectListener
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.SpanUtils
import com.google.gson.Gson
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.event.UpdateAvatorEvent
import com.sdy.jitangapplication.event.UpdateMyLabelEvent
import com.sdy.jitangapplication.event.UserCenterLabelEvent
import com.sdy.jitangapplication.model.AddLabelResultBean
import com.sdy.jitangapplication.model.LabelQualityBean
import com.sdy.jitangapplication.model.NewLabel
import com.sdy.jitangapplication.presenter.LabelQualityPresenter
import com.sdy.jitangapplication.presenter.view.LabelQualityView
import com.sdy.jitangapplication.ui.dialog.LoadingDialog
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.activity_label_introduce.*
import kotlinx.android.synthetic.main.layout_actionbar.*
import org.greenrobot.eventbus.EventBus
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
    private val params by lazy { hashMapOf<String, Any>() }
    private var data: MutableList<LabelQualityBean>? = null
    private val labelBean by lazy { intent.getSerializableExtra("data") as NewLabel? }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_label_introduce)
        initView()

    }

    private val from: Int by lazy { intent.getIntExtra("from", AddLabelActivity.FROM_REGISTER) }

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
        rightBtn1.isEnabled = false

        labelPurposeBtn.setOnClickListener(this)
        labelIntroduceModel.setOnClickListener(this)

        if (from == AddLabelActivity.FROM_EDIT) {
            rightBtn1.text = "保存"
            labelPurposeCl.isVisible = false
            if (!intent.getStringExtra("describle").isNullOrEmpty()) {
                labelIntroduceContent.setText(intent.getStringExtra("describle").trim())
                labelIntroduceContent.setSelection(labelIntroduceContent.text.trim().length)
                labelIntroduceInputLength.text = SpanUtils.with(labelIntroduceInputLength)
                    .append(labelIntroduceContent.text.trim().length.toString())
                    .setFontSize(14, true)
                    .setForegroundColor(resources.getColor(R.color.colorOrange))
                    .setBold()
                    .append("/${(labelIntroduceContent.filters[0] as InputFilter.LengthFilter).max}")
                    .setFontSize(10, true)
                    .create()
            } else {
                labelIntroduceContent.hint = intent.getStringExtra("describleHint").trim()
            }
        } else if (from == AddLabelActivity.FROM_REGISTER) {
            rightBtn1.text = "保存并继续"
            labelPurposeCl.isVisible = true
            labelIntroduceContent.hint = labelBean!!.intro_descr
        } else {
            rightBtn1.text = "保存"
            labelPurposeCl.isVisible = true
            labelIntroduceContent.hint = labelBean!!.intro_descr
        }

        params["tag_id"] = intent.getIntExtra("tag_id", 0)
        params["label_quality"] = intent.getStringExtra("label_quality")
        if (intent.getStringExtra("intention") != null && intent.getStringExtra("intention").isNotEmpty())
            params["intention"] = intent.getStringExtra("intention")
        params["type"] = 1



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
                if (data == null) {
                    mPresenter.getTagTraitInfo(hashMapOf("tag_id" to intent.getIntExtra("tag_id", 0), "type" to 3))
                } else {
                    showConditionPicker(data ?: mutableListOf())
                }
            }
            rightBtn1 -> { //保存标签
                if (from == AddLabelActivity.FROM_EDIT) {
                    intent.putExtra("describle", labelIntroduceContent.text.trim().toString())
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                } else {
                    params["describle"] = labelIntroduceContent.text.trim()
                    mPresenter.addClassifyTag(params)
                }
            }
            labelIntroduceModel -> {//标签介绍模板
                startActivityForResult<ModelAboutMeActivity>(
                    100,
                    "tag_id" to intent.getIntExtra("tag_id", 0),
                    "from" to ModelAboutMeActivity.FROM_LABEL
                )
//                startActivityForResult<ModelLabelIntroduceActivity>(
//                    100,
//                    "tag_id" to intent.getIntExtra("tag_id", 0),
//                    "from" to ModelAboutMeActivity.FROM_LABEL
//                )
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
        this.data = data
        showConditionPicker(data ?: mutableListOf())
    }

    override fun addTagResult(result: Boolean, data: AddLabelResultBean?) {
        if (result) {
            if (data != null) {
                UserManager.saveLabels(data.list)
                if (from != AddLabelActivity.FROM_REGISTER)
                    EventBus.getDefault().post(UpdateAvatorEvent(true))

                //todo 这里标签是来自于发布或者已经在该标签下发布过内容，就不走发布流程
                if (from != AddLabelActivity.FROM_PUBLISH && !data!!.is_published) {
                    if (ActivityUtils.isActivityAlive(LabelQualityActivity::class.java.newInstance()))
                        ActivityUtils.finishActivity(LabelQualityActivity::class.java)
                    finish()
                    startActivity<AddLabelSuccessActivity>("data" to labelBean)
                } else {
                    EventBus.getDefault().post(UpdateMyLabelEvent())
                    EventBus.getDefault().post(UserCenterLabelEvent())
                    if (ActivityUtils.isActivityAlive(LabelQualityActivity::class.java.newInstance())) {
                        ActivityUtils.finishActivity(LabelQualityActivity::class.java)
                    }
                    if (ActivityUtils.isActivityAlive(MyLabelQualityActivity::class.java.newInstance()))
                        ActivityUtils.finishActivity(MyLabelQualityActivity::class.java)
                    if (ActivityUtils.isActivityAlive(AddLabelActivity::class.java.newInstance()))
                        ActivityUtils.finishActivity(AddLabelActivity::class.java)
//                if (ActivityUtils.isActivityAlive(MyLabelActivity::class.java.newInstance()))
//                    ActivityUtils.finishActivity(MyLabelActivity::class.java)
                    finish()
//                startActivity<MyLabelActivity>()
                }
            }
        }
    }


    /**
     * 展示条件选择器
     */
    private fun showConditionPicker(data: MutableList<LabelQualityBean>) {
        //条件选择器
        val pvOptions = OptionsPickerBuilder(this,
            OnOptionsSelectListener { options1, options2, options3, v ->
                if (data.size > 0 && data.size > options1) {
                    params["intention"] = Gson().toJson(mutableListOf(data[options1].id))
                    labelPurposeBtn.text = data[options1].content
                }
            })
            .setSubmitText("确定")
            .setTitleText("请选择您的意愿")
            .setTitleColor(resources.getColor(R.color.colorBlack))
            .setTitleSize(16)
            .setDividerColor(resources.getColor(R.color.colorDivider))
            .setContentTextSize(20)
            .setDecorView(contentView as ViewGroup)
            .setSubmitColor(resources.getColor(R.color.colorBlueSky1))
            .build<LabelQualityBean>()
        pvOptions.setPicker(data)
        pvOptions.show()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 100) {
                if (data != null && data.getStringExtra("content") != null) {
                    labelIntroduceContent.setText(data.getStringExtra("content"))
                    labelIntroduceContent.setSelection(labelIntroduceContent.length())
                    labelIntroduceInputLength.text = SpanUtils.with(labelIntroduceInputLength)
                        .append(labelIntroduceContent.length().toString())
                        .setFontSize(14, true)
                        .setForegroundColor(resources.getColor(R.color.colorOrange))
                        .setBold()
                        .append("/${(labelIntroduceContent.filters[0] as InputFilter.LengthFilter).max}")
                        .setFontSize(10, true)
                        .create()
                }
            }
        }
    }
}
