package com.sdy.jitangapplication.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.bigkoo.pickerview.builder.OptionsPickerBuilder
import com.bigkoo.pickerview.listener.OnOptionsSelectListener
import com.blankj.utilcode.util.ActivityUtils
import com.google.android.flexbox.*
import com.google.gson.Gson
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.event.UpdateAvatorEvent
import com.sdy.jitangapplication.event.UpdateMyLabelEvent
import com.sdy.jitangapplication.model.AddLabelResultBean
import com.sdy.jitangapplication.model.LabelQualityBean
import com.sdy.jitangapplication.model.MyLabelBean
import com.sdy.jitangapplication.presenter.MyLabelQualityPresenter
import com.sdy.jitangapplication.presenter.view.MyLabelQualityView
import com.sdy.jitangapplication.ui.adapter.LabelQualityAdapter
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.activity_my_label_quality.*
import org.greenrobot.eventbus.EventBus

/**
 * 我的兴趣属性
 */
class MyLabelQualityActivity : BaseMvpActivity<MyLabelQualityPresenter>(), MyLabelQualityView, View.OnClickListener {
    companion object {
        const val MIN_QUALITY = 3
        const val MAX_QUALITY = 5

        //获取标签的  1介绍模板 2.标签特质 3.标签意向   4.标签标题
        const val TYPE_MODEL = 1
        const val TYPE_QUALITY = 2
        const val TYPE_AIM = 3
        const val TYPE_TITLE = 4

        const val REQUEST_INTRODUCE = 100
        const val REQUEST_QUALITY = 101
    }


