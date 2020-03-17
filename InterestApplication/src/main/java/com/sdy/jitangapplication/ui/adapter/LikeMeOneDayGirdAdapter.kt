package com.sdy.jitangapplication.ui.adapter

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.LikeMeOneDayBean
import jp.wasabeef.glide.transformations.BlurTransformation
import jp.wasabeef.glide.transformations.RoundedCornersTransformation
import kotlinx.android.synthetic.main.item_like_me_one_day_all.view.*

/**
 *    author : ZFM
 *    date   : 2019/8/516:02
 *    desc   :
 *    version: 1.0
 */
class LikeMeOneDayGirdAdapter(var freeShow: Boolean = false) :
    BaseQuickAdapter<LikeMeOneDayBean, BaseViewHolder>(R.layout.item_like_me_one_day_all) {

    private val SPAN_COUNT = 3
    override fun convert(holder: BaseViewHolder, item: LikeMeOneDayBean) {
        val itemView = holder.itemView
        val params = itemView.layoutParams as RecyclerView.LayoutParams
        params.width =
            ((ScreenUtils.getScreenWidth() - SizeUtils.dp2px(15F) * 2 - (SPAN_COUNT - 1) * SizeUtils.dp2px(12F)) * 1.0F / SPAN_COUNT).toInt()
        params.height = (16 / 9F * params.width).toInt()
        //左上右下
        params.setMargins(
            0
            , if ((holder.layoutPosition - headerLayoutCount) / 3 == 0) {
                SizeUtils.dp2px(15F)
            } else {
                SizeUtils.dp2px(12F)
            }, 0, 0
        )
        itemView.layoutParams = params

        itemView.likeMeNickname.text ="${item.nickname}"
        itemView.likeMeNickname.isSelected = true
//        23岁·处女座·21.8km备份 2
        itemView.likeMeInfo.text = "${item.age}·${if (item.gender == 1) {
            "男"
        } else {
            "女"
        }}·${item.constellation}·${item.distance}  ${if (!item.job.isNullOrEmpty()) {
            "·${item.job}"
        } else {
            ""
        }}"
        itemView.likeMeInfo.isSelected = true

        itemView.view.isVisible = !(item.is_read ?: true)
        if (freeShow) {
            itemView.likeMeInfoCover.visibility = View.GONE
            itemView.likeMeNicknameCover.visibility = View.GONE
            itemView.likeMeOneDayType.visibility = View.VISIBLE
            GlideUtil.loadRoundImgCenterCrop(mContext, item.avatar, itemView.likeMeOneDayAvator, SizeUtils.dp2px(5F))

            holder.addOnClickListener(R.id.likeMeOneDayType)
            if (item.isfriend == 1)
                itemView.likeMeOneDayType.setImageResource(R.drawable.icon_chat_with_circle)
            else
                itemView.likeMeOneDayType.setImageResource(R.drawable.icon_like_with_circle)
        } else {
            itemView.likeMeOneDayType.visibility = View.INVISIBLE
            itemView.likeMeInfoCover.visibility = View.VISIBLE
            itemView.likeMeNicknameCover.visibility = View.VISIBLE
            val transformation = MultiTransformation(
                CenterCrop(),
                RoundedCornersTransformation(SizeUtils.dp2px(5F), 0),
                BlurTransformation(25)
            )
            Glide.with(mContext)
                .load(item.avatar ?: "")
                .priority(Priority.NORMAL)
                .thumbnail(0.5F)
                .transform(transformation)
                .into(itemView.likeMeOneDayAvator)
        }


    }
}