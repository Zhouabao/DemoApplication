package com.example.demoapplication.ui.activity

import android.os.Bundle
import androidx.core.content.ContextCompat
import com.example.demoapplication.R
import com.example.demoapplication.model.Label
import com.example.demoapplication.presenter.LabelsPresenter
import com.example.demoapplication.presenter.view.LabelsView
import com.example.demoapplication.ui.adapter.LabelAdapter
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.kotlin.base.common.BaseApplication
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.kotlin.base.ui.adapter.BaseRecyclerViewAdapter
import jp.wasabeef.recyclerview.animators.ScaleInLeftAnimator
import kotlinx.android.synthetic.main.activity_labels.*


/**
 * 页面标签的选择和删除应当根据其父级标签来判定，
 * 如果是三级标签，就取消选中
 * 如果是二级标签，就取消选中其子级
 * 如果是一级标签，就取消选中子级和子级的子级
 */
class LabelsActivity : BaseMvpActivity<LabelsPresenter>(), LabelsView {
    private lateinit var adapter: LabelAdapter
    //拿一个集合来存储所有的标签
    private lateinit var allLabels: MutableList<Label>
    //拿一个集合来存储当前选中的标签
    private val checkedLabels: MutableList<Label> = mutableListOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_labels)
        mPresenter = LabelsPresenter()
        mPresenter.mView = this
        initView()


    }

    override fun onStart() {
        super.onStart()
        mPresenter.getLabels(1)

    }

    private fun initView() {
        btnBack.onClick {
            finish()
        }

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



        adapter.setOnItemClickListener(object : BaseRecyclerViewAdapter.OnItemClickListener<Label> {
            override fun onItemClick(item: Label, position: Int) {
//                adapter.labelList[position].checked = !item.checked
                item.checked = !item.checked
                adapter.notifyItemChanged(position)
                updateCheckedLabels(item)
                if (adapter.dataList[position].checked) {
                    mPresenter.getSubLabels(item.parId, item.subId, item.subSubId)
                } else {
                    //反选就清除父标签的所有子标签
                    adapter.notifyItemRemoved(0)
                }
            }
        })

    }

    /**
     * 获取标签数据
     */
    override fun onGetLabelsResult(labels: MutableList<Label>?) {
        if (labels != null && labels.size > 0) {
            for (index in labels.indices) {
                labels[index].parId = index
            }
            adapter.setData(labels)
        }
        if (labels != null) {
            for (label in labels)
                updateCheckedLabels(label)
        }
    }


    /**
     * 添加父级标签的子标签
     */
    override fun onGetSubLabelsResult(labels: MutableList<Label>?, parentPos: Int) {

        if (labels != null && labels.size > 0) {
            for (index in labels.indices) {
                labels[index].level = parentPos * 10
            }
            adapter.dataList.addAll(labels)
            adapter.notifyItemRangeInserted(parentPos + 1, labels.size)
//            adapter.notifyItemInserted(parentPos + 1)
        }
    }


    /**
     * 此处判断标签最少选择三个
     */

    fun updateCheckedLabels(label: Label) {
        if (label.checked) {
            if (!checkedLabels.contains(label)) {
                checkedLabels.add(label)
            }
            if (checkedLabels.size < 3) {
                completeLabelBtn.isEnabled = false
                completeLabelBtn.text = "再选${3 - checkedLabels.size}个"
                completeLabelBtn.setCompoundDrawables(null, null, null, null)
            } else {
                completeLabelBtn.isEnabled = true
                completeLabelBtn.text = "完成"
                val drawable1 = ContextCompat.getDrawable(BaseApplication.context, R.drawable.icon_gou)
                drawable1!!.setBounds(0, 0, drawable1.intrinsicWidth, drawable1.intrinsicHeight)    //需要设置图片的大小才能显示
                completeLabelBtn.setCompoundDrawables(drawable1, null, null, null)
            }
        } else {
            if (checkedLabels.contains(label)) {
                checkedLabels.remove(label)
                completeLabelBtn.isEnabled = false
                completeLabelBtn.text = "再选${3 - checkedLabels.size}个"
                completeLabelBtn.setCompoundDrawables(null, null, null, null)
            }
        }
    }


}
