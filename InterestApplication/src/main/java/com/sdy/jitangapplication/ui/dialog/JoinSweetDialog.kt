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
import com.sdy.jitangapplication.model.SweetProgressBean
import com.sdy.jitangapplication.ui.activity.SweetHeartVerifyActivity
import com.sdy.jitangapplication.ui.activity.VipPowerActivity
import kotlinx.android.synthetic.main.dialog_join_sweet.*
import org.jetbrains.anko.startActivity

/**
 *    author : ZFM
 *    date   : 2020/5/99:45
 *    desc   :加入甜心圈弹窗
 *    version: 1.0
 */
class JoinSweetDialog(val context1: Context, private val progressBean: SweetProgressBean) :
    Dialog(context1, R.style.MyDialog) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_join_sweet)
        initWindow()
        initView()

    }

    private fun initView() {
        setCancelable(true)
        setCanceledOnTouchOutside(true)
        //assets_audit_state 甜心圈认证状态 1没有 2认证中 3认证通过
        //female_mv_state 	女性视频认证 1没有通过 2审核中 3视频认证通过
        //now_money 	男性充值的钱
        //normal_money 	标准充值的钱

        if (progressBean.gender == 1) {
            sweetHeartTitle.gravity = Gravity.CENTER
            sweetHeartTitle.text = "满足下列任意一个条件即可进入甜心圈"
            verifyNowNum1.isVisible = false
            verifyNowNum2.isVisible = false
            sweetVerifyIconMan.isVisible = true
            verifyTitle1.text = "充值金额大于${progressBean.normal_money}"
            verifyTitle2.text = "通过资产认证"

            if (progressBean.now_money.toFloat() > progressBean.normal_money.toFloat()) {
                verifyNowBtn1.setTextColor(Color.parseColor("#FF212225"))
                verifyNowBtn1.setBackgroundResource(R.drawable.shape_light_orange_13dp)
                verifyNowBtn1.text = "立即加入"
                verifyNowBtn1.clickWithTrigger {
//                    mPresenter.joinSweetApply()
                }
            } else {
                verifyNowBtn1.setTextColor(Color.parseColor("#FFC5C6C8"))
                verifyNowBtn1.setBackgroundColor(Color.WHITE)
                verifyNowBtn1.text = "${progressBean.now_money}/${progressBean.normal_money}"
                verifyNowBtn1.clickWithTrigger {
                    CommonFunction.startToVip(context1, VipPowerActivity.SOURCE_BIG_CHARGE)
                }
            }


            when (progressBean.assets_audit_state) {
                1 -> {
                    verifyNowBtn2.setTextColor(Color.parseColor("#FFFFCD52"))
                    verifyNowBtn2.setBackgroundResource(R.drawable.shape_black_13dp)
                    verifyNowBtn2.text = "立即认证"
                    verifyNowBtn2.isEnabled = true
                }
                2 -> {
                    verifyNowBtn2.setTextColor(Color.parseColor("#FFC5C6C8"))
                    verifyNowBtn2.setBackgroundColor(Color.WHITE)
                    verifyNowBtn2.text = "审核中"
                    verifyNowBtn2.isEnabled = false
                }
                3 -> {
                    verifyNowBtn2.setTextColor(Color.parseColor("#FFC5C6C8"))
                    verifyNowBtn2.setBackgroundColor(Color.WHITE)
                    verifyNowBtn2.text = "认证通过"
                    verifyNowBtn2.isEnabled = false
                }
            }
            verifyNowBtn2.clickWithTrigger {
                context1.startActivity<SweetHeartVerifyActivity>()
            }
        } else {
            sweetHeartTitle.text = "达成要求自动进入甜心圈"
            verifyNowNum1.isVisible = true
            verifyNowNum2.isVisible = true
            sweetVerifyIconMan.isVisible = false
            verifyTitle1.text = "上传认证视频"
            verifyTitle2.text = "认证职业或身材"
            //assets_audit_state 甜心圈认证状态 1没有 2认证中 3认证通过
            //female_mv_state 	女性视频认证 1没有通过 2审核中 3视频认证通过
            //now_money 	男性充值的钱
            //normal_money 	标准充值的钱

            when (progressBean.female_mv_state) {
                1 -> {
                    verifyNowBtn1.setTextColor(Color.WHITE)
                    verifyNowBtn1.setBackgroundResource(R.drawable.shape_pink_13dp)
                    verifyNowBtn1.text = "立即认证"
                    verifyNowBtn1.isEnabled = true
                }
                2 -> {
                    verifyNowBtn1.setTextColor(Color.parseColor("#FFC5C6C8"))
                    verifyNowBtn1.setBackgroundColor(Color.WHITE)
                    verifyNowBtn1.text = "审核中"
                    verifyNowBtn1.isEnabled = false
                }
                3 -> {
                    verifyNowBtn1.setTextColor(Color.parseColor("#FFC5C6C8"))
                    verifyNowBtn1.setBackgroundColor(Color.WHITE)
                    verifyNowBtn1.text = "认证通过"
                    verifyNowBtn1.isEnabled = false
                }
            }


            when (progressBean.assets_audit_state) {
                1 -> {
                    verifyNowBtn2.setTextColor(Color.WHITE)
                    verifyNowBtn2.setBackgroundResource(R.drawable.shape_pink_13dp)
                    verifyNowBtn2.text = "立即认证"
                    verifyNowBtn2.isEnabled = true
                }
                2 -> {
                    verifyNowBtn2.setTextColor(Color.parseColor("#FFC5C6C8"))
                    verifyNowBtn2.setBackgroundColor(Color.WHITE)
                    verifyNowBtn2.text = "认证中"
                    verifyNowBtn2.isEnabled = false
                }
                3 -> {
                    verifyNowBtn2.setTextColor(Color.parseColor("#FFC5C6C8"))
                    verifyNowBtn2.setBackgroundColor(Color.WHITE)
                    verifyNowBtn2.text = "认证通过"
                    verifyNowBtn2.isEnabled = false
                }
            }

            verifyNowBtn1.clickWithTrigger {
                CommonFunction.startToVideoIntroduce(context1)
            }
            verifyNowBtn2.clickWithTrigger {
                context1.startActivity<SweetHeartVerifyActivity>()
            }

        }


        val params = sweetPowerIv.layoutParams as ConstraintLayout.LayoutParams
        params.width = ScreenUtils.getScreenWidth() - SizeUtils.dp2px(15 * 2F + 10 * 2F)
        params.height = (params.width * (588 / 1035f)).toInt()
        GlideUtil.loadRoundImgCenterCrop(
            context1,
            progressBean.img,
            sweetPowerIv,
            SizeUtils.dp2px(10F)
        )
    }

    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.BOTTOM)
        val params = window?.attributes
        params?.width = ScreenUtils.getScreenWidth() - SizeUtils.dp2px(20F)
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT
        params?.windowAnimations = R.style.MyDialogTopInAnimation
        window?.attributes = params

    }

    override fun show() {
        super.show()
    }


}