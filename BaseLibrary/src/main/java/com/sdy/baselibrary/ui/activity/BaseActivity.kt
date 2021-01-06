package com.kotlin.base.ui.activity

import android.annotation.TargetApi
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import com.blankj.utilcode.util.BarUtils
import com.braintreepayments.api.BraintreeFragment
import com.braintreepayments.api.exceptions.InvalidArgumentException
import com.braintreepayments.api.interfaces.BraintreeListener
import com.braintreepayments.api.interfaces.ConfigurationListener
import com.braintreepayments.api.models.Configuration
import com.facebook.CallbackManager
import com.kotlin.base.common.AppManager
import com.sdy.baselibrary.widgets.swipeback.SwipeBackLayout
import com.sdy.baselibrary.widgets.swipeback.app.SwipeBackActivity
import org.jetbrains.anko.find


/*
    Activity基类，业务无关
 */
open class BaseActivity : SwipeBackActivity(), BraintreeListener {
    public val TAG1 = this::class.java.simpleName
    val callbackManager by lazy { CallbackManager.Factory.create() }



    companion object {
        public const val LOADING = 0
        public const val CONTENT = 1
        public const val ERROR = 2
        public const val EMPTY = 3
    }

    private lateinit var mSwipeBackLayout: SwipeBackLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            BarUtils.transparentStatusBar(this)
            BarUtils.setStatusBarLightMode(this, true)
        } else {
//            //取消设置透明状态栏,使 ContentView 内容不再覆盖状态栏
//            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//            //需要设置这个 flag 才能调用 setStatusBarColor 来设置状态栏颜色
//            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            //设置状态栏颜色
//            window.statusBarColor = Color.BLACK
        }
//        StatusBarUtil.immersive(this)
        AppManager.instance.addActivity(this)
        initSwipeBackFinish()
    }

    private fun initSwipeBackFinish() {
        mSwipeBackLayout = swipeBackLayout
        mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT)
    }

    override fun onDestroy() {
        super.onDestroy()
        AppManager.instance.finishActivity(this)
//        UMShareAPI.get(this).release()

    }

    //获取Window中视图content
    val contentView: View
        get() {
            val content = find<FrameLayout>(android.R.id.content)
            return content.getChildAt(0)
        }


    fun initState() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setTranslucentStatus(true)
        }
        //val manager = SystemBarTintManager(this)
        //激活状态栏设置
        //  manager.isStatusBarTintEnabled = true
        //激活导航栏设置
        // manager.setNavigationBarTintEnabled(true)
        //设置一个状态栏颜色
        // manager.setStatusBarTintColor(Color.WHITE)
    }

    @TargetApi(19)
    private fun setTranslucentStatus(on: Boolean) {
        val win = window
        val winParams = win.attributes
        val bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
        if (on) {
            winParams.flags = winParams.flags or bits
        } else {
            winParams.flags = winParams.flags and bits.inv()
        }
        win.attributes = winParams
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
//        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }


}
