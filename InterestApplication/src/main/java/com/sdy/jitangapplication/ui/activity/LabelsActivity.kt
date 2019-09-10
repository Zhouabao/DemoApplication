package com.sdy.jitangapplication.ui.activity

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.ToastUtils
import com.google.android.flexbox.*
import com.kennyc.view.MultiStateView
import com.kotlin.base.common.AppManager
import com.kotlin.base.ext.onClick
import com.kotlin.base.ext.setVisible
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.event.UpdateAvatorEvent
import com.sdy.jitangapplication.model.LabelBean
import com.sdy.jitangapplication.model.LoginBean
import com.sdy.jitangapplication.presenter.LabelsPresenter
import com.sdy.jitangapplication.presenter.view.LabelsView
import com.sdy.jitangapplication.ui.adapter.LabelAdapter
import com.sdy.jitangapplication.utils.UserManager
import jp.wasabeef.recyclerview.animators.ScaleInLeftAnimator
import kotlinx.android.synthetic.main.activity_labels.*
import kotlinx.android.synthetic.main.error_layout.view.*
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.startActivity

class LabelsActivity : BaseMvpActivity<LabelsPresenter>(), LabelsView, View.OnClickListener {

    private lateinit var adapter: LabelAdapter
    //拿一个集合来存储所有的标签
    private var allLabels: MutableList<LabelBean> = mutableListOf()
    //拿一个集合来存储当前选中的标签
    private val checkedLabels: MutableList<LabelBean> = mutableListOf()
    //拿一个集合来存储之前选中的标签
    private val saveLabels: MutableList<LabelBean> = mutableListOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_labels)
        mPresenter = LabelsPresenter()
        mPresenter.mView = this
        mPresenter.context = this
        initView()
        getLabel()


    }

    //todo 此处的version应该要更改
    private fun getLabel() {
        val params = HashMap<String, String>()
        params["accid"] = UserManager.getAccid()
        params["token"] = UserManager.getToken()
        params["version"] = "${1}"
        params["_timestamp"] = "${System.currentTimeMillis()}"
        mPresenter.getLabels(params)
    }

    private fun initView() {
        //首页禁止滑动
        setSwipeBackEnable(false)


        stateview.retryBtn.onClick {
            stateview.viewState = MultiStateView.VIEW_STATE_LOADING
            getLabel()
        }

        if (intent.getStringExtra("from") != null && (intent.getStringExtra("from") == "mainactivity"
                    || intent.getStringExtra("from") == "publish" || intent.getStringExtra("from") == "usercenter")
        ) {
            btnBack.setVisible(true)
        } else {
            btnBack.visibility = View.INVISIBLE
        }


        btnBack.onClick {
            finish()
        }
        completeLabelLL.setOnClickListener(this)

        val manager = FlexboxLayoutManager(this, FlexDirection.ROW, FlexWrap.WRAP)
        manager.alignItems = AlignItems.STRETCH
        manager.justifyContent = JustifyContent.CENTER
        labelRecyclerview.layoutManager = manager
        adapter = LabelAdapter()
        labelRecyclerview.adapter = adapter
        //设置添加和移除动画
        labelRecyclerview.itemAnimator = ScaleInLeftAnimator()

        adapter.setOnItemClickListener { _, view, position ->
            if (adapter.data[position].id != Constants.RECOMMEND_TAG_ID) {
                adapter.data[position].checked = !adapter.data[position].checked
                adapter.notifyItemChanged(position)
                updateCheckedLabels(adapter.data[position])
                if (adapter.data[position].checked) {
                    mPresenter.mView.onGetSubLabelsResult(adapter.data[position].son, position)
                } else {
                    //反选就清除父标签的所有子标签
                    mPresenter.mView.onRemoveSubLablesResult(adapter.data[position], position)
                }
            }

        }


    }

    /**
     * 获取标签数据
     */
    override fun onGetLabelsResult(labels: MutableList<LabelBean>) {
        stateview.viewState = MultiStateView.VIEW_STATE_CONTENT
        if (SPUtils.getInstance(Constants.SPNAME).getStringSet("checkedLabels").isNotEmpty()) {
            saveLabels.addAll(UserManager.getSpLabels())
            var index = 0
            while (labels.iterator().hasNext()) {
                if (index >= labels.size) {
                    break
                }
                for (j in 0 until saveLabels.size) {
                    if (labels[index].id == saveLabels[j].id || labels[index].id == Constants.RECOMMEND_TAG_ID) {
                        labels[index].checked = true
                        updateCheckedLabels(labels[index])
                        labels.addAll(index + 1, labels[index].son ?: mutableListOf())
                        break
                    }
                }
                index++
            }
            adapter.setNewData(labels)

        } else {
            if (labels != null && labels.size > 0) {
                //默认设置选中精选标签，并加载其子标签
                for (label in labels)
                    label.checked = label.id == Constants.RECOMMEND_TAG_ID
                adapter.setNewData(labels)
                allLabels = labels
                //默认选中之后加载子标签
                mPresenter.mView.onGetSubLabelsResult(labels[0].son, 0)
            }
            if (labels != null) {
                for (label in labels)
                    updateCheckedLabels(label)
            }
        }
    }

    /**
     * 添加父级标签的子标签
     */
    override fun onGetSubLabelsResult(labels: List<LabelBean>?, parentPos: Int) {
        if (labels != null && labels.size > 0) {
            for (i in 0 until labels.size) {
                adapter.addData(parentPos + (i + 1), labels[i])
            }
        }
    }


    /**
     * 移除子级标签
     *
     */
    override fun onRemoveSubLablesResult(label: LabelBean, parentPos: Int) {
        for (tempLabel in label.son ?: mutableListOf()) {
            tempLabel.checked = false
            adapter.data.remove(tempLabel)
            updateCheckedLabels(tempLabel)
            onRemoveSubLablesResult(tempLabel, parentPos)
        }
        adapter.notifyDataSetChanged()
    }


    /**
     * 此处判断标签最少选择三个
     */
    private fun updateCheckedLabels(label: LabelBean) {
        if (label.checked) {
            if (!checkedLabels.contains(label)) {
                checkedLabels.add(label)
            }
        } else {
            //此处应该还要删除父级的子级数据
            if (checkedLabels.contains(label)) {
                checkedLabels.remove(label)
            }
        }
        if (checkedLabels.size < 4 || checkedLabels.size > Constants.LABEL_MAX_COUNT + 1) {
//            shape_rectangle_unable_btn_15dp
            completeLabelLL.setBackgroundResource(R.drawable.shape_rectangle_unable_btn_15dp)
            completeLabelBtn.setTextColor(resources.getColor(R.color.colorBlackText))
            iconChecked.visibility = View.GONE
            completeLabelBtn.text = if (checkedLabels.size < 4) {
                "再选${4 - checkedLabels.size}个"
            } else {
                ToastUtils.showShort("最多只能选${Constants.LABEL_MAX_COUNT}个标签")
                "完成"
            }
            completeLabelLL.isEnabled = false
        } else {
            completeLabelLL.setBackgroundResource(R.drawable.shape_rectangle_enable_btn_15dp)
            completeLabelLL.isEnabled = true
            completeLabelBtn.setTextColor(resources.getColor(R.color.colorWhite))
            completeLabelBtn.text = "完成"
            iconChecked.visibility = View.VISIBLE
        }
    }


    override fun onUploadLabelsResult(result: Boolean, data: LoginBean?) {
        if (result) {
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
                for (checkLabel in checkedLabels) {
                    if (checkLabel.id == Constants.RECOMMEND_TAG_ID) {
                        checkedLabels.remove(checkLabel)
                        break
                    }
                }
                for (index in 0 until checkedLabels.size) {
                    checkIds[index] = checkedLabels[index].id
                }
                Log.i("params", "${android.os.Build.BRAND},${android.os.Build.HOST},${android.os.Build.PRODUCT}")
                mPresenter.uploadLabels(params, checkIds)
            }
        }
    }

    override fun onError(text: String) {
        super.onError(text)
        stateview.viewState = MultiStateView.VIEW_STATE_ERROR
        stateview.errorMsg.text = if (mPresenter.checkNetWork()) {
            getString(R.string.retry_load_error)
        } else {
            getString(R.string.retry_net_error)
        }
    }

    override fun onBackPressed() {
        if (intent.getStringExtra("from") != null && (intent.getStringExtra("from") == "mainactivity"
                    || intent.getStringExtra("from") == "publish" || intent.getStringExtra("from") == "usercenter")
        ) {
            super.onBackPressed()
        } else {
        }
    }
}
