package com.example.demoapplication.model

/**
 *    author : ZFM
 *    date   : 2019/8/1715:06
 *    desc   : 聊天界面返回用户信息
 *    version: 1.0
 */


data class NimBean(
    val avatar: String? = "",
    val isfriend: Boolean = false,//	是否好友 true 是 f alse 不是
    val isinitiated: Boolean = false,//是否自己发起的 true自己发起的 false 他人发起
    val taglist: ArrayList<Tag>? = arrayListOf()
)

