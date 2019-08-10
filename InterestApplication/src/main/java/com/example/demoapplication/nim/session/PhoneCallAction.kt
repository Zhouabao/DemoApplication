package com.example.demoapplication.nim.session

import com.blankj.utilcode.util.ToastUtils
import com.example.demoapplication.R

/**
 *    author : ZFM
 *    date   : 2019/8/814:26
 *    desc   :
 *    version: 1.0
 */
class PhoneCallAction : ChatBaseAction(R.drawable.send_phone_check, R.drawable.send_phone_uncheck,R.string.phone) {
    override fun onClick() {
        ToastUtils.showShort("暂未开放哦~敬请期待")

    }
}