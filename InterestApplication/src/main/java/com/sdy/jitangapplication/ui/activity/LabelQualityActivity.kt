package com.sdy.jitangapplication.ui.activity

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
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.event.RefreshEvent
import com.sdy.jitangapplication.event.UpdateFindByTagListEvent
import com.sdy.jitangapplication.event.UpdateMyLabelEvent
import com.sdy.jitangapplication.event.UserCenterEvent
import com.sdy.jitangapplication.model.AddLabelResultBean
import com.sdy.jitangapplication.model.LabelQualityBean
import com.sdy.jitangapplication.model.LabelQualitysBean
import com.sdy.jitangapplication.model.MyLabelBean
import com.sdy.jitangapplication.presenter.LabelQualityPresenter
import com.sdy.jitangapplication.presenter.view.LabelQualityView
import com.sdy.jitangapplication.ui.adapter.LabelQualityAdapter
import com.sdy.jitangapplication.ui.dialog.CorrectDialog
import com.sdy.jitangapplication.ui.dialog.LoadingDialog
import kotlinx.android.synthetic.main.activity_label_quality.*
import kotlinx.android.synthetic.main.correct_dialog_layout.*
import kotlinx.android.synthetic.main.error_layout.view.*
import kotlinx.android.synthetic.main.item_marquee_tag.view.*
import kotlinx.android.synthetic.main.layout_actionbar.*
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.startActivity

/**
 * 兴趣特质
 */
class LabelQualityActivity : BaseMvpActivity<LabelQualityPresenter>(), LabelQualityView, View.OnClickListener {

    companion object {
        const val MIN_QUALITY = 1
        const val MAX_QUALITY = 5
        const val MODE_NEW = 1
        const val MODE_EDIT = 2

        //获取兴趣的  1介绍模板 2.兴趣特质 3.兴趣意向   4.兴趣标题
        const val TYPE_TITLE = 4
    }

    //    private val labelBean by lazy { intent.getSerializableExtra("data") as NewLabel? }
    private val myLabelBean by lazy { intent.getSerializableExtra("aimData") as MyLabelBean? }
    //所有特质适配器
    private val adapter by lazy { LabelQualityAdapter(false) }
    //已经选择的特质适配器
    private val choosedQualityAdapter by lazy { LabelQualityAdapter(true) }
    //所有特质中选中的特质
    private val choosedFromAllQuality = mutableListOf<LabelQualityBean>()
    //用户自拟兴趣特质
    private val customQuality = mutableListOf<String>()
    private val mode by lazy { intent.getIntExtra("mode", MODE_NEW) }

