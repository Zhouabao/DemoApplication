package com.sdy.jitangapplication.ui.activity

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import com.bigkoo.pickerview.builder.OptionsPickerBuilder
import com.bigkoo.pickerview.listener.OnOptionsSelectListener
import com.blankj.utilcode.constant.PermissionConstants
import com.blankj.utilcode.util.PermissionUtils
import com.blankj.utilcode.util.SPUtils
import com.kennyc.view.MultiStateView
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.auth.AuthService
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.common.OnLazyClickListener
import com.sdy.jitangapplication.event.UpdateSettingEvent
import com.sdy.jitangapplication.model.SettingsBean
import com.sdy.jitangapplication.model.StateBean
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
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.startActivity

/**
 * 系统设置
 */
class SettingsActivity : BaseMvpActivity<SettingsPresenter>(),
    OnLazyClickListener, SettingsView {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        initView()
        mPresenter.mySettings()
        mPresenter.getVersion()

        initData()
    }


    private fun initData() {
        if (UserManager.isShowSettingNew()) {
            hideModeNew.isVisible = false
            privacyNew.isVisible = false
            notificationNew.isVisible = false
        } else {
            hideModeNew.isVisible = true
            privacyNew.isVisible = true
            notificationNew.isVisible = true
        }

        if (UserManager.getGender() == 2) {
            privacyPowerBtn.isVisible = true
            privacyPowerContent.isVisible = true
        } else {
            privacyPowerBtn.isVisible = false
            privacyPowerContent.isVisible = false
            privacyNew.isVisible = false
        }


        cacheDataSize.text = DataCleanManager.getTotalCacheSize(this)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        UserManager.saveShowSettingNew(true)
    }

    private fun initView() {
        EventBus.getDefault().register(this)
        mPresenter = SettingsPresenter()
        mPresenter.mView = this
        mPresenter.context = this

        blackListBtn.setOnClickListener(this)
        seeBlackListBtn.setOnClickListener(this)
        msgNotificateCl.setOnClickListener(this)
        helpCenter.setOnClickListener(this)
        aboutUs.setOnClickListener(this)
        clearData.setOnClickListener(this)
        loginOutBtn.setOnClickListener(this)
        btnBack.setOnClickListener(this)
        filterContacts.setOnClickListener(this)
        filterDistance.setOnClickListener(this)

        hideModeCl.setOnClickListener(this)
        privacyPowerCl.setOnClickListener(this)


        aboutAccount.setOnClickListener(this)
        hotT1.text = getString(R.string.setting)
        stateSettings.retryBtn.onClick {
            stateSettings.viewState = MultiStateView.VIEW_STATE_LOADING
            mPresenter.mySettings()
        }

    }

    private val dialog by lazy {
        CommonAlertDialog.Builder(this)
            .setTitle(getString(R.string.login_out))
            .setContent(getString(R.string.is_confirm_login_out))
            .setOnCancelListener(object : CommonAlertDialog.OnCancelListener {
                override fun onClick(dialog: Dialog) {
                    dialog.cancel()
                }

            })
            .setOnConfirmListener(object : CommonAlertDialog.OnConfirmListener {
                override fun onClick(dialog: Dialog) {
                    NIMClient.getService(AuthService::class.java).logout()
                    UserManager.startToLogin(this@SettingsActivity)
                    dialog.cancel()
                }

            })
            .create()
    }

    override fun onLazyClick(view: View) {
        when (view.id) {
            //黑名单
            R.id.blackListBtn, R.id.seeBlackListBtn -> {
                startActivity<BlackListActivity>()
            }
            //消息提醒
            R.id.msgNotificateCl -> {
                // notify_square_like_state  notify_square_comment_state
                startActivity<NotificationActivity>()
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
                CommonFunction.toast(getString(R.string.clear_cache_success))

            }
            //退出登录，同时退出IM和服务器
            R.id.loginOutBtn -> {
                if (!dialog.isShowing)
                    dialog.show()
            }
            //返回
            R.id.btnBack -> {
                onBackPressed()
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
                    if (!PermissionUtils.isGranted(
                            *PermissionConstants.getPermissions(
                                PermissionConstants.CONTACTS
                            )
                        )
                    ) {
                        PermissionUtils.permission(PermissionConstants.CONTACTS)
                            .callback(object : PermissionUtils.SimpleCallback {
                                override fun onGranted() {
                                    obtainContacts()
                                }

                                override fun onDenied() {
                                    CommonFunction.toast(getString(R.string.permission_contact))
                                }

                            })
                            .request()
                    }

                }
            }

            R.id.hideModeCl -> {
                showHideModePicker(
                    hideModeContent,
                    invisible_state,
                    hideMode,
                    getString(R.string.hide_mode),
                    2
                )
            }
            R.id.privacyPowerCl -> {
                showHideModePicker(
                    privacyPowerContent,
                    private_chat_state,
                    privacyPowers,
                    getString(R.string.privacy_chat),
                    3
                )
            }

        }
    }


    /**
     * 展示条件选择器
     */
    private var hideMode: MutableList<StateBean> = mutableListOf()
    private var privacyPowers: MutableList<StateBean> = mutableListOf()

    //type 1隐身模式  2私聊权限
    private fun showHideModePicker(
        textView: TextView,
        checkedState: StateBean,
        states: MutableList<StateBean>,
        title: String,
        type: Int
    ) {
        //条件选择器
        val pvOptions = OptionsPickerBuilder(this,
            OnOptionsSelectListener { options1, options2, options3, v ->
                textView.text = states[options1].title
                mPresenter.switchSet(type, states[options1].id)
            })
            .setSubmitText(getString(R.string.ok))
            .setTitleText(title)
            .setTitleColor(Color.parseColor("#191919"))
            .setTitleSize(16)
            .setDividerColor(resources.getColor(R.color.colorDivider))
            .setContentTextSize(20)
            .setDecorView(window.decorView.findViewById(android.R.id.content) as ViewGroup)
            .setSubmitColor(resources.getColor(R.color.colorBlueSky1))
            .build<StateBean>()

        pvOptions.setPicker(states)
        pvOptions.setSelectOptions(states.indexOf(checkedState))
        pvOptions.show()
    }


    private fun obtainContacts() {
        //权限申请成功
        val contacts = UriUtils.getPhoneContacts(this)
        if (contacts.isNullOrEmpty()) {
            CommonFunction.toast(getString(R.string.empty_contact1))
            return
        }
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
    private var invisible_state = StateBean()//1 不隐身 2 离线时间隐身 3 一直隐身
    private var private_chat_state = StateBean()//1 所有用户 2针对黄金会员

    override fun onSettingsBeanResult(success: Boolean, settingsBean: SettingsBean?) {
        if (success) {
            this.settingsBean = settingsBean
            SPUtils.getInstance(Constants.SPNAME)
                .put("switchDianzan", settingsBean?.notify_square_like_state ?: true)
            SPUtils.getInstance(Constants.SPNAME)
                .put("switchComment", settingsBean?.notify_square_comment_state ?: true)


            stateSettings.viewState = MultiStateView.VIEW_STATE_CONTENT
            switchDistance.isChecked = settingsBean!!.hide_distance
            switchContacts.isChecked = settingsBean!!.hide_book
            invisible_state = settingsBean!!.invisible_state
            private_chat_state = settingsBean!!.private_chat_state
            hideMode = settingsBean!!.invisible_list
            privacyPowers = settingsBean!!.private_chat_list
            hideModeContent.text = invisible_state.title
            privacyPowerContent.text = private_chat_state.title
        } else {
            stateSettings.viewState = MultiStateView.VIEW_STATE_ERROR
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateSettingEvent(event: UpdateSettingEvent) {
        mPresenter.mySettings()
    }
}
