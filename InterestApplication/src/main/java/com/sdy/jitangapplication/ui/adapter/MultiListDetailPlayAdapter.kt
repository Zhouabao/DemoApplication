package com.sdy.demoap

import android.animation.ObjectAnimator
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.core.view.size
import androidx.viewpager.widget.ViewPager
import com.blankj.utilcode.util.SizeUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.kotlin.base.ext.onClick
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.SquareBean
import com.sdy.jitangapplication.model.VideoJson
import com.sdy.jitangapplication.player.IjkMediaPlayerUtil
import com.sdy.jitangapplication.switchplay.SwitchUtil
import com.sdy.jitangapplication.ui.activity.MatchDetailActivity
import com.sdy.jitangapplication.ui.activity.SquarePlayListDetailActivity
import com.sdy.jitangapplication.ui.adapter.MultiListSquareAdapter
import com.sdy.jitangapplication.ui.adapter.SquareDetailImgsAdaper
import com.sdy.jitangapplication.utils.UserManager
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.android.synthetic.main.item_square_detail_play_cover.view.*
import kotlinx.android.synthetic.main.item_square_play_detail_audio.view.*
import kotlinx.android.synthetic.main.item_square_play_detail_pics.view.*
import kotlinx.android.synthetic.main.item_square_play_detail_video.view.*


/**
 *    author : ZFM
 *    date   : 2019/6/2616:27
 *    desc   :
 *    version: 1.0
 */
