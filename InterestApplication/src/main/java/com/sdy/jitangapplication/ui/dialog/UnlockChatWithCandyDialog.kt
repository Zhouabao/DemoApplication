package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.event.UpdateApproveEvent
import com.sdy.jitangapplication.utils.UserManager
import com.shuyu.gsyvideoplayer.GSYVideoManager
import kotlinx.android.synthetic.main.dialog_unlock_with_candy.*
import org.greenrobot.eventbus.EventBus

/**
 *    author : ZFM
 *    date   : 2020/6/919:39
 *    desc   : 糖果解锁聊天
 *    version: 1.0
 */
class UnlockChatWithCandyDialog(
    val context1: Context,
    val candyAmount: Int,
    val target_accid: String
) :
    Dialog(context1, R.style.MyDialog) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_unlock_with_candy)
        initWindow()
        initView()

    }

    private fun initView() {
        unlockBtn.text = "${candyAmount}糖果解锁"

        closeBtn.clickWithTrigger {
            dismiss()
        }


        //解锁聊天
        unlockBtn.clickWithTrigger {
            lockChatup()
        }
    }

    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.CENTER)
        val params = window?.attributes
        // 设置窗口背景透明度
//        params?.alpha = 0.5f
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.MATCH_PARENT
        window?.attributes = params
        //点击外部可取消
        setCanceledOnTouchOutside(false)

    }


    override fun dismiss() {
        super.dismiss()
        GSYVideoManager.releaseAllVideos()
    }

    /**
     * 男性解锁糖果聊天
     */
    fun lockChatup() {
        val loadingDialog = LoadingDialog(context1)
        val params = hashMapOf<String, Any>("target_accid" to target_accid)
        RetrofitFactory.instance.create(Api::class.java)
            .lockChatup(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<Any?>>(null) {
                override fun onStart() {
                    super.onStart()
                    loadingDialog.show()
                }


                override fun onNext(t: BaseResp<Any?>) {
                    super.onNext(t)
                    loadingDialog.dismiss()
                    if (t.code == 200) {
                        EventBus.getDefault().post(UpdateApproveEvent())
                        dismiss()
                    } else if (t.code == 201) {
                        OpenVipDialog(context1).show()
                        dismiss()
                    } else if (t.code == 419) {
                        AlertCandyEnoughDialog(
                            context1,
                            AlertCandyEnoughDialog.FROM_SEND_GIFT
                        ).show()
                        dismiss()
                    }
                }

                override fun onError(e: Throwable?) {
                    super.onError(e)
                    loadingDialog.dismiss()
                }
            })
    }
}