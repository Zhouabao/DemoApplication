package com.sdy.jitangapplication.nim.session

import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction

/**
 *    author : ZFM
 *    date   : 2019/8/814:26
 *    desc   :
 *    version: 1.0
 */
class PhoneCallAction : ChatBaseAction(R.drawable.send_phone_check, R.drawable.send_phone_uncheck,R.string.phone) {
    override fun onClick() {
        CommonFunction.toast("暂未开放哦~敬请期待")

    }
}