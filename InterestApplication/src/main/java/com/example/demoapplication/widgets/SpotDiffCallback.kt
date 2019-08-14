package com.example.demoapplication.widgets

import androidx.recyclerview.widget.DiffUtil
import com.example.demoapplication.model.MatchBean

class SpotDiffCallback(
    private val old: List<MatchBean>,
    private val new: List<MatchBean>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int {
        return old.size
    }

    override fun getNewListSize(): Int {
        return new.size
    }

    override fun areItemsTheSame(oldPosition: Int, newPosition: Int): Boolean {
        return old[oldPosition].accid == new[newPosition].accid
    }

    override fun areContentsTheSame(oldPosition: Int, newPosition: Int): Boolean {
        return old[oldPosition] == new[newPosition]
    }

}
