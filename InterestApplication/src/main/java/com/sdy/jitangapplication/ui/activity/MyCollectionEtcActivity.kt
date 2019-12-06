package com.sdy.jitangapplication.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseActivity
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.event.NotifyEvent
import com.sdy.jitangapplication.ui.fragment.MySquareFragment
import com.umeng.socialize.UMShareAPI
import kotlinx.android.synthetic.main.layout_actionbar.*
import org.greenrobot.eventbus.EventBus

/**
 *  1,我的所有动态
 */
class MyCollectionEtcActivity : BaseActivity() {
    private val type by lazy { intent.getIntExtra("type", 0) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_collection_etc)

        btnBack.onClick {
            finish()
        }
        hotT1.text = "我的动态"
        supportFragmentManager.beginTransaction().add(R.id.content, MySquareFragment(type)).commit()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK)
            if (requestCode == SquarePlayDetailActivity.REQUEST_CODE) {
                EventBus.getDefault()
                    .post(NotifyEvent(data!!.getIntExtra("position", -1), data!!.getIntExtra("type", 0)))
            }
    }

}