    //标签意愿数据源
    private var aimData: MutableList<LabelQualityBean>? = null
    //已经选中的特质
    private val chooseLabelQuality by lazy { mutableListOf<LabelQualityBean>() }
    private var labelBean: MyLabelBean? = null
    private val labelQualityMyAdapter by lazy { LabelQualityAdapter() }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_label_quality)
        initView()
        initData()

    }

    private fun initData() {
        if (labelBean != null) {
            if (chooseLabelQuality.isNotEmpty())
                chooseLabelQuality.clear()
            labelName.text = "${labelBean!!.title}"
            GlideUtil.loadImg(this, labelBean!!.icon, labelImg)
            myLabelIntroduce.text = labelBean!!.describle
            if (labelBean!!.intention.isNotEmpty())
                myLabelAim.text = labelBean!!.intention[0].content
            for (data in labelBean!!.label_quality) {
                data.checked = true
                chooseLabelQuality.add(data)
            }
            labelQualityMyAdapter.setNewData(labelBean!!.label_quality)
        }

    }

    private fun initView() {
        mPresenter = MyLabelQualityPresenter()
        mPresenter.context = this
        mPresenter.mView = this

        labelBean = intent.getSerializableExtra("aimData") as MyLabelBean
        saveLabelBtn.setOnClickListener(this)
        showAllQuality.setOnClickListener(this)
        labelReselectBtn.setOnClickListener(this)
        myLabelIntroduceBtn.setOnClickListener(this)
        myLabelAimBtn.setOnClickListener(this)


        val manager = FlexboxLayoutManager(this, FlexDirection.ROW, FlexWrap.WRAP)
        manager.alignItems = AlignItems.STRETCH
        manager.justifyContent = JustifyContent.FLEX_START
        labelMyQualityRv.layoutManager = manager
        labelMyQualityRv.adapter = labelQualityMyAdapter
    }

    override fun getTagTraitInfoResult(type: Int, result: Boolean, data: MutableList<LabelQualityBean>?) {
        if (result) {
            this.aimData = data
            showConditionPicker(data ?: mutableListOf())
        }
    }


    override fun addTagResult(result: Boolean, data: AddLabelResultBean?) {
        if (result) {
            if (data != null) {
                UserManager.saveLabels(data.list)
                EventBus.getDefault().post(UpdateAvatorEvent(true))
            }

            if (ActivityUtils.isActivityAlive(AddLabelActivity::class.java.newInstance()))
                ActivityUtils.finishActivity(AddLabelActivity::class.java)
            EventBus.getDefault().post(UpdateMyLabelEvent())
            finish()
        }
    }


    override fun onClick(view: View) {
        when (view) {
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
            saveLabelBtn -> {
                if (chooseLabelQuality.size < MIN_QUALITY) {
                    return
                }
                if (myLabelIntroduce.text.trim().isEmpty()) {
                    CommonFunction.toast("请先填写兴趣介绍")
                    return
                }
                params["tag_id"] = labelBean!!.tag_id
                params["type"] = 1
                params["describle"] = myLabelIntroduce.text
                if (labelBean!!.intention.isNotEmpty())
                    params["intention"] = Gson().toJson(mutableListOf(labelBean!!.intention[0].id))
                val tagIds = mutableListOf<Any>()
                for (label in chooseLabelQuality) {
                    if (label.id != 0)
                        tagIds.add(label.id)
                    else
                        tagIds.add(label.content)
                }
                params["label_quality"] = Gson().toJson(tagIds)
                mPresenter.addClassifyTag(params)
            }
            labelReselectBtn -> {//重选标签
                finish()
            }
            showAllQuality -> {//所有标签特质
                intent.setClass(this, LabelQualityActivity::class.java)
                startActivityForResult(intent, REQUEST_QUALITY)
            }
            myLabelIntroduceBtn -> {//兴趣介绍
                val tagIds = mutableListOf<Int>()
                for (label in chooseLabelQuality) {
                    tagIds.add(label.id)
                }

                if (labelBean!!.intention.isNotEmpty()) {
                    intent.putExtra("intention", Gson().toJson(mutableListOf(labelBean!!.intention[0].id)))
                }
                intent.putExtra("tag_id", labelBean!!.tag_id)
                if (myLabelIntroduce.text.isNotEmpty())
                    intent.putExtra("describle", myLabelIntroduce.text)
                else
                    intent.putExtra("describleHint", myLabelIntroduce.text)

                intent.putExtra("label_quality", Gson().toJson(tagIds))
                intent.putExtra("aimData", labelBean)
                intent.setClass(this, LabelIntroduceActivity::class.java)
                startActivityForResult(intent, REQUEST_INTRODUCE)
            }
            myLabelAimBtn -> { //标签意愿
                if (aimData == null) {
                    mPresenter.getTagTraitInfo(
                        hashMapOf(
                            "tag_id" to labelBean!!.tag_id,
                            "type" to TYPE_AIM
                        ), TYPE_AIM
                    )
                } else {
                    showConditionPicker(aimData ?: mutableListOf())
                }
            }

        }

    }

    private val params by lazy { hashMapOf<String, Any>() }

    /**
     * 展示条件选择器
     */
    private fun showConditionPicker(data: MutableList<LabelQualityBean>) {
        //条件选择器
        val pvOptions = OptionsPickerBuilder(this,
            OnOptionsSelectListener { options1, options2, options3, v ->
                if (data.size > 0 && data.size > options1) {
                    labelBean!!.intention.add(data[options1])
                    params["intention"] = Gson().toJson(mutableListOf(data[options1].id))
                    myLabelAim.text = data[options1].content
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
            if (requestCode == REQUEST_INTRODUCE) {
                if (data != null && !data.getStringExtra("describle").isNullOrEmpty()) {
                    myLabelIntroduce.text = data.getStringExtra("describle")
                }
            } else if (requestCode == REQUEST_QUALITY) {
                if (data != null) {
                    intent = data
                    if ((data.getSerializableExtra("aimData") as MyLabelBean) != null) {
                        labelBean = data.getSerializableExtra("aimData") as MyLabelBean
                        initData()
                    }
                }
            }
        }
    }
}
