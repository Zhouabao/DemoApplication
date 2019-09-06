package com.sdy.jitangapplication.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.model.LabelBean
import com.sdy.jitangapplication.presenter.LabelsPresenter
import com.sdy.jitangapplication.ui.adapter.LabelAdapter
import com.sdy.jitangapplication.utils.UserManager
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import jp.wasabeef.recyclerview.animators.ScaleInLeftAnimator
import kotlinx.android.synthetic.main.activity_publish_choose_labels.*
import org.jetbrains.anko.startActivityForResult
import java.io.Serializable


/**
 * 目前存在的问题是从发布进入标签选择要默认展开和选中已经选过的标签及其子级
 */
class PublishChooseLabelsActivity : BaseMvpActivity<LabelsPresenter>(), View.OnClickListener {
    private lateinit var adapter: LabelAdapter
    //拿一个集合来存储当前选中的标签
    private val checkedLabels: MutableList<LabelBean> = mutableListOf()
    //拿一个集合来存储之前选中的标签
    private var saveLabels: MutableList<LabelBean> = mutableListOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_publish_choose_labels)
        initView()
        saveLabels = intent.getSerializableExtra("checkedLabels") as MutableList<LabelBean>

        //初次进入获取标签
        onGetLabelsResult(UserManager.getSpLabels())
    }


    private fun initView() {
        btnBack.onClick {
            finish()
        }
        completeLabelLL.setOnClickListener(this)
        addTag.setOnClickListener(this)

        val manager = FlexboxLayoutManager(this)
        //item的排列方向
        manager.flexDirection = FlexDirection.ROW
        //是否换行
        manager.flexWrap = FlexWrap.WRAP
        manager.alignItems = AlignItems.STRETCH
        labelRecyclerview.layoutManager = manager
        adapter = LabelAdapter()
        labelRecyclerview.adapter = adapter
        //设置添加和移除动画
        labelRecyclerview.itemAnimator = ScaleInLeftAnimator()
        adapter.setOnItemClickListener { _, view, position ->
            //                adapter.dataList[position].checked = !item.checked
            adapter.data[position].checked = !adapter.data[position].checked
            adapter.notifyItemChanged(position)
            updateCheckedLabels(adapter.data[position])
            if (adapter.data[position].checked) {
                onGetSubLabelsResult(adapter.data[position].son, position)
            } else {
                //反选就清除父标签的所有子标签
                onRemoveSubLablesResult(adapter.data[position], position)
            }
        }

    }

    /**
     * 获取标签数据
     * 第一次进入页面进行标签默认选中状态整理
     */
    private fun onGetLabelsResult(labels: MutableList<LabelBean>) {
        var index = 0
        while (labels.iterator().hasNext()) {
            if (index >= labels.size) {
                break
            }

            //移除精选标签
            if (labels[index].id == Constants.RECOMMEND_TAG_ID) {
                labels.remove(labels[index])
            }


            for (j in 0 until saveLabels.size) {
                if (labels[index].id == saveLabels[j].id) {
                    labels[index].checked = true
                    updateCheckedLabels(labels[index])
                    labels.addAll(index + 1, labels[index].son ?: mutableListOf())
                    break
                }
            }
            index++
        }


        adapter.setNewData(labels)
    }


    /**
     * 添加父级标签的子标签
     */
    private fun onGetSubLabelsResult(labels: List<LabelBean>?, parentPos: Int) {
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
    private fun onRemoveSubLablesResult(label: LabelBean, parentPos: Int) {
        for (tempLabel in label.son ?: mutableListOf()) {
            tempLabel.checked = false
            adapter.data.remove(tempLabel)
            updateCheckedLabels(tempLabel)
            onRemoveSubLablesResult(tempLabel, parentPos)
        }
        adapter.notifyDataSetChanged()
    }


    /**
     * 此处判断标签最少选择1个
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
        if (checkedLabels.size == 0) {
//            shape_rectangle_unable_btn_15dp
            completeLabelLL.setBackgroundResource(R.drawable.shape_rectangle_unable_btn_15dp)
            completeLabelBtn.setTextColor(resources.getColor(R.color.colorBlackText))
            iconChecked.visibility = View.GONE
            completeLabelLL.isEnabled = false
        } else {
            completeLabelLL.setBackgroundResource(R.drawable.shape_rectangle_enable_btn_15dp)
            completeLabelLL.isEnabled = true
            completeLabelBtn.setTextColor(resources.getColor(R.color.colorWhite))
            iconChecked.visibility = View.VISIBLE
        }
    }

    companion object {
        const val REQUEST_ADDTAG = 16
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.completeLabelLL -> {
                setResult(Activity.RESULT_OK, intent.putExtra("checkedLabels", checkedLabels as Serializable))
                finish()
            }
            R.id.addTag -> {
                startActivityForResult<LabelsActivity>(REQUEST_ADDTAG, "from" to "publish")
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_ADDTAG) {
                checkedLabels.clear()
                onGetLabelsResult(UserManager.getSpLabels())
            }
        }
    }
}
