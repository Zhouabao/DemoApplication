package com.sdy.jitangapplication.ui.adapter

import android.graphics.Typeface
import android.os.Build
import androidx.core.view.isVisible
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
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.model.ProductBean
import com.sdy.jitangapplication.ui.dialog.AddAndMessageDialog
import com.sdy.jitangapplication.ui.dialog.AddExchangeAddressDialog
import com.sdy.jitangapplication.ui.dialog.AlertCandyEnoughDialog
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.sdy.jitangapplication.utils.UserManager
import jp.wasabeef.glide.transformations.RoundedCornersTransformation
import kotlinx.android.synthetic.main.item_candy_product.view.*

/**
 *    author : ZFM
 *    date   : 2020/3/2411:16
 *    desc   :糖果商品
 *    version: 1.0
 */
class CandyProductAdapter :
    BaseQuickAdapter<ProductBean, BaseViewHolder>(R.layout.item_candy_product) {
    var mycandy: Int = 0
    override fun convert(helper: BaseViewHolder, item: ProductBean) {
        helper.itemView.ProductCandyPrice.typeface =
            Typeface.createFromAsset(mContext.assets, "DIN_Alternate_Bold.ttf")
        helper.itemView.addProductProgressCount.typeface =
            Typeface.createFromAsset(mContext.assets, "DIN_Alternate_Bold.ttf")
        GlideUtil.loadRoundImgFitcenter(
            mContext,
            item.icon,
            helper.itemView.productImg,
            SizeUtils.dp2px(10F),
            RoundedCornersTransformation.CornerType.LEFT
        )
        if (item.is_wished) {
            helper.itemView.collectProduct.setImageResource(R.drawable.icon_collected)
        } else {
            helper.itemView.collectProduct.setImageResource(R.drawable.icon_collect)
            helper.itemView.addProductBtn.text = if (item.friend_wish_cnt > 0) {
                "${item.friend_wish_cnt}个好友想要"
            } else {
                "加入心愿单"
            }
        }

        helper.itemView.ProductCandyPrice.text = CommonFunction.num2thousand("${item.amount}")
        helper.itemView.productDesc.text = "${item.title}"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            helper.itemView.addProductProgress.setProgress(
                if (mycandy >= item.amount) {
                    100
                } else {
                    ((mycandy * 1.0F / item.amount) * 100).toInt()
                }, true
            )
        } else {
            helper.itemView.addProductProgress.progress = if (mycandy >= item.amount) {
                100
            } else {
                ((mycandy * 1.0F / item.amount) * 100).toInt()
            }
        }
        helper.itemView.addProductProgressCount.text =
            "${if (mycandy >= item.amount) {
                100
            } else {
                ((mycandy * 1.0F / item.amount) * 100).toInt()
            }}%"

        helper.itemView.addProductProgress.isVisible = item.is_wished
        helper.itemView.addProductProgressCount.isVisible = item.is_wished
        helper.itemView.addProductBtn.isVisible = !item.is_wished

        //加入心愿
        helper.itemView.addProductBtn.onClick {
            if (item.is_wished) {
                goodsDelWish(helper.layoutPosition, item.id)
            } else
                goodsAddWish(helper.layoutPosition, item.id)
        }

        //兑换礼品
        helper.itemView.exchangeBtn.onClick {
            if (mycandy >= item.amount) {
                AddExchangeAddressDialog(mContext, item.id).show()
            } else {
                AlertCandyEnoughDialog(mContext).show()
            }
        }

    }

    /**
     * 商品加入心愿单
     */
    fun goodsAddWish(position: Int, id: Int) {
        val params = hashMapOf<String, Any>()
        params["goods_id"] = id
        RetrofitFactory.instance.create(Api::class.java)
            .goodsAddWish(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<Any?>>(null) {
                override fun onNext(t: BaseResp<Any?>) {
                    super.onNext(t)
                    if (t.code == 200) {
                        mData[position].is_wished = true
                        notifyItemChanged(position)
                        AddAndMessageDialog(mContext, mData[position].id).show()
                    } else {
                        CommonFunction.toast(t.msg)
                    }
                }

                override fun onError(e: Throwable?) {
                    super.onError(e)
                    if (e is BaseException) {
                        TickDialog(mContext).show()
                    }
                }
            })

    }

    /**
     * 商品取消加入心愿单
     */
    fun goodsDelWish(position: Int, id: Int) {
        val params = hashMapOf<String, Any>()
        params["goods_id"] = id
        RetrofitFactory.instance.create(Api::class.java)
            .goodsDelWish(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<Any?>>(null) {
                override fun onNext(t: BaseResp<Any?>) {
                    super.onNext(t)
                    if (t.code == 200) {
                        mData[position].is_wished = false
                        notifyItemChanged(position)
                        AddAndMessageDialog(mContext, mData[position].id).show()
                    } else {
                        CommonFunction.toast(t.msg)
                    }
                }

                override fun onError(e: Throwable?) {
                    super.onError(e)
                    if (e is BaseException) {
                        TickDialog(mContext).show()
                    }
                }
            })

    }
}