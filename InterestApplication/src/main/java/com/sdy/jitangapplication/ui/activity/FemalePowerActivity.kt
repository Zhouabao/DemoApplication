package com.sdy.jitangapplication.ui.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.blankj.utilcode.util.ScreenUtils
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseActivity
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.baselibrary.utils.StatusBarUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.OnLazyClickListener
import com.sdy.jitangapplication.event.FemaleVerifyEvent
import com.sdy.jitangapplication.event.FemaleVideoEvent
import kotlinx.android.synthetic.main.activity_female_power.*
import kotlinx.android.synthetic.main.layout_actionbar.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.startActivityForResult

/**
 * 女性个人权益
 */
//       "contact" to userInfoBean?.userinfo?.contact_way,
//                    "verify" to userInfoBean?.userinfo?.isfaced,
//                    "video" to userInfoBean?.userinfo?.mv_faced
class FemalePowerActivity : BaseActivity(), OnLazyClickListener {
    private val contact by lazy { intent.getIntExtra("contact", 0) }//联系方式  0  没有 1 电话 2微信 3 qq
    private var verify = 0
    private var video = 0
    private val url by lazy { intent.getStringExtra("url") }

    companion object {
        const val REQUEST_ACCOUNT = 110
        const val REQUEST_VERIFY = 120
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_female_power)
        initView()
    }

    private fun initView() {
        EventBus.getDefault().register(this)
        StatusBarUtil.immersive(this)
        llTitle.setBackgroundColor(Color.parseColor("#FFFFDCC1"))
        hotT1.text = getString(R.string.power_title)
        hotT1.setTextColor(Color.WHITE)
        btnBack.setImageResource(R.drawable.icon_back_white)
        btnBack.onClick {
            finish()
        }
        video = intent.getIntExtra("video", 0)
        verify = intent.getIntExtra("verify", 0)


//        0 未认证 1通过 2机审中 3人审中 4被拒（弹框）
        updateVerifyAndVideo()

        when (contact) {
            0 -> {
                changeContact.text = getString(R.string.bind_now)
            }
            else -> {
                changeContact.text = getString(R.string.change)
            }
        }


        powerContact.setOnClickListener(this)
        changeContact.setOnClickListener(this)
        powerVerify.setOnClickListener(this)
        changeVerify.setOnClickListener(this)
        powerVideo.setOnClickListener(this)
        changeVideo.setOnClickListener(this)


        val params = powerBg.layoutParams as ConstraintLayout.LayoutParams
        params.width = ScreenUtils.getScreenWidth()
        params.height = (210 * ScreenUtils.getScreenWidth() / 350F).toInt()
        powerBg.layoutParams = params

        powerVerify.setCompoundDrawablesWithIntrinsicBounds(
            null, resources.getDrawable(
                if (verify != 1) {
                    R.drawable.icon_female_verify_no
                } else {
                    R.drawable.icon_female_verify_open
                }
            ), null, null
        )
        powerContact.setCompoundDrawablesWithIntrinsicBounds(
            null, resources.getDrawable(
                if (contact != 0) {
                    R.drawable.icon_female_contact_open
                } else {
                    R.drawable.icon_female_contact_no
                }
            ), null, null
        )


        GlideUtil.loadImg(this, url, allPowerIv)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_ACCOUNT) {
                powerContact.setCompoundDrawablesWithIntrinsicBounds(
                    null, resources.getDrawable(
                        if (data?.getIntExtra("contact", 0) != 0) {
                            R.drawable.icon_female_contact_open
                        } else {
                            R.drawable.icon_female_contact_no
                        }
                    ), null, null
                )
            } else if (requestCode == REQUEST_VERIFY) {
//                /verify
                powerContact.setCompoundDrawablesWithIntrinsicBounds(
                    null, resources.getDrawable(
                        if (data?.getIntExtra("verify", 0) != 1) {
                            R.drawable.icon_female_contact_no
                        } else {
                            R.drawable.icon_female_contact_open
                        }
                    ), null, null
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }


    /**
     * @param event showTop是否展示topShow
     *       //      0 没有视频/拒绝   1视频通过  2视频审核中
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTopCardEvent(event: FemaleVideoEvent) {
        video = event.videoState
        updateVerifyAndVideo()
    }

    /**
     *
     *         //        0 未认证 1通过 2机审中 3人审中 4被拒（弹框）
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onFemaleVerifyEvent(event: FemaleVerifyEvent) {
        verify = event.verifyState
        updateVerifyAndVideo()
    }

    private fun updateVerifyAndVideo() {
        powerVideo.setCompoundDrawablesWithIntrinsicBounds(
            null, resources.getDrawable(
                if (video == 1) {
                    R.drawable.icon_female_video_open
                } else {
                    R.drawable.icon_female_video_no
                }
            ), null, null
        )

        //        0 未认证 1通过 2机审中 3人审中 4被拒（弹框）
        when (verify) {
            0 -> {
                changeVerify.isVisible = true
                changeVerify.text = getString(R.string.verify_now)
                changeVideo.text = getString(R.string.please_verify)
            }
            1 -> {
                changeVerify.isVisible = false
                //      0 没有视频/拒绝   1视频通过  2视频审核中
                if (video == 1) {
                    changeVideo.text = getString(R.string.replace_video)
                } else if (video == 0) {
                    changeVideo.text = getString(R.string.record_video)
                } else if (video == 2) {
                    changeVideo.text = getString(R.string.checking)
                }
            }
            2, 3 -> {
                changeVerify.isVisible = false
                changeVideo.text = getString(R.string.please_verify)
            }
        }
    }

    override fun onLazyClick(v: View) {
        when (v.id) {
            R.id.powerContact, R.id.changeContact -> { //联系方式
                startActivityForResult<ChangeUserContactActivity>(REQUEST_ACCOUNT)
            }
            R.id.powerVerify, R.id.changeVerify -> { //真人认证
                when (verify) {
                    1 -> {
                        CommonFunction.toast(getString(R.string.verify_pass))
                    }
                    2, 3 -> {
                        CommonFunction.toast(getString(R.string.verify_checking))
                    }
                    else -> {
                        CommonFunction.startToFace(this, requestCode = REQUEST_VERIFY)
                    }
                }
            }
            R.id.powerVideo, R.id.changeVideo -> { //视频介绍
//                CommonFunction.startToVideoIntroduce(this)
                if (verify == 1) {
                    if (video == 0) {
                        CommonFunction.startToVideoIntroduce(this)
                    } else if (video == 2) {
                        CommonFunction.toast(getString(R.string.video_checking_waiting))
                    } else {
                        CommonFunction.startToVideoIntroduce(this)
//                    CommonFunction.toast("您已经通过视频介绍")
                    }
                } else {
                    CommonFunction.toast(getString(R.string.video_verify_first_then_introduce))
                }
            }
        }
    }

}
