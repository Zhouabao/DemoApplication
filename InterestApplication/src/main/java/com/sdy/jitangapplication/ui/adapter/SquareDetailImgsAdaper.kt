package com.sdy.jitangapplication.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.viewpager.widget.PagerAdapter
import com.kotlin.base.ext.onClick
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.SquareBean
import com.sdy.jitangapplication.model.VideoJson
import com.sdy.jitangapplication.ui.activity.SquarePlayListDetailActivity
import kotlinx.android.synthetic.main.item_detail_img_viewpager.view.*

/**
 *    author : ZFM
 *    date   : 2019/7/1214:10
 *    desc   :
 *    version: 1.0
 */
class SquareDetailImgsAdaper(val context: Context, val datas: MutableList<VideoJson>, val position: Int = 0) :
    PagerAdapter() {
    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun getCount(): Int {
        return datas.size
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = LayoutInflater.from(context).inflate(R.layout.item_detail_img_viewpager, null)
        view.detailImg.transitionName = "imageview"
        GlideUtil.loadRoundImgCenterinside(context, datas[position].url, view.detailImg, 1F, 0)
        view.onClick {
            if (context is SquarePlayListDetailActivity) {
                context.hideCover(position, SquareBean.PIC)
            }
        }


//        val imageview = RoundImageView(context)
//        imageview.setType(RoundImageView.TYPE_NORMAL)
//        imageview.layoutParams = ViewGroup.LayoutParams(ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight())
//        imageview.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        val imageView = `object` as LinearLayout
        container.removeView(imageView)
    }
}