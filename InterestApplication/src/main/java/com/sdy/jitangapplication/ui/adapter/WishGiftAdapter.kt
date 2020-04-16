package com.sdy.jitangapplication.ui.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.PopupWindow
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.kotlin.base.ext.onClick
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.GiftBean
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.item_wish_gift.view.*
import kotlinx.android.synthetic.main.popupwindow_matchdetail_guide_wish_help.view.*

/**
 *    author : ZFM
 *    date   : 2020/3/2411:16
 *    desc   : 心愿礼物适配器
 *    version: 1.0
 */
class WishGiftAdapter(val activity: Activity) :
    BaseQuickAdapter<GiftBean, BaseViewHolder>(R.layout.item_wish_gift) {

    private val guideVerifyWindow by lazy {
        PopupWindow(mContext).apply {
            contentView = LayoutInflater.from(mContext)
                .inflate(R.layout.popupwindow_matchdetail_guide_wish_help, null, false)
            contentView.helpWishBn.onClick {
                dismiss()
            }
            width = ViewGroup.LayoutParams.WRAP_CONTENT
            height = ViewGroup.LayoutParams.WRAP_CONTENT
            setBackgroundDrawable(null)
//            animationStyle = R.style.MyDialogLeftBottomAnimation
//            isFocusable = true
            isOutsideTouchable = true
            setOnDismissListener {
                setWindowAlpha(1F, activity)
                UserManager.saveShowGuideHelpWish(true)
            }
        }
    }

    override fun convert(helper: BaseViewHolder, item: GiftBean) {

        GlideUtil.loadRoundImgCenterCrop(
            mContext,
            item.icon,
            helper.itemView.giftImg,
            SizeUtils.dp2px(10F)
        )

        helper.itemView.giftCandyAmount.text = "${item.amount}"
//        helper.itemView.giftName.text = "${item.title}"
        helper.itemView.giftDesc.text = "${item.title}"
        helper.addOnClickListener(R.id.helpWishBtn)

//        if (helper.layoutPosition == 0 && !UserManager.isShowGuideHelpWish()) {
//            helper.itemView.giftName.postDelayed({
//                setWindowAlpha(0.5F, activity)
//                guideVerifyWindow.showAsDropDown(
//                    helper.itemView.giftName,
//                    SizeUtils.dp2px(75F),
//                    SizeUtils.dp2px(-75F),
//                    Gravity.RIGHT and Gravity.TOP
//                )
//            }, 500L)
//        }
    }

    fun setWindowAlpha(alpha: Float, activity: Activity) {
        val lp = activity.window.attributes
        lp.alpha = alpha; //0.0-1.0
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        activity.window.attributes = lp;
    }
}