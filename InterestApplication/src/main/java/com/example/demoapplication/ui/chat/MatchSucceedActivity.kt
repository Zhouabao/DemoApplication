package com.example.demoapplication.ui.chat

import android.os.Bundle
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.KeyboardUtils
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.example.baselibrary.glide.GlideUtil
import com.example.demoapplication.R
import com.kotlin.base.ext.onClick
import kotlinx.android.synthetic.main.activity_match_succeed.*

/**
 * 配对成功进入聊天界面
 */
class MatchSucceedActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_match_succeed)
        initView()
        initData()
    }

    private fun initData() {
        GlideUtil.loadImg(this, R.drawable.img_avatar_04, iconMine)
        GlideUtil.loadImg(this, R.drawable.img_avatar_05, iconOther)

    }

    private fun initView() {
        btnBack.onClick { finish() }
        KeyboardUtils.showSoftInput(et)


        YoYo.with(Techniques.ZoomIn)
            .duration(2000)
            .playOn(iconMine)

        YoYo.with(Techniques.ZoomIn)
            .duration(2000)
            .playOn(iconOther)


        val animation = AnimationUtils.loadAnimation(this, R.anim.anim_rotate)
        animation.interpolator = LinearInterpolator()
        iconMine.startAnimation(animation)

        val anima1 = AnimationUtils.loadAnimation(this, R.anim.anim_rotate_reverse)
        anima1.interpolator = LinearInterpolator()
        iconOther.startAnimation(anima1)


        YoYo.with(Techniques.RubberBand)
            .delay(1000)
            .duration(2000)
            .playOn(iconLike)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

}
