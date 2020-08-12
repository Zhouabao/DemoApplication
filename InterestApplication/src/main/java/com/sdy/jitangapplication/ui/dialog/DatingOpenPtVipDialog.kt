package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import androidx.core.view.isVisible
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.event.CloseDialogEvent
import com.sdy.jitangapplication.model.ChatUpBean
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.dialog_dating_open_pt_vip.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 *
 * 约会开通会员
 */
class DatingOpenPtVipDialog(
    val context1: Context,
    val type: Int = TYPE_DATING_PUBLISH,
    val chatUpBean: ChatUpBean
) :
    Dialog(context1, R.style.MyDialog) {

    companion object {
        val TYPE_DATING_PUBLISH = 1 //发布约会
        val TYPE_DATING_APPLYFOR = 2 //报名约会
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_dating_open_pt_vip)
        initWindow()
        initChatData()
        EventBus.getDefault().register(this)
    }


    private fun initChatData() {
        GlideUtil.loadCircleImg(context1, UserManager.getAvator(), datingAvator)
        when (type) {
            TYPE_DATING_PUBLISH -> { //发布约会
                openPtVipBtn.setBackgroundResource(R.drawable.gradient_gold_vip)
                datingTitle.text= "仅高级用户能发布约会"
                datingContent.text= "约会为高级用户独享功能\n成为黄金会员，建立和她的浪漫约会吧"
                openPtVipBtn.text  ="成为黄金会员"
                applyForDatingBtn.isVisible = false
            }
            TYPE_DATING_APPLYFOR -> {
                //1.先判断有无高级限制
                openPtVipBtn.setBackgroundResource(R.drawable.gradient_gold_vip)
                datingTitle.text= "对方仅允许高级用户报名"
                datingContent.text= "对方仅允许高级用户报名\n立即成为高级会员，不要错过她"
                openPtVipBtn.text  ="成为黄金会员"
                applyForDatingBtn.isVisible = false

                //2.再判断有无次数
                openPtVipBtn.setBackgroundResource(R.drawable.gradient_gold_vip)
                applyForDatingBtn.text = "报名约会（30糖果）"
                applyForDatingBtn.isVisible = true
                datingTitle.text= "要报名约会吗"
                datingContent.text= "用糖果证明约会诚意或许更好哦"
                openPtVipBtn.text  ="成为黄金会员，更多免费机会"

                //3.报名约会
                openPtVipBtn.setBackgroundResource(R.drawable.gradient_orange_15_bottom)
                applyForDatingBtn.isVisible = false
                datingTitle.text= "要报名约会吗"
                datingContent.text= "报名消耗一次聊天机会\n今日还有3次免费聊天机会"
                openPtVipBtn.text  ="报名约会"
            }
        }

    }

    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.BOTTOM)
//        window?.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        val params = window?.attributes
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT
        params?.windowAnimations = R.style.MyDialogBottomAnimation
        window?.attributes = params
        setCancelable(true)
        setCanceledOnTouchOutside(true)
    }


    override fun show() {
        super.show()
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