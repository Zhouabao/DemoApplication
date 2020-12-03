package com.sdy.jitangapplication.ui.adapter

import android.view.MotionEvent
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.blankj.utilcode.util.ActivityUtils.isActivityExistsInStack
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.SquareTagBean
import com.sdy.jitangapplication.ui.activity.TagDetailCategoryActivity
import com.sdy.jitangapplication.ui.dialog.TouristDialog
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.item_layout_tag_square.view.*
import org.jetbrains.anko.sdk27.coroutines.onTouch
import org.jetbrains.anko.startActivity

/**
 * 兴趣标签适配器
 */
class TagSquareAdapter :
    BaseQuickAdapter<SquareTagBean, BaseViewHolder>(R.layout.item_layout_tag_square) {
    override fun convert(helper: BaseViewHolder, item: SquareTagBean) {
        val itemview = helper.itemView
        helper.addOnClickListener(R.id.btnTagMore)
        helper.addOnClickListener(R.id.rvTagSquareImg)
        itemview.rvTagSquareImg.layoutManager = GridLayoutManager(mContext, 3)
        val adapter = TagSquarePicAdapter(3)
        adapter.addData(item.cover_list)

        itemview.rvTagSquareImg.setOnTouchListener { _, event -> helper.itemView.onTouchEvent(event) }
        adapter.setOnItemClickListener { _, view, position ->
            if (UserManager.touristMode) {
                TouristDialog(mContext).show()
            } else
                if (!isActivityExistsInStack(TagDetailCategoryActivity::class.java))
                    mContext.startActivity<TagDetailCategoryActivity>(
                        "id" to item.id,
                        "type" to TagDetailCategoryActivity.TYPE_TAG
                    )

        }




        itemview.rvTagSquareImg.adapter = adapter
        GlideUtil.loadCircleImg(mContext, item.icon, itemview.tagImg)
        itemview.tagName.text = item.title
        if (item.is_hot) {
            itemview.tagIsHot.text = mContext.getString(R.string.hot_label)
            itemview.tagIsHot.isVisible = true
        } else if (item.is_join) {
            itemview.tagIsHot.text = mContext.getString(R.string.join_label)
            itemview.tagIsHot.isVisible = true
        } else {
            itemview.tagIsHot.isVisible = false
        }
    }
}