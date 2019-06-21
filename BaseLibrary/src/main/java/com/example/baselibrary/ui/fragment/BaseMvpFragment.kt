package com.kotlin.base.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.presenter.view.BaseView
import org.jetbrains.anko.support.v4.toast

/*
    Fragment基类，业务相关
 */
abstract open class BaseMvpFragment<T : BasePresenter<*>> : BaseFragment(), BaseView {


    lateinit var mPresenter: T


//    private lateinit var mLoadingDialog:ProgressLoading

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        //初始加载框
        TODO("初始化加载框")
        //mLoadingDialog = ProgressLoading.create(context!!)
        return super.onCreateView(inflater, container, savedInstanceState)
    }




    /*
       显示加载框，默认实现
    */
    override fun showLoading() {
        //mLoadingDialog.showLoading()
        toast("展示加载框")
    }

    /*
        隐藏加载框，默认实现
     */
    override fun hideLoading() {
        //mLoadingDialog.hideLoading()
        toast("隐藏加载框")
    }

    /*
        错误信息提示，默认实现
     */
    override fun onError(text:String) {
        toast(text)
    }
}
