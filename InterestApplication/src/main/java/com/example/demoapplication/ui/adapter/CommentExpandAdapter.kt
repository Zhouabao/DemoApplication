package com.example.demoapplication.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import com.example.demoapplication.R

/**
 *    author : ZFM
 *    date   : 2019/6/2815:32
 *    desc   :
 *    version: 1.0
 */
class CommentExpandAdapter(var context: Context,var grounpCount:Int) : BaseExpandableListAdapter() {

    //    todo 根据父级数量来设置回复数

    override fun getGroupCount(): Int {
        return grounpCount
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return 2
    }

    override fun getGroup(p0: Int): Any {
        return ""
    }

    override fun getChild(p0: Int, p1: Int): Any {
        return ""
    }


    override fun getGroupView(groupPosition: Int, isExpand: Boolean, convertView: View?, viewGroup: ViewGroup?): View {
        var holder: GroupHolder? = null
        val view: View
        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_comment_parent, viewGroup, false)
            holder = GroupHolder(view)
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as GroupHolder
        }

        //todo 数据填充

        return view
    }

    override fun getChildView(
        groupPosition: Int,
        childPosition: Int,
        b: Boolean,
        convertView: View?,
        viewGroup: ViewGroup?
    ): View {
        var holder: ChildHolder? = null
        val view: View
        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_comment_child, viewGroup, false)
            holder = ChildHolder(view)
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as ChildHolder
        }

        //todo 数据填充

        return view
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition * 1L
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return getCombinedChildId(groupPosition * 1L, childPosition * 1L)
    }

    override fun isChildSelectable(p0: Int, p1: Int): Boolean {
        return true
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    class GroupHolder(view: View)

    class ChildHolder(view: View)


}