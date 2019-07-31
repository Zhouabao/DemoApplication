package com.example.demoapplication.ui.activity

import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.ToastUtils
import com.example.demoapplication.R
import kotlinx.android.synthetic.main.activity_settings.*

/**
 * 系统设置
 */
class SettingsActivity : AppCompatActivity(), CompoundButton.OnCheckedChangeListener, View.OnClickListener {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        initView()
    }

    private fun initView() {
        switchContacts.setOnCheckedChangeListener(this)
        switchDistance.setOnCheckedChangeListener(this)

        blackListBtn.setOnClickListener(this)
        msgNotificate.setOnClickListener(this)
        feedBack.setOnClickListener(this)
        helpCenter.setOnClickListener(this)
        aboutUs.setOnClickListener(this)
        clearData.setOnClickListener(this)
        loginOutBtn.setOnClickListener(this)
        btnBack.setOnClickListener(this)


    }


    override fun onCheckedChanged(button: CompoundButton, check: Boolean) {
        when (button.id) {
            //屏蔽通讯录
            R.id.switchContacts -> {
                if (check) {
                    ToastUtils.showShort("已屏蔽")
                } else {
                    ToastUtils.showShort("已取消屏蔽")
                }
            }
            //隐藏距离
            R.id.switchDistance -> {
                if (check) {
                    ToastUtils.showShort("已隐藏距离")
                } else {
                    ToastUtils.showShort("已取消隐藏距离")
                }
            }
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            //黑名单
            R.id.blackListBtn -> {
            }
            //消息提醒
            R.id.msgNotificate -> {
            }
            //意见反馈
            R.id.feedBack -> {
            }
            //进入帮助
            R.id.helpCenter -> {
            }
            //关于
            R.id.aboutUs -> {
            }
            //清理缓存
            R.id.clearData -> {
            }
            //退出登录
            R.id.loginOutBtn -> {
            }
            //返回
            R.id.btnBack -> {
                finish()
            }
        }
    }

}
