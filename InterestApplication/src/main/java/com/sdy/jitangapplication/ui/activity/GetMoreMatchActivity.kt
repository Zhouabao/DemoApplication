package com.sdy.jitangapplication.ui.activity

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.util.Log
import android.view.View
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.common.OnLazyClickListener
import com.sdy.jitangapplication.model.MoreMatchBean
import com.sdy.jitangapplication.presenter.GetMoreMatchPresenter
import com.sdy.jitangapplication.presenter.view.GetMoreMatchView
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.activity_get_more_match.*
import org.jetbrains.anko.startActivity

/**
 * 获取更多精准匹配
 */
class GetMoreMatchActivity : BaseMvpActivity<GetMoreMatchPresenter>(), GetMoreMatchView,
    OnLazyClickListener {

    private val moreMatch by lazy { intent.getSerializableExtra("morematchbean") as MoreMatchBean? }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_more_match)

        initView()

    }

    private fun initView() {
        mPresenter = GetMoreMatchPresenter()
        mPresenter.context = this
        mPresenter.mView = this

        setSwipeBackEnable(false)

        if (UserManager.getGender() == 1) {
            lottieMoreMatch.setAnimation("data_boy_more_match.json")
        } else {
            lottieMoreMatch.setAnimation("data_girl_more_match.json")
        }


        if (moreMatch != null) {
            t2.text = getString(
                R.string.inapp,
                if (UserManager.getGender() == 1) {
                    getString(R.string.better_woman)
                } else {
                    getString(R.string.better_man)
                },
                moreMatch!!.city_name,
                moreMatch!!.people_amount,
                if (UserManager.getGender() == 1) {
                    getString(R.string.attractive_women)
                } else {
                    getString(R.string.elite_men)
                }
            )
        }

        nextStep.setOnClickListener(this)


        Log.d("getAvator()", UserManager.getAvator())
        if (UserManager.getAvator().isNullOrEmpty() || UserManager.getAvator()
                .contains(Constants.DEFAULT_AVATAR)
        )
            GlideUtil.loadCircleImg(this, R.drawable.icon_logo_orange_circle, myAvator)
        else
            GlideUtil.loadCircleImg(this, UserManager.getAvator(), myAvator)


        val scaleAnimationX = ObjectAnimator.ofFloat(myAvator, "scaleX", 0F, 1F)
        val scaleAnimationY = ObjectAnimator.ofFloat(myAvator, "scaleY", 0F, 1F)
        val alphaAnimation = ObjectAnimator.ofFloat(myAvator, "alpha", 0F, 1F)
        val animationSet = AnimatorSet()
        animationSet.duration = 500
        animationSet.playTogether(scaleAnimationX, scaleAnimationY, alphaAnimation)
        animationSet.start()

        animationSet.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {

            }

            override fun onAnimationEnd(animation: Animator?) {
                lottieMoreMatch.playAnimation()
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
            }

        })


        lottieMoreMatch.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {
                lottieMoreMatchRipple.playAnimation()
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
//                lottieMoreMatchRipple.playAnimation()

            }

        })
    }

    override fun onLazyClick(v: View) {
        when (v.id) {
            R.id.nextStep -> {
                startActivity<GetRelationshipActivity>(
                    "morematchbean" to moreMatch
                )
                finish()
            }
        }
    }

    override fun onBackPressed() {

    }



    override fun scrollToFinishActivity() {

    }
}
