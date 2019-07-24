package com.example.demoapplication.ui.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.example.baselibrary.glide.GlideUtil
import com.example.demoapplication.R
import com.example.demoapplication.model.MediaBean
import kotlinx.android.synthetic.main.item_choose_photo.view.*

/**
 *    author : ZFM
 *    date   : 2019/7/2315:18
 *    desc   :
 *    version: 1.0
 *    type 0:全部相册 1:选中的    2:全部视频封面
 */
class ChoosePhotosAdapter(val type: Int = 0, var pickedPhotos: MutableList<MediaBean> = mutableListOf()) :
    BaseQuickAdapter<MediaBean, BaseViewHolder>(R.layout.item_choose_photo) {

    override fun convert(helper: BaseViewHolder, item: MediaBean) {
        if (helper.layoutPosition == 0 && type == 1) {
            val params = helper.itemView.layoutParams as RecyclerView.LayoutParams
            params.leftMargin = SizeUtils.dp2px(15F)
            helper.itemView.layoutParams = params
        }

        if (type == 0) {//0:全部相册
            if (helper.layoutPosition == 0) {
                helper.itemView.choosePhotoDel.visibility = View.GONE
                helper.itemView.choosePhotoIndex.visibility = View.GONE
                helper.itemView.choosePhoto.setImageResource(R.drawable.icon_camera)
            } else {
                helper.itemView.choosePhotoDel.visibility = if (!item.ischecked) {
                    helper.itemView.choosePhotoDel.setImageResource(R.drawable.icon_choose)
                    View.VISIBLE
                } else {
                    View.GONE
                }
                helper.itemView.choosePhotoIndex.visibility = if (item.ischecked) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
                for (index in 0 until pickedPhotos.size) {
                    if (pickedPhotos[index] == item) {
                        helper.itemView.choosePhotoIndex.text = "${index + 1}"
                    }
                }
                GlideUtil.loadImg(mContext, item.filePath, helper.itemView.choosePhoto)

            }
        } else if (type == 1) {//1:选中的相册
            helper.itemView.choosePhotoIndex.visibility = View.GONE
            GlideUtil.loadImg(mContext, item.filePath, helper.itemView.choosePhoto)
            helper.itemView.choosePhotoDel.visibility = if (item.ischecked) {
                helper.itemView.choosePhotoDel.setImageResource(R.drawable.icon_delete)
                View.VISIBLE
            } else {
                View.GONE
            }
        } else if (type == 2) {
            helper.itemView.choosePhotoIndex.visibility = View.GONE
            if (helper.layoutPosition == 0) {
                helper.itemView.choosePhotoDel.visibility = View.GONE
                helper.itemView.choosePhoto.setImageResource(R.drawable.icon_camera)
            } else {
                helper.itemView.choosePhotoDel.visibility = if (item.ischecked) {
                    helper.itemView.choosePhotoDel.setImageResource(R.drawable.icon_delete)
                    View.VISIBLE
                } else {
                    View.GONE
                }
                for (index in 0 until pickedPhotos.size) {
                    if (pickedPhotos[index] == item) {
                        helper.itemView.choosePhotoIndex.text = "${index + 1}"
                    }
                }
                GlideUtil.loadImg(mContext, item.filePath, helper.itemView.choosePhoto)
            }
        }
    }
}