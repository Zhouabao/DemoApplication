package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.model.NearBean
import com.sdy.jitangapplication.model.TodayFateBean
import com.sdy.jitangapplication.ui.activity.NewUserInfoSettingsActivity
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.dialog_complete_info.*
import org.jetbrains.anko.startActivity

/**
 *    author : ZFM
 *    date   : 2019/9/2316:45
 *    desc   : 提醒游客登录
 *    version: 1.0
 */
class CompleteInfoDialog(val context1: Context,  val nearBean: NearBean?,
                         val indexRecommends: TodayFateBean?) :
    Dialog(context1, R.style.MyDialog) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_complete_info)
        initWindow()
        initView()
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
        setCanceledOnTouchOutside(false)
        setCancelable(false)
    }


    private fun initView() {
        normalPercent.text = "您目前个人信息未完善，请优先补充个人信息，\n信息填充大于${nearBean?.complete_percent_normal}%的用户才会对外优先展示"
        completeBtn.clickWithTrigger {
            context1.startActivity<NewUserInfoSettingsActivity>()
            dismiss()
        }


    }

    override fun dismiss() {
        super.dismiss()
        if (nearBean?.want_step_man_pull == true) {
            ChooseCharacterDialog(context1).show()
        } else if (!indexRecommends?.list.isNullOrEmpty() && indexRecommends?.today_pull == false && !UserManager.showIndexRecommend) {
            if (UserManager.getGender() == 2)
                TodayFateWomanDialog(context1, nearBean, indexRecommends).show()
        } else if (!UserManager.showCompleteUserCenterDialog) {
            if (nearBean?.today_pull_share == false) {
                InviteFriendDialog(context1).show()
            } else if (nearBean?.today_pull_dating == false) {
                PublishDatingDialog(context1).show()
            }
        }
    }

}