package com.sdy.jitangapplication.ui.activity

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import androidx.core.view.isVisible
import com.google.android.flexbox.*
import com.google.gson.Gson
import com.kennyc.view.MultiStateView
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.model.AddLabelResultBean
import com.sdy.jitangapplication.model.LabelQualityBean
import com.sdy.jitangapplication.model.MyLabelBean
import com.sdy.jitangapplication.model.NewLabel
import com.sdy.jitangapplication.presenter.LabelQualityPresenter
import com.sdy.jitangapplication.presenter.view.LabelQualityView
import com.sdy.jitangapplication.ui.adapter.LabelQualityAdapter
import com.sdy.jitangapplication.ui.dialog.CorrectDialog
import kotlinx.android.synthetic.main.activity_label_quality.*
import kotlinx.android.synthetic.main.correct_dialog_layout.*
import kotlinx.android.synthetic.main.error_layout.view.*
import kotlinx.android.synthetic.main.layout_actionbar.*

/**
 * 标签特质
 */
class LabelQualityActivity : BaseMvpActivity<LabelQualityPresenter>(), LabelQualityView, View.OnClickListener {
    companion object {
        const val MIN_QUALITY = 1
        const val MAX_QUALITY = 5
        const val MODE_NEW = 1
        const val MODE_EDIT = 2
    }

    private val labelBean by lazy { intent.getSerializableExtra("data") as NewLabel? }
    private val myLabelBean by lazy { intent.getSerializableExtra("aimData") as MyLabelBean? }
    //所有特质适配器
    private val adapter by lazy { LabelQualityAdapter(false) }
    //已经选择的特质适配器
    private val choosedQualityAdapter by lazy { LabelQualityAdapter(true) }
    //所有特质中选中的特质
    private val choosedFromAllQuality = mutableListOf<LabelQualityBean>()
    //用户自拟标签特质
    private val customQuality = mutableListOf<String>()

    private val from by lazy { intent.getIntExtra("from", AddLabelActivity.FROM_ADD_NEW) }

