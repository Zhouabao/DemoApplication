package com.example.demoapplication.ui.activity

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.blankj.utilcode.util.ToastUtils
import com.example.demoapplication.R
import com.example.demoapplication.nim.activity.ChatActivity
import com.example.demoapplication.presenter.SettingsPresenter
import com.example.demoapplication.presenter.view.SettingsView
import com.example.demoapplication.utils.DataCleanManager
import com.example.demoapplication.utils.UriUtils
import com.example.demoapplication.utils.UserManager
import com.kotlin.base.common.AppManager
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.auth.AuthService
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.layout_actionbar.*
import org.jetbrains.anko.startActivity

/**
 * 系统设置
 */
class SettingsActivity : BaseMvpActivity<SettingsPresenter>(),
    View.OnClickListener, SettingsView {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        initView()
        initData()
    }

    private fun initData() {
        cacheDataSize.text = DataCleanManager.getTotalCacheSize(this)
    }

    private fun initView() {
        mPresenter = SettingsPresenter()
        mPresenter.mView = this
        mPresenter.context = this


        blackListBtn.setOnClickListener(this)
        msgNotificate.setOnClickListener(this)
        feedBack.setOnClickListener(this)
        helpCenter.setOnClickListener(this)
        aboutUs.setOnClickListener(this)
        clearData.setOnClickListener(this)
        loginOutBtn.setOnClickListener(this)
        btnBack.setOnClickListener(this)
        filterContacts.setOnClickListener(this)
        filterDistance.setOnClickListener(this)
        hotT1.text = "设置"

        switchDistance.isChecked = intent.getBooleanExtra("hide_distance",false)
        switchContacts.isChecked = intent.getBooleanExtra("hide_book",false)

    }

    override fun onClick(view: View) {
        when (view.id) {
            //黑名单
            R.id.blackListBtn -> {
                startActivity<BlackListActivity>()
            }
            //消息提醒
            R.id.msgNotificate -> {
                startActivity<NotificationActivity>()
            }
            //意见反馈
            R.id.feedBack -> {
                ChatActivity.start(this, com.example.demoapplication.common.Constants.ASSISTANT_ACCID)
            }
            //进入帮助
            R.id.helpCenter -> {
            }
            //关于
            R.id.aboutUs -> {
                startActivity<AboutActivity>()
            }
            //清理缓存
            R.id.clearData -> {
                DataCleanManager.clearAllCache(this)
                cacheDataSize.text = DataCleanManager.getTotalCacheSize(this)
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
            //屏蔽距离
            R.id.filterDistance -> {
                mPresenter.isHideDistance(
                    UserManager.getAccid(),
                    UserManager.getToken(),
                    if (switchDistance.isChecked) {
                        0
                    } else {
                        1
                    }
                )
            }
            //屏蔽通讯录
            R.id.filterContacts -> {
                if (switchContacts.isChecked) {
                    mPresenter.blockedAddressBook(UserManager.getAccid(), UserManager.getToken())
                } else {
                    //TODO 请求接口看是否已经屏蔽过通讯录
                    if (ContextCompat.checkSelfPermission(
                            this,
                            android.Manifest.permission.READ_CONTACTS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        //申请权限
                        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_CONTACTS), 1)
                    } else {
                        obtainContacts()
                    }
                }
            }
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                obtainContacts()
            } else {
                ToastUtils.showShort("您已拒绝获取联系人列表权限的开启！")
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun obtainContacts() {
        //权限申请成功
        val contacts = UriUtils.getPhoneContacts(this)
        val content = arrayOfNulls<String>(contacts.size)
        for (contact in contacts.withIndex()) {
            content[contact.index] = contact.value.phone
            Log.d("contacts", "${contact.value.name}：${contact.value.phone}")
        }
        mPresenter.blockedAddressBook(UserManager.getAccid(), UserManager.getToken(), content)
    }


    //是否已经屏蔽过
    private var filter = false

    override fun onBlockedAddressBookResult(success: Boolean) {
        if (success) {
            switchContacts.isChecked = !switchContacts.isChecked
        }
    }

    override fun onHideDistanceResult(success: Boolean) {
        if (success) {
            switchDistance.isChecked = !switchDistance.isChecked
        }

    }
}
