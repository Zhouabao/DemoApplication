package com.sdy.jitangapplication.ui.activity

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.*
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.model.NewLabel
import com.sdy.jitangapplication.model.NewLabelBean
import com.sdy.jitangapplication.presenter.NewLabelsPresenter
import com.sdy.jitangapplication.presenter.view.NewLabelsView
import com.sdy.jitangapplication.ui.adapter.AllLabelAdapter
import com.sdy.jitangapplication.ui.adapter.ChooseLabelAdapter
import com.sdy.jitangapplication.ui.adapter.LabelTabAdapter
import kotlinx.android.synthetic.main.activity_new_labels.*
import org.jetbrains.anko.toast

/**
 * 新的标签页面
 */
class NewLabelsActivity : BaseMvpActivity<NewLabelsPresenter>(), NewLabelsView, View.OnClickListener {

    //所有的标签
    private lateinit var labels: MutableList<NewLabelBean>
    //所有标签的adapter
    private val allLabekAdapter by lazy { AllLabelAdapter() }

    //选中的标签
//    private val chooseLabels by lazy { mutableListOf<NewLabel>(NewLabel("推荐", 1, -1, true)) }
    //选中的标签的adapter
    private val chooseLabelsAdapter by lazy { ChooseLabelAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_labels)
        initData()
        initView()
    }

    private fun initData() {
        labels = mutableListOf()
        labels.add(NewLabelBean("时尚", 1, mutableListOf(NewLabel("look", 1, 1), NewLabel("球鞋控", 2, 1))))
        labels.add(NewLabelBean("宠物", 2))
        labels.add(
            NewLabelBean(
                "游戏",
                3,
                mutableListOf(
                    NewLabel("LOL", 1, 3),
                    NewLabel("吃鸡", 2, 3),
                    NewLabel("剑三", 3, 3),
                    NewLabel("王者荣耀", 4, 3)
                )
            )
        )
        labels.add(NewLabelBean("恋爱", 4))
        labels.add(
            NewLabelBean(
                "街头文化",
                5,
                mutableListOf(
                    NewLabel("街舞", 1, 5),
                    NewLabel("滑板", 2, 5),
                    NewLabel("说唱", 3, 5),
                    NewLabel("二次元", 4, 5)
                )
            )
        )
        labels.add(NewLabelBean("机车", 6))
        labels.add(NewLabelBean("180", 7))
        labels.add(
            NewLabelBean(
                "时尚", 8,
                mutableListOf(NewLabel("look", 1, 8), NewLabel("球鞋控", 2, 8))
            )
        )
        labels.add(NewLabelBean("宠物", 9))
        labels.add(
            NewLabelBean(
                "游戏",
                10,
                mutableListOf(
                    NewLabel("LOL", 1, 10),
                    NewLabel("吃鸡", 2, 10),
                    NewLabel("剑三", 3, 10),
                    NewLabel("王者荣耀", 4, 10)
                )
            )
        )
        labels.add(NewLabelBean("恋爱", 11))
        labels.add(
            NewLabelBean(
                "街头文化",
                12,
                mutableListOf(
                    NewLabel("街舞", 1, 12),
                    NewLabel("滑板", 2, 12),
                    NewLabel("说唱", 3, 12),
                    NewLabel("二次元", 4, 12)
                )
            )
        )
        labels.add(
            NewLabelBean(
                "机车", 13, mutableListOf(
                    NewLabel("街舞", 1, 13),
                    NewLabel("滑板", 2, 13),
                    NewLabel("说唱", 3, 13),
                    NewLabel("二次元", 4, 13)
                )
            )
        )
        labels.add(
            NewLabelBean(
                "180", 14, mutableListOf(
                    NewLabel("街舞", 1, 14),
                    NewLabel("滑板", 2, 14),
                    NewLabel("说唱", 3, 14),
                    NewLabel("二次元", 4, 14)
                )
            )
        )

        val allLabel = NewLabelBean("全部", 0, checked = true)
        for (label in labels) {
            allLabel.newLabels.addAll(label.newLabels)
        }
        labels.add(0, allLabel)
    }

    private fun initView() {
        mPresenter = NewLabelsPresenter()
        mPresenter.mView = this
        mPresenter.context = this
        completeLabelLL.setOnClickListener(this)

        initIndicator()


        allLabelsRv.layoutManager = GridLayoutManager(this, 3, RecyclerView.VERTICAL, false)
        allLabelsRv.adapter = allLabekAdapter
        allLabekAdapter.setNewData(labels[0].newLabels)
        allLabekAdapter.setOnItemClickListener { _, view, position ->
            allLabekAdapter.data[position].checked = !allLabekAdapter.data[position].checked
            if (allLabekAdapter.data[position].checked) {
                //选中标签中添加
                if (!chooseLabelsAdapter.data.contains(allLabekAdapter.data[position])) {
                    chooseLabelsAdapter.addData(allLabekAdapter.data[position])
//                    chooseLabelsAdapter.addData(allLabekAdapter.data[position])
                }
                //在所有标签中选中状态
                for (label in labels) {
                    for (label1 in label.newLabels) {
                        if (label1.parentId == allLabekAdapter.data[position].parentId && label1.id == allLabekAdapter.data[position].id) {
                            label1.checked = true
                        }
                    }
                }
            } else {
                //选中标签中取消选中
                if (chooseLabelsAdapter.data.contains(allLabekAdapter.data[position])) {
                    chooseLabelsAdapter.data.remove(allLabekAdapter.data[position])
//                    chooseLabelsAdapter.data.remove(allLabekAdapter.data[position])
                    chooseLabelsAdapter.notifyDataSetChanged()
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
        chooseLabelsAdapter.addData(NewLabel("推荐", 1, -1, true))
        chooseLabelsAdapter.setOnItemClickListener { _, view, position ->
            if (position != 0) {
                //从选中的标签中移除
//                chooseLabels.remove(chooseLabelsAdapter.data[position])
                val removeLabel = chooseLabelsAdapter.data[position]
                chooseLabelsAdapter.data.remove(removeLabel)
                chooseLabelsAdapter.notifyDataSetChanged()
                for (label in labels) {
                    for (label1 in label.newLabels) {
                        if (label1.id == removeLabel.id && label1.parentId == removeLabel.parentId) {
                            label1.checked = false
                        }
                    }
                }
                allLabekAdapter.notifyDataSetChanged()
                checkConfirmBtnEnable()
            }
        }

    }


    //初始化指示器
    private val labelTabAdapter by lazy { LabelTabAdapter() }

    private fun initIndicator() {
        labelTabAdapter.setNewData(labels)
        tabLabelParent.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        tabLabelParent.adapter = labelTabAdapter
        labelTabAdapter.setOnItemClickListener { _, view, position ->
            for (data in labelTabAdapter.data.withIndex()) {
                data.value.checked = data.index == position
            }
            allLabekAdapter.setNewData(labels[position].newLabels)

            labelTabAdapter.notifyDataSetChanged()
        }
    }

    /**
     * 检查确定按钮是否可以启用
     */
    private fun checkConfirmBtnEnable() {
        if (chooseLabelsAdapter.data.size < 4) {
            completeLabelBtn.text = "再选${4 - chooseLabelsAdapter.data.size}个"
            completeLabelBtn.setTextColor(resources.getColor(R.color.colorBlackText))
            iconChecked.isVisible = false
            completeLabelLL.setBackgroundResource(R.drawable.shape_rectangle_unable_btn_15dp)
            completeLabelLL.isEnabled = false
        } else {
            if (chooseLabelsAdapter.data.size > 11) {
                toast("最多只能选${Constants.LABEL_MAX_COUNT}个标签")
            }
            completeLabelBtn.text = "完成"
            completeLabelBtn.setTextColor(resources.getColor(R.color.colorBlackText))
            iconChecked.isVisible = false
            completeLabelLL.setBackgroundResource(R.drawable.shape_rectangle_unable_btn_15dp)
            completeLabelLL.isEnabled = false
        }
    }


    override fun onClick(view: View) {
        when (view.id) {
            R.id.completeLabelLL -> {
                toast("完成~~")
            }

        }

    }

}
