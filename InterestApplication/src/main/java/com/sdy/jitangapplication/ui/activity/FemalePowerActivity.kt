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
    private val verify by lazy { intent.getIntExtra("verify", 0) }
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
        hotT1.text = "个人权益"
        hotT1.setTextColor(Color.WHITE)
        btnBack.setImageResource(R.drawable.icon_back_white)
        btnBack.onClick {
            finish()
        }
        video = intent.getIntExtra("video", 0)



        when (verify) {
            0 -> {
                changeVerify.isVisible = true
                changeVerify.text = "立即认证"
                changeVideo.text = "请先认证"
            }
            1 -> {
                changeVerify.isVisible = false
                if (video == 1)
                    changeVideo.text = "替换视频"
                else if (video == 0)
                    changeVideo.text = "录制视频"
            }
            2, 3 -> {
                changeVerify.isVisible = false
                changeVideo.text = "请先认证"
            }

        }

        when (contact) {
            0 -> {
                changeContact.text = "立即绑定"
            }
            else -> {
                changeContact.text = "变更"
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

        powerContact.setCompoundDrawablesWithIntrinsicBounds(
            null, resources.getDrawable(
                if (contact != 0) {
                    R.drawable.icon_female_contact_open
                } else {
                    R.drawable.icon_female_contact_no
                }
            ), null, null
        )

        powerVerify.setCompoundDrawablesWithIntrinsicBounds(
            null, resources.getDrawable(
                if (verify != 1) {
                    R.drawable.icon_female_verify_no
                } else {
                    R.drawable.icon_female_verify_open
                }
            ), null, null
        )
        powerVideo.setCompoundDrawablesWithIntrinsicBounds(
            null, resources.getDrawable(
                if (video != 1) {
                    R.drawable.icon_female_video_no
                } else {
                    R.drawable.icon_female_video_open
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
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTopCardEvent(event: FemaleVideoEvent) {
        video = event.videoState
        powerVideo.setCompoundDrawablesWithIntrinsicBounds(
            null, resources.getDrawable(
                if (event.videoState == 1) {
                    R.drawable.icon_female_video_open
                } else {
                    R.drawable.icon_female_video_no
                }
            ), null, null
        )
    }

    override fun onLazyClick(v: View) {
        when (v.id) {
            R.id.powerContact, R.id.changeContact -> { //联系方式
                startActivityForResult<ChangeUserContactActivity>(REQUEST_ACCOUNT)
            }
            R.id.powerVerify, R.id.changeVerify -> { //真人认证
                when (verify) {
                    1 -> {
                        CommonFunction.toast("您已通过认证")
                    }
                    2, 3 -> {
                        CommonFunction.toast("认证审核中...")
                    }
                    else -> {
                        CommonFunction.startToFace(this, requestCode = REQUEST_VERIFY)
                    }
                }
            }
            R.id.powerVideo, R.id.changeVideo -> { //视频介绍
                //todo 还原视频介绍代码
//                CommonFunction.startToVideoIntroduce(this)
                if (verify == 1) {
                    if (video == 0) {
                        CommonFunction.startToVideoIntroduce(this)
                    } else if (video == 2) {
                        CommonFunction.toast("视频正在审核中，请耐心等待")
                    } else {
                        CommonFunction.startToVideoIntroduce(this)
//                    CommonFunction.toast("您已经通过视频介绍")
                    }
                } else {
                    CommonFunction.toast("请先完成真人认证再录制视频介绍")
                }
            }
        }


    }

}
