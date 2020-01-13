package com.sdy.jitangapplication.ui.chat

import android.animation.Animator
import android.animation.ObjectAnimator
import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.SizeUtils
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseActivity
import com.netease.nim.uikit.business.session.module.Container
import com.netease.nim.uikit.business.session.module.ModuleProxy
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.msg.MessageBuilder
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import kotlinx.android.synthetic.main.activity_match_succeed.*

/**
 * 配对成功进入聊天界面
 */
class MatchSucceedActivity : BaseActivity(), View.OnClickListener, ModuleProxy {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_match_succeed)
        initView()
        initData()
    }

    private fun initData() {
        GlideUtil.loadRoundImgCenterCrop(this, avator, iconOther, SizeUtils.dp2px(10F))

        matchTip.text = "你与$nickname\n于千万人中彼此欣赏，终于相遇！"
    }

    private lateinit var accid: String
    private lateinit var avator: String
    private lateinit var nickname: String

    private fun initView() {
        setSwipeBackEnable(true)
//        accid = ""
//        avator = UserManager.getAvator()
//        nickname = "nickname"

        accid = intent.getStringExtra("accid") ?: ""
        avator = intent.getStringExtra("avator") ?: ""
        nickname = intent.getStringExtra("nickname") ?: ""

        btnBack.onClick { onBackPressed() }

//        KeyboardUtils.registerSoftInputChangedListener(this) {
//            Log.d("registerSoftInputChangedListener", "$it")
//            val ani = ObjectAnimator.ofFloat(clMsg, "translationY", -it.toFloat())
//            ani.duration = 50L
//            ani.start()
//        }


        val objAnim = ObjectAnimator.ofFloat(ivSmallLove, "rotation", 0.0f, 360.0f)
        //设定动画的旋转周期
        objAnim.duration = 5 * 1000L
        //设置动画的插值器，这个为匀速旋转
        objAnim.interpolator = LinearInterpolator()
        //设置动画为无限重复
        objAnim.repeatCount = 0
        objAnim.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {

            }

            override fun onAnimationEnd(animation: Animator?) {
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator) {
                ivSmallLove.postDelayed({
                    animation.pause()
//                    animation.cancel()
                }, 2000L)
            }
        })
        objAnim.start()


        val objAnimMiddle = ObjectAnimator.ofFloat(ivMiddleLove, "rotation", 0.0f, -360.0f)
        //设定动画的旋转周期
        objAnimMiddle.duration = objAnim.duration
        //设置动画的插值器，这个为匀速旋转
        objAnimMiddle.interpolator = objAnim.interpolator
        //设置动画为无限重复
        objAnimMiddle.repeatCount = objAnim.repeatCount
        objAnimMiddle.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {

            }

            override fun onAnimationEnd(animation: Animator?) {
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator) {
                ivMiddleLove.postDelayed({
                    animation.pause()
//                    animation.cancel()
                }, 2000L)
            }

        })
        objAnimMiddle.start()


        val objAnimBig = ObjectAnimator.ofFloat(ivBigLove, "rotation", 0.0f, 360.0f)
        //设定动画的旋转周期
        objAnimBig.duration = objAnim.duration
        //设置动画的插值器，这个为匀速旋转
        objAnimBig.interpolator = objAnim.interpolator
        //设置动画为无限重复
        objAnimBig.repeatCount = objAnim.repeatCount
        objAnimBig.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {

            }

            override fun onAnimationEnd(animation: Animator?) {
                Log.d("objAnimBig", "onAnimationEnd")
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator) {
                ivBigLove.postDelayed({
                    animation.pause()
//                    animation.cancel()
                }, 2000L)
            }

        })
        //设置动画的插值器，这个为匀速旋转
        objAnimBig.start()

        //主动弹起键盘
//        etMsg.postDelayed({
//            KeyboardUtils.showSoftInput(etMsg)
//        }, 200L)

        //编辑框
        etMsg.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                sendMsg.isEnabled = !p0.isNullOrEmpty()
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

        })
        //发送消息
        sendMsg.setOnClickListener(this)

    }


    override fun onBackPressed() {
        if (KeyboardUtils.isSoftInputVisible(this)) {
            KeyboardUtils.hideSoftInput(this)
        } else {
            setResult(Activity.RESULT_OK)
            super.onBackPressed()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        if (KeyboardUtils.isSoftInputVisible(this))
            KeyboardUtils.hideSoftInput(this)
    }

    override fun scrollToFinishActivity() {
        if (KeyboardUtils.isSoftInputVisible(this))
            KeyboardUtils.hideSoftInput(this)
        super.scrollToFinishActivity()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.sendMsg -> {
                sendMsg.isEnabled = false
                sendMatchHiMessage()

            }
        }
    }


    /*--------------------------消息代理------------------------*/
    //发送匹配成功第一次对话
    private fun sendMatchHiMessage() {
        val container = Container(this, accid, SessionTypeEnum.P2P, this, true)
        val message = MessageBuilder.createTextMessage(accid, SessionTypeEnum.P2P, etMsg.text.toString())
        container.proxy.sendMessage(message)
    }


    override fun sendMessage(msg: IMMessage): Boolean {
        NIMClient.getService(MsgService::class.java).sendMessage(msg, false).setCallback(object :
            RequestCallback<Void?> {
            override fun onSuccess(param: Void?) {
                CommonFunction.toast("发送成功！")
                finish()
            }

            override fun onFailed(code: Int) {
                CommonFunction.toast("发送失败！")
                sendMsg.isEnabled = true
            }

            override fun onException(exception: Throwable) {
                CommonFunction.toast("发送失败！")
                sendMsg.isEnabled = true
            }
        })
        return true
    }

    override fun onInputPanelExpand() {

    }

    override fun shouldCollapseInputPanel() {

    }

    override fun isLongClickEnabled(): Boolean {
        return false
    }

    override fun onItemFooterClick(message: IMMessage?) {

    }

}