    private var page = 1
    private val params by lazy {
        hashMapOf<String, Any>("tag_id" to myLabelBean!!.tag_id)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_label_quality)
        initView()
        mPresenter.getLabelQuality(params)

    }

    private val marqueeLabels by lazy { mutableListOf<LabelQualityBean>() }
    private fun initView() {
        mPresenter = LabelQualityPresenter()
        mPresenter.context = this
        mPresenter.mView = this

        btnBack.setOnClickListener(this)
        confirmBtn.setOnClickListener(this)
        laterBtn.setOnClickListener(this)
        switchOne.setOnClickListener(this)
        hotT1.text = myLabelBean?.title ?: "完善兴趣特质"
        confirmBtn.isEnabled = false
        stateLabelQuality.retryBtn.onClick {
            stateLabelQuality.viewState = MultiStateView.VIEW_STATE_LOADING
            mPresenter.getLabelQuality(params)
        }

        val decoration = FlexboxItemDecoration(this)
        decoration.setDrawable(resources.getDrawable(R.drawable.flex_divide_10dp))

        labelQualityAddBtn.setOnClickListener(this)
        val manager = FlexboxLayoutManager(this, FlexDirection.ROW, FlexWrap.WRAP)
        manager.alignItems = AlignItems.STRETCH
        manager.justifyContent = JustifyContent.FLEX_START
        labelQualityRv.layoutManager = manager
        labelQualityRv.addItemDecoration(decoration)
        labelQualityRv.adapter = adapter
        adapter.setOnItemClickListener { _, view, position ->
            val tempData = adapter.data[position]
            if (!tempData.isfuse && choosedQualityAdapter.data.size == MAX_QUALITY) {
                showWarningDialog(MAX_QUALITY)
                return@setOnItemClickListener
            } else {
                choosedFromAllQuality.add(tempData)
                adapter.remove(position)
//                adapter.notifyItemChanged(position)
                choosedQualityAdapter.addData(tempData)
                checkConfirmEnable()
            }

        }

        val manager1 = FlexboxLayoutManager(this, FlexDirection.ROW, FlexWrap.WRAP)
        manager1.alignItems = AlignItems.STRETCH
        manager1.justifyContent = JustifyContent.FLEX_START
        labelQualityChoosedRv.layoutManager = manager1
        labelQualityChoosedRv.addItemDecoration(decoration)
        labelQualityChoosedRv.adapter = choosedQualityAdapter
        choosedQualityAdapter.setOnItemClickListener { _, view, position ->
            val data = choosedQualityAdapter.data[position]
            for (tempData in adapter.data.withIndex()) {
                if (data.id == tempData.value.id) {
                    tempData.value.isfuse = false
                    adapter.notifyItemChanged(tempData.index)
                }
            }
            choosedQualityAdapter.remove(position)
            customQuality.remove(data.content)
            for (data1 in choosedFromAllQuality) {
                if (data1.id == data.id) {
                    adapter.addData(data1)
                    choosedFromAllQuality.remove(data1)
                    break
                }
            }

            checkConfirmEnable()
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

    private fun getMarqueeView(content: LabelQualityBean): View {
        val view = layoutInflater.inflate(R.layout.item_marquee_tag, null, false)
        view.marqueeTagName.text = content.content
        GlideUtil.loadCircleImg(this, content.icon, view.marqueeTagIcon)
        return view
    }


    /**
     * 检查保存按钮是否可用
     */
    private fun checkConfirmEnable() {
//        rightBtn1.isVisible = true
        confirmBtn.text = if (choosedQualityAdapter.data.size > 0) {
            "完成"
        } else {
            "再选${MIN_QUALITY - choosedQualityAdapter.data.size}个"
        }

        t2.isVisible = choosedQualityAdapter.data.size <= 0
        labelQualityChoosedRv.isVisible = choosedQualityAdapter.data.size > 0
        confirmBtn.isEnabled = choosedQualityAdapter.data.size in 1..5
    }


    override fun addTagResult(result: Boolean, data: AddLabelResultBean?) {
        if (result) {
            if (data != null) {
                // 这里兴趣是来自于发布或者已经在该兴趣下发布过内容，就不走发布流程
                if (mode != MODE_EDIT && !data!!.is_published) {
                    startActivity<AddLabelSuccessActivity>("data" to myLabelBean)
                }
                EventBus.getDefault().post(UpdateMyLabelEvent())
                EventBus.getDefault().post(RefreshEvent(true))
                EventBus.getDefault().post(UserCenterEvent(true))
                EventBus.getDefault().post(UpdateFindByTagListEvent())

                finish()
            }
        }
    }

    override fun getQualityResult(result: Boolean, data: LabelQualitysBean?) {
        if (result) {
            stateLabelQuality.viewState = MultiStateView.VIEW_STATE_CONTENT
            adapter.setNewData(data?.list ?: mutableListOf<LabelQualityBean>())
            switchOne.isVisible = data?.has_button ?: false
            if (page == 1) {
                if (myLabelBean != null) {
                    choosedQualityAdapter.setNewData(myLabelBean!!.label_quality)
                }
            }
            for (data in choosedQualityAdapter.data) {
                for (data1 in adapter.data.withIndex()) {
                    if (data.id == data1.value.id) {
//                        data1.unable = true
                        data1.value.isfuse = false
                        adapter.remove(data1.index)
                        choosedFromAllQuality.add(data1.value)
                        break
                    }
                }
            }
            if (data?.roll_list.isNullOrEmpty()) {
                marqueeOtherTags.isVisible = false
            } else {
                marqueeLabels.addAll(data?.roll_list ?: mutableListOf())
                for (marqueeLabel in marqueeLabels) {
                    marqueeOtherTags.addView(getMarqueeView(marqueeLabel))
                }
            }

            checkConfirmEnable()
        } else {
            stateLabelQuality.viewState = MultiStateView.VIEW_STATE_ERROR
        }
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


    private val saveParams by lazy { hashMapOf<String, Any>() }
    override fun onClick(view: View) {
        when (view) {
            confirmBtn -> {
                if (choosedQualityAdapter.data.size < MIN_QUALITY) {
                    showWarningDialog(MIN_QUALITY)
                    return
                }
                val tagIds = mutableListOf<Any>()
                for (label in choosedQualityAdapter.data) {
                    if (label.id != 0)
                        tagIds.add(label.id)
                }
                tagIds.addAll(customQuality)

                saveParams["id"] = myLabelBean!!.id
                saveParams["label_quality"] = Gson().toJson(tagIds)
                saveParams["type"] = 1
                mPresenter.saveMyQuality(saveParams)

            }
            laterBtn, btnBack -> {
                finish()
            }

            switchOne -> {
                page += 1
                mPresenter.getLabelQuality(params)
//                val iterator = choosedQualityAdapter.data.iterator()
//                while (iterator.hasNext()) {
//                    val next = iterator.next()
//                    for (data in choosedFromAllQuality) {
//                        if (next == data) {
//                            iterator.remove()
//                            break
//                        }
//                    }
//                }
//                choosedQualityAdapter.notifyDataSetChanged()

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
                    CommonFunction.toast("最多能填写${MAX_QUALITY}个兴趣特质")
                    return
                }

                var hasDuplicate = false
                for (data in choosedQualityAdapter.data) {
                    if (data.content == labelQualityAddEt.text.toString().trim()) {
                        hasDuplicate = true
                        break
                    }
                }
                if (hasDuplicate) {
                    CommonFunction.toast("不能添加重复的兴趣特质")
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


    private val loading by lazy { LoadingDialog(this) }

    override fun showLoading() {
        loading.show()
    }

    override fun hideLoading() {
        loading.dismiss()
    }


}
