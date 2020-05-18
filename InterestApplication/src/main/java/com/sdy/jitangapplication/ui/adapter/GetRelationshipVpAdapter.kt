package com.sdy.jitangapplication.ui.adapter

import android.graphics.Color
import android.os.Build
import android.widget.ProgressBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.MyTapsBean
import com.sdy.jitangapplication.widgets.DancingNumberView
import com.sdy.jitangapplication.widgets.DividerItemDecoration
import kotlinx.android.synthetic.main.item_get_relationship_vp.view.*

/**
 *    author : ZFM
 *    date   : 2020/5/714:45
 *    desc   :
 *    version: 1.0
 */
class GetRelationshipVpAdapter(
    val watingMatchCount: DancingNumberView,
    val completeProgress: ProgressBar
) :
    BaseQuickAdapter<MyTapsBean, BaseViewHolder>(R.layout.item_get_relationship_vp) {
    //
    public var checkList = arrayOfNulls<Int>(3)

    override fun convert(helper: BaseViewHolder, item: MyTapsBean) {
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
                completeProgress.setProgress((helper.layoutPosition + 1) * 50, true)
            } else {
                completeProgress.progress = (helper.layoutPosition + 1) * 50
            }
            adapter.notifyDataSetChanged()
            checkList[helper.layoutPosition] = adapter.data[position].id
        }
    }
}