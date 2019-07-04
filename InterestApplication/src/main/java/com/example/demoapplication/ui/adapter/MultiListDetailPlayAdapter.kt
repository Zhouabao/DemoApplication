package com.example.demoapplication.ui.adapter

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import com.blankj.utilcode.util.ImageUtils
import com.blankj.utilcode.util.ToastUtils
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.example.demoapplication.R
import com.example.demoapplication.model.MatchBean
import com.example.demoapplication.ui.dialog.TranspondDialog
import com.kotlin.base.ext.onClick
import kotlinx.android.synthetic.main.item_square_play_detail_audio.view.*
import kotlinx.android.synthetic.main.item_square_play_detail_pics.view.*
import kotlinx.android.synthetic.main.item_square_play_detail_pics.view.detailPlayComment
import kotlinx.android.synthetic.main.item_square_play_detail_pics.view.detailPlayCommentSend
import kotlinx.android.synthetic.main.item_square_play_detail_pics.view.detailPlaydianzan
import kotlinx.android.synthetic.main.item_square_play_detail_video.view.*


/**
 *    author : ZFM
 *    date   : 2019/6/2616:27
 *    desc   :
 *    version: 1.0
 */
class MultiListDetailPlayAdapter(var context: Context, data: MutableList<MatchBean>) :
    BaseMultiItemQuickAdapter<MatchBean, BaseViewHolder>(data) {

    init {
        addItemType(MatchBean.PIC, R.layout.item_square_play_detail_pics)
        addItemType(MatchBean.VIDEO, R.layout.item_square_play_detail_video)
        addItemType(MatchBean.AUDIO, R.layout.item_square_play_detail_audio)
    }

    override fun convert(holder: BaseViewHolder, item: MatchBean) {


        when (holder.itemViewType) {
            MatchBean.PIC -> {
                val drawable1 =
                    context.resources.getDrawable(if (item.zan) R.drawable.icon_dianzan_red else R.drawable.icon_dianzan)
                drawable1!!.setBounds(0, 0, drawable1.intrinsicWidth, drawable1.intrinsicHeight)    //需要设置图片的大小才能显示
                holder.itemView.detailPlaydianzan.setCompoundDrawables(drawable1, null, null, null)
                holder.itemView.detailPlaydianzan.onClick {
                    item.zan = !item.zan
                    val drawable1 =
                        context.resources.getDrawable(if (item.zan) R.drawable.icon_dianzan_red else R.drawable.icon_dianzan)
                    drawable1!!.setBounds(0, 0, drawable1.intrinsicWidth, drawable1.intrinsicHeight)    //需要设置图片的大小才能显示
                    holder.itemView.detailPlaydianzan.setCompoundDrawables(drawable1, null, null, null)
                }
                holder.itemView.detailPlayCommentSend.onClick {
                    ToastUtils.showShort(holder.itemView.detailPlayComment.text.toString())
                }

                holder.itemView.picFl.background = BitmapDrawable(
                    ImageUtils.fastBlur(
                        BitmapFactory.decodeResource(context.resources, R.drawable.img_avatar_01),
                        1f,
                        25f
                    )
                )


            }
            MatchBean.VIDEO -> {
                val drawable1 =
                    context.resources.getDrawable(if (item.zan) R.drawable.icon_dianzan_red else R.drawable.icon_dianzan)
                drawable1!!.setBounds(0, 0, drawable1.intrinsicWidth, drawable1.intrinsicHeight)    //需要设置图片的大小才能显示
                holder.itemView.detailPlaydianzan.setCompoundDrawables(drawable1, null, null, null)
                holder.itemView.detailPlaydianzan.onClick {
                    item.zan = !item.zan
                    val drawable1 =
                        context.resources.getDrawable(if (item.zan) R.drawable.icon_dianzan_red else R.drawable.icon_dianzan)
                    drawable1!!.setBounds(0, 0, drawable1.intrinsicWidth, drawable1.intrinsicHeight)    //需要设置图片的大小才能显示
                    holder.itemView.detailPlaydianzan.setCompoundDrawables(drawable1, null, null, null)
                }
                holder.itemView.detailPlayCommentSend.onClick {
                    ToastUtils.showShort(holder.itemView.detailPlayComment.text.toString())
                }

                holder.itemView.videoFl.background = BitmapDrawable(
                    ImageUtils.fastBlur(
                        BitmapFactory.decodeResource(context.resources, R.drawable.img_avatar_01),
                        1f,
                        25f
                    )
                )


//                holder.itemView.squareUserVideo.backButton.visibility = View.GONE
//                holder.itemView.squareUserVideo.fullscreenButton.onClick {
//                    holder.itemView.squareUserVideo.startWindowFullscreen(context, false, true)
//                }
//                holder.itemView.squareUserVideo.isReleaseWhenLossAudio = false
//                holder.itemView.squareUserVideo.setIsTouchWiget(false)
//                holder.itemView.squareUserVideo.isShowFullAnimation = true
            }
            MatchBean.AUDIO -> {
                val drawable1 =
                    context.resources.getDrawable(if (item.zan) R.drawable.icon_dianzan_red else R.drawable.icon_dianzan)
                drawable1!!.setBounds(0, 0, drawable1.intrinsicWidth, drawable1.intrinsicHeight)    //需要设置图片的大小才能显示
                holder.itemView.detailPlaydianzan.setCompoundDrawables(drawable1, null, null, null)
                holder.itemView.detailPlaydianzan.onClick {
                    item.zan = !item.zan
                    val drawable1 =
                        context.resources.getDrawable(if (item.zan) R.drawable.icon_dianzan_red else R.drawable.icon_dianzan)
                    drawable1!!.setBounds(0, 0, drawable1.intrinsicWidth, drawable1.intrinsicHeight)    //需要设置图片的大小才能显示
                    holder.itemView.detailPlaydianzan.setCompoundDrawables(drawable1, null, null, null)
                }
                holder.itemView.detailPlayCommentSend.onClick {
                    ToastUtils.showShort(holder.itemView.detailPlayComment.text.toString())
                }

                holder.itemView.audioFl.background = BitmapDrawable(
                    ImageUtils.fastBlur(
                        BitmapFactory.decodeResource(context.resources, R.drawable.img_avatar_01),
                        1f,
                        25f
                    )
                )
            }
        }

    }

    /**
     * 展示转发动态对话框
     */
    private fun showTranspondDialog() {
        TranspondDialog(context).show()
    }
}