class MultiListDetailPlayAdapter(var context: Context, data: MutableList<SquareBean>) :
    BaseMultiItemQuickAdapter<SquareBean, BaseViewHolder>(data) {
    val TAG = "MultiListDetailPlayAdapter"

    init {
        addItemType(SquareBean.PIC, R.layout.item_square_play_detail_pics)
        addItemType(SquareBean.VIDEO, R.layout.item_square_play_detail_video)
        addItemType(SquareBean.AUDIO, R.layout.item_square_play_detail_audio)
    }


    override fun convert(holder: BaseViewHolder, item: SquareBean) {
        // 这里指定了被共享的视图元素
        holder.addOnClickListener(R.id.detailPlaydianzan)
        holder.addOnClickListener(R.id.detailPlayMoreActions)
//        holder.addOnClickListener(R.id.detailPlayComment)
        holder.addOnClickListener(R.id.detailPlayContent)
        holder.addOnClickListener(R.id.detailPlayCommentSend)
        holder.addOnClickListener(R.id.detailPlayCommentBtn)

        GlideUtil.loadAvatorImg(context, item.avatar, holder.itemView.detailPlayUserAvatar)
        holder.itemView.detailPlayUserLocationAndTime.text =

            item.province_name.plus(
                if (item.city_name != item.province_name) {
                    item.city_name
                } else {
                    ""
                }
            ).plus(
                if (!item.province_name.isNullOrEmpty() || !item.city_name.isNullOrEmpty()) {
                    "\t\t"
                } else {
                    ""
                }
            ).plus(item.out_time)
        holder.itemView.detailPlayUserName.text = item.nickname ?: ""
        holder.itemView.detailPlayContent.text = item.descr
        holder.itemView.detailPlayUserVipIv.isVisible = item.isvip == 1

        val drawable1 =
            context.resources.getDrawable(if (item.isliked == 1) R.drawable.icon_dianzan_red else R.drawable.icon_dianzan_white)
        drawable1!!.setBounds(0, 0, drawable1.intrinsicWidth, drawable1.intrinsicHeight)    //需要设置图片的大小才能显示
        holder.itemView.detailPlaydianzan.setCompoundDrawables(drawable1, null, null, null)
        holder.itemView.detailPlaydianzan.text = "${if (item.like_cnt < 0) {
            0
        } else {
            item.like_cnt
        }}"
        if (holder.itemView.detailPlayComment.text.toString().isNotEmpty())
            holder.itemView.detailPlayComment.setText("")//尤其是在评论之后清除数据
        holder.itemView.detailPlayComment.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(edit: Editable?) {
                if (onTextChangeListener != null) {
                    onTextChangeListener!!.afterTextChanged(edit.toString(), holder.layoutPosition)
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

        })

        holder.itemView.detailPlayUserAvatar.onClick {
            if (UserManager.getAccid() != item.accid) {
                MatchDetailActivity.start(mContext, item.accid)
            }
        }

        var play = false
        when (holder.itemViewType) {
            SquareBean.PIC -> {
                if (holder.itemView.detailPlayVp2Indicator1.childCount == 0) {
                    if (item.photo_json.isNullOrEmpty()) {
                        item.photo_json = mutableListOf(VideoJson(url = item.avatar))
                    }
                    holder.itemView.detailPlayVp2.adapter =
                        SquareDetailImgsAdaper(mContext, item.photo_json!!, holder.layoutPosition)

                    //自定义你的Holder，实现更多复杂的界面，不一定是图片翻页，其他任何控件翻页亦可。
                    holder.itemView.detailPlayVp2.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                        }

                        override fun onPageScrollStateChanged(state: Int) {
                        }

                        override fun onPageSelected(position: Int) {
                            for (i in 0 until holder.itemView.detailPlayVp2Indicator1.size) {
                                (holder.itemView.detailPlayVp2Indicator1[i] as RadioButton).isChecked = i == position
                            }
                        }
                    })
                    if (item.photo_json != null && item.photo_json!!.size > 1) {
                        for (i in 0 until item.photo_json!!.size) {
                            val indicator = RadioButton(mContext)
                            indicator.width = SizeUtils.dp2px(10F)
                            indicator.height = SizeUtils.dp2px(10F)
                            indicator.buttonDrawable = null
                            indicator.background = mContext.resources.getDrawable(R.drawable.selector_circle_indicator)

                            indicator.layoutParams = LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            )
                            val layoutParams: LinearLayout.LayoutParams =
                                indicator.layoutParams as LinearLayout.LayoutParams
                            layoutParams.setMargins(0, 0, SizeUtils.dp2px(6f), 0)
                            indicator.layoutParams = layoutParams

                            indicator.isChecked = i == 0
                            holder.itemView.detailPlayVp2Indicator1.addView(indicator)
                        }
                    }
                }

            }
            SquareBean.VIDEO -> {
                holder.itemView.btnBack.isVisible = false
                holder.itemView.btnBack.onClick {
                    (mContext as SquarePlayListDetailActivity).onBackPressed()
                }
                //增加封面
                val imageview = ImageView(mContext)
                imageview.scaleType = ImageView.ScaleType.CENTER_INSIDE
                GlideUtil.loadImg(mContext, item.cover_url ?: "", imageview)
                if (imageview.parent != null) {
                    val vg = imageview.parent as ViewGroup
                    vg.removeView(imageview)
                }
                holder.itemView.detailPlayVideo.thumbImageView = imageview
                holder.itemView.detailPlayVideo.playTag = MultiListSquareAdapter.TAG
                holder.itemView.detailPlayVideo.playPosition = holder.layoutPosition
                SwitchUtil.optionPlayer(
                    holder.itemView.detailPlayVideo,
                    item.video_json?.get(0)?.url ?: "",
                    true
                )
                holder.itemView.detailPlayVideo.setUp(
                    item.video_json?.get(0)?.url ?: "",
                    false,
                    null,
                    null,
                    ""
                )


                holder.itemView.detailPlayVideo.setVideoAllCallBack(object : GSYSampleCallBack() {
                    override fun onClickBlank(url: String?, vararg objects: Any?) {
                        super.onClickBlank(url, *objects)
                        (mContext as SquarePlayListDetailActivity).hideCover(holder.layoutPosition, SquareBean.VIDEO)
                    }
                })
            }
            SquareBean.AUDIO -> {
                holder.addOnClickListener(R.id.detailPlayBtn)
                holder.addOnClickListener(R.id.audioFl)

                Glide.with(mContext)
                    .load(item.avatar)
                    .thumbnail(0.5F)
                    .priority(Priority.NORMAL)
                    .transform(BlurTransformation(25))
                    .into(holder.itemView.audioFl)
//                holder.itemView.audioFl.alpha = 0.1F
                GlideUtil.loadImg(mContext, item.avatar, holder.itemView.detailPlayAudioBg)


                //设定动画作用于的控件，以及什么动画，旋转的开始角度和结束角度
                val objAnim = ObjectAnimator.ofFloat(holder.itemView.detailPlayAudioBg, "rotation", 0.0f, 360.0f)
                //设定动画的旋转周期
                objAnim.duration = (item.audio_json?.get(0)?.duration ?: 0) * 1000L
                //设置动画的插值器，这个为匀速旋转
                objAnim.interpolator = LinearInterpolator()
                //设置动画为无限重复
                objAnim.repeatCount = 0

                if (item.isPlayAudio == IjkMediaPlayerUtil.MEDIA_PLAY) { //播放中
                    if (objAnim.isPaused) {
                        objAnim.resume()
                    } else {
                        objAnim.start()
                    }
                    holder.itemView.detailPlayBtn.setImageResource(R.drawable.icon_pause_white)
                } else if (item.isPlayAudio == IjkMediaPlayerUtil.MEDIA_PAUSE) {//暂停中
                    holder.itemView.detailPlayBtn.setImageResource(R.drawable.icon_play_white)
                    objAnim.pause()
                } else if (item.isPlayAudio == IjkMediaPlayerUtil.MEDIA_STOP || item.isPlayAudio == IjkMediaPlayerUtil.MEDIA_ERROR) {//停止中
                    holder.itemView.detailPlayBtn.setImageResource(R.drawable.icon_play_white)
                    objAnim.end()
                    objAnim.cancel()
                } else if (item.isPlayAudio == IjkMediaPlayerUtil.MEDIA_PREPARE) {
                    holder.itemView.detailPlayBtn.setImageResource(R.drawable.icon_play_white)
                    objAnim.end()
                }


            }
        }

    }


    var onTextChangeListener: OnTextChangeListener? = null

    interface OnTextChangeListener {
        fun afterTextChanged(text: String, position: Int)
    }
}
