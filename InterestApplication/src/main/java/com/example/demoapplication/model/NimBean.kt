package com.example.demoapplication.model

/**
 *    author : ZFM
 *    date   : 2019/8/1715:06
 *    desc   : 聊天界面返回用户信息
 *    version: 1.0
 */


data class NimBean(
    val avatar: String? = "",
    val isfriend: Boolean = false,
    val taglist: ArrayList<Tag>? = arrayListOf()
)

