package com.example.demoapplication.ui.chat

import android.os.Bundle
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.ScreenUtils
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
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
        ScreenUtils.setFullScreen(this)
        initView()

    }

    private fun initView() {
        btnBack.onClick { finish() }
    }

    override fun onResume() {
        super.onResume()

        val animation = AnimationUtils.loadAnimation(this, R.anim.anim_rotate)
        val lin = LinearInterpolator()
        animation.interpolator = lin
        iconMine.startAnimation(animation)

        val anima1 = AnimationUtils.loadAnimation(this, R.anim.anim_rotate_reverse)
        val lin1 = LinearInterpolator()
        anima1.interpolator = lin1
        iconOther.startAnimation(anima1)

        YoYo.with(Techniques.ZoomIn)
            .delay(1000)
            .duration(3000)
            .playOn(iconMine)

        YoYo.with(Techniques.ZoomIn)
            .delay(1000)
            .duration(3000)
            .playOn(iconOther)



        YoYo.with(Techniques.RubberBand)
            .delay(1000)
            .duration(3000)
            .playOn(iconLike)

    }
}
