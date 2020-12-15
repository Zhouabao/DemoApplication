package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import androidx.recyclerview.widget.GridLayoutManager
import com.blankj.utilcode.util.LanguageUtils
import com.google.gson.Gson
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.rx.BaseSubscriber
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.msg.MessageBuilder
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.event.ShowGuideChangeStyleEvent
import com.sdy.jitangapplication.model.BatchGreetBean
import com.sdy.jitangapplication.model.NearBean
import com.sdy.jitangapplication.model.NearPersonBean
import com.sdy.jitangapplication.model.TodayFateBean
import com.sdy.jitangapplication.nim.attachment.ChatUpAttachment
import com.sdy.jitangapplication.nim.uikit.common.ui.recyclerview.util.RecyclerViewUtil
import com.sdy.jitangapplication.ui.adapter.FateAdapter
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.dialog_today_fate_woman.*
import org.greenrobot.eventbus.EventBus
import java.util.*

/**
 *    author : ZFM
 *    date   : 2020/4/2717:00
 *    desc   : 今日缘分
 *    version: 1.0
 */
class TodayFateWomanDialog(
    val context1: Context,
    val nearBean: NearBean?,
    val data: TodayFateBean?
) :
    Dialog(context1, R.style.MyDialog) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_today_fate_woman)
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
        t2.text = context1.getString(
            R.string.hurry_give,
            if (CommonFunction.isEnglishLanguage()) {
                ""
            } else {
                if (UserManager.getGender() == 1) {
                    context1.getString(R.string.she)
                } else {
                    context1.getString(R.string.he)
                }
            }
        )

        rvFate.layoutManager = GridLayoutManager(context1, 3)
        rvFate.adapter = adapter


        //全部默认选中
        for (data in data?.list ?: mutableListOf()) {
            data.checked = true
        }
        adapter.setNewData(data?.list ?: mutableListOf<NearPersonBean>())
        checkFateEnable()

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
        params["batch_accid"] = Gson().toJson(ids)
        RetrofitFactory.instance.create(Api::class.java)
            .batchGreetWoman(UserManager.getSignParams(params))
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
                                //随机发送一条搭讪语消息
                                val chatUpAttachment = ChatUpAttachment(data.value.msg)
                                val msg = MessageBuilder.createCustomMessage(
                                    data.value.accid,
                                    SessionTypeEnum.P2P,
                                    chatUpAttachment
                                )


                                NIMClient.getService(MsgService::class.java).sendMessage(msg, false)
                                    .setCallback(object : RequestCallback<Void> {
                                        override fun onSuccess(p0: Void?) {
                                            if (data.index == (t.data?.size ?: 0) - 1) {
                                                loadingDialog.dismiss()
                                                dismiss()
                                                CommonFunction.toast(context1.getString(R.string.send_hi_success))
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
        if (!UserManager.showCompleteUserCenterDialog) {
            if (nearBean?.today_pull_share == false) {
                //如果自己的完善度小于标准值的完善度，就弹出完善个人资料的弹窗
                InviteFriendDialog(context1).show()
            } else if (nearBean?.today_pull_dating == false) {
                PublishDatingDialog(context1).show()
            } else {
                EventBus.getDefault().post(ShowGuideChangeStyleEvent())
            }
        }
        UserManager.showIndexRecommend = true

    }

}