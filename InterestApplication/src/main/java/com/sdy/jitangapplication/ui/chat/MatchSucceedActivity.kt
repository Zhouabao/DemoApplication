package com.sdy.jitangapplication.ui.chat

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.animation.LinearInterpolator
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.ToastUtils
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.MatchBean
import com.sdy.jitangapplication.utils.UserManager
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
import kotlinx.android.synthetic.main.activity_match_succeed.*

/**
 * 配对成功进入聊天界面
 */
class MatchSucceedActivity : BaseActivity(), View.OnClickListener, ModuleProxy {

    private val matchBean by lazy { intent.getSerializableExtra("matchBean") as MatchBean }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_match_succeed)
        initView()
        initData()
    }

    private fun initData() {
        GlideUtil.loadRoundImgCenterCrop(this, matchBean.avatar ?: "", iconOther, SizeUtils.dp2px(10F))
        GlideUtil.loadRoundImgCenterCrop(this, UserManager.getAvator(), iconMine, SizeUtils.dp2px(10F))

        matchTip.text = "你和 ${matchBean.nickname} 都彼此欣赏 \n不如就说先点什么吧"
    }

    private fun initView() {
        btnBack.onClick { onBackPressed() }

        //主动弹起键盘
        etMsg.postDelayed({
            KeyboardUtils.showSoftInput(etMsg)
        }, 200L)

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

        //我的头像动画集合
        val animator = AnimatorSet()
        animator.duration = 2000L
        animator.interpolator = LinearInterpolator()
        animator.playTogether(
            ObjectAnimator.ofFloat(iconMine, "scaleX", 0.45f, 1f),
            ObjectAnimator.ofFloat(iconMine, "scaleY", 0.45f, 1f),
            ObjectAnimator.ofFloat(iconMine, "alpha", 0f, 1f),
            ObjectAnimator.ofFloat(iconMine, "rotation", 0F, -22.5F)
        )
        animator.start()

        //对方的头像动画集合
        val animator1 = AnimatorSet()
        animator1.duration = 2000L
        animator1.interpolator = LinearInterpolator()
        animator1.playTogether(
            ObjectAnimator.ofFloat(iconOther, "scaleX", 0.45f, 1f),
            ObjectAnimator.ofFloat(iconOther, "scaleY", 0.45f, 1f),
            ObjectAnimator.ofFloat(iconOther, "alpha", 0f, 1f),
            ObjectAnimator.ofFloat(iconOther, "rotation", 0F, 22.5F)
        )
        animator1.start()

        //笑脸动画
        YoYo.with(Techniques.RubberBand)
            .duration(2000)
            .playOn(iconLike)
    }




    override fun onBackPressed() {
        KeyboardUtils.hideSoftInput(etMsg)
        super.onBackPressed()
    }


    override fun onDestroy() {
        super.onDestroy()
        KeyboardUtils.hideSoftInput(etMsg)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.sendMsg -> {
                sendMatchHiMessage()
            }
        }
    }


    /*--------------------------消息代理------------------------*/
    //发送匹配成功第一次对话
    private fun sendMatchHiMessage() {
        val container = Container(this, matchBean.accid, SessionTypeEnum.P2P, this, true)
        val message = MessageBuilder.createTextMessage(
            matchBean.accid,
            SessionTypeEnum.P2P,
            etMsg.text.toString()
        )
        container.proxy.sendMessage(message)
    }


    override fun sendMessage(msg: IMMessage): Boolean {
        NIMClient.getService(MsgService::class.java).sendMessage(msg, false).setCallback(object :
            RequestCallback<Void?> {
            override fun onSuccess(param: Void?) {
                ToastUtils.showShort("发送成功！")
                finish()
            }

            override fun onFailed(code: Int) {
                ToastUtils.showShort("发送失败！")
            }

            override fun onException(exception: Throwable) {
                ToastUtils.showShort("发送失败！")
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
