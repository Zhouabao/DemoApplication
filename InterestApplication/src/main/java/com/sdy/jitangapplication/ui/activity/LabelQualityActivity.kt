package com.sdy.jitangapplication.ui.activity

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.google.android.flexbox.*
import com.google.gson.Gson
import com.kennyc.view.MultiStateView
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.model.LabelQualityBean
import com.sdy.jitangapplication.model.LoginBean
import com.sdy.jitangapplication.model.NewLabel
import com.sdy.jitangapplication.presenter.LabelQualityPresenter
import com.sdy.jitangapplication.presenter.view.LabelQualityView
import com.sdy.jitangapplication.ui.adapter.LabelQualityAdapter
import com.sdy.jitangapplication.ui.dialog.CorrectDialog
import kotlinx.android.synthetic.main.activity_label_quality.*
import kotlinx.android.synthetic.main.correct_dialog_layout.*
import kotlinx.android.synthetic.main.error_layout.view.*

/**
 * 标签特质
 */
class LabelQualityActivity : BaseMvpActivity<LabelQualityPresenter>(), LabelQualityView, View.OnClickListener {
    companion object {
        const val MIN_QUALITY = 3
        const val MAX_QUALITY = 5
    }

    private val labelBean by lazy { intent.getSerializableExtra("data") as NewLabel }
    //所有特质适配器
    private val adapter by lazy { LabelQualityAdapter(false) }
    //已经选择的特质适配器
    private val choosedQualityAdapter by lazy { LabelQualityAdapter(true) }
    //用户自拟标签特质
    private val customQuality = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_label_quality)
        initView()
        mPresenter.getTagTraitInfo(hashMapOf("tag_id" to labelBean.id, "type" to 2))

    }

    private fun initView() {
        mPresenter = LabelQualityPresenter()
        mPresenter.context = this
        mPresenter.mView = this

        stateLabelQuality.retryBtn.onClick {
            stateLabelQuality.viewState = MultiStateView.VIEW_STATE_LOADING
            mPresenter.getTagTraitInfo(hashMapOf("tag_id" to labelBean.id, "type" to 2))
        }


        nextBtn.setOnClickListener(this)
        labelReselectBtn.setOnClickListener(this)
        labelQualityAddBtn.setOnClickListener(this)
        labelName.text = "“${labelBean.title}”"
        GlideUtil.loadImg(this, labelBean.icon, labelImg)
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
                    tempData.value.unable = false
                    tempData.value.checked = false
                    adapter.notifyItemChanged(tempData.index)
                }
            }
            choosedQualityAdapter.remove(position)
        }


        adapter.setOnItemClickListener { _, view, position ->
            val tempData = adapter.data[position]
            if (!tempData.unable)
                if (!tempData.checked && choosedQualityAdapter.data.size == MAX_QUALITY) {
                    showWarningDialog(MAX_QUALITY)
                    return@setOnItemClickListener
                } else {
                    tempData.unable = true
                    tempData.checked = false
                    adapter.notifyItemChanged(position)
                    choosedQualityAdapter.addData(tempData)
                }

        }
    }

    override fun getTagTraitInfoResult(result: Boolean, data: MutableList<LabelQualityBean>?) {
        if (result) {
            stateLabelQuality.viewState = MultiStateView.VIEW_STATE_CONTENT
            adapter.addData(data ?: mutableListOf<LabelQualityBean>())
        } else {
            stateLabelQuality.viewState = MultiStateView.VIEW_STATE_ERROR
        }
    }


    override fun addTagResult(result: Boolean, data: LoginBean?) {

    }


    private val warningDialog by lazy { CorrectDialog(this) }
    fun showWarningDialog(type: Int) {
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
            nextBtn -> {
                if (choosedQualityAdapter.data.size < MIN_QUALITY) {
                    showWarningDialog(MIN_QUALITY)
                    return
                }
                val tagIds = mutableListOf<Any>()
                for (label in choosedQualityAdapter.data) {
                    tagIds.add(label.id)
                }
                tagIds.addAll(customQuality)

                intent.putExtra("tag_id", labelBean.id)
                intent.putExtra("label_quality", Gson().toJson(tagIds))
                intent.putExtra("data", labelBean)
                intent.setClass(this, LabelIntroduceActivity::class.java)
                startActivity(intent)
            }
            labelReselectBtn -> {
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
                if (labelQualityAddEt.text.trim().isNotEmpty() && !TextUtils.isDigitsOnly(labelQualityAddEt.text.trim())) {
                    customQuality.add(labelQualityAddEt.text.trim().toString())
                    choosedQualityAdapter.addData(LabelQualityBean(content = labelQualityAddEt.text.trim().toString()))
                    labelQualityAddEt.setText("")
                }
            }

        }

    }


}
