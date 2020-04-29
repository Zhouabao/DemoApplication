package com.sdy.jitangapplication.model

/**
 *    author : ZFM
 *    date   : 2020/4/2715:58
 *    desc   :
 *    version: 1.0
 */
data class CheckBean(
    var normalIcon: Int = -1,
    var checkedIcon: Int = -1,
    var title: String = "",
    var checked: Boolean = false,
    var icon: String = ""
)