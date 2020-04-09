package com.sdy.jitangapplication.ui.adapter

import android.app.Activity
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isVisible
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.kotlin.base.ext.onClick
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.ProductDetailMediaBean
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack
import kotlinx.android.synthetic.main.item_product_detail_img.view.*
import kotlinx.android.synthetic.main.item_product_detail_video.view.*


/**
 *    author : ZFM
 *    date   : 2020/4/716:14
 *    desc   :
 *    version: 1.0
 */
class ProductDetailMediaAdapter(val activity: Activity) :
    BaseMultiItemQuickAdapter<ProductDetailMediaBean, BaseViewHolder>(mutableListOf()) {

    init {
        addItemType(ProductDetailMediaBean.DETAIL_IMG, R.layout.item_product_detail_img)
        addItemType(ProductDetailMediaBean.DETAIL_VIDEO, R.layout.item_product_detail_video)
    }

    private val gsyVideoOptionBuilder by lazy { GSYVideoOptionBuilder() }

    override fun convert(helper: BaseViewHolder, item: ProductDetailMediaBean) {
        when (helper.itemViewType) {
            //图片
            ProductDetailMediaBean.DETAIL_IMG -> {
                GlideUtil.loadRoundImgCenterCrop(mContext, item.url, helper.itemView.ivProduct, 0)
            }
            //视频
            ProductDetailMediaBean.DETAIL_VIDEO -> {
                //增加封面
                val imageview = ImageView(mContext)
                imageview.scaleType = ImageView.ScaleType.CENTER_CROP
                GlideUtil.loadImg(mContext, item.cover_url, imageview)
                if (imageview.parent != null) {
                    val vg = imageview.parent as ViewGroup
                    vg.removeView(imageview)
                }

                gsyVideoOptionBuilder.setIsTouchWiget(false)
                    .setThumbImageView(imageview)
                    .setUrl(item.url)
                    .setCacheWithPlay(false)
                    .setRotateViewAuto(false)
                    .setLockLand(false)
                    .setPlayTag(MultiListSquareAdapter.TAG)
                    .setShowFullAnimation(true)
                    .setNeedLockFull(true)
                    .setPlayPosition(helper.layoutPosition)
                    .setVideoAllCallBack(object : GSYSampleCallBack() {
                        override fun onPrepared(url: String?, vararg objects: Any?) {
                            super.onPrepared(url, *objects)
                            if (!helper.itemView.detailProductPlayVideo.isIfCurrentIsFullscreen) {
                                //静音
                                GSYVideoManager.instance().isNeedMute = true
                            }
                        }

                        override fun onQuitFullscreen(url: String?, vararg objects: Any?) {
                            super.onQuitFullscreen(url, *objects)
                            //全屏不静音
                            GSYVideoManager.instance().isNeedMute = true
                        }

                        override fun onEnterFullscreen(url: String?, vararg objects: Any?) {
                            super.onEnterFullscreen(url, *objects)
                            GSYVideoManager.instance().isNeedMute = false

                        }
                    })
                    .build(helper.itemView.detailProductPlayVideo)
                helper.itemView.detailProductPlayVideo.titleTextView.isVisible = false
                helper.itemView.detailProductPlayVideo.backButton.isVisible = false
                helper.itemView.detailProductPlayVideo.fullscreenButton.onClick {
                    helper.itemView.detailProductPlayVideo.startWindowFullscreen(
                        activity,
                        true,
                        true
                    )
                }

            }
        }
    }
}