    private val mode by lazy { intent.getIntExtra("mode", LabelQualityActivity.MODE_NEW) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_label_quality)
        initView()
        mPresenter.getTagTraitInfo(
            hashMapOf(
                "tag_id" to if (mode == MODE_EDIT) {
                    myLabelBean!!.tag_id
                } else {
                    labelBean!!.id
                }, "type" to MyLabelQualityActivity.TYPE_QUALITY
            )
        )

    }

    private fun initView() {
        mPresenter = LabelQualityPresenter()
        mPresenter.context = this
        mPresenter.mView = this

        btnBack.setOnClickListener(this)
        rightBtn1.setOnClickListener(this)
        hotT1.visibility = View.INVISIBLE
        rightBtn1.isVisible = true
        rightBtn1.text =
            if (mode == MODE_NEW || from == AddLabelActivity.FROM_ADD_NEW || from == AddLabelActivity.FROM_REGISTER) {
                "保存并继续"
            } else {
                "保存"
            }
        rightBtn1.setBackgroundResource(R.drawable.selector_confirm_btn_25dp)
        rightBtn1.isEnabled = false

        stateLabelQuality.retryBtn.onClick {
            stateLabelQuality.viewState = MultiStateView.VIEW_STATE_LOADING
            mPresenter.getTagTraitInfo(
                hashMapOf(
                    "tag_id" to if (from == AddLabelActivity.FROM_EDIT) {
                        myLabelBean!!.tag_id
                    } else {
                        labelBean!!.id
                    }, "type" to MyLabelQualityActivity.TYPE_QUALITY
                )
            )
        }

        labelQualityAddBtn.setOnClickListener(this)
        val manager = FlexboxLayoutManager(this, FlexDirection.ROW, FlexWrap.WRAP)
        manager.alignItems = AlignItems.STRETCH
        manager.justifyContent = JustifyContent.FLEX_START
        labelQualityRv.layoutManager = manager
        labelQualityRv.adapter = adapter

        val manager1 = FlexboxLayoutManager(this, FlexDirection.ROW, FlexWrap.WRAP)
        manager1.alignItems = AlignItems.STRETCH
        manager1.justifyContent = JustifyContent.FLEX_START
        labelQualityChoosedRv.layoutManager = manager1
        labelQualityChoosedRv.adapter = choosedQualityAdapter
        choosedQualityAdapter.setOnItemClickListener { _, view, position ->
            val data = choosedQualityAdapter.data[position]
            for (tempData in adapter.data.withIndex()) {
                if (data.id == tempData.value.id) {
                    tempData.value.checked = false
                    adapter.notifyItemChanged(tempData.index)
                }
            }
            choosedQualityAdapter.remove(position)
            for (data1 in choosedFromAllQuality) {
                if (data1.id == data.id) {
                    adapter.addData(data1)
                    choosedFromAllQuality.remove(data1)
                    break
                }
            }
            checkConfirmEnable()
        }


        adapter.setOnItemClickListener { _, view, position ->
            val tempData = adapter.data[position]
            if (!tempData.checked && choosedQualityAdapter.data.size == MAX_QUALITY) {
                showWarningDialog(MAX_QUALITY)
                return@setOnItemClickListener
            } else {
                choosedFromAllQuality.add(tempData)
                adapter.remove(position)
//                    adapter.notifyItemChanged(position)
                choosedQualityAdapter.addData(tempData)
                checkConfirmEnable()
            }

        }


        labelQualityAddEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                labelQualityAddBtn.isEnabled = !labelQualityAddEt.text.trim().isEmpty()
            }
        })
    }

    override fun getTagTraitInfoResult(result: Boolean, data: MutableList<LabelQualityBean>?) {
        if (result) {
            stateLabelQuality.viewState = MultiStateView.VIEW_STATE_CONTENT
            adapter.addData(data ?: mutableListOf<LabelQualityBean>())

            if (myLabelBean != null) {
                choosedQualityAdapter.addData(myLabelBean!!.label_quality)
            }

            for (data in choosedQualityAdapter.data) {
                for (data1 in adapter.data.withIndex()) {
                    if (data.id == data1.value.id) {
//                        data1.unable = true
                        data1.value.checked = false
                        adapter.remove(data1.index)
                        choosedFromAllQuality.add(data1.value)
                        break
                    }
                }
            }

            checkConfirmEnable()
        } else {
            stateLabelQuality.viewState = MultiStateView.VIEW_STATE_ERROR
        }
    }


    /**
     * 检查保存按钮是否可用
     */
    private fun checkConfirmEnable() {
        t2.isVisible = choosedQualityAdapter.data.size <= 0
        labelQualityChoosedRv.isVisible = choosedQualityAdapter.data.size > 0
        rightBtn1.isEnabled = choosedQualityAdapter.data.size in 3..5
    }


    override fun addTagResult(result: Boolean, data: AddLabelResultBean?) {

    }


    private val warningDialog by lazy { CorrectDialog(this) }
    private fun showWarningDialog(type: Int) {
        warningDialog.show()
        warningDialog.correctLogo.setImageResource(R.drawable.icon_notice)
        if (type == MIN_QUALITY)
            warningDialog.correctTip.text = "至少选择${MIN_QUALITY}个"
        else
            warningDialog.correctTip.text = "最多选择${MAX_QUALITY}个"

        labelQualityRv.postDelayed({ warningDialog.dismiss() }, 1000L)
    }

    override fun onClick(view: View) {
        when (view) {
            rightBtn1 -> {
                if (choosedQualityAdapter.data.size < MIN_QUALITY) {
                    showWarningDialog(MIN_QUALITY)
                    return
                }

                if (mode == MODE_EDIT) {
                    myLabelBean!!.label_quality = choosedQualityAdapter.data
                    intent.putExtra("aimData", myLabelBean)
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                } else {
                    val tagIds = mutableListOf<Any>()
                    for (label in choosedQualityAdapter.data) {
                        tagIds.add(label.id)
                    }
                    tagIds.addAll(customQuality)

                    intent.putExtra(
                        "tag_id", if (labelBean == null) {
                            myLabelBean!!.tag_id
                        } else {
                            labelBean!!.id
                        }
                    )
                    intent.putExtra("label_quality", Gson().toJson(tagIds))
                    intent.putExtra("data", labelBean)
                    intent.setClass(this, LabelIntroduceActivity::class.java)
                    startActivity(intent)
                }

            }
            btnBack -> {
                finish()
            }
            labelQualityAddBtn -> {
                if (labelQualityAddEt.text.trim().isNullOrEmpty()) {
                    CommonFunction.toast("请先填写特质哦")
                    return
                }
                if (TextUtils.isDigitsOnly(labelQualityAddEt.text.trim())) {
                    CommonFunction.toast("请认真填写特质哦")
                    return
                }
                if (choosedQualityAdapter.data.size >= MAX_QUALITY) {
                    CommonFunction.toast("最多能填写5个兴趣特质")
                    return
                }
                if (labelQualityAddEt.text.trim().isNotEmpty() && !TextUtils.isDigitsOnly(labelQualityAddEt.text.trim())) {
                    customQuality.add(labelQualityAddEt.text.trim().toString())
                    choosedQualityAdapter.addData(LabelQualityBean(content = labelQualityAddEt.text.trim().toString()))
                    checkConfirmEnable()
                    labelQualityAddEt.setText("")
                }
            }

        }

    }


}
