package com.example.demoapplication.model

import java.io.Serializable

/**
 *    author : ZFM
 *    date   : 2019/6/2418:17
 *    desc   :  sex：1男2女
 *    version: 1.0
 */
data class MatchBean(var name: String, var age: Int, var sex: Int, var imgs: MutableList<Int>) : Serializable