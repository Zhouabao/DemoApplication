package com.example.demoapplication.ui.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import com.blankj.utilcode.util.ScreenUtils
import com.example.baselibrary.glide.GlideUtil
import com.example.baselibrary.widgets.RoundImageView
import com.example.demoapplication.model.VideoJson

/**
 *    author : ZFM
 *    date   : 2019/7/1214:10
 *    desc   :
 *    version: 1.0
 */
class SquareDetailImgsAdaper(val context: Context, val datas: MutableList<VideoJson>) : PagerAdapter() {
    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun getCount(): Int {
        return datas.size
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val imageview = RoundImageView(context)
        imageview.setType(RoundImageView.TYPE_NORMAL)
        imageview.transitionName = "imageview"
        imageview.layoutParams = ViewGroup.LayoutParams(ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight())
        GlideUtil.loadImg(context, datas[position].url, imageview)
        container.addView(imageview)
        return imageview
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        val imageView = `object` as ImageView
        container.removeView(imageView)
    }
}