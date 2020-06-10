package com.sdy.jitangapplication.ui.activity

import android.graphics.Color
import android.os.Bundle
import androidx.constraintlayout.widget.ConstraintLayout
import com.blankj.utilcode.util.ScreenUtils
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseActivity
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.baselibrary.utils.StatusBarUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.clickWithTrigger
import kotlinx.android.synthetic.main.activity_female_power.*
import kotlinx.android.synthetic.main.layout_actionbar.*
import org.jetbrains.anko.startActivity

/**
 * 女性个人权益
 */
//       "contact" to userInfoBean?.userinfo?.contact_way,
//                    "verify" to userInfoBean?.userinfo?.isfaced,
//                    "video" to userInfoBean?.userinfo?.mv_faced
class FemalePowerActivity : BaseActivity() {
    private val contact by lazy { intent.getIntExtra("contact", 0) }
    private val verify by lazy { intent.getIntExtra("verify", 0) }
    private val video by lazy { intent.getIntExtra("video", 0) }
    private val url by lazy { intent.getStringExtra("url") }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_female_power)
        initView()
    }

    private fun initView() {
        StatusBarUtil.immersive(this)
        llTitle.setBackgroundColor(Color.parseColor("#FFFFDCC1"))
        hotT1.text = "个人权益"
        hotT1.setTextColor(Color.WHITE)
        btnBack.setImageResource(R.drawable.icon_back_white)
        btnBack.onClick {
            finish()
        }

        //联系方式
        powerContact.clickWithTrigger {
            startActivity<ChangeUserContactActivity>()
        }

        //真人认证
        powerVerify.clickWithTrigger {
            CommonFunction.startToFace(this)
        }

        //视频介绍
        powerVideo.clickWithTrigger {
            if (video == 0)
                CommonFunction.startToVideoIntroduce(this)
        }


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
}
