package com.sdy.jitangapplication.ui.activity

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SPUtils
import com.google.android.flexbox.*
import com.kennyc.view.MultiStateView
import com.kotlin.base.common.AppManager
import com.kotlin.base.ext.onClick
import com.kotlin.base.ext.setVisible
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.event.UpdateAvatorEvent
import com.sdy.jitangapplication.model.LoginBean
import com.sdy.jitangapplication.model.NewLabel
import com.sdy.jitangapplication.presenter.NewLabelsPresenter
import com.sdy.jitangapplication.presenter.view.NewLabelsView
import com.sdy.jitangapplication.ui.adapter.AllNewLabelAdapter
import com.sdy.jitangapplication.ui.adapter.ChooseNewLabelAdapter
import com.sdy.jitangapplication.ui.adapter.LabelTabAdapter
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.activity_new_labels.*
import kotlinx.android.synthetic.main.error_layout.view.*
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.startActivity

/**
 * 新的标签页面
 */
class NewLabelsActivity : BaseMvpActivity<NewLabelsPresenter>(), NewLabelsView, View.OnClickListener {
    override fun onUploadLabelsResult(success: Boolean, data: LoginBean?) {
        if (success) {
            if (data != null) {
                UserManager.saveUserInfo(data)
            }
            if (intent.getStringExtra("from") != null && (intent.getStringExtra("from") == "mainactivity" || intent.getStringExtra(
                    "from"
                ) == "publish" || intent.getStringExtra("from") == "usercenter")
            ) {
                EventBus.getDefault().post(UpdateAvatorEvent(true))
                setResult(Activity.RESULT_OK, intent)
                finish()
            } else {
                AppManager.instance.finishAllActivity()
                startActivity<MainActivity>()
            }
            CommonFunction.toast("上传成功！")
        }

    }

    override fun onGetLabelsResult(data: MutableList<NewLabel>) {
        stateLabel.viewState = MultiStateView.VIEW_STATE_CONTENT
        chooseLabelsAdapter.addData(NewLabel(title = "推荐", checked = true))
        data[0].checked = true//默认选中“全部”
        newLabels.addAll(data)
        if (SPUtils.getInstance(Constants.SPNAME).getStringSet("checkedLabels").isNotEmpty()) {
            for (label in UserManager.getSpLabels()) {
                for (label1 in newLabels) {
                    for (label2 in label1.son) {
                        if (label.id == label2.id && label.id != Constants.RECOMMEND_TAG_ID) {
                            label2.checked = true
                            if (!chooseLabelsAdapter.data.contains(label2))
                                chooseLabelsAdapter.addData(label2)
                        }
                    }
                }
            }
        }

        labelTabAdapter.setNewData(newLabels)
        allLabekAdapter.setNewData(newLabels[0].son)
        checkConfirmBtnEnable()
    }

