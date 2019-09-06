package com.sdy.jitangapplication.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R

/**
 *    author : ZFM
 *    date   : 2019/6/2418:04
 *    desc   : 加载用户多张图片的adapter
 *    version: 1.0
 */
class MatchImgsPagerAdapter(var context: Context, private var datas: MutableList<String>) :
    PagerAdapter() {
    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun getCount(): Int {
        return datas.size
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val imageview: ImageView = LayoutInflater.from(context).inflate(R.layout.item_match_roundimg,null) as ImageView
        GlideUtil.loadImg(context, datas[position], imageview)
        container.addView(imageview)
        return imageview
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        val imageView = `object` as ImageView
        container.removeView(imageView)
    }


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
}