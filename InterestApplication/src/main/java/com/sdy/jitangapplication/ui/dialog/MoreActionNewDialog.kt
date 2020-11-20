package com.sdy.jitangapplication.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.core.view.isVisible
import com.huawei.hms.framework.common.PackageUtils
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.model.SquareBean
import com.sdy.jitangapplication.ui.activity.ContactBookActivity
import com.sdy.jitangapplication.utils.UserManager
import com.umeng.socialize.ShareAction
import com.umeng.socialize.UMShareAPI
import com.umeng.socialize.UMShareListener
import com.umeng.socialize.bean.SHARE_MEDIA
import com.umeng.socialize.media.UMImage
import com.umeng.socialize.media.UMVideo
import com.umeng.socialize.media.UMWeb
import com.umeng.socialize.media.UMusic
import kotlinx.android.synthetic.main.dialog_more_action_new.*


/**
 *    author : ZFM
 *    date   : 2019/6/2716:22
 *    desc   : 新的更多操作对话框
 *    version: 1.0
 */
class MoreActionNewDialog(
    private var myContext: Context,
    var squareBean: SquareBean? = null,
    var url: String = "",
    var type: Int = TYPE_SHARE_SQUARE,
    var title: String = "",
    var content: String = "",
    var pic: String = ""
) :
    Dialog(myContext, R.style.MyDialog), View.OnClickListener {

    companion object {
        const val TYPE_SHARE_SQUARE = 1
        const val TYPE_SHARE_VIP_URL = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_more_action_new)
        initWindow()
        initView()
    }


    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.BOTTOM)
        val params = window?.attributes
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT
        params?.windowAnimations = R.style.MyDialogBottomAnimation

        window?.attributes = params
        //点击外部可取消
        setCanceledOnTouchOutside(true)
    }


    private val umShareAPI  by lazy { UMShareAPI.get(myContext) }
    private fun initView() {
        transpondFriend.isVisible = type != TYPE_SHARE_VIP_URL
        transpondFriend.setOnClickListener(this)
        transpondWechat.setOnClickListener(this)
        transpondWechatZone.setOnClickListener(this)
        transpondWebo.setOnClickListener(this)
        transpondQQ.setOnClickListener(this)
        transpondQQZone.setOnClickListener(this)
        transpondFacebook.setOnClickListener(this)
        transpondIns.setOnClickListener(this)
        transpondPinterest.setOnClickListener(this)

        try {
            transpondWechat.isVisible =
                umShareAPI.isInstall(myContext as Activity, SHARE_MEDIA.WEIXIN)
            transpondWechatZone.isVisible =
                umShareAPI.isInstall(myContext as Activity, SHARE_MEDIA.WEIXIN)
            transpondQQ.isVisible =
                umShareAPI.isInstall(myContext as Activity, SHARE_MEDIA.QQ)
            transpondQQZone.isVisible =
                umShareAPI.isInstall(myContext as Activity, SHARE_MEDIA.QQ)
            transpondWebo.isVisible =
                umShareAPI.isInstall(myContext as Activity, SHARE_MEDIA.SINA)

            transpondFacebook.isVisible = umShareAPI.isInstall(myContext as Activity, SHARE_MEDIA.FACEBOOK)
            transpondIns.isVisible = umShareAPI.isInstall(myContext as Activity, SHARE_MEDIA.INSTAGRAM)
            transpondPinterest.isVisible = umShareAPI.isInstall(myContext as Activity, SHARE_MEDIA.PINTEREST)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.transpondFriend -> {//转发到好友
                if (squareBean != null) {
                    ContactBookActivity.start(context, squareBean!!)
                    dismiss()
                }
            }
            R.id.transpondWebo -> {//微博
                shareToThirdParty(SHARE_MEDIA.SINA)
            }
            R.id.transpondWechat -> {//微信
                shareToThirdParty(SHARE_MEDIA.WEIXIN)
            }
            R.id.transpondWechatZone -> {//朋友圈
                shareToThirdParty(SHARE_MEDIA.WEIXIN_CIRCLE)

            }
            R.id.transpondQQ -> {//QQ
                shareToThirdParty(SHARE_MEDIA.QQ)

            }
            R.id.transpondQQZone -> {//QQ空间
                shareToThirdParty(SHARE_MEDIA.QZONE)
            }
            R.id.transpondFacebook -> {//facebook
                shareToThirdParty(SHARE_MEDIA.FACEBOOK)
            }
            R.id.transpondIns -> {//instagram
                shareToThirdParty(SHARE_MEDIA.INSTAGRAM)
            }
            R.id.transpondPinterest -> {//pinterest
                shareToThirdParty(SHARE_MEDIA.PINTEREST)
            }
        }

    }


    /**
     * 封装分享
     */
    private fun shareToThirdParty(platformConfig: SHARE_MEDIA) {
        if (type == TYPE_SHARE_VIP_URL) {
            shareWeb(platformConfig)
        } else {
            shareSquare(platformConfig)
        }
    }


    /**
     * 分享动态
    const val PIC = 1
    const val VIDEO = 2
    const val AUDIO = 3
     */
    private fun shareSquare(platformConfig: SHARE_MEDIA) {
        if (squareBean?.type == SquareBean.PIC) {
            //多图上传,需要带文字描述
            if (!squareBean?.photo_json.isNullOrEmpty()) {
                val image = UMImage(myContext, squareBean?.photo_json?.get(0)?.url)//
                //大小压缩，默认为大小压缩，适合普通很大的图
                image.compressStyle = UMImage.CompressStyle.SCALE
                image.compressFormat = Bitmap.CompressFormat.JPEG
                image.title =
                    myContext.getString(R.string.send_a_pic_in_app, squareBean?.nickname.toString())
                image.description = if (!squareBean?.descr.isNullOrEmpty()) {
                    squareBean?.descr
                } else myContext.getString(R.string.hurry_to_see_this)
                ShareAction(myContext as Activity)
                    .setPlatform(platformConfig)
                    .withText(
                        if (!squareBean?.descr.isNullOrEmpty()) {
                            squareBean?.descr
                        } else myContext.getString(R.string.hurry_to_see_this)
                    )//分享内容
                    .withMedia(image)//多张图片
                    //                    .withMedias(*images)//多张图片
                    .setCallback(callback)
                    .share()

            } else {            //文本分享
                //                http://www.baidu.com
                val web = UMWeb("http://")
                web.title = myContext.getString(
                    R.string.send_a_square_in_app,
                    squareBean?.nickname.toString()
                )//标题
                web.setThumb(UMImage(myContext, squareBean?.avatar ?: ""))  //缩略图
                web.description = squareBean?.descr ?: ""//描述
                if (platformConfig == SHARE_MEDIA.QQ) {
                    ShareAction(myContext as Activity)
                        .setPlatform(platformConfig)
                        .withText(squareBean?.descr ?: "")
                        .withMedia(web)
                        .setCallback(callback)
                        .share()
                } else {
                    ShareAction(myContext as Activity)
                        .setPlatform(platformConfig)
                        .withText(squareBean?.descr ?: "")
                        .setCallback(callback)
                        .share()
                }


            }
        } else if (squareBean?.type == SquareBean.VIDEO) {//视频分享
            val video = UMVideo(squareBean?.video_json?.get(0)?.url)//
            val thumbImg = UMImage(myContext, squareBean?.cover_url ?: "")
            //大小压缩，默认为大小压缩，适合普通很大的图
            thumbImg.compressStyle = UMImage.CompressStyle.SCALE
            thumbImg.compressFormat = Bitmap.CompressFormat.PNG
            video.setThumb(thumbImg)
            video.title =
                myContext.getString(R.string.send_a_video_in_app, squareBean?.nickname.toString())
            video.description = if (!squareBean?.descr.isNullOrEmpty()) {
                squareBean?.descr
            } else myContext.getString(R.string.hurry_to_see_this)
            ShareAction(myContext as Activity)
                .setPlatform(platformConfig)
                .withMedia(video)
                .setCallback(callback)
                .share()
        } else if (squareBean?.type == SquareBean.AUDIO) {
            val audio = UMusic(squareBean?.audio_json?.get(0)?.url)
            audio.setThumb(UMImage(myContext, squareBean?.avatar ?: ""))
            audio.setmTargetUrl(squareBean?.audio_json?.get(0)?.url)
            audio.title =
                myContext.getString(R.string.send_a_audio_in_app, squareBean?.nickname.toString())
            audio.description = if (!squareBean?.descr.isNullOrEmpty()) {
                squareBean?.descr
            } else myContext.getString(R.string.hurry_to_see_this)
            ShareAction(myContext as Activity)
                .setPlatform(platformConfig)
                .withText(squareBean?.descr ?: "")
                .withMedia(audio)
                .setCallback(callback)
                .share()
        }
    }


    /**
     * 链接分享
     */
    private fun shareWeb(platformConfig: SHARE_MEDIA) {
        val web = UMWeb(url)
        web.title = title
        web.description = content
        web.setThumb(UMImage(myContext, pic)) //缩略图
        ShareAction(myContext as Activity)
            .setPlatform(platformConfig)
            .withMedia(web)
            .setCallback(callback)
            .share()
    }


    /*第三方平台分享回调*/
    private val callback = object : UMShareListener {
        /**
         * @descrption 分享成功的回调
         * @param platform 平台类型
         */
        override fun onResult(p0: SHARE_MEDIA?) {
            if (type == TYPE_SHARE_SQUARE)
                addShare()
            else
                dismiss()
        }

        /**
         * @descrption 分享取消的回调
         * @param platform 平台类型
         */
        override fun onCancel(p0: SHARE_MEDIA?) {

            dismiss()
        }

        /**
         * @descrption 分享失败的回调
         * @param platform 平台类型
         * @param t 错误原因
         */
        override fun onError(p0: SHARE_MEDIA, p1: Throwable) {
            Log.d("share===", "${p0.getName()}================${p1.message ?: ""}")
            CommonFunction.toast(myContext.getString(R.string.share_fail))
        }

        /**
         * @descrption 分享开始的回调
         * @param platform 平台类型
         */
        override fun onStart(p0: SHARE_MEDIA?) {

        }

    }


    /*-------------------------分享成功回调----------------------------*/
    private fun addShare() {
        val params = hashMapOf<String, Any>()
        params["square_id"] = squareBean?.id ?: 0
        RetrofitFactory.instance.create(Api::class.java)
            .addShare(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<Any?>>(null) {
                override fun onNext(t: BaseResp<Any?>) {
                    CommonFunction.toast(t.msg)
                    dismiss()
                }

                override fun onError(e: Throwable?) {
                    dismiss()
                }
            })
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        umShareAPI.release()

    }

}