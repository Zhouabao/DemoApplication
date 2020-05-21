package com.sdy.jitangapplication.ui.adapter

import android.graphics.Color
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.widget.ProgressBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.MyTapsBean
import com.sdy.jitangapplication.widgets.DividerItemDecoration
import kotlinx.android.synthetic.main.item_get_relationship_investigate.view.*
import kotlinx.android.synthetic.main.item_get_relationship_vp.view.*

/**
 *    author : ZFM
 *    date   : 2020/5/714:45
 *    desc   :
 *    version: 1.0
 */
class GetRelationshipVpAdapter(val completeProgress: ProgressBar) :
    BaseMultiItemQuickAdapter<MyTapsBean, BaseViewHolder>(mutableListOf()) {
    init {
        addItemType(MyTapsBean.TYPE_INVESTIGATION, R.layout.item_get_relationship_investigate)
        addItemType(MyTapsBean.TYPE_MYTAP, R.layout.item_get_relationship_vp)
    }

    public var checkList = mutableListOf(-1, -1, -1, -1)
    public var channel_string: String = ""

    override fun convert(helper: BaseViewHolder, item: MyTapsBean) {
        when (helper.itemViewType) {
            MyTapsBean.TYPE_INVESTIGATION -> {
                helper.itemView.investigateTitle.text = item.title
                helper.itemView.investigateEt.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {
                        channel_string = s.toString()
                    }

                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                    }

                })
            }
            MyTapsBean.TYPE_MYTAP -> {
                helper.itemView.moreInfoTitle.text = item.title
                helper.itemView.rvRelationship.layoutManager =
                    LinearLayoutManager(mContext, RecyclerView.VERTICAL, false)

                helper.itemView.rvRelationship.addItemDecoration(
                    DividerItemDecoration(
                        mContext,
                        DividerItemDecoration.HORIZONTAL_LIST,
                        SizeUtils.dp2px(10f),
                        Color.WHITE
                    )
                )
                val adapter = GetRelationshipPicAdapter()
                helper.itemView.rvRelationship.adapter = adapter
                adapter.setNewData(item.child)
                adapter.setOnItemClickListener { _, view, position ->
                    for (data in adapter.data) {
                        data.checked = data == adapter.data[position]
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        completeProgress.setProgress(
                            ((helper.layoutPosition + 1) * 100 * 1F / (mData.size - 1)).toInt(),
                            true
                        )
                    } else {
                        completeProgress.progress =
                            ((helper.layoutPosition + 1) * 100 * 1F / (mData.size - 1)).toInt()
                    }
                    adapter.notifyDataSetChanged()
                    checkList[helper.layoutPosition] = adapter.data[position].id
                }
            }

        }

    }
}