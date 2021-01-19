package com.sdy.jitangapplication.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.core.view.isVisible
import com.blankj.utilcode.constant.PermissionConstants
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.PermissionUtils
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.share.Sharer
import com.facebook.share.model.SharePhoto
import com.facebook.share.model.SharePhotoContent
import com.facebook.share.widget.ShareDialog
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.rx.BaseSubscriber
import com.kotlin.base.ui.activity.BaseActivity
import com.sdy.baselibrary.utils.RandomUtils
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.event.SaveImgSuccessEvent
import com.sdy.jitangapplication.model.SquareBean
import com.sdy.jitangapplication.ui.activity.ContactBookActivity
import com.sdy.jitangapplication.utils.SaveNetPhotoUtils
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.dialog_more_action_new.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File


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

        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        builder.detectFileUriExposure()
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


//    private val umShareAPI by lazy { UMShareAPI.get(myContext) }
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


        if (UserManager.overseas) {
            transpondFacebook.isVisible =
                !squareBean?.photo_json.isNullOrEmpty() || !squareBean?.audio_json.isNullOrEmpty() || !squareBean?.video_json.isNullOrEmpty()
            transpondIns.isVisible =
                AppUtils.isAppInstalled("com.instagram.android") && !squareBean?.photo_json.isNullOrEmpty()
            transpondWechat.isVisible = false
            transpondWechatZone.isVisible = false
            transpondQQ.isVisible = false
            transpondQQZone.isVisible = false
            transpondWebo.isVisible = false
        } else {
            transpondWechat.isVisible = AppUtils.isAppInstalled("com.tencent.mm")
            transpondWechatZone.isVisible = AppUtils.isAppInstalled("com.tencent.mm")
            transpondQQ.isVisible = AppUtils.isAppInstalled("com.tencent.mobileqq")
            transpondQQZone.isVisible = AppUtils.isAppInstalled("com.tencent.mobileqq")
            transpondWebo.isVisible = AppUtils.isAppInstalled("com.sina.weibo")

            transpondFacebook.isVisible = false
            transpondIns.isVisible = false
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
//                shareToThirdParty(SHARE_MEDIA.SINA)
            }
            R.id.transpondWechat -> {//微信
//                shareToThirdParty(SHARE_MEDIA.WEIXIN)
            }
            R.id.transpondWechatZone -> {//朋友圈
//                shareToThirdParty(SHARE_MEDIA.WEIXIN_CIRCLE)

            }
            R.id.transpondQQ -> {//QQ
//                shareToThirdParty(SHARE_MEDIA.QQ)

            }
            R.id.transpondQQZone -> {//QQ空间
//                shareToThirdParty(SHARE_MEDIA.QZONE)
            }
            R.id.transpondFacebook -> {//facebook
                if (!PermissionUtils.isGranted(
                        *PermissionConstants.getPermissions(
                            PermissionConstants.STORAGE
                        )
                    )
                ) {
                    PermissionUtils.permission(PermissionConstants.STORAGE)
                        .callback(object : PermissionUtils.SimpleCallback {
                            override fun onGranted() {
                                shareWay = 1
                                downloadShareImg()
                            }

                            override fun onDenied() {
                                CommonFunction.toast(myContext.getString(R.string.permission_storage))
                            }

                        })
                        .request()
                } else {
                    shareWay = 1
                    downloadShareImg()
                }
            }
            R.id.transpondIns -> {//instagram
                shareWay=2
                downloadShareImg()
//                shareToThirdParty(SHARE_MEDIA.INSTAGRAM)
            }

        }

    }

    /**
     * 封装分享
     */
