package com.sdy.jitangapplication.ui.fragment

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.blankj.utilcode.util.FragmentUtils
import com.blankj.utilcode.util.SizeUtils
import com.kotlin.base.ui.fragment.BaseFragment
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.baselibrary.utils.StatusBarUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.model.CustomerMsgBean
import com.sdy.jitangapplication.nim.activity.ChatActivity
import kotlinx.android.synthetic.main.fragment_snack_bar.*

/**
 * 顶级通知fragment
 */
class SnackBarFragment(val msgBean: CustomerMsgBean) : BaseFragment() {

    companion object {
        const val SOMEONE_LIKE_YOU = 31//喜欢了你
        const val SOMEONE_MATCH_SUCCESS = 32//匹配成功
        const val GREET_SUCCESS = 41//招呼
        const val FLASH_SUCCESS = 42//闪聊
        const val CHAT_SUCCESS = 43//发消息
        const val HELP_CANDY = 51//助力
        const val GIVE_GIFT = 52//赠送礼物
        const val SEND_FAILED = 53//发送消息失败
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_snack_bar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private val colors by lazy {
        intArrayOf(
            Color.parseColor("#FF6318"),
            Color.parseColor("#7CBAFD"),
            Color.parseColor("#2BD683"),
            Color.parseColor("#FFFF7736")
        )
    }

    private fun initView() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            StatusBarUtil.setMargin(activity!!, contentView)
        }

//        contentView
//        EventBus.getDefault().register(this)
        when (msgBean.type) {
            SOMEONE_LIKE_YOU, SOMEONE_MATCH_SUCCESS -> {
                contentView.setCardBackgroundColor(colors[0])
            }
            FLASH_SUCCESS,
            CHAT_SUCCESS -> {
                contentView.setCardBackgroundColor(colors[1])
            }
            HELP_CANDY,
            GIVE_GIFT -> {
                contentView.setCardBackgroundColor(colors[2])
            }
            SEND_FAILED -> {
                contentView.setCardBackgroundColor(colors[3])
            }
        }

        contentView.clickWithTrigger {
            contentView.isVisible = false
            FragmentUtils.remove(this@SnackBarFragment)
        }
        contentView.postDelayed({
            showAnimation(250L)
        }, 3000L)

        GlideUtil.loadCircleImg(activity!!, msgBean.avatar, matchIcon)
        matchName.text = msgBean.title
        moreInfoTitle.text = msgBean.content

        contentView.clickWithTrigger {
            when (msgBean.type) {
                //喜欢了你
//                SOMEONE_LIKE_YOU -> {
//                    if (ActivityUtils.getTopActivity() !is LikeMeReceivedActivity)
//                        startActivity<LikeMeReceivedActivity>()
//                }
                //助力
                HELP_CANDY,
                    //赠送礼物
                GIVE_GIFT,
                    //发消息
                CHAT_SUCCESS,
                    //匹配成功
                SOMEONE_MATCH_SUCCESS -> {
                    ChatActivity.start(activity!!, msgBean.accid ?: "")
                }
                //闪聊
                FLASH_SUCCESS -> {
                }
            }
        }


    }

    fun showAnimation(duration: Long) {
        val translationY =
            ObjectAnimator.ofFloat(contentView, "translationY", SizeUtils.dp2px(-68F).toFloat())
        val alphaInAnimation = ObjectAnimator.ofFloat(contentView, "alpha", 1f, 0f)
        val set = AnimatorSet()
        set.duration = duration
        set.playTogether(translationY, alphaInAnimation)
        set.start()
        set.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {
                FragmentUtils.remove(this@SnackBarFragment)
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {

            }

        })
    }


    override fun onDestroy() {
        super.onDestroy()
//        EventBus.getDefault().unregister(this)
    }


}
