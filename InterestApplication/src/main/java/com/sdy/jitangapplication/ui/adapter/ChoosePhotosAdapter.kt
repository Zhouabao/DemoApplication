package com.sdy.jitangapplication.ui.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseItemDraggableAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.MediaBean
import com.sdy.jitangapplication.utils.UriUtils
import kotlinx.android.synthetic.main.item_choose_photo.view.*

/**
 *    author : ZFM
 *    date   : 2019/7/2315:18
 *    desc   :
 *    version: 1.0
 *    type 0:全部相册  1:选中的    2:全部视频封面
 */
class ChoosePhotosAdapter(val type: Int = 0, var pickedPhotos: MutableList<MediaBean> = mutableListOf()) :
    BaseItemDraggableAdapter<MediaBean, BaseViewHolder>(R.layout.item_choose_photo, mutableListOf()) {

    override fun convert(helper: BaseViewHolder, item: MediaBean) {
        if (type == 1) {
            val params = helper.itemView.layoutParams as RecyclerView.LayoutParams
            params.width = SizeUtils.dp2px(58F)
            params.height = SizeUtils.dp2px(58F)
            helper.itemView.layoutParams = params
        }


        if (helper.layoutPosition == 0 && type == 1) {
            val params = helper.itemView.layoutParams as RecyclerView.LayoutParams
            params.leftMargin = SizeUtils.dp2px(15F)
            helper.itemView.layoutParams = params
        } else {
            val params = helper.itemView.layoutParams as RecyclerView.LayoutParams
            params.leftMargin = SizeUtils.dp2px(0f)
            helper.itemView.layoutParams = params
        }
        helper.addOnClickListener(R.id.choosePhoto)
        helper.addOnClickListener(R.id.choosePhotoDel)
        helper.addOnClickListener(R.id.chooseCamera)

        if (type == 0) {//0:全部相册
            helper.itemView.chooseVideoDuration.visibility = View.GONE
            if (helper.layoutPosition == 0) {
                helper.itemView.choosePhotoDel.visibility = View.GONE
                helper.itemView.chooseCamera.visibility = View.VISIBLE
                helper.itemView.choosePhoto.visibility = View.GONE
                helper.itemView.choosePhoto.setImageResource(R.drawable.icon_way_camera)
            } else {
                helper.itemView.choosePhotoDel.visibility = View.VISIBLE
                if (item.ischecked) {
                    helper.itemView.choosePhotoDel.setImageResource(R.drawable.icon_checked_video)
                } else {
                    helper.itemView.choosePhotoDel.setImageResource(R.drawable.icon_choose)
                }
                helper.itemView.chooseCamera.visibility = View.GONE
                helper.itemView.choosePhoto.visibility = View.VISIBLE
                helper.itemView.choosePhotoDel.visibility = View.VISIBLE
                GlideUtil.loadRoundImgCenterCropNoHolder(
                    mContext,
                    item.filePath,
                    helper.itemView.choosePhoto,
                    SizeUtils.dp2px(5F)
                )

            }
        } else if (type == 1) {//1:选中的相册
            helper.itemView.chooseVideoDuration.visibility = View.GONE
            GlideUtil.loadRoundImgCenterCropNoHolder(
                mContext,
                item.filePath,
                helper.itemView.choosePhoto,
                SizeUtils.dp2px(5F)
            )
            helper.itemView.choosePhotoDel.visibility = if (item.ischecked) {
                helper.itemView.choosePhotoDel.setImageResource(R.drawable.icon_delete_gray)
                View.VISIBLE
            } else {
                View.GONE
            }
        } else if (type == 2) {
            if (helper.layoutPosition == 0) {
                helper.itemView.choosePhotoDel.visibility = View.GONE
                helper.itemView.chooseCamera.visibility = View.VISIBLE
                helper.itemView.choosePhoto.visibility = View.GONE
                helper.itemView.chooseVideoDuration.visibility = View.GONE
                helper.itemView.choosePhoto.setImageResource(R.drawable.icon_way_camera)
            } else {
                helper.itemView.choosePhotoDel.visibility = View.VISIBLE
                if (item.ischecked) {
                    helper.itemView.choosePhotoDel.setImageResource(R.drawable.icon_checked_video)
                } else {
                    helper.itemView.choosePhotoDel.setImageResource(R.drawable.icon_choose)
                }
                GlideUtil.loadRoundImgCenterCropNoHolder(
                    mContext,
                    item.filePath,
                    helper.itemView.choosePhoto,
                    SizeUtils.dp2px(5F)
                )
                helper.itemView.chooseCamera.visibility = View.GONE
                helper.itemView.choosePhoto.visibility = View.VISIBLE
                helper.itemView.chooseVideoDuration.visibility = View.VISIBLE
                //
                helper.itemView.chooseVideoDuration.text = UriUtils.getShowTime(item.duration / 1000)

            }
        }
    }
}