//    private fun shareToThirdParty(platformConfig: SHARE_MEDIA) {
//        if (type == TYPE_SHARE_VIP_URL) {
//            shareWeb(platformConfig)
//        } else {
//            shareSquare(platformConfig)
//        }
//    }


    /**
     * facebook分享先下载图片再分享
     */
    private fun downloadShareImg() {
        val newImgPath = "share-${RandomUtils.getRandomString(16)}.jpg"
        SaveNetPhotoUtils.savePhoto(
            myContext, if (!squareBean?.video_json.isNullOrEmpty()) {
                squareBean?.video_json!![0].url
            } else if (!squareBean?.photo_json.isNullOrEmpty()) {
                squareBean?.photo_json!![0].url
            } else if (!squareBean?.audio_json.isNullOrEmpty()) {
                squareBean?.audio_json!![0].url
            } else {
                squareBean?.avatar
            }, newImgPath
        )

    }

    /**
     * facebook分享单独封装
     */
    fun shareFacebook(newImgPath: File) {
        val shareDialog = ShareDialog(myContext as Activity)
        shareDialog.registerCallback(
            (myContext as BaseActivity).callbackManager,
            object : FacebookCallback<Sharer.Result> {
                override fun onSuccess(result: Sharer.Result) {
                    FileUtils.delete(newImgPath)
                    Log.d("share===", "onresult ${result}================")
                    addShare()
                    dismiss()
                }

                override fun onCancel() {
                    Log.d("share===", "onCancel ================")

                }

                override fun onError(error: FacebookException?) {
                    Log.d("share===", "onError ${error}================")

                }

            })

        if (newImgPath.exists()) {
            val shareMedia = SharePhoto.Builder()
                .setImageUrl(Uri.fromFile(newImgPath))
                .setCaption(
                    if (!squareBean?.descr.isNullOrEmpty()) {
                        squareBean?.descr
                    } else myContext.getString(R.string.hurry_to_see_this)
                )
                .build()
            val shareUrl = SharePhotoContent.Builder()
                .addPhoto(shareMedia)
                .build()
            shareDialog.show(shareUrl)
        }
    }

    /**
     * 分享到Ins(本地图片)
     */

    private fun sharedToIns(newImgPath: File) {
        val loadingDialog = LoadingDialog(myContext)
        loadingDialog.show()
        val type = "image/*"
        val uri = Uri.fromFile(newImgPath)
//            Uri.parse("android.resource://" + context?.getPackageName() + "/" + R.mipmap.img_share_toins)
        val share = Intent(Intent.ACTION_SEND)
        share.type = type
        share.putExtra(Intent.EXTRA_STREAM, uri)
        share.putExtra(Intent.EXTRA_TITLE, "share SugarTown")
        share.setPackage("com.instagram.android")
//        myContext.startActivity(Intent.createChooser(share,"share SugarTown"))
        myContext.startActivity(share)
        loadingDialog.dismiss()
    }


    /**
     * 分享动态
    const val PIC = 1
    const val VIDEO = 2
    const val AUDIO = 3
     */
//    private fun shareSquare(platformConfig: SHARE_MEDIA) {
//        if (squareBean?.type == SquareBean.PIC) {
//            //多图上传,需要带文字描述
//            if (!squareBean?.photo_json.isNullOrEmpty()) {
//                val image = UMImage(myContext, squareBean?.photo_json?.get(0)?.url)//
//                //大小压缩，默认为大小压缩，适合普通很大的图
//                image.compressStyle = UMImage.CompressStyle.SCALE
//                image.compressFormat = Bitmap.CompressFormat.JPEG
//                image.title =
//                    myContext.getString(
//                        R.string.send_a_pic_in_app,
//                        squareBean?.nickname.toString()
//                    )
//                image.description = if (!squareBean?.descr.isNullOrEmpty()) {
//                    squareBean?.descr
//                } else myContext.getString(R.string.hurry_to_see_this)
//                ShareAction(myContext as Activity)
//                    .setPlatform(platformConfig)
//                    .withText(
//                        if (!squareBean?.descr.isNullOrEmpty()) {
//                            squareBean?.descr
//                        } else myContext.getString(R.string.hurry_to_see_this)
//                    )//分享内容
//                    .withMedia(image)//多张图片
//                    //                    .withMedias(*images)//多张图片
//                    .setCallback(callback)
//                    .share()
//
//            } else {            //文本分享
//                //                http://www.baidu.com
//                val web = UMWeb("http://")
//                web.title = myContext.getString(
//                    R.string.send_a_square_in_app,
//                    squareBean?.nickname.toString()
//                )//标题
//                web.setThumb(UMImage(myContext, squareBean?.avatar ?: ""))  //缩略图
//                web.description = squareBean?.descr ?: ""//描述
//                if (platformConfig == SHARE_MEDIA.QQ) {
//                    ShareAction(myContext as Activity)
//                        .setPlatform(platformConfig)
//                        .withText(squareBean?.descr ?: "")
//                        .withMedia(web)
//                        .setCallback(callback)
//                        .share()
//                } else {
//                    ShareAction(myContext as Activity)
//                        .setPlatform(platformConfig)
//                        .withText(squareBean?.descr ?: "")
//                        .setCallback(callback)
//                        .share()
//                }
//
//
//            }
//        } else if (squareBean?.type == SquareBean.VIDEO) {//视频分享
//            val video = UMVideo(squareBean?.video_json?.get(0)?.url)//
//            val thumbImg = UMImage(myContext, squareBean?.cover_url ?: "")
//            //大小压缩，默认为大小压缩，适合普通很大的图
//            thumbImg.compressStyle = UMImage.CompressStyle.SCALE
//            thumbImg.compressFormat = Bitmap.CompressFormat.PNG
//            video.setThumb(thumbImg)
//            video.title =
//                myContext.getString(
//                    R.string.send_a_video_in_app,
//                    squareBean?.nickname.toString()
//                )
//            video.description = if (!squareBean?.descr.isNullOrEmpty()) {
//                squareBean?.descr
//            } else myContext.getString(R.string.hurry_to_see_this)
//            ShareAction(myContext as Activity)
//                .setPlatform(platformConfig)
//                .withMedia(video)
//                .setCallback(callback)
//                .share()
//        } else if (squareBean?.type == SquareBean.AUDIO) {
//            val audio = UMusic(squareBean?.audio_json?.get(0)?.url)
//            audio.setThumb(UMImage(myContext, squareBean?.avatar ?: ""))
//            audio.setmTargetUrl(squareBean?.audio_json?.get(0)?.url)
//            audio.title =
//                myContext.getString(
//                    R.string.send_a_audio_in_app,
//                    squareBean?.nickname.toString()
//                )
//            audio.description = if (!squareBean?.descr.isNullOrEmpty()) {
//                squareBean?.descr
//            } else myContext.getString(R.string.hurry_to_see_this)
//            ShareAction(myContext as Activity)
//                .setPlatform(platformConfig)
//                .withText(squareBean?.descr ?: "")
//                .withMedia(audio)
//                .setCallback(callback)
//                .share()
//        }
//    }


    /**
     * 链接分享
     */
