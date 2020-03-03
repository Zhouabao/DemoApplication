package com.sdy.jitangapplication.ui.adapter

import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.ext.onClick
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.baselibrary.utils.CustomClickListener
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.model.SamePersonBean
import com.sdy.jitangapplication.ui.activity.MatchDetailActivity
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.item_same_person.view.*

class SquareSamePersonAdapter(var hasmore: Boolean = true) :
    BaseQuickAdapter<SamePersonBean, BaseViewHolder>(R.layout.item_same_person) {
    override fun convert(helper: BaseViewHolder, item: SamePersonBean) {
        if (helper.layoutPosition % 2 == 0) {
            (helper.itemView.layoutParams as RecyclerView.LayoutParams).leftMargin = SizeUtils.dp2px(15f)
            (helper.itemView.layoutParams as RecyclerView.LayoutParams).rightMargin = SizeUtils.dp2px(10F)
        } else {
            (helper.itemView.layoutParams as RecyclerView.LayoutParams).leftMargin = SizeUtils.dp2px(0f)
            (helper.itemView.layoutParams as RecyclerView.LayoutParams).rightMargin = SizeUtils.dp2px(15F)
        }
        if (helper.layoutPosition / 2 == 0) {
            (helper.itemView.layoutParams as RecyclerView.LayoutParams).topMargin = SizeUtils.dp2px(15f)
        } else {
            (helper.itemView.layoutParams as RecyclerView.LayoutParams).topMargin = SizeUtils.dp2px(10f)
        }
        if (helper.layoutPosition / 2 == (mData.size - 1) / 2 && !hasmore) {
            (helper.itemView.layoutParams as RecyclerView.LayoutParams).bottomMargin = SizeUtils.dp2px(10f)
        }
        GlideUtil.loadImg(mContext, item.avatar, helper.itemView.sameAvator)
        GlideUtil.loadImg(mContext, item.cover_url, helper.itemView.sameIv)
        helper.itemView.sameInterestTv.text = item.tags
        helper.itemView.sameName.text = item.nickname
        helper.itemView.sameInfo.text = "${item.gender}.${item.constellation}.${item.distance}"
        helper.itemView.sameLike.setImageResource(
            if (item.isliked == 1) {
                R.drawable.icon_dianzan_red
            } else {
                R.drawable.icon_dianzan_white
            }
        )
        helper.itemView.sameAvator.onClick(object : CustomClickListener() {
            override fun onSingleClick(view: View) {
                MatchDetailActivity.start(mContext, item.accid)
            }

        })

        helper.itemView.sameLike.onClick {
            clickZan(helper.itemView.sameLike, helper.layoutPosition)
        }
    }


    /**
     * 点赞按钮
     */
    private fun clickZan(likeBtn: ImageView, position: Int) {
        if (data[position].isliked == 1) {
            data[position].isliked = 0
            likeBtn.setImageResource(R.drawable.icon_dianzan_white)
        } else {
            data[position].isliked = 1
            likeBtn.setImageResource(R.drawable.icon_dianzan_red)
        }

        likeBtn.postDelayed({
            if (data.isEmpty() || data.size - 1 < position)
                return@postDelayed
            if (data[position].originalLike == data[position].isliked) {
                return@postDelayed
            }
            val params = hashMapOf(
                "token" to SPUtils.getInstance(Constants.SPNAME).getString("token"),
                "accid" to SPUtils.getInstance(Constants.SPNAME).getString("accid"),
                "type" to if (data[position].isliked == 0) {
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
        }
    }

}