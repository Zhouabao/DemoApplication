package com.example.demoapplication.ui.activity

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.ToastUtils
import com.example.demoapplication.R
import com.example.demoapplication.common.Constants
import com.example.demoapplication.model.LabelBean
import com.example.demoapplication.model.LoginBean
import com.example.demoapplication.presenter.LabelsPresenter
import com.example.demoapplication.presenter.view.LabelsView
import com.example.demoapplication.ui.adapter.LabelAdapter
import com.example.demoapplication.utils.SharedPreferenceUtil
import com.example.demoapplication.utils.UserManager
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.kotlin.base.common.AppManager
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.kotlin.base.ui.adapter.BaseRecyclerViewAdapter
import jp.wasabeef.recyclerview.animators.ScaleInLeftAnimator
import kotlinx.android.synthetic.main.activity_labels.*
import org.jetbrains.anko.startActivity


/**
 * 目前存在的问题是从发布进入标签选择要默认展开和选中已经选过的标签及其子级
 */
class LabelsActivity : BaseMvpActivity<LabelsPresenter>(), LabelsView, View.OnClickListener {


    private lateinit var adapter: LabelAdapter
    //拿一个集合来存储所有的标签
    private lateinit var allLabels: MutableList<LabelBean>
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
        btnBack.onClick {
            finish()
        }
        completeLabelLL.setOnClickListener(this)

        val manager = FlexboxLayoutManager(this)
        //item的排列方向
        manager.flexDirection = FlexDirection.ROW
        //是否换行
        manager.flexWrap = FlexWrap.WRAP
        manager.alignItems = AlignItems.STRETCH
        labelRecyclerview.layoutManager = manager
        adapter = LabelAdapter(this)
        labelRecyclerview.adapter = adapter
        //设置添加和移除动画
        labelRecyclerview.itemAnimator = ScaleInLeftAnimator()

        adapter.setOnItemClickListener(object : BaseRecyclerViewAdapter.OnItemClickListener<LabelBean> {
            override fun onItemClick(item: LabelBean, position: Int) {
//                adapter.dataList[position].checked = !item.checked
                item.checked = !item.checked
                adapter.notifyItemChanged(position)
                updateCheckedLabels(item)
                if (adapter.dataList[position].checked) {
                    mPresenter.mView.onGetSubLabelsResult(item.son, position)
                } else {
                    //反选就清除父标签的所有子标签
                    mPresenter.mView.onRemoveSubLablesResult(item, position)
                }
            }
        })

    }

    /**
     * 获取标签数据
     */
    override fun onGetLabelsResult(labels: MutableList<LabelBean>) {
        if (SPUtils.getInstance(Constants.SPNAME).getStringSet("checkedLabels").isNotEmpty()) {
            (SPUtils.getInstance(Constants.SPNAME).getStringSet("checkedLabels")).forEach {
                saveLabels.add(SharedPreferenceUtil.String2Object(it) as LabelBean)
            }

            for (i in 0 until labels.size) {
                for (j in 0 until saveLabels.size) {
                    if (labels[i].id == saveLabels[j].id) {
                        labels[i].checked = true
                        updateCheckedLabels(labels[i])
                        labels.addAll(i + 1, labels[i].son ?: mutableListOf())
                    }
                }
            }
            adapter.setData(labels)

        } else {
            if (labels != null && labels.size > 0) {
                //默认设置选中第一个标签，并加载其子标签
                labels[0].checked = true
                adapter.setData(labels)
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
        for (tempLabel in label.son!!) {
            tempLabel.checked = false
            adapter.dataList.remove(tempLabel)
            updateCheckedLabels(tempLabel)
            onRemoveSubLablesResult(tempLabel, parentPos)
        }
        adapter.notifyDataSetChanged()
    }


    /**
     * 此处判断标签最少选择三个
     */
    fun updateCheckedLabels(label: LabelBean) {
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
        if (checkedLabels.size < 3 || checkedLabels.size > Constants.LABEL_MAX_COUNT) {
//            shape_rectangle_unable_btn_15dp
            completeLabelLL.setBackgroundResource(R.drawable.shape_rectangle_unable_btn_15dp)
            completeLabelBtn.setTextColor(resources.getColor(R.color.colorBlackText))
            iconChecked.visibility = View.GONE
            completeLabelBtn.text = if (checkedLabels.size < 3) {
                "再选${3 - checkedLabels.size}个"
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
            if (intent.getStringExtra("from") != null && intent.getStringExtra("from") == "mainactivity") {
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
                val checkIds = arrayOfNulls<Int>(9)
                for (index in 0 until checkedLabels.size) {
                    checkIds[index] = checkedLabels[index].id
                }
                Log.i("params", "${android.os.Build.BRAND},${android.os.Build.HOST},${android.os.Build.PRODUCT}")
                mPresenter.uploadLabels(params, checkIds)
            }
        }
    }
}
