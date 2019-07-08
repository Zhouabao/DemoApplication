package com.example.demoapplication.ui.activity

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import com.example.demoapplication.R
import com.example.demoapplication.model.LabelBean
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
import org.jetbrains.anko.startActivity


/**
 * 页面标签的选择和删除应当根据其父级标签来判定，
 * 如果是三级标签，就取消选中
 * 如果是二级标签，就取消选中其子级
 * 如果是一级标签，就取消选中子级和子级的子级
 * //todo 回去整理标签
 */
class LabelsActivity : BaseMvpActivity<LabelsPresenter>(), LabelsView, View.OnClickListener {


    private lateinit var adapter: LabelAdapter
    //拿一个集合来存储所有的标签
    private lateinit var allLabels: MutableList<LabelBean>
    //拿一个集合来存储当前选中的标签
    private val checkedLabels: MutableList<LabelBean> = mutableListOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_labels)
        mPresenter = LabelsPresenter()
        mPresenter.mView = this
        mPresenter.context = this
        initView()
        getLabel()


    }

    private fun getLabel() {
        val params = HashMap<String, String>()
//        params["accid"] = SPUtils.getInstance(Constants.SPNAME).getString("accid")
//        params["token"] = SPUtils.getInstance(Constants.SPNAME).getString("token")
        params["accid"] = "e3a623fbef21dd5fc00b189cb9949ade"
        params["token"] = "9ece2129f6400972bde861bd816ccca7"
        params["version"] = "${1}"
        params["timestamp"] = "${System.currentTimeMillis()}"
        mPresenter.getLabels(params)
    }

    private fun initView() {
        btnBack.onClick {
            finish()
        }
        completeLabelBtn.setOnClickListener(this)

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
//                adapter.labelList[position].checked = !item.checked
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
    override fun onGetLabelsResult(labels: MutableList<LabelBean>?) {
        if (labels != null && labels.size > 0) {
            adapter.setData(labels)
            allLabels = labels
        }
        if (labels != null) {
            for (label in labels)
                updateCheckedLabels(label)
        }
    }


    /**
     * 添加父级标签的子标签
     */
    override fun onGetSubLabelsResult(labels: List<LabelBean>?, parentPos: Int) {
        if (labels != null && labels.size > 0) {
            adapter.dataList.addAll(labels)
            adapter.notifyItemRangeInserted(parentPos + 1, labels.size)
        }
    }


    /**
     * 移除子级标签
     * //todo  设计标签移除的算法
     */
    override fun onRemoveSubLablesResult(label: LabelBean, parentPos: Int) {
        for (tempLabel in label.son) {
            adapter.dataList.remove(tempLabel)
            onRemoveSubLablesResult(tempLabel,parentPos)
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


    override fun onClick(view: View) {
        when (view.id) {
            R.id.completeLabelBtn -> {
                startActivity<MainActivity>()
            }
        }
    }
}
