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
import kotlinx.android.synthetic.main.dialog_transpond.*


/**
 *    author : ZFM
 *    date   : 2019/6/2716:22
 *    desc   : 转发动态对话框
 *    version: 1.0
 */
class TranspondDialog(val myContext: Context, var squareBean: SquareBean? = null) : Dialog(myContext, R.style.MyDialog),
    View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_transpond)
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
    }


    private var position: Int = -1
    private fun initView() {
        transpondFriend.setOnClickListener(this)
        transpondWechat.setOnClickListener(this)
        transpondWechatZone.setOnClickListener(this)
        transpondWebo.setOnClickListener(this)
        transpondQQ.setOnClickListener(this)
        transpondQQZone.setOnClickListener(this)
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
        }

    }


    /**
     * 分享动态
    const val PIC = 1
    const val VIDEO = 2
    const val AUDIO = 3
     */
    private fun shareToThirdParty(platformConfig: SHARE_MEDIA) {
        if (squareBean?.type == SquareBean.PIC) {
            //多图上传,需要带文字描述
            if (!squareBean?.photo_json.isNullOrEmpty()) {
                val image = UMImage(myContext, squareBean?.photo_json?.get(0)?.url)//
                //大小压缩，默认为大小压缩，适合普通很大的图
                image.compressStyle = UMImage.CompressStyle.SCALE
                image.compressFormat = Bitmap.CompressFormat.JPEG
                image.title = "${squareBean?.nickname} 在积糖发了一张照片"
                image.description = if (!squareBean?.descr.isNullOrEmpty()) {
                    squareBean?.descr
                } else "「你先抓紧看看这个」"
                ShareAction(myContext as Activity)
                    .setPlatform(platformConfig)
                    .withText(
                        if (!squareBean?.descr.isNullOrEmpty()) {
                            squareBean?.descr
                        } else "「你先抓紧看看这个」"
                    )//分享内容
                    .withMedia(image)//多张图片
//                    .withMedias(*images)//多张图片
                    .setCallback(callback)
                    .share()

            } else {            //文本分享
//                http://www.baidu.com
                val web = UMWeb("http://")
                web.title = "${squareBean?.nickname} 在积糖发了动态"//标题
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
            video.title = "${squareBean?.nickname} 在积糖发了一段视频"
            video.description = if (!squareBean?.descr.isNullOrEmpty()) {
                squareBean?.descr
            } else "「你先抓紧看看这个」"
            ShareAction(myContext as Activity)
                .setPlatform(platformConfig)
                .withMedia(video)
                .setCallback(callback)
                .share()
        } else if (squareBean?.type == SquareBean.AUDIO) {
            val audio = UMusic(squareBean?.audio_json?.get(0)?.url)
            audio.setThumb(UMImage(myContext, squareBean?.avatar ?: ""))
            audio.setmTargetUrl(squareBean?.audio_json?.get(0)?.url)
            audio.title = "${squareBean?.nickname} 在积糖发了一段语音"
            audio.description = if (!squareBean?.descr.isNullOrEmpty()) {
                squareBean?.descr
            } else "「你先抓紧看看这个」"
            ShareAction(myContext as Activity)
                .setPlatform(platformConfig)
                .withText(squareBean?.descr ?: "")
                .withMedia(audio)
                .setCallback(callback)
                .share()
        }
    }


    /*第三方平台分享回调*/
    private val callback = object : UMShareListener {
        /**
         * @descrption 分享成功的回调
         * @param platform 平台类型
         */
        override fun onResult(p0: SHARE_MEDIA?) {
            Log.d("share===", "结果。。。。")

            addShare()
        }

        /**
         * @descrption 分享取消的回调
         * @param platform 平台类型
         */
        override fun onCancel(p0: SHARE_MEDIA?) {
            Log.d("share===", "取消。。。。")

            dismiss()
        }

        /**
         * @descrption 分享失败的回调
         * @param platform 平台类型
         * @param t 错误原因
         */
        override fun onError(p0: SHARE_MEDIA, p1: Throwable) {
            Log.d("share===", "${p0.getName()}================${p1.message ?: ""}")
            CommonFunction.toast("分享失败")
        }

        /**
         * @descrption 分享开始的回调
         * @param platform 平台类型
         */
        override fun onStart(p0: SHARE_MEDIA?) {
            Log.d("share===", "开始。。。。")

        }

    }


    /*-------------------------分享成功回调----------------------------*/
    private fun addShare() {
        val params = UserManager.getBaseParams()
        params["square_id"] = squareBean?.id ?: 0
        RetrofitFactory.instance.create(Api::class.java)
            .addShare(params)
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
        UMShareAPI.get(myContext).release()

    }


}