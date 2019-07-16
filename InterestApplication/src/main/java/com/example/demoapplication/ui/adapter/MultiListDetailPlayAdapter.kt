package com.example.demoap

import android.animation.ObjectAnimator
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.LinearLayout
import android.widget.RadioButton
import androidx.core.view.get
import androidx.core.view.size
import androidx.viewpager.widget.ViewPager
import com.blankj.utilcode.util.SizeUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions.bitmapTransform
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.example.baselibrary.glide.GlideUtil
import com.example.demoapplication.R
import com.example.demoapplication.model.SquareBean
import com.example.demoapplication.ui.adapter.SquareDetailImgsAdaper
import com.example.demoapplication.ui.dialog.TranspondDialog
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder
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

    private val gsyVideoOptionBuilder by lazy { GSYVideoOptionBuilder() }

    override fun convert(holder: BaseViewHolder, item: SquareBean) {
        // 这里指定了被共享的视图元素
        holder.addOnClickListener(R.id.detailPlaydianzan)
        holder.addOnClickListener(R.id.detailPlayMoreActions)
//        holder.addOnClickListener(R.id.detailPlayComment)
        holder.addOnClickListener(R.id.detailPlayContent)
        holder.addOnClickListener(R.id.detailPlayCommentSend)

        GlideUtil.loadAvatorImg(context, item.avatar ?: "", holder.itemView.detailPlayUserAvatar)
        holder.itemView.detailPlayUserLocationAndTime.text = item.city_name ?: "".plus("\t").plus(item.out_time ?: "")
        holder.itemView.detailPlayUserName.text = item.nickname ?: ""
        holder.itemView.detailPlayContent.text = item.descr ?: ""

        val drawable1 = context.resources.getDrawable(if (item.isliked == 1) R.drawable.icon_dianzan_red else R.drawable.icon_dianzan)
        drawable1!!.setBounds(0, 0, drawable1.intrinsicWidth, drawable1.intrinsicHeight)    //需要设置图片的大小才能显示
        holder.itemView.detailPlaydianzan.setCompoundDrawables(drawable1, null, null, null)
        holder.itemView.detailPlaydianzan.text = "${item.like_cnt}"
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


        var play = false
        when (holder.itemViewType) {
            SquareBean.PIC -> {
//                holder.itemView.picFl.setBackgroundResource(R.color.colorBlack)
                if (holder.itemView.detailPlayVp2Indicator1.childCount == 0) {
                    if (item.photo_json.isNullOrEmpty()) {
                        item.photo_json = mutableListOf(item.avatar ?: "")
                    }
                    holder.itemView.detailPlayVp2.adapter =
                        SquareDetailImgsAdaper(context, item.photo_json ?: mutableListOf())

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
                Glide.with(context)
                    .load(item.avatar!!)
                    .apply(bitmapTransform(BlurTransformation(25)))
                    .into(holder.itemView.videoFl)



                gsyVideoOptionBuilder.setIsTouchWiget(false)
//                    .setThumbImageView()
//                    .setUrl(item.video_json?.get(0) ?: "")
                    .setUrl("http://9890.vod.myqcloud.com/9890_4e292f9a3dd011e6b4078980237cc3d3.f20.mp4")
                    .setCacheWithPlay(false)
                    .setRotateViewAuto(false)
                    .setLockLand(false)
                    .setPlayTag(TAG)
                    .setPlayPosition(holder.layoutPosition)
                    .setShowFullAnimation(true)
                    .build(holder.itemView.detailPlayVideo)

                holder.itemView.detailPlayVideo.backButton.visibility = View.GONE
                holder.itemView.detailPlayVideo.fullscreenButton.visibility = View.GONE
//                holder.itemView.squareUserVideo.fullscreenButton.onClick {
//                    holder.itemView.squareUserVideo.startWindowFullscreen(context, false, true)
//                }
//                holder.itemView.squareUserVideo.isReleaseWhenLossAudio = false
//                holder.itemView.squareUserVideo.setIsTouchWiget(false)
//                holder.itemView.squareUserVideo.isShowFullAnimation = true
            }
            SquareBean.AUDIO -> {
                holder.addOnClickListener(R.id.detailPlayBtn)
                Glide.with(context)
                    .load(item.avatar!!)
                    .apply(bitmapTransform(BlurTransformation(25)))
                    .into(holder.itemView.audioFl)
                GlideUtil.loadImg(context, item.avatar ?: "", holder.itemView.detailPlayAudioBg)

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

                if (item.isPlayAudio) {
                    if (objAnim.isStarted) {
                        objAnim.resume()
                    } else {
                    }
                    objAnim.start()
                    holder.itemView.detailPlayBtn.setImageResource(R.drawable.icon_pause_white)
//
                } else {
                    holder.itemView.detailPlayBtn.setImageResource(R.drawable.icon_play_white)
                    objAnim.pause()
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

    var onTextChangeListener: OnTextChangeListener? = null

    interface OnTextChangeListener {
        fun afterTextChanged(text: String, position: Int)
    }
}