//    private fun shareWeb(platformConfig: SHARE_MEDIA) {
//        val web = UMWeb(url)
//        web.title = title
//        web.description = content
//        web.setThumb(UMImage(myContext, pic)) //缩略图
//        ShareAction(myContext as Activity)
//            .setPlatform(platformConfig)
//            .withMedia(web)
//            .setCallback(callback)
//            .share()
//    }


    /*第三方平台分享回调*/
//    private val callback = object : UMShareListener {
//        /**
//         * @descrption 分享成功的回调
//         * @param platform 平台类型
//         */
//        override fun onResult(p0: SHARE_MEDIA) {
//            Log.d("share===", "onresult ${p0?.getName()}================")
//            if (type == TYPE_SHARE_SQUARE)
//                addShare(p0)
//            else
//                dismiss()
//        }
//
//        /**
//         * @descrption 分享取消的回调
//         * @param platform 平台类型
//         */
//        override fun onCancel(p0: SHARE_MEDIA?) {
//            Log.d("share===", "cancel ${p0?.getName()}================")
//
//            dismiss()
//        }
//
//        /**
//         * @descrption 分享失败的回调
//         * @param platform 平台类型
//         * @param t 错误原因
//         */
//        override fun onError(p0: SHARE_MEDIA, p1: Throwable) {
//            Log.d("share===", "onerror ${p0.getName()}================${p1.message ?: ""}")
//            CommonFunction.toast(myContext.getString(R.string.share_fail))
//        }
//
//        /**
//         * @descrption 分享开始的回调
//         * @param platform 平台类型
//         */
//        override fun onStart(p0: SHARE_MEDIA) {
//            Log.d("share===", "onStart ${p0.getName()}================")
//        }
//
//    }


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
//        umShareAPI.release()

    }

    override fun dismiss() {
        super.dismiss()
//        umShareAPI.release()
        EventBus.getDefault().unregister(this)
    }

    override fun show() {
        super.show()
        EventBus.getDefault().register(this)
    }


    private var shareWay = 1 //1 facebook 2 instagarm

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun saveImgSuccess(successEvent: SaveImgSuccessEvent) {
        if (shareWay == 1) {
            shareFacebook(successEvent.filePath)
        } else {
            sharedToIns(successEvent.filePath)
        }
//        shareToIns(successEvent.filePath)
    }


    fun onActivityResult(activity: Activity, requestCode: Int, resultCode: Int, data: Intent?) {
//        UMShareAPI.get(activity).onActivityResult(requestCode, resultCode, data)
//        callbackManager.onActivityResult(requestCode, resultCode, data)
    }
}