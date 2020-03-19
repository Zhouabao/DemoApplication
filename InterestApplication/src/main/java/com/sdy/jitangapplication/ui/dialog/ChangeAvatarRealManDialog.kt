package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import androidx.core.view.isVisible
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.ext.onClick
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.baselibrary.utils.CustomClickListener
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.model.MatchBean
import com.sdy.jitangapplication.ui.activity.NewUserInfoSettingsActivity
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.change_avator_real_man_dialog_layout.*
import org.jetbrains.anko.startActivity

/**
 *    author : ZFM
 *    date   : 2019/10/1614:30
 *    desc   : 提醒去替换头像
 *    version: 1.0
 */
class ChangeAvatarRealManDialog(
    val context1: Context,
    var status: Int = VERIFY_NEED_REAL_MAN,
    var avator: String = "",
    var matchBean: MatchBean? = null,
    var view1: View? = null
) :
    Dialog(context1, R.style.MyDialog) {

    companion object {
        const val VERIFY_NEED_REAL_MAN = 0 //替换真人
        const val VERIFY_NEED_VALID_REAL_MAN = 1 //替换为合规的真人照片
        const val VERIFY_NEED_REAL_MAN_GREET = 2 //打招呼滑动时提示替换真人
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.change_avator_real_man_dialog_layout)
        initWindow()
        initView()
    }

    private fun initView() {
        GlideUtil.loadCircleImg(context1, UserManager.getAvator(), accountImg)
        close.onClick {
            dismiss()
        }
        accountDangerBtn.onClick {
            context1.startActivity<NewUserInfoSettingsActivity>()
            dismiss()
        }

        continueGreet.onClick(object : CustomClickListener() {
            override fun onSingleClick(view: View) {
                if (matchBean != null)
                    CommonFunction.commonGreet(
                        context1,
                        matchBean!!.accid,
                        view1,
                        targetAvator = matchBean!!.avatar ?: "",
                        needSwipe = true
                    )
                dismiss()
            }
        })

        if (UserManager.getGender() == 1) {
            standardImg.setImageResource(R.drawable.icon_standard_man_avator)
        } else {
            standardImg.setImageResource(R.drawable.icon_standard_woman_avator)
        }

        when (status) {
            VERIFY_NEED_REAL_MAN -> {
                setCancelable(true)
                setCanceledOnTouchOutside(true)
                setOnKeyListener { dialogInterface, keyCode, event ->
                    false
                }
                close.isVisible = false
                continueGreet.isVisible = false
                accountDangerTitle.text = "您的头像为非真人"
                accountDangerContent.text = "替换真人头像将会推荐显示到匹配首页，\n人气直线提升，获得超多喜欢关注"
                accountDangerBtn.text = "立即替换"
            }
            VERIFY_NEED_VALID_REAL_MAN -> {
                setCancelable(true)
                setCanceledOnTouchOutside(true)
                setOnKeyListener { dialogInterface, keyCode, event ->
                    false
                }
                close.isVisible = true
                continueGreet.isVisible = false
                accountDangerTitle.text = "请替换头像"
                accountDangerContent.text = "当前头像不符合标准\n请替换头像"
                accountDangerBtn.text = "立即替换"

            }
            else -> {
                setCancelable(false)
                setCanceledOnTouchOutside(false)
                setOnKeyListener { dialogInterface, keyCode, event ->
                    keyCode == KeyEvent.KEYCODE_BACK && event.repeatCount == 0
                }
                close.isVisible = false
                continueGreet.isVisible = true
                accountDangerTitle.text = "替换真实头像"
                accountDangerContent.text = "本平台为真人社交平台，非真实头像用户打招呼回复几率偏低，且无法获得首页和广场内容推荐呈现机会"
                accountDangerBtn.text = "立即替换"
            }
        }


    }

    override fun dismiss() {
        super.dismiss()
    }


    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.CENTER)
        val params = window?.attributes
        params?.width = WindowManager.LayoutParams.WRAP_CONTENT
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT
        params?.windowAnimations = R.style.MyDialogCenterAnimation
        window?.attributes = params

    }

    /**
     * 人工审核
     * 1 人工认证 2重传头像或则取消
     */
    fun humanVerify(type: Int) {
        val params = UserManager.getBaseParams()
        params["type"] = type
        RetrofitFactory.instance.create(Api::class.java)
            .humanAduit(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<Any?>>(null) {
                override fun onNext(t: BaseResp<Any?>) {

                }

                override fun onError(e: Throwable?) {

                }
            })

    }

}