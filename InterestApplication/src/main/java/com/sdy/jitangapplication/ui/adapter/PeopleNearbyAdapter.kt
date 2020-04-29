package com.sdy.jitangapplication.ui.adapter

import androidx.recyclerview.widget.GridLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.netease.nim.uikit.impl.NimUIKitImpl
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.NearPersonBean
import kotlinx.android.synthetic.main.item_people_nearby.view.*

/**
 *    author : ZFM
 *    date   : 2020/4/2710:36
 *    desc   :
 *    version: 1.0
 */
class PeopleNearbyAdapter :
    BaseQuickAdapter<NearPersonBean, BaseViewHolder>(R.layout.item_people_nearby) {
    override fun convert(helper: BaseViewHolder, item: NearPersonBean) {
        val itemView = helper.itemView

        GlideUtil.loadCircleImg(mContext, item.avatar, itemView.nearPeopleAvator)
        itemView.nearPeopleName.text = item.nickname
        itemView.nearPeopleSign.text = item.sign
        itemView.nearPeopleDistance.text = "${item.distance}·${if (NimUIKitImpl.enableOnlineState()
            && !NimUIKitImpl.getOnlineStateContentProvider().getSimpleDisplay(item.accid).isNullOrEmpty()
            && NimUIKitImpl.getOnlineStateContentProvider().getSimpleDisplay(item.accid).contains(
                "在线"
            )
        ) {
            "在线"
        } else {
            "离线"
        }}"

        itemView.nearPeopleGender.text = "${item.age}岁"
        if (item.gender == 1) {
            itemView.nearPeopleGender.setCompoundDrawablesWithIntrinsicBounds(
                mContext.resources.getDrawable(
                    R.drawable.icon_gender_man_gray
                ), null, null, null
            )
        } else {
            itemView.nearPeopleGender.setCompoundDrawablesWithIntrinsicBounds(
                mContext.resources.getDrawable(
                    R.drawable.icon_gender_woman_gray
                ), null, null, null
            )
        }
        itemView.nearPeopleCollapstation.text = item.constellation
        itemView.nearPeoplePhotos.layoutManager = GridLayoutManager(mContext, 5)
        val adapter = PeopleNearbyPhotosAdapter()
        itemView.nearPeoplePhotos.adapter = adapter
        adapter.dataSize = item.photos.size
        adapter.setNewData(
            if (item.photos.size > 5) {
                item.photos.subList(0, 5)
            } else {
                item.photos
            }
        )
    }
}