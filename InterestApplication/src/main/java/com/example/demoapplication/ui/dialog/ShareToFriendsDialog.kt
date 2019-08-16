package com.example.demoapplication.ui.dialog

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
import com.blankj.utilcode.util.ToastUtils
import com.example.baselibrary.glide.GlideUtil
import com.example.demoapplication.R
import com.example.demoapplication.model.SquareBean
import com.example.demoapplication.nim.extension.ShareSquareAttachment
import com.kotlin.base.ext.onClick
import com.netease.nim.uikit.business.session.module.Container
import com.netease.nim.uikit.business.session.module.ModuleProxy
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.msg.MessageBuilder
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.CustomMessageConfig
import com.netease.nimlib.sdk.msg.model.IMMessage
import kotlinx.android.synthetic.main.dialog_share_to_friends.*
import java.net.HttpURLConnection
import java.net.URL

/**
 *    author : ZFM
 *    date   : 2019/8/1210:08
 *    desc   : 转发到某个好友弹窗
 *    version: 1.0
 */
class ShareToFriendsDialog @JvmOverloads constructor(
    context: Context,
    private var avator: String?,
    private var nickname: String?,
    private var accid: String?,
    private var squareBean: SquareBean
) : Dialog(context, R.style.MyDialog),
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
        GlideUtil.loadImg(context, avator ?: "", friendImg)
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
                GlideUtil.loadRoundImgCenterinside(context, squareBean.photo_json?.get(0)?.url ?: "", friendShareImg, 0.1F, SizeUtils.dp2px(5F))
                friendShareImg.visibility = View.VISIBLE
                friendShareContent.visibility = View.GONE
            }
        } else if (squareBean.type == SquareBean.VIDEO) {
            val params = friendShareImg.layoutParams
            params.height = SizeUtils.dp2px(140F)
            params.width = LinearLayout.LayoutParams.WRAP_CONTENT
            friendShareImg.layoutParams = params
            GlideUtil.loadRoundImgCenterinside(context, squareBean.cover_url ?: "", friendShareImg, 0.1F, SizeUtils.dp2px(5F))
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

        send.onClick {
            //发送消息
            sendShareMsg()
        }

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
                ToastUtils.showShort("转发成功!")
                dismiss()
                ownerActivity?.finish()
            }

            override fun onFailed(code: Int) {
                ToastUtils.showShort("$code")
            }

            override fun onException(exception: Throwable) {
                ToastUtils.showShort(exception.message ?: "")
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