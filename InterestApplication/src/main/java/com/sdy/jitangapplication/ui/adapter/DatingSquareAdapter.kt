package com.sdy.jitangapplication.ui.adapter

import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.model.DatingBean
import com.sdy.jitangapplication.ui.activity.DatingDetailActivity
import kotlinx.android.synthetic.main.item_layout_dating_square_man.view.*
import kotlinx.android.synthetic.main.item_layout_dating_square_woman.view.*
import org.jetbrains.anko.startActivity


/**
 * 约会适配器
 */
class DatingSquareAdapter : BaseMultiItemQuickAdapter<DatingBean, BaseViewHolder>
    (mutableListOf()) {

    init {
        addItemType(DatingBean.TYPE_WOMAN, R.layout.item_layout_dating_square_woman)
        addItemType(DatingBean.TYPE_MAN, R.layout.item_layout_dating_square_man)
    }

    override fun convert(helper: BaseViewHolder, item: DatingBean) {
        when (helper.itemViewType) {
            DatingBean.TYPE_WOMAN -> {
                val itemview = helper.itemView
                val params = itemview.layoutParams as RecyclerView.LayoutParams
                params.width = ScreenUtils.getScreenWidth() - SizeUtils.dp2px(15 * 2F)
                params.height = params.width
                itemview.layoutParams = params
                GlideUtil.loadRoundImgCenterCrop(
                    mContext,
                    item.icon,
                    itemview.datingAvatorWoman,
                    SizeUtils.dp2px(10F)
                )
                itemview.datingContentAudioWoman.setUi(
                    R.drawable.shape_rectangle_gray4df_10dp,
                    audioTip = "点击播放约会语音描述"
                )

                itemview.clickWithTrigger {
                    mContext.startActivity<DatingDetailActivity>()
                }

            }
            DatingBean.TYPE_MAN -> {
                val itemview = helper.itemView
                GlideUtil.loadCircleImg(mContext, item.icon, itemview.datingAvator)

                itemview.clickWithTrigger {
                    mContext.startActivity<DatingDetailActivity>()
                }
                itemview.datingContentAudio.setUi(
                    R.drawable.shape_rectangle_gray4df_10dp,
                    audioTip = "点击播放约会语音描述"
                )
            }
        }

    }
}