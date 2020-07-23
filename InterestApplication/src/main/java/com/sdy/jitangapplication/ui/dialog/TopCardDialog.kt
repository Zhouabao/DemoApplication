package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.model.VipPowerBean
import com.sdy.jitangapplication.ui.activity.VipPowerActivity
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.dialog_top_card.*
import org.jetbrains.anko.startActivity

/**
 *    author : ZFM
 *    date   : 2019/9/2316:45
 *    desc   : 置顶卡片（高级会员功能）
 *    version: 1.0
 */
class TopCardDialog(val context1: Context) : Dialog(context1, R.style.MyDialog) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_top_card)
        initWindow()
        initView()
    }

    private fun initView() {

         if (UserManager.getGender() == 1) {
             text.text ="成为高级会员\n将你的卡片置于顶部，获取更多联系"
             tobePtVipBtn.text = "成为高级会员"
             tobePtVipBtn.clickWithTrigger {
                 ChargePtVipDialog(
                     0,
                     context1,
                     ChargePtVipDialog.PURCHASE_PT_VIP
                 ).show()
                 dismiss()
             }

         } else {
             text.text =  "添加你的视频介绍\n获取首页优先推荐，解锁无限聊天机会"
             tobePtVipBtn.text = "添加视频介绍"
             tobePtVipBtn.clickWithTrigger {
                 CommonFunction.startToVideoIntroduce(context1)
                 dismiss()
             }

         }

    }

    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.BOTTOM)
        val params = window?.attributes
//        params?.width = ScreenUtils.getScreenWidth() - SizeUtils.dp2px(15F) * 2
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT

        params?.windowAnimations = R.style.MyDialogBottomAnimation
//        params?.y = SizeUtils.dp2px(15F)
        window?.attributes = params
        //点击外部可取消
        setCanceledOnTouchOutside(true)
    }

}