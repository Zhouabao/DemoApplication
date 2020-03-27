package com.sdy.jitangapplication.model

/**
 *    author : ZFM
 *    date   : 2020/3/2710:02
 *    desc   :
 *    version: 1.0
 */

data class GoodsListBean(
    var banner: MutableList<MutableList<BannerProductBean>> = mutableListOf(),
    var list: MutableList<NewLabel> = mutableListOf(),
    var myinfo: Myinfo = Myinfo()
)

data class ProductTitleBean(
    var descr: String = "",
    var id: Int = 0,
    var title: String = ""
)

data class BannerProductBean(
    var descr: String = "",
    var icon: String = "",
    var id: Int = 0,
    var title: String = ""
)


data class Myinfo(
    var yue: Int = 0
)