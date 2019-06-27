package com.example.demoapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.example.demoapplication.R

/**
 *    author : ZFM
 *    date   : 2019/6/259:44
 *    desc   :年龄筛选器
 *    version: 1.0
 */
class FilterUserDialog(context: Context) : Dialog(context, R.style.MyDialog) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_match_filter)
        //点击外部可取消
        setCanceledOnTouchOutside(true)

    }

}