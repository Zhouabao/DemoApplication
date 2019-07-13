package com.kotlin.base.ui.activity

import android.annotation.TargetApi
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.kotlin.base.common.AppManager
import com.readystatesoftware.systembartint.SystemBarTintManager
import org.jetbrains.anko.find


/*
    Activity基类，业务无关
 */
open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppManager.instance.addActivity(this)
        //initState()

    }

    override fun onDestroy() {
        super.onDestroy()
        AppManager.instance.finishActivity(this)
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
        val manager = SystemBarTintManager(this)
        //激活状态栏设置
        manager.isStatusBarTintEnabled = true
        //激活导航栏设置
        manager.setNavigationBarTintEnabled(true)
        //设置一个状态栏颜色
        manager.setStatusBarTintColor(Color.WHITE)
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

}
