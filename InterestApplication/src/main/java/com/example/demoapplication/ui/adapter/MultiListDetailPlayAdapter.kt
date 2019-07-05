package com.example.demoapplication.ui.adapter

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import org.jetbrains.anko.backgroundColorResource


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
        var play = false
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



                holder.itemView.picFl.backgroundColorResource = R.color.colorBlack
                holder.itemView.detailPlayVp2.layoutManager =
                    LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
                holder.itemView.detailPlayVp2.adapter = ListSquareImgsAdapter(context, item.imgs, true)

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
                val rotateAnimation =
                    RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
                rotateAnimation.interpolator = LinearInterpolator()
                rotateAnimation.duration = 4000
                rotateAnimation.repeatCount = Animation.INFINITE
                //设定动画作用于的控件，以及什么动画，旋转的开始角度和结束角度
                val objAnim = ObjectAnimator.ofFloat(holder.itemView.detailPlayAudioBg, "rotation", 0.0f, 360.0f);
                //设定动画的旋转周期
                objAnim.setDuration(20000);
                //设置动画的插值器，这个为匀速旋转
                objAnim.setInterpolator(LinearInterpolator());
                //设置动画为无限重复
                objAnim.setRepeatCount(-1);
                //设置动画重复模式
                objAnim.setRepeatMode(ObjectAnimator.RESTART);


                holder.itemView.detailPlayBtn.onClick {
                    play = !play
                    if (play) {
                        if (objAnim.isStarted)
                            objAnim.resume()
                        else
                            objAnim.start()
                        holder.itemView.detailPlayBtn.setImageResource(R.drawable.ugc_pause_record)
//                        holder.itemView.detailPlayAudioBg.startAnimation(rotateAnimation)
                    } else {
                        holder.itemView.detailPlayBtn.setImageResource(R.drawable.icon_play_white)
                        objAnim.pause()
//                        holder.itemView.detailPlayAudioBg.clearAnimation()
                    }
                }
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
