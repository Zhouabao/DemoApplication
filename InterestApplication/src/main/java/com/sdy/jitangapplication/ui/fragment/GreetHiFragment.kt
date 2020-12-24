package com.sdy.jitangapplication.ui.fragment

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.widget.FrameLayout
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.VibrateUtils
import com.kotlin.base.ui.fragment.BaseFragment
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.model.CustomerMsgBean
import com.sdy.jitangapplication.ui.dialog.VisitorsPayChatDialog
import com.sdy.jitangapplication.utils.UserManager
import com.sdy.jitangapplication.widgets.GreetHiView
import kotlinx.android.synthetic.main.fragment_greet_hi.*

/**
 * 顶级通知fragment
 */
class GreetHiFragment(val msgBean: CustomerMsgBean) : BaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_greet_hi, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initNewMessages(greetHiView, msgBean)
    }

    private fun initNewMessages(view1: GreetHiView, customerMsgBean: CustomerMsgBean) {

        val params = contentView.layoutParams as FrameLayout.LayoutParams
        params.gravity = Gravity.BOTTOM

        val leftMargin =
            (SizeUtils.dp2px(15f)..ScreenUtils.getScreenWidth() - SizeUtils.dp2px(15F)).random()
        params.leftMargin = leftMargin
        params.bottomMargin = (SizeUtils.dp2px(166F)..SizeUtils.dp2px(270F)).random()
        contentView.layoutParams = params

        if (leftMargin > ScreenUtils.getScreenWidth() / 2) {
            view1.setDirection(GreetHiView.DIRECTION_LEFT)
        } else {
            view1.setDirection(GreetHiView.DIRECTION_RIGHT)
        }

        view1.loadImg(customerMsgBean.avatar, customerMsgBean.accid)
        view1.isVisible = true
        view1.clickWithTrigger {
            if (UserManager.isUserFoot()) {
                //男性解锁聊天
                CommonFunction.checkChat(requireContext(), customerMsgBean.accid)
            } else {
                VisitorsPayChatDialog(requireActivity()).show()
            }

        }
        val animatorSet = AnimatorSet().apply {
            val scaleX = ObjectAnimator.ofFloat(view1, "scaleX", 0.8F, 1f, 0.8F)
            scaleX.repeatCount = 5
            scaleX.duration = 1000
            val scaleY = ObjectAnimator.ofFloat(view1, "scaleY", 0.8F, 1f, 0.8F)
            scaleY.repeatCount = 5
            scaleY.duration = 1000
            interpolator = AccelerateInterpolator()
            playTogether(scaleX, scaleY)
        }
        animatorSet.start()

        animatorSet.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {

            }

            override fun onAnimationEnd(animation: Animator?) {
                view1.isInvisible = true
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
                VibrateUtils.vibrate(1000L)
            }

        })
    }


    override fun onDestroy() {
        super.onDestroy()
//        EventBus.getDefault().unregister(this)
    }


}
