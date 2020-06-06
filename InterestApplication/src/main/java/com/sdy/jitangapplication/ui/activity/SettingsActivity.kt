package com.sdy.jitangapplication.ui.activity

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.View
import com.blankj.utilcode.constant.PermissionConstants
import com.blankj.utilcode.util.PermissionUtils
import com.blankj.utilcode.util.SPUtils
import com.kennyc.view.MultiStateView
import com.kotlin.base.common.AppManager
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.auth.AuthService
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.model.SettingsBean
import com.sdy.jitangapplication.model.VersionBean
import com.sdy.jitangapplication.presenter.SettingsPresenter
import com.sdy.jitangapplication.presenter.view.SettingsView
import com.sdy.jitangapplication.utils.DataCleanManager
import com.sdy.jitangapplication.utils.UriUtils
import com.sdy.jitangapplication.utils.UserManager
import com.sdy.jitangapplication.widgets.CommonAlertDialog
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.error_layout.view.*
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
        mPresenter.mySettings(UserManager.getToken(), UserManager.getAccid())
        mPresenter.getVersion()

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
        helpCenter.setOnClickListener(this)
        aboutUs.setOnClickListener(this)
        clearData.setOnClickListener(this)
        loginOutBtn.setOnClickListener(this)
        btnBack.setOnClickListener(this)
        filterContacts.setOnClickListener(this)
        filterDistance.setOnClickListener(this)

        aboutAccount.setOnClickListener(this)
        hotT1.text = "设置"
        stateSettings.retryBtn.onClick {
            stateSettings.viewState = MultiStateView.VIEW_STATE_LOADING
            mPresenter.mySettings(UserManager.getToken(), UserManager.getAccid())
        }

    }

    override fun onClick(view: View) {
        when (view.id) {
            //黑名单
            R.id.blackListBtn -> {
                startActivity<BlackListActivity>()
            }
            //消息提醒
            R.id.msgNotificate -> {
                // notify_square_like_state  notify_square_comment_state
                startActivity<NotificationActivity>(
                    "notify_square_like_state" to settingsBean?.notify_square_like_state,
                    "notify_square_comment_state" to settingsBean?.notify_square_comment_state
                )
            }

            //进入帮助
            R.id.helpCenter -> {
            }
            //账号相关
            R.id.aboutAccount -> {
                startActivity<AccountAboutActivity>()
            }
            //关于
            R.id.aboutUs -> {
                startActivity<AboutActivity>()
            }
            //清理缓存
            R.id.clearData -> {
                DataCleanManager.clearAllCache(this)
                cacheDataSize.text = DataCleanManager.getTotalCacheSize(this)
                CommonFunction.toast("缓存清理成功")

            }
            //退出登录，同时退出IM和服务器
            R.id.loginOutBtn -> {
                CommonAlertDialog.Builder(this)
                    .setTitle("退出登录")
                    .setContent("是否退出登录？\n退出后用户信息将在上次登录位置对其他用户持续可见")
                    .setOnCancelListener(object : CommonAlertDialog.OnCancelListener {
                        override fun onClick(dialog: Dialog) {
                            dialog.cancel()
                        }

                    })
                    .setOnConfirmListener(object : CommonAlertDialog.OnConfirmListener {
                        override fun onClick(dialog: Dialog) {
                            NIMClient.getService(AuthService::class.java).logout()
                            UserManager.startToLogin(this@SettingsActivity)
                        }

                    })
                    .create()
                    .show()

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
                    // 请求接口看是否已经屏蔽过通讯录
                    if (!PermissionUtils.isGranted(PermissionConstants.CONTACTS)) {
                        PermissionUtils.permission(PermissionConstants.CONTACTS)
                            .callback(object : PermissionUtils.SimpleCallback {
                                override fun onGranted() {
                                    obtainContacts()
                                }

                                override fun onDenied() {
                                    CommonFunction.toast("您已拒绝获取联系人列表权限的开启！")
                                }

                            })
                            .request()
                    }

                }
            }

        }
    }


    private fun obtainContacts() {
        //权限申请成功
        val contacts = UriUtils.getPhoneContacts(this)
        val content = mutableListOf<String?>()
        for (contact in contacts.withIndex()) {
            content.add(contact.value.phone)
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


    override fun onGetVersionResult(versionBean: VersionBean?) {
        //newVersionTip.isVisible = versionBean != null && versionBean.version != AppUtils.getAppVersionName()
    }



    private var settingsBean: SettingsBean? = null

    override fun onSettingsBeanResult(success: Boolean, settingsBean: SettingsBean?) {
        if (success) {
            this.settingsBean = settingsBean
            SPUtils.getInstance(Constants.SPNAME).put("switchDianzan", settingsBean?.notify_square_like_state?:true)
            SPUtils.getInstance(Constants.SPNAME).put("switchComment", settingsBean?.notify_square_comment_state?:true)


            stateSettings.viewState = MultiStateView.VIEW_STATE_CONTENT
            switchDistance.isChecked = settingsBean!!.hide_distance
            switchContacts.isChecked = settingsBean!!.hide_book
        } else {
            stateSettings.viewState = MultiStateView.VIEW_STATE_ERROR
        }
    }
}
