package com.sdy.jitangapplication.widgets.drag

import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.callback.ItemDragAndSwipeCallback
import com.sdy.jitangapplication.ui.adapter.UserPhotoAdapter

/**
 *    author : ZFM
 *    date   : 2019/8/116:22
 *    desc   :
 *    version: 1.0
 */
class MyItemDragAndSwipeCallback(val adapter: UserPhotoAdapter) : ItemDragAndSwipeCallback(adapter) {
    override fun onMove(
        recyclerView: RecyclerView,
        source: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
//        if (adapter.data[source.layoutPosition].isNotEmpty() && adapter.data[target.layoutPosition].isNotEmpty())
//            return true
//        else
            return super.onMove(recyclerView, source, target)
    }
}