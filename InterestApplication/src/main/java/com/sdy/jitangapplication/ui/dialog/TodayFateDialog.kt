package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import androidx.recyclerview.widget.GridLayoutManager
import com.google.gson.Gson
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.rx.BaseSubscriber
import com.netease.nim.uikit.common.ui.recyclerview.util.RecyclerViewUtil
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.msg.MessageBuilder
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.CustomMessageConfig
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.model.BatchGreetBean
import com.sdy.jitangapplication.model.IndexRecommendBean
import com.sdy.jitangapplication.model.NearBean
import com.sdy.jitangapplication.nim.attachment.ChatHiAttachment
import com.sdy.jitangapplication.ui.adapter.FateAdapter
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.dialog_today_fate.*

/**
 *    author : ZFM
 *    date   : 2020/4/2717:00
 *    desc   : 今日缘分
 *    version: 1.0
 */
class TodayFateDialog(
    val context1: Context,
    val nearBean: NearBean?,
    val data: MutableList<IndexRecommendBean>
) :
    Dialog(context1, R.style.MyDialog) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_today_fate)
        initWindow()
        initView()
    }

    private val adapter by lazy { FateAdapter() }

    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.CENTER)
        val params = window?.attributes
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.MATCH_PARENT
        params?.windowAnimations = R.style.MyDialogCenterAnimation
        window?.attributes = params
        setCanceledOnTouchOutside(true)
    }

    private fun initView() {
        t2.text = "快给${if (UserManager.getGender() == 1) {
            "她"
        } else {
            "他"
        }}们打个招呼吧"

        rvFate.layoutManager = GridLayoutManager(context1, 3)
        rvFate.adapter = adapter
        adapter.setNewData(data)
        RecyclerViewUtil.changeItemAnimation(rvFate, false)
        adapter.setOnItemClickListener { _, view, position ->
            adapter.data[position].checked = !adapter.data[position].checked

            checkFateEnable()
            adapter.notifyItemChanged(position)
        }


        closeBtn.clickWithTrigger {
            dismiss()
        }
        hiFateBtn.clickWithTrigger {
            batchGreet()
        }
    }

    private fun checkFateEnable() {
        for (data in adapter.data) {
            if (data.checked) {
                hiFateBtn.isEnabled = true
                break
            } else {
                hiFateBtn.isEnabled = false
            }
        }
    }


    /**
     *
     */
    fun batchGreet() {
        val loadingDialog = LoadingDialog(context1)
        val ids = mutableListOf<String>()
        for (data in adapter.data) {
            if (data.checked) {
                ids.add(data.accid)
            }
        }
        val params = hashMapOf<String, Any>()
        params["accid_json"] = Gson().toJson(ids)
        RetrofitFactory.instance.create(Api::class.java)
            .batchGreet(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<MutableList<BatchGreetBean>?>>() {
                override fun onStart() {
                    super.onStart()
                    loadingDialog.show()
                }

                override fun onCompleted() {
                    super.onCompleted()
                }

                override fun onNext(t: BaseResp<MutableList<BatchGreetBean>?>) {
                    super.onNext(t)
                    if (t.code == 200 && !t.data.isNullOrEmpty()) {
                        for (data in (t.data ?: mutableListOf()).withIndex()) {
                            if (!data.value.msg.isNullOrEmpty()) {
                                //发送招呼消息
                                val chatHiAttachment = ChatHiAttachment(ChatHiAttachment.CHATHI_HI)
                                val config = CustomMessageConfig()
                                config.enableUnreadCount = false
                                config.enablePush = false
                                val message = MessageBuilder.createCustomMessage(
                                    data.value.accid,
                                    SessionTypeEnum.P2P,
                                    "",
                                    chatHiAttachment,
                                    config
                                )
                                NIMClient.getService(MsgService::class.java)
                                    .sendMessage(message, false)

                                //随机发送一条招呼文本消息
                                val msg = MessageBuilder.createTextMessage(
                                    data.value.accid,
                                    SessionTypeEnum.P2P,
                                    data.value.msg
                                )
                                val params = hashMapOf<String, Any>("needCandyImg" to false)
                                msg.remoteExtension = params
                                NIMClient.getService(MsgService::class.java).sendMessage(msg, false)
                                    .setCallback(object : RequestCallback<Void> {
                                        override fun onSuccess(p0: Void?) {
//                                            UserManager.saveLightingCount(UserManager.getLightingCount() - 1)
//                                            EventBus.getDefault().post(UpdateHiCoumntEvent())
                                            if (data.index == (t.data?.size ?: 0) - 1) {
                                                loadingDialog.dismiss()
                                                CommonFunction.toast("送出招呼成功！")
                                            }
                                        }

                                        override fun onFailed(p0: Int) {
                                        }

                                        override fun onException(p0: Throwable?) {
                                        }

                                    })
                            }
                        }
                    } else {
                        loadingDialog.dismiss()
                    }
                }

                override fun onError(e: Throwable?) {
                    super.onError(e)
                    loadingDialog.dismiss()
                }
            })

    }


    override fun dismiss() {
        super.dismiss()
        if (nearBean != null && nearBean!!.today_find!!.id == -1 && !nearBean?.today_find_pull) {
            TodayWantDialog(context1, nearBean).show()
        } else if (nearBean != null && nearBean!!.complete_percent < nearBean!!.complete_percent_normal && !UserManager.showCompleteUserCenterDialog) {
            //如果自己的完善度小于标准值的完善度，就弹出完善个人资料的弹窗
            CompleteUserCenterDialog(context1).show()
        }
    }

}