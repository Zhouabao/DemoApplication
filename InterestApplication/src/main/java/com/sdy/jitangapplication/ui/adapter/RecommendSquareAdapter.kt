package com.sdy.jitangapplication.ui.adapter

import android.app.Activity
import android.graphics.Color
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.SpanUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.ext.onClick
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.model.RecommendSquareBean
import com.sdy.jitangapplication.ui.activity.SquareCommentDetailActivity
import com.sdy.jitangapplication.ui.activity.SquarePlayDetailActivity
import com.sdy.jitangapplication.ui.activity.TagDetailCategoryActivity
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.sdy.jitangapplication.utils.UserManager
import com.sdy.jitangapplication.widgets.CustomMovementMethod
import jp.wasabeef.glide.transformations.BlurTransformation
import jp.wasabeef.glide.transformations.RoundedCornersTransformation
import kotlinx.android.synthetic.main.item_recommend_square.view.*
import org.jetbrains.anko.startActivity

//0文本 1图片 2视频 3语音
class RecommendSquareAdapter : BaseQuickAdapter<RecommendSquareBean, BaseViewHolder>(R.layout.item_recommend_square) {
    override fun convert(helper: BaseViewHolder, item: RecommendSquareBean) {
        val params = helper.itemView.squareImg.layoutParams as ConstraintLayout.LayoutParams
        params.width = (ScreenUtils.getScreenWidth() - SizeUtils.dp2px(37F)) / 2
        (helper.itemView.layoutParams as RecyclerView.LayoutParams).width = params.width
        params.height =
            if (item.type == 0 || item.type == 3) { //纯文本和语音是1:1
                params.width
            } else if (item.type == 1) { //图片
                when {
                    item.width > item.height -> (3 / 4F * params.width).toInt()
                    item.width < item.height -> (4 / 3F * params.width).toInt()
                    else -> params.width
                }
            } else {
                when {
                    item.width > item.height -> (3 / 4F * params.width).toInt()
                    item.width < item.height -> (4 / 3F * params.width).toInt()
                    else -> params.width
                }
                params.width
            }
        helper.itemView.squareImg.layoutParams = params
        helper.itemView.squareOnlyTextContent.layoutParams = params


        if (item.type == 0) {//纯文本
            helper.itemView.squareImg.visibility = View.INVISIBLE
            helper.itemView.squareAudioCover.isVisible = false
            helper.itemView.squareOnlyTextContent.isVisible = true
            helper.itemView.squareVideoState.isVisible = false
            helper.itemView.squareOnlyTextContent.text =
                spannableStringBuilder(helper.itemView.squareOnlyTextContent, item)
        } else if (item.type == 3) {//语音
            helper.itemView.squareOnlyTextContent.isVisible = false
            helper.itemView.squareImg.isVisible = true
            helper.itemView.squareAudioCover.isVisible = true
            helper.itemView.squareVideoState.isVisible = true

            val transformation = MultiTransformation(
                CenterCrop(),
                BlurTransformation(SizeUtils.dp2px(10F)),
                RoundedCornersTransformation(SizeUtils.dp2px(10F), 0, RoundedCornersTransformation.CornerType.TOP)
            )
            Glide.with(mContext)
                .load(item.avatar)
                .priority(Priority.LOW)
                .thumbnail(0.5F)
                .transform(transformation)
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
            GlideUtil.loadRoundImgCenterCrop(
                mContext,
                item.cover_url,
                helper.itemView.squareImg,
                SizeUtils.dp2px(10F),
                RoundedCornersTransformation.CornerType.TOP
            )
        }

        helper.itemView.squareDistance.isVisible = !item.distance.isNullOrEmpty()
        helper.itemView.squareDistance.text = "${item.distance}"
        helper.itemView.squareContent.isVisible =
            item.type != 0 && (!item.descr.isNullOrEmpty() || !item.title_list.isNullOrEmpty())
        helper.itemView.squareContent.text = spannableStringBuilder(helper.itemView.squareContent, item)
        helper.itemView.squareLike.text = "${item.like_cnt}"
        helper.itemView.squareName.text = item.nickname
        GlideUtil.loadCircleImg(mContext, item.avatar, helper.itemView.squareAvator)
        //设置点赞状态
        setLikeStatus(item.isliked, item.like_cnt, helper.itemView.squareLike)
        //点赞
        helper.itemView.squareLike.onClick {
            clickZan(helper.itemView.squareLike, helper.layoutPosition - headerLayoutCount)
        }

        //点击跳转
        helper.itemView.onClick {
            if (item.type == 2)  //视频
                SquarePlayDetailActivity.startActivity(mContext as Activity, id = item.id,fromRecommend = true)
            else//文本.语音.图片
                SquareCommentDetailActivity.start(mContext, squareId = item.id)
        }
    }

    /**
     * 制造富文本链接跳转
     */
    private fun spannableStringBuilder(textView: TextView, item: RecommendSquareBean): SpannableStringBuilder {
        textView.movementMethod = CustomMovementMethod.getInstance()
        val spanUtils = SpanUtils.with(textView)
        for (data in item.title_list ?: mutableListOf()) {
            //点击跳转link
            val clickableSpan = object : ClickableSpan() {
                override fun updateDrawState(ds: TextPaint) {
                    ds.linkColor = Color.parseColor("#FF6796FA")
                    ds.isUnderlineText = false
                }

                override fun onClick(widget: View) {
                    if (mContext as Activity !is TagDetailCategoryActivity)
                        mContext.startActivity<TagDetailCategoryActivity>(
                            "id" to data.id,
                            "type" to TagDetailCategoryActivity.TYPE_TOPIC
                        )
                }
            }
            spanUtils.append("${data.title}")
                .setClickSpan(clickableSpan)
                .setForegroundColor(Color.parseColor("#FF6796FA"))
        }

        return spanUtils.append("${item.descr}")
            .setForegroundColor(Color.parseColor("#FF2C2F31"))
            .create()
    }

    /**
     * 设置点赞状态
     */
    private fun setLikeStatus(isliked: Boolean, likeCount: Int, likeView: TextView) {
        val drawable1 =
            mContext.resources.getDrawable(if (isliked) R.drawable.icon_zan_clicked else R.drawable.icon_zan_normal)
        drawable1!!.setBounds(0, 0, drawable1.intrinsicWidth, drawable1.intrinsicHeight)    //需要设置图片的大小才能显示
        likeView.setCompoundDrawables(drawable1, null, null, null)
        likeView.text = "${if (likeCount < 0) {
            0
        } else {
            likeCount
        }}"
    }


    /**
     * 点赞按钮
     */
    private fun clickZan(likeBtn: TextView, position: Int) {
        if (data[position].isliked) {
            data[position].isliked = !data[position].isliked
            data[position].like_cnt = data[position].like_cnt!!.minus(1)
        } else {
            data[position].isliked = !data[position].isliked
            data[position].like_cnt = data[position].like_cnt!!.plus(1)
        }
        setLikeStatus(data[position].isliked, data[position].like_cnt, likeBtn)

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
        }, 2000L)


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
                        TickDialog(mContext).show()
                    } else {
                        CommonFunction.toast(t.msg)
                        onGetSquareLikeResult(position, false)
                    }

                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(mContext).show()
                    } else {
                        CommonFunction.toast(mContext.getString(R.string.service_error))
                    }
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