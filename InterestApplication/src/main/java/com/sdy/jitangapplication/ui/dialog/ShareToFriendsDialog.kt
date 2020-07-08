package com.sdy.jitangapplication.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.ext.onClick
import com.kotlin.base.rx.BaseSubscriber
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.msg.MessageBuilder
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.CustomMessageConfig
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.baselibrary.utils.CustomClickListener
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.model.SquareBean
import com.sdy.jitangapplication.nim.attachment.ShareSquareAttachment
import com.sdy.jitangapplication.nim.uikit.business.session.module.Container
import com.sdy.jitangapplication.nim.uikit.business.session.module.ModuleProxy
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.dialog_share_to_friends.*
import java.net.HttpURLConnection
import java.net.URL

/**
 *    author : ZFM
 *    date   : 2019/8/1210:08
 *    desc   : 转发到某个好友弹窗
 *    version: 1.0
 */
class ShareToFriendsDialog constructor(
    private val myContext: Context,
    private var avator: String?,
    private var nickname: String?,
    private var accid: String?,
    private var squareBean: SquareBean
) : Dialog(myContext, R.style.MyDialog),
    ModuleProxy {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_share_to_friends)
        initWindow()
        initView()
    }

    //        const val PIC = 1
    //        const val VIDEO = 2
    //        const val AUDIO = 3
    private fun initView() {
        if (accid == com.sdy.jitangapplication.common.Constants.ASSISTANT_ACCID) {
            GlideUtil.loadImg(myContext, R.drawable.icon_assistant, friendImg)
        } else {
            GlideUtil.loadImg(myContext, avator ?: "", friendImg)
        }
        friendNick.text = nickname ?: ""
        if (squareBean.type == SquareBean.PIC) { //图片
            if (squareBean.photo_json.isNullOrEmpty()) {
                friendShareContent.text = squareBean.descr ?: ""
                friendShareImg.visibility = View.GONE
                friendShareContent.visibility = View.VISIBLE
            } else {
                val params = friendShareImg.layoutParams
                params.height = SizeUtils.dp2px(140F)
                params.width = LinearLayout.LayoutParams.WRAP_CONTENT
                friendShareImg.layoutParams = params
                GlideUtil.loadRoundImgCenterinside(
                    myContext,
                    squareBean.photo_json?.get(0)?.url ?: "",
                    friendShareImg,
                    0.1F,
                    SizeUtils.dp2px(5F)
                )
                friendShareImg.visibility = View.VISIBLE
                friendShareContent.visibility = View.GONE
            }
        } else if (squareBean.type == SquareBean.VIDEO) {
            val params = friendShareImg.layoutParams
            params.height = SizeUtils.dp2px(140F)
            params.width = LinearLayout.LayoutParams.WRAP_CONTENT
            friendShareImg.layoutParams = params
            GlideUtil.loadRoundImgCenterinside(
                myContext,
                squareBean.cover_url ?: "",
                friendShareImg,
                0.1F,
                SizeUtils.dp2px(5F)
            )
            friendShareImg.visibility = View.VISIBLE
            friendShareContent.visibility = View.GONE
        } else if (squareBean.type == SquareBean.AUDIO) {
            friendShareContent.text = squareBean.descr ?: ""
            friendShareImg.visibility = View.GONE
            friendShareContent.visibility = View.VISIBLE
        }
        cancel.onClick {
            dismiss()
        }

        send.onClick(object : CustomClickListener() {
            override fun onSingleClick(view: View) {
                //发送消息
                sendShareMsg()
            }
        })

    }

    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.CENTER)
        val params = window?.attributes
        params?.width = ScreenUtils.getScreenWidth() - SizeUtils.dp2px(15F) * 2
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT
        params?.windowAnimations = R.style.MyDialogBottomAnimation

        window?.attributes = params
        //点击外部可取消
        setCanceledOnTouchOutside(false)
    }

    /**
     * 获取服务器上的图片尺寸
     */
    public fun getImgWH(urls: String): IntArray {

        try {
            val url = URL(urls)
            val conn = url.openConnection() as HttpURLConnection
            conn.doInput = true
            conn.connect()
            val inputStream = conn.inputStream
            val image = BitmapFactory.decodeStream(inputStream)

            val srcWidth = image.width     // 源图宽度
            val srcHeight = image.height   // 源图高度
            val intArray = intArrayOf(srcWidth, srcHeight)

            //释放资源
            image.recycle()
            inputStream.close()
            conn.disconnect()

            return intArray
        } catch (e: Exception) {

        }
        return intArrayOf(-1, -1)
    }

    /*-------------------------分享成功回调----------------------------*/
    private fun addShare() {
        val params = hashMapOf<String, Any>()
        params["square_id"] = squareBean?.id ?: 0
        RetrofitFactory.instance.create(Api::class.java)
            .addShare(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<Any?>>(null) {
                override fun onNext(t: BaseResp<Any?>) {
                    if (t.code == 200)
                        CommonFunction.toast("转发成功!")
                    else
                        CommonFunction.toast(t.msg)
                    dismiss()
                    (myContext as Activity).finish()
                }

                override fun onError(e: Throwable?) {
                    CommonFunction.toast("转发成功!")
                    dismiss()
                    (myContext as Activity).finish()
                }
            })
    }


    /*--------------------------消息代理------------------------*/
    private fun sendShareMsg() {
        val container = Container(ownerActivity, accid, SessionTypeEnum.P2P, this, true)
        val shareSquareAttachment = ShareSquareAttachment(
            squareBean.descr ?: "",
            friendMsgBox.text.toString(),
            squareBean.type,
            when (squareBean.type) {
                SquareBean.AUDIO -> {
                    squareBean.avatar ?: ""
                }
                SquareBean.VIDEO -> {
                    squareBean.cover_url ?: ""
                }
                SquareBean.PIC -> {
                    if (squareBean.photo_json.isNullOrEmpty()) {
                        squareBean.avatar
                    } else squareBean.photo_json!![0].url
                }
                else -> {
                    ""
                }
            }, squareBean.id ?: -1
        )
        val message = MessageBuilder.createCustomMessage(
            accid,
            SessionTypeEnum.P2P,
            "",
            shareSquareAttachment,
            CustomMessageConfig()
        )
        container.proxy.sendMessage(message)
    }

    override fun sendMessage(msg: IMMessage): Boolean {
        NIMClient.getService(MsgService::class.java).sendMessage(msg, false).setCallback(object :
            RequestCallback<Void?> {
            override fun onSuccess(param: Void?) {
                addShare()

            }

            override fun onFailed(code: Int) {
                CommonFunction.toast("转发失败！")
                dismiss()
            }

            override fun onException(exception: Throwable) {

            }
        })
        return true
    }

    override fun onInputPanelExpand() {

    }

    override fun shouldCollapseInputPanel() {

    }

    override fun isLongClickEnabled(): Boolean {
        return false
    }

    override fun onItemFooterClick(message: IMMessage?) {

    }
}