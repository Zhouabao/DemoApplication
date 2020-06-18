package com.sdy.jitangapplication.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import androidx.core.view.isVisible
import com.blankj.utilcode.util.SizeUtils
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.ext.onClick
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.model.CopyMvBean
import com.sdy.jitangapplication.ui.activity.VideoVerifyActivity
import com.sdy.jitangapplication.utils.UserManager
import com.shuyu.gsyvideoplayer.GSYVideoManager
import kotlinx.android.synthetic.main.dialog_video_introduce_before.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.startActivityForResult

/**
 *    author : ZFM
 *    date   : 2020/6/919:39
 *    desc   :
 *    version: 1.0
 */
class VideoIntroduceBeforeDialog(val context1: Context, var requestCode: Int = -1) :
    Dialog(context1, R.style.MyDialog) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_video_introduce_before)
        initWindow()
        initView()

        getNormalMv()
    }

    private fun initView() {
        GlideUtil.loadCircleImg(context1, UserManager.getAvator(), avator)

        videoPlay.clickWithTrigger {
            videoStandard.isVisible = true
            playVideo()

        }


        verifyBtn.clickWithTrigger {
            if (requestCode != -1) {
                (context1 as Activity).startActivityForResult<VideoVerifyActivity>(
                    requestCode,
                    "copyMv" to copyMvBean
                )
            } else {
                context1.startActivity<VideoVerifyActivity>("copyMv" to copyMvBean)
            }
            verifyBtn.postDelayed({
                dismiss()
            }, 1000L)
        }


        //
        videoStandard.backButton.onClick {
            videoStandard.isVisible = false
            GSYVideoManager.releaseAllVideos()
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


    private var copyMvBean: CopyMvBean? = null
    //videoStandard
    private fun getNormalMv() {
        RetrofitFactory.instance.create(Api::class.java)
            .normalMv(UserManager.getSignParams())
            .excute(object : BaseSubscriber<BaseResp<CopyMvBean?>>() {
                override fun onNext(t: BaseResp<CopyMvBean?>) {
                    super.onNext(t)
                    copyMvBean = t.data
                    setVideoView()
                }


                override fun onError(e: Throwable?) {
                    super.onError(e)
                }
            })
    }


    private fun setVideoView() {
        if (copyMvBean != null && !copyMvBean?.mv_url.isNullOrEmpty()) {
            videoCover.isVisible = true
            videoPlay.isVisible = true
            avator.isVisible = false
            playVideo()
            GlideUtil.loadRoundImgCenterCrop(
                context1,
                copyMvBean?.mv_url_cover,
                videoCover,
                SizeUtils.dp2px(10f)
            )
        } else {
            videoCover.isVisible = false
            videoPlay.isVisible = false
            avator.isVisible = true
        }
    }

    private fun playVideo() {
        CommonFunction.initVideo(context1, videoStandard, copyMvBean?.mv_url ?: "")
    }

    override fun dismiss() {
        super.dismiss()
        GSYVideoManager.releaseAllVideos()
    }
}