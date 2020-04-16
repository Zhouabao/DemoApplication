package com.sdy.jitangapplication.ui.dialog

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import androidx.core.view.isVisible
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.SpanUtils
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.ext.onClick
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.event.CloseDialogEvent
import com.sdy.jitangapplication.event.RefreshCandyMallDetailEvent
import com.sdy.jitangapplication.event.UpdateMyCandyAmountEvent
import com.sdy.jitangapplication.model.GiftBean
import com.sdy.jitangapplication.nim.activity.ChatActivity
import com.sdy.jitangapplication.ui.activity.CandyProductDetailActivity
import com.sdy.jitangapplication.ui.activity.MatchDetailActivity
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.dialog_help_wish.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 *    author : ZFM
 *    date   : 2020/4/29:36
 *    desc   :聊天赠送礼物弹窗
 *    version: 1.0
 */
class HelpWishDialog(
    var myCandy_amount: Int,
    val target_accid: String,
    val nickname: String,
    val giftBean: GiftBean,
    context: Context
) :
    BottomSheetDialog(context, R.style.MyDialog) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_help_wish)
        initWindow()
        initView()
    }

    private fun initView() {
        confirmHelp.onClick {
            dismiss()
        }

        helpWishSeekBar.max = giftBean.amount.toFloat()
        helpWishSeekBar.min = giftBean.min_amount.toFloat()
        myCandyCount.text = "${myCandy_amount}"
        GlideUtil.loadRoundImgCenterCrop(
            context, giftBean.icon,
            giftImg, SizeUtils.dp2px(10F)
        )
        GlideUtil.loadRoundImgCenterCrop(
            context, giftBean.icon,
            giftImg1, SizeUtils.dp2px(10F)
        )
        giftCandyAmount.text = "${giftBean.amount}"
        giftName.text = "${giftBean.title}"

        //最大值
        maxHelpBtn.onClick {
            helpWishSeekBar.setProgress(giftBean.amount.toFloat())
        }

        //确认助力
        confirmHelp.onClick {
            if (myCandy_amount < helpWishSeekBar.progress) {
                CommonFunction.toast("糖果余额不足")
            } else {
                val params = hashMapOf<String, Any>(
                    "target_accid" to target_accid,
                    "amount" to helpWishSeekBar.progress,
                    "goods_id" to giftBean.id
                )
                wishHelp(params)
            }
        }

        gotoChat.onClick {
            ChatActivity.start(context, target_accid)
            dismiss()
        }

    }


    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.BOTTOM)
        val params = window?.attributes
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT

        params?.windowAnimations = R.style.MyDialogBottomAnimation
        window?.attributes = params
        //点击外部可取消
        setCanceledOnTouchOutside(true)
    }


    private fun wishHelp(params: HashMap<String, Any>) {
        RetrofitFactory.instance.create(Api::class.java)
            .wishHelp(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<Any?>>(null) {
                override fun onNext(t: BaseResp<Any?>) {
                    super.onNext(t)
                    if (t.code == 200) {
                        wishHelpSuccessLl.isVisible = true
                        wishHelpCl.isVisible = false
                        helpAmount.text = SpanUtils.with(helpAmount)
                            .append("助力额度")
                            .append("${helpWishSeekBar.progress}")
                            .setTypeface(
                                Typeface.createFromAsset(
                                    context.assets,
                                    "DIN_Alternate_Bold.ttf"
                                )
                            )
                            .setForegroundColor(Color.parseColor("#FF6318"))
                            .append("糖果\n你与${nickname}已达成好友关系，快去聊聊吧")
                            .create()


                        if (ActivityUtils.getTopActivity() is CandyProductDetailActivity) {
                            EventBus.getDefault().post(RefreshCandyMallDetailEvent())
                        } else if (ActivityUtils.getTopActivity() is MatchDetailActivity) {
                            EventBus.getDefault().post(UpdateMyCandyAmountEvent(helpWishSeekBar.progress))
                        }

                    } else {
                        CommonFunction.toast(t.msg)
                    }
                }

            })
    }


    override fun show() {
        super.show()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCloseDialogEvent(event: CloseDialogEvent) {
        if (isShowing) {
            dismiss()
        }
    }
}