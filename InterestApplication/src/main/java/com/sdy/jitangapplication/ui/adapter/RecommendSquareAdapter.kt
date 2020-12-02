package com.sdy.jitangapplication.ui.adapter

import android.app.Activity
import android.graphics.Color
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.airbnb.lottie.LottieAnimationView
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.VibrateUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.ext.onClick
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.model.RecommendSquareBean
import com.sdy.jitangapplication.ui.activity.MatchDetailActivity
import com.sdy.jitangapplication.ui.activity.SquareCommentDetailActivity
import com.sdy.jitangapplication.ui.activity.SquarePlayDetailActivity
import com.sdy.jitangapplication.ui.dialog.TouristDialog
import com.sdy.jitangapplication.utils.UserManager
import jp.wasabeef.glide.transformations.BlurTransformation
import jp.wasabeef.glide.transformations.RoundedCornersTransformation
import kotlinx.android.synthetic.main.item_recommend_square.view.*

//0文本 1图片 2视频 3语音
class RecommendSquareAdapter :
    BaseQuickAdapter<RecommendSquareBean, BaseViewHolder>(R.layout.item_recommend_square) {
    override fun convert(helper: BaseViewHolder, item: RecommendSquareBean) {
        val params = helper.itemView.squareImg.layoutParams as ConstraintLayout.LayoutParams
//        params.width = (ScreenUtils.getScreenWidth() - SizeUtils.dp2px(37F)) / 2
        val width = (ScreenUtils.getScreenWidth() - SizeUtils.dp2px(37F)) / 2
//        (helper.itemView.layoutParams as RecyclerView.LayoutParams).width = params.width
        params.height =
            if (item.type == 0 || item.type == 3) { //纯文本和语音是1:1
                width
            } else if (item.type == 1) { //图片
                when {
                    item.width > item.height -> (3 / 4F * width).toInt()
                    item.width < item.height -> (4 / 3F * width).toInt()
                    else -> width
                }
            } else {
                when {
                    item.width > item.height -> (3 / 4F * width).toInt()
                    item.width < item.height -> (4 / 3F * width).toInt()
                    else -> width
                }
                width
            }
        helper.itemView.squareImg.layoutParams = params
        helper.itemView.squareOnlyTextContent.layoutParams = params


        if (item.approve_type != 0) {
            helper.itemView.squareDistanceLl.isVisible = false
            helper.itemView.squareSweet.isVisible = true
            helper.itemView.squareSweetLogo.isVisible = true

            //0普通动态 1 资产 2豪车 3身材 4职业
            helper.itemView.squareSweet.text = when (item.approve_type) {
                1 -> {
                    mContext.getString(R.string.sweet_wealth_title)
                }
                2 -> {
                    mContext.getString(R.string.sweet_luxury_car_title)
                }
                3 -> {
                    mContext.getString(R.string.sweet_figure_title)
                }
                4 -> {
                    mContext.getString(R.string.sweet_job_title)
                }
                else -> {
                    ""
                }
            }

            if (item.approve_type == 1 || item.approve_type == 2 || item.approve_type == 5) {
                if (CommonFunction.isEnglishLanguage()) {
                    helper.itemView.squareSweetLogo.imageAssetsFolder = "images_sweet_logo_man_en"
                    helper.itemView.squareSweetLogo.setAnimation("data_sweet_logo_man_en.json")
                } else {
                    helper.itemView.squareSweetLogo.imageAssetsFolder = "images_sweet_logo_man"
                    helper.itemView.squareSweetLogo.setAnimation("data_sweet_logo_man.json")
                }
                helper.itemView.squareSweetLogo.playAnimation()

                helper.itemView.squareSweet.setTextColor(Color.parseColor("#FFFFCD52"))
                helper.itemView.squareSweet.setBackgroundResource(R.drawable.shape_black_9dp)
                helper.itemView.squareContent.setTextColor(Color.parseColor("#FFFFCD52"))
            } else {
                if (CommonFunction.isEnglishLanguage()) {
                    helper.itemView.squareSweetLogo.imageAssetsFolder = "images_sweet_logo_woman_en"
                    helper.itemView.squareSweetLogo.setAnimation("data_sweet_logo_woman_en.json")
                } else {
                    helper.itemView.squareSweetLogo.imageAssetsFolder = "images_sweet_logo_woman"
                    helper.itemView.squareSweetLogo.setAnimation("data_sweet_logo_woman.json")
                }
                helper.itemView.squareSweetLogo.playAnimation()

                helper.itemView.squareSweet.setTextColor(Color.WHITE)
                helper.itemView.squareSweet.setBackgroundResource(R.drawable.shape_pink_9dp)
                helper.itemView.squareContent.setTextColor(Color.parseColor("#FFFF7CA8"))
            }
        } else {
            helper.itemView.squareSweet.isVisible = false
            helper.itemView.squareSweetLogo.isVisible = false
            if (!item.is_elite) {
                helper.itemView.squareDistanceLl.isVisible = !item.distance.isNullOrEmpty()
                helper.itemView.squareDistance.text = "${item.distance}"
            } else {
                helper.itemView.squareDistanceLl.isVisible = true
                helper.itemView.squareDistance.text = mContext.getString(R.string.recommend)
            }
        }


        if (item.type == 0) {//纯文本
            helper.itemView.squareImg.visibility = View.INVISIBLE
            helper.itemView.squareAudioCover.isVisible = false
            helper.itemView.squareOnlyTextContent.isVisible = true
            helper.itemView.squareVideoState.isVisible = false

            helper.itemView.squareOnlyTextContent.text = "${item.descr}"
        } else if (item.type == 3) {//语音
            helper.itemView.squareOnlyTextContent.isVisible = false
            helper.itemView.squareImg.isVisible = true
            helper.itemView.squareAudioCover.isVisible = true
            helper.itemView.squareVideoState.isVisible = true
            helper.itemView.squareVideoState.setImageResource(R.drawable.icon_audio_recommend)


            Glide.with(mContext)
                .load(item.avatar)
                .priority(Priority.LOW)
                .thumbnail(0.5F)
                .transform(BlurTransformation(SizeUtils.dp2px(10F)))
                .into(helper.itemView.squareImg)
            GlideUtil.loadCircleImg(mContext, item.avatar, helper.itemView.squareAudioCover)
        } else if (item.type == 1) {//图片
            helper.itemView.squareOnlyTextContent.isVisible = false
            helper.itemView.squareImg.isVisible = true
            helper.itemView.squareAudioCover.isVisible = false
            helper.itemView.squareVideoState.isVisible = false
            GlideUtil.loadRoundImgCenterCrop(
                mContext,
                item.cover_url,
                helper.itemView.squareImg,
                SizeUtils.dp2px(10F),
                RoundedCornersTransformation.CornerType.TOP
            )
        } else {//视频
            helper.itemView.squareOnlyTextContent.isVisible = false
            helper.itemView.squareImg.isVisible = true
            helper.itemView.squareAudioCover.isVisible = false
            helper.itemView.squareVideoState.isVisible = true
            helper.itemView.squareVideoState.setImageResource(R.drawable.icon_play_transparent)
            GlideUtil.loadImgCenterCrop(
                mContext,
                item.cover_url,
                helper.itemView.squareImg
            )
        }



        helper.itemView.squareContent.isVisible = !item.descr.isNullOrEmpty()
        helper.itemView.squareContent.text = "${item.descr}"
        helper.itemView.squareLike.text = "${item.like_cnt}"
        helper.itemView.squareName.text = item.nickname
        GlideUtil.loadCircleImg(mContext, item.avatar, helper.itemView.squareAvator)
        //设置点赞状态
        setLikeStatus(
            item.isliked,
            item.like_cnt,
            helper.itemView.clickZanViewAni,
            helper.itemView.squareLike, false
        )

        helper.itemView.clickZanViewAni.onClick {
            if (UserManager.touristMode) {
                TouristDialog(mContext).show()
            } else
                if (item.accid != UserManager.getAccid()) {
                    clickZan(
                        helper.itemView.clickZanViewAni,
                        helper.itemView.squareLike,
                        helper.layoutPosition - headerLayoutCount
                    )
                }
        }
//        helper.itemView.squareLike.onClick {
//            if (item.accid != UserManager.getAccid()) {
//                clickZan(
//                    helper.itemView.clickZanViewImg,
//                    helper.itemView.clickZanViewAni,
//                    helper.itemView.squareLike,
//                    helper.layoutPosition - headerLayoutCount
//                )
//            }
//        }

        //点击跳转
        helper.itemView.clickWithTrigger {
            if (UserManager.touristMode) {
                TouristDialog(mContext).show()
            } else {
                if (item.type == 2)  //视频
                    SquarePlayDetailActivity.startActivity(
                        mContext as Activity,
                        id = item.id,
                        fromRecommend = true
                    )
                else {//文本.语音.图片
                    if (item.type == 1) {
                        if (item.accid != UserManager.getAccid() && !item.isliked)
                            clickZan(
                                helper.itemView.clickZanViewAni,
                                helper.itemView.squareLike,
                                helper.layoutPosition - headerLayoutCount
                            )
                    }
                    SquareCommentDetailActivity.start(
                        mContext,
                        squareId = item.id,
                        position = helper.layoutPosition - headerLayoutCount,
                        type = if (item.approve_type != 0) {
                            SquareCommentDetailActivity.TYPE_SWEET
                        } else {
                            SquareCommentDetailActivity.TYPE_SQUARE
                        },
                        gender = item.gender
                    )

                }
            }


        }

        helper.itemView.squareAvator.clickWithTrigger {
            if (UserManager.touristMode) {
                TouristDialog(mContext).show()
            } else
                MatchDetailActivity.start(mContext, item.accid)
        }
    }

    /**
     * 设置点赞状态
     */
    private fun setLikeStatus(
        isliked: Boolean,
        likeCount: Int,
        likeAnim: LottieAnimationView,
        likeView: TextView,
        animate: Boolean = true
    ) {
        likeAnim.isVisible = true
        if (isliked) {
            if (animate) {
                likeAnim.playAnimation()
                VibrateUtils.vibrate(50L)
            } else {
                likeAnim.progress = 1F
            }
        } else {
            likeAnim.progress = 0F
        }

        likeView.text = "${if (likeCount < 0) {
            0
        } else {
            likeCount
        }}"
    }


    /**
     * 点赞按钮
     */
    private fun clickZan(
        likeAnim: LottieAnimationView,
        likeBtn: TextView,
        position: Int
    ) {
        if (data[position].isliked) {
            data[position].isliked = !data[position].isliked
            data[position].like_cnt = data[position].like_cnt!!.minus(1)
        } else {
            data[position].isliked = !data[position].isliked
            data[position].like_cnt = data[position].like_cnt!!.plus(1)
        }
        setLikeStatus(data[position].isliked, data[position].like_cnt, likeAnim, likeBtn)

        likeBtn.postDelayed({
            if (data.isEmpty() || data.size - 1 < position)
                return@postDelayed
            if (data[position].originalLike == data[position].isliked) {
                return@postDelayed
            }
            val params = hashMapOf<String, Any>(
                "type" to if (!data[position].isliked) {
                    2
                } else {
                    1
                },
                "square_id" to data[position].id!!
            )
            getSquareLike(params, position)
        }, 500L)


    }


    /**
     * 点赞 取消点赞
     * 1 点赞 2取消点赞
     */
    fun getSquareLike(params: HashMap<String, Any>, position: Int) {
        RetrofitFactory.instance.create(Api::class.java)
            .getSquareLike(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<Any?>>(null) {
                override fun onNext(t: BaseResp<Any?>) {
                    super.onNext(t)
                    if (t.code == 200) {
                        onGetSquareLikeResult(position, true)
                    } else if (t.code == 403) {
                        UserManager.startToLogin(mContext as Activity)
                    } else {
                        onGetSquareLikeResult(position, false)
                    }

                }

                override fun onError(e: Throwable?) {
                }
            })
    }


    /**
     * 点赞结果
     */
    private fun onGetSquareLikeResult(position: Int, success: Boolean) {
        if (success) {
            data[position].originalLike = data[position].isliked
        } else {
            data[position].isliked = data[position].originalLike
            data[position].like_cnt = data[position].originalLikeCount
//            refreshNotifyItemChanged(position)

        }
    }

}