    //所有的标签数据源
    private val newLabels: MutableList<NewLabel> = mutableListOf()
    //所有标签的adapter
    private val allLabekAdapter by lazy { AllNewLabelAdapter() }
    //选中的标签的adapter
    private val chooseLabelsAdapter by lazy { ChooseNewLabelAdapter() }
    //初始化指示器
    private val labelTabAdapter by lazy { LabelTabAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_labels)
        initView()
        mPresenter.tagListv2(UserManager.getToken(), UserManager.getAccid())
    }

    private fun initView() {
        mPresenter = NewLabelsPresenter()
        mPresenter.mView = this
        mPresenter.context = this


        if (intent.getStringExtra("from") != null && (intent.getStringExtra("from") == "mainactivity"
                    || intent.getStringExtra("from") == "publish" || intent.getStringExtra("from") == "usercenter")
        ) {
            btnBack.setVisible(true)
        } else {
            btnBack.visibility = View.INVISIBLE
        }

        stateLabel.retryBtn.onClick {
            stateLabel.viewState = MultiStateView.VIEW_STATE_LOADING
            mPresenter.tagListv2(UserManager.getToken(), UserManager.getAccid())
        }

        completeLabelLL.setOnClickListener(this)
        btnBack.setOnClickListener(this)

        initIndicator()


        //所有标签
        allLabelsRv.layoutManager = GridLayoutManager(this, 3, RecyclerView.VERTICAL, false)
        allLabelsRv.adapter = allLabekAdapter
        allLabekAdapter.setOnItemClickListener { _, view, position ->
            allLabekAdapter.data[position].checked = !allLabekAdapter.data[position].checked
            //在所有标签中选中状态
            for (label in newLabels) {
                for (label1 in label.son) {
                    if (label1.parent_id == allLabekAdapter.data[position].parent_id && label1.id == allLabekAdapter.data[position].id) {
                        label1.checked = allLabekAdapter.data[position].checked
                    }
                }
            }

            if (allLabekAdapter.data[position].checked) {
                //选中标签中添加
                if (!chooseLabelsAdapter.data.contains(allLabekAdapter.data[position])) {
                    chooseLabelsAdapter.addData(allLabekAdapter.data[position])
                }
            } else {
                //选中标签中取消选中
                if (chooseLabelsAdapter.data.contains(allLabekAdapter.data[position])) {
                    chooseLabelsAdapter.data.remove(allLabekAdapter.data[position])
                    chooseLabelsAdapter.notifyDataSetChanged()
//                    chooseLabelsAdapter.remove(position)
                }
            }
            allLabekAdapter.notifyItemChanged(position)
            checkConfirmBtnEnable()

        }


        val manager = FlexboxLayoutManager(this, FlexDirection.ROW, FlexWrap.WRAP)
        manager.alignItems = AlignItems.STRETCH
        manager.justifyContent = JustifyContent.FLEX_START
        choosedLabelsRv.layoutManager = manager
        choosedLabelsRv.adapter = chooseLabelsAdapter

        chooseLabelsAdapter.setOnItemClickListener { _, view, position ->
            if (position != 0) {
                //从选中的标签中移除
//                chooseLabels.remove(chooseLabelsAdapter.data[position])
                val removeLabel = chooseLabelsAdapter.data[position]
                chooseLabelsAdapter.data.remove(removeLabel)
                chooseLabelsAdapter.notifyDataSetChanged()
                for (label in newLabels) {
                    for (label1 in label.son) {
                        if (label1.id == removeLabel.id && label1.parent_id == removeLabel.parent_id) {
                            label1.checked = false
                        }
                    }
                }
                allLabekAdapter.notifyDataSetChanged()
                checkConfirmBtnEnable()
            }
        }

    }


    private fun initIndicator() {
        tabLabelParent.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        tabLabelParent.adapter = labelTabAdapter
        labelTabAdapter.setOnItemClickListener { _, view, position ->
            for (data in labelTabAdapter.data.withIndex()) {
                data.value.checked = data.index == position
            }
            allLabekAdapter.setNewData(newLabels[position].son)

            labelTabAdapter.notifyDataSetChanged()
            tabLabelParent.smoothScrollToPosition(position)
        }
    }

    /**
     * 检查确定按钮是否可以启用
     */
    private fun checkConfirmBtnEnable() {
        if (chooseLabelsAdapter.data.size < 4 || chooseLabelsAdapter.data.size > 11) {
            if (chooseLabelsAdapter.data.size < 4)
                completeLabelBtn.text = "再选${4 - chooseLabelsAdapter.data.size}个"
            if (chooseLabelsAdapter.data.size > 11) {
                completeLabelBtn.text = "完成"
                CommonFunction.toast("最多只能选${Constants.LABEL_MAX_COUNT}个标签")
            }
            completeLabelBtn.setTextColor(resources.getColor(R.color.colorBlackText))
            iconChecked.isVisible = false
            completeLabelLL.setBackgroundResource(R.drawable.shape_rectangle_unable_btn_15dp)
            completeLabelLL.isEnabled = false
        } else {
            completeLabelBtn.text = "完成"
            completeLabelBtn.setTextColor(resources.getColor(R.color.colorWhite))
            iconChecked.isVisible = true
            completeLabelLL.setBackgroundResource(R.drawable.shape_rectangle_enable_btn_15dp)
            completeLabelLL.isEnabled = true
        }
    }


    override fun onClick(view: View) {
        when (view.id) {
            R.id.completeLabelLL -> {
                val params = hashMapOf<String, String>()
                params["accid"] = SPUtils.getInstance(Constants.SPNAME).getString("accid")
                params["token"] = SPUtils.getInstance(Constants.SPNAME).getString("token")
                params["_timestamp"] = "${System.currentTimeMillis()}"
                val checkIds = arrayOfNulls<Int>(Constants.LABEL_MAX_COUNT)
                for (checkLabel in chooseLabelsAdapter.data) {
                    if (checkLabel.id == -1) {
                        chooseLabelsAdapter.data.remove(checkLabel)
                        break
                    }
                }
                for (index in 0 until chooseLabelsAdapter.data.size) {
                    checkIds[index] = chooseLabelsAdapter.data[index].id
                }
                Log.i("params", "${android.os.Build.BRAND},${android.os.Build.HOST},${android.os.Build.PRODUCT}")
                mPresenter.uploadLabels(params, checkIds)
            }
            R.id.btnBack -> {
                finish()
            }
        }

    }

    override fun onError(text: String) {
        stateLabel.viewState = MultiStateView.VIEW_STATE_ERROR
        stateLabel.errorMsg.text = text
    }

    override fun onBackPressed() {
        if (intent.getStringExtra("from") != null && (intent.getStringExtra("from") == "mainactivity"
                    || intent.getStringExtra("from") == "publish" || intent.getStringExtra("from") == "usercenter")
        ) {
            super.onBackPressed()
        }

    }
}
