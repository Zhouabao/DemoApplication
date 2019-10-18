package com.kotlin.base.ui.activity

import android.os.Bundle
import android.view.Gravity
import com.blankj.utilcode.util.ToastUtils
import com.kotlin.base.ext.toast
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.presenter.view.BaseView


/*
    Activity基类，业务相关
 */
abstract open class BaseMvpActivity<T : BasePresenter<*>> : BaseActivity(), BaseView {

    //Presenter泛型，Dagger注入
    lateinit var mPresenter: T


    //todo
    //  private lateinit var mLoadingDialog:ProgressLoading

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        //初始加载框
        // mLoadingDialog = ProgressLoading.create(this)
    }


    /*
        显示加载框，默认实现
     */
    override fun showLoading() {
        //   mLoadingDialog.showLoading()
    }

    /*
        隐藏加载框，默认实现
     */
    override fun hideLoading() {
        //   mLoadingDialog.hideLoading()
    }

    /*
        错误信息提示，默认实现
     */
    override fun onError(text: String) {
        // SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE).setContentText(text).show()
        ToastUtils.setGravity(Gravity.CENTER, 0, 0)
        toast(text)
    }
}
