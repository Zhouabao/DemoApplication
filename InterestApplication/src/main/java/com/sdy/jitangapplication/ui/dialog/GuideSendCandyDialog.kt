package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.WindowManager
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.ext.onClick
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.model.IndexRecommendBean
import com.sdy.jitangapplication.model.NearBean
import com.sdy.jitangapplication.ui.activity.MyCandyActivity
import com.sdy.jitangapplication.ui.activity.ProtocolActivity
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.dialog_guide_send_candy.*
import org.jetbrains.anko.startActivity

/**
 *    author : ZFM
 *    date   : 2019/9/1917:18
 *    desc   :
 *    version: 1.0
 */
class GuideSendCandyDialog(
    val myContext: Context,
    val nearBean: NearBean?,
    val indexRecommends: MutableList<IndexRecommendBean>
) : Dialog(myContext, R.style.MyDialog) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_guide_send_candy)
        initWindow()
        initView()
        completeGuide()
    }

    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.CENTER)
        window?.setBackgroundDrawableResource(R.color.color80Black)
        val params = window?.attributes
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.MATCH_PARENT
        window?.attributes = params
        setCanceledOnTouchOutside(false)
        setOnKeyListener { dialogInterface, keyCode, event ->
            keyCode == KeyEvent.KEYCODE_BACK && event.repeatCount == 0
        }
    }


    private fun initView() {
        welldone.playAnimation()

        startUse.onClick {
            dismiss()
        }
        useOfCandy.onClick {
            if (UserManager.getGender() == 1) {
                context.startActivity<ProtocolActivity>("type" to ProtocolActivity.TYPE_CANDY_USAGE)
            } else {
                context.startActivity<MyCandyActivity>()
            }
            completeGuide()
            dismiss()
        }
    }

    private fun completeGuide() {
        RetrofitFactory.instance.create(Api::class.java)
            .completeGuide(UserManager.getSignParams())
            .excute(object : BaseSubscriber<BaseResp<Any?>>(null) {

            })
    }


    override fun dismiss() {
        super.dismiss()
        if (!indexRecommends.isNullOrEmpty()) {
            TodayFateDialog(myContext, nearBean, indexRecommends).show()
        } else if (nearBean != null && nearBean!!.today_find!!.id == -1 && !nearBean?.today_find_pull) {
            TodayWantDialog(myContext, nearBean).show()
        } else if (nearBean != null && nearBean!!.complete_percent < nearBean!!.complete_percent_normal && !UserManager.showCompleteUserCenterDialog) {
            //如果自己的完善度小于标准值的完善度，就弹出完善个人资料的弹窗
            CompleteUserCenterDialog(myContext).show()
        }
    }
}
