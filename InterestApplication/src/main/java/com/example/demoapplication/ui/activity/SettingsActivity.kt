package com.example.demoapplication.ui.activity

import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.CompoundButton
import com.blankj.utilcode.util.*
import com.example.demoapplication.R
import com.example.demoapplication.presenter.SetInfoPresenter
import com.example.demoapplication.utils.UserManager
import com.kotlin.base.common.AppManager
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.auth.AuthService
import kotlinx.android.synthetic.main.activity_settings.*
import org.jetbrains.anko.startActivity
import java.io.File

/**
 * 系统设置
 */
class SettingsActivity : BaseMvpActivity<SetInfoPresenter>(), CompoundButton.OnCheckedChangeListener,
    View.OnClickListener {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        initView()
        initData()
    }

    private fun initData() {
        val totalCache =
            CacheDiskUtils.getInstance().cacheSize + CacheDoubleUtils.getInstance().cacheDiskSize + CacheMemoryUtils.getInstance().cacheCount
        cacheDataSize.text = "${totalCache / 1024 / 1024}M"
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
                CleanUtils.cleanInternalCache()//清除内部文件
                CleanUtils.cleanExternalCache()//外部缓存
                CleanUtils.cleanCustomDir(
                    File(
                        Environment.getExternalStorageDirectory().absolutePath.plus(File.separator).plus(
                            "demoapplicaiton"
                        )
                    )
                )
                val totalCache =
                    CacheDiskUtils.getInstance().cacheSize + CacheDoubleUtils.getInstance().cacheDiskSize + CacheMemoryUtils.getInstance().cacheCount
                cacheDataSize.text = "${totalCache / 1024 / 1024}M"
                ToastUtils.showShort("缓存清理成功")

            }
            //退出登录，同时退出IM和服务器
            R.id.loginOutBtn -> {
                UserManager.clearLoginData()
                NIMClient.getService(AuthService::class.java).logout()
                AppManager.instance.finishAllActivity()
                startActivity<WelcomeActivity>()
            }
            //返回
            R.id.btnBack -> {
                finish()
            }
        }
    }

}
