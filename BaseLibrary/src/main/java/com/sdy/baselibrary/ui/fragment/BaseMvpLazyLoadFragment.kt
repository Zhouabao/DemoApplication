package com.kotlin.base.ui.fragment

import android.os.Bundle
import com.kotlin.base.ext.toast
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.presenter.view.BaseView

/*
    Fragment基类，业务相关
 */
abstract open class BaseMvpLazyLoadFragment<T : BasePresenter<*>> : BaseFragment(), BaseView {
    lateinit var mPresenter: T

    /*
       显示加载框，默认实现
    */
    override fun showLoading() {
        //mLoadingDialog.showLoading()
//        toast("展示加载框")
    }

    /*
        隐藏加载框，默认实现
     */
    override fun hideLoading() {
        //mLoadingDialog.hideLoading()
//        toast("隐藏加载框")
    }

    /*
        错误信息提示，默认实现
     */
    override fun onError(text: String) {
        toast(text)
    }


    private var isViewCreated: Boolean = false// 界面是否已创建完成
    private var isVisibleToUser: Boolean = false // 是否对用户可见
    private var isDataLoaded: Boolean = false // 数据是否已请求, isNeedReload()返回false的时起作用

    // 实现具体的数据请求逻辑
    protected abstract fun loadData()

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        this.isVisibleToUser = isVisibleToUser
        tryLoadData()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        isViewCreated = true
        tryLoadData()
    }

    /**
     * ViewPager场景下，判断父fragment是否可见
     *
     * @return
     */
    private fun isParentVisible(): Boolean {
        val fragment = parentFragment
        return fragment == null || (fragment is BaseMvpLazyLoadFragment<*> && fragment.isVisibleToUser)
    }

    /**
     * ViewPager场景下，当前fragment可见，如果其子fragment也可见，则尝试让子fragment请求数据
     */
    private fun dispatchParentVisibleState() {
        val fragmentManager = childFragmentManager
        val fragments = fragmentManager.fragments
        if (fragments.isEmpty()) {
            return
        }
        for (child in fragments) {
            if (child is BaseMvpLazyLoadFragment<*> && child.isVisibleToUser) {
                child.tryLoadData()
            }
        }
    }

    public fun tryLoadData() {
        if (isViewCreated && isVisibleToUser && isParentVisible() && !isDataLoaded) {
            loadData()
            isDataLoaded = true
            // 通知 子 Fragment 请求数据
            dispatchParentVisibleState()
        }
    }

}
