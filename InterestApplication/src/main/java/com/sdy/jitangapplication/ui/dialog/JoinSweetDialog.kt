package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.event.CloseDialogEvent
import com.sdy.jitangapplication.event.JoinSweetEvent
import com.sdy.jitangapplication.model.SweetProgressBean
import com.sdy.jitangapplication.ui.activity.SweetHeartVerifyActivity
import com.sdy.jitangapplication.ui.activity.SweetHeartVerifyUploadActivity
import com.sdy.jitangapplication.ui.activity.VipPowerActivity
import kotlinx.android.synthetic.main.dialog_join_sweet.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.startActivity

/**
 *    author : ZFM
 *    date   : 2020/5/99:45
 *    desc   :加入甜心圈弹窗
 *    version: 1.0
 */
class JoinSweetDialog(val context1: Context, var progressBean: SweetProgressBean) :
    Dialog(context1, R.style.MyDialog) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_join_sweet)
        initWindow()
        setCancelable(true)
        setCanceledOnTouchOutside(true)

    }

    fun initView() {
        //assets_audit_state 甜心圈认证状态 1没有 2认证中 3认证通过
        //female_mv_state 	女性视频认证 1没有通过 2审核中 3视频认证通过
        //now_money 	男性充值的钱
        //normal_money 	标准充值的钱

        if (progressBean.gender == 1) {
            sweetHeartTitle.gravity = Gravity.CENTER
            sweetHeartTitle.text = context1.getString(R.string.meet_any_one_come_sweet)
            verifyNowNum1.isVisible = false
            verifyNowNum2.isVisible = false
            sweetVerifyIconMan.isVisible = true
            verifyTitle1.text =
                context1.getString(R.string.charge_more_than, progressBean.normal_money)
            verifyTitle2.text = context1.getString(R.string.pass_wealth_verify)
            if (progressBean.now_money.toFloat() > progressBean.normal_money.toFloat()) {
                verifyNowBtn1.setTextColor(Color.parseColor("#FF212225"))
                verifyNowBtn1.setBackgroundResource(R.drawable.shape_light_orange_13dp)
                verifyNowBtn1.text = context1.getString(R.string.join_now)
                verifyNowBtn1.clickWithTrigger {
                    EventBus.getDefault().post(JoinSweetEvent())
                }
            } else {
                verifyNowBtn1.setTextColor(Color.parseColor("#FFC5C6C8"))
                verifyNowBtn1.setBackgroundColor(Color.WHITE)
                verifyNowBtn1.text = "${progressBean.now_money}/${progressBean.normal_money}"
                verifyNowBtn1.clickWithTrigger {
                    CommonFunction.startToVip(context1, VipPowerActivity.SOURCE_BIG_CHARGE)
                    dismiss()
                }
            }


            when (progressBean.assets_audit_state) {
                1 -> {
                    verifyNowBtn2.setTextColor(Color.parseColor("#FFFFCD52"))
                    verifyNowBtn2.setBackgroundResource(R.drawable.shape_black_13dp)
                    verifyNowBtn2.text = context1.getString(R.string.verify_now)
                    verifyNowBtn2.isEnabled = true
                }
                2 -> {
                    verifyNowBtn2.setTextColor(Color.parseColor("#FFC5C6C8"))
                    verifyNowBtn2.setBackgroundColor(Color.WHITE)
                    verifyNowBtn2.text = context1.getString(R.string.checking)
                    verifyNowBtn2.isEnabled = false

                }
                3 -> {
                    verifyNowBtn2.setTextColor(Color.parseColor("#FFC5C6C8"))
                    verifyNowBtn2.setBackgroundColor(Color.WHITE)
                    verifyNowBtn2.text = context1.getString(R.string.veriy_pass)
                    verifyNowBtn2.isEnabled = false
                }
            }
            verifyNowBtn2.clickWithTrigger {
                context1.startActivity<SweetHeartVerifyActivity>()
                dismiss()
            }

        } else {
            sweetHeartTitle.text = context1.getString(R.string.reach_auto_in_sweet)
            verifyNowNum1.isVisible = true
            verifyNowNum2.isVisible = true
            sweetVerifyIconMan.isVisible = false
            verifyTitle1.text = context1.getString(R.string.upload_verify_video)
            verifyTitle2.text = context1.getString(R.string.verify_figure_or_job)
            //assets_audit_state 甜心圈认证状态 1没有 2认证中 3认证通过
            //female_mv_state 	女性视频认证 1没有通过 2审核中 3视频认证通过
            //now_money 	男性充值的钱
            //normal_money 	标准充值的钱

            when (progressBean.female_mv_state) {
                1 -> {
                    verifyNowBtn1.setTextColor(Color.WHITE)
                    verifyNowBtn1.setBackgroundResource(R.drawable.shape_pink_13dp)
                    verifyNowBtn1.text = context1.getString(R.string.verify_now)
                    verifyNowBtn1.isEnabled = true
                }
                2 -> {
                    verifyNowBtn1.setTextColor(Color.parseColor("#FFC5C6C8"))
                    verifyNowBtn1.setBackgroundColor(Color.WHITE)
                    verifyNowBtn1.text = context1.getString(R.string.verify_in_ing)
                    verifyNowBtn1.isEnabled = false
                }
                3 -> {
                    verifyNowBtn1.setTextColor(Color.parseColor("#FFC5C6C8"))
                    verifyNowBtn1.setBackgroundColor(Color.WHITE)
                    verifyNowBtn1.text = context1.getString(R.string.verify_pass)
                    verifyNowBtn1.isEnabled = false
                }
            }


            when (progressBean.assets_audit_state) {
                1 -> {
                    verifyNowBtn2.setTextColor(Color.WHITE)
                    verifyNowBtn2.setBackgroundResource(R.drawable.shape_pink_13dp)
                    verifyNowBtn2.text = context1.getString(R.string.verify_now)
                    verifyNowBtn2.isEnabled = true
                }
                2 -> {
                    verifyNowBtn2.setTextColor(Color.parseColor("#FFC5C6C8"))
                    verifyNowBtn2.setBackgroundColor(Color.WHITE)
                    verifyNowBtn2.text = context1.getString(R.string.verify_in_ing)
                    verifyNowBtn2.isEnabled = false
                }
                3 -> {
                    verifyNowBtn2.setTextColor(Color.parseColor("#FFC5C6C8"))
                    verifyNowBtn2.setBackgroundColor(Color.WHITE)
                    verifyNowBtn2.text = context1.getString(R.string.verify_pass)
                    verifyNowBtn2.isEnabled = false
                }
            }

            verifyNowBtn1.clickWithTrigger {
                CommonFunction.startToVideoIntroduce(context1)
                dismiss()
            }
            verifyNowBtn2.clickWithTrigger {
                context1.startActivity<SweetHeartVerifyActivity>()
                dismiss()
            }

        }

        val params = sweetPowerIv.layoutParams as ConstraintLayout.LayoutParams
        params.width = ScreenUtils.getScreenWidth() - SizeUtils.dp2px(15 * 2F + 10 * 2F)
        params.height = (params.width * (303F / 335)).toInt()
        GlideUtil.loadImg(context1, progressBean.img, sweetPowerIv)
    }

    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.BOTTOM)
        val params = window?.attributes
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT
        params?.windowAnimations = R.style.MyDialogBottomAnimation
        window?.attributes = params

    }

    override fun show() {
        super.show()
        EventBus.getDefault().register(this)
        initView()

    }

    override fun dismiss() {
        super.dismiss()
        EventBus.getDefault().unregister(this)
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateNearPeopleParamsEvent(event: CloseDialogEvent) {
        dismiss()
    }


}