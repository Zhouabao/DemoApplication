package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import com.blankj.utilcode.util.SizeUtils
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.model.VideoVerifyBannerBean
import com.sdy.jitangapplication.ui.holder.VideoVerifyHolderView
import com.zhpan.bannerview.BannerViewPager
import kotlinx.android.synthetic.main.dialog_face_verify.*

/**
 *    author : ZFM
 *    date   : 2020/5/1910:12
 *    desc   :视频认证弹窗
 *    version: 1.0
 */
class VerifyAddChatDialog(val myContext: Context, val chatCount: Int) :
    Dialog(myContext, R.style.MyDialog) {
    companion object {
        const val POSITION_CANDY = 0 //获取糖果
        const val POSITION_CHAT = 1//无限聊天
        const val POSITION_EXPOSURE = 2//更多曝光
        const val POSITION_VERIFY_LOGO = 3//认证标识
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_face_verify)
        initWindow()
        initView()
    }


    private fun initView() {
        (bannerVideoVerify as BannerViewPager<VideoVerifyBannerBean, VideoVerifyHolderView>)
            .setHolderCreator { VideoVerifyHolderView() }
            .setIndicatorSliderRadius(SizeUtils.dp2px(3F))
            .setIndicatorSliderWidth(SizeUtils.dp2px(6f), SizeUtils.dp2px(18F))
            .setIndicatorHeight(SizeUtils.dp2px(6f))
            .setIndicatorSliderGap(SizeUtils.dp2px(5F))
            .create(
                mutableListOf(
                    VideoVerifyBannerBean(
                        "获得糖果奖励",
                        "过往积累的糖果奖励一次性领取",
                        R.drawable.icon_verify_to_get_candy
                    ),
                    //增加聊天机会
                    VideoVerifyBannerBean(
                        "今日聊天机会用完",
                        "每天增加$chatCount 次聊天机会",
                        R.drawable.icon_verify_to_chat
                    ),
                    VideoVerifyBannerBean(
                        "增加曝光率",
                        "增加卡片曝光率，附近页面优先推荐",
                        R.drawable.icon_verify_to_exposure
                    ),
                    VideoVerifyBannerBean(
                        "获取认证标识",
                        "获取认证标识，收获更多信任聊天",
                        R.drawable.icon_verify_to_logo
                    )
                )
            )
        bannerVideoVerify.currentItem = 1
        goVerifyBtn.clickWithTrigger {
            CommonFunction.startToFace(myContext)
        }
    }

    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.BOTTOM)
        val params = window?.attributes
        // 设置窗口背景透明度
//        params?.alpha = 0.5f
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT
//        params?.y = SizeUtils.dp2px(10F)
        params?.windowAnimations = R.style.MyDialogBottomAnimation
        window?.attributes = params
        //点击外部可取消
        setCanceledOnTouchOutside(true)
        setCancelable(true)
    }


}