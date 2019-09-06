package com.sdy.baselibrary.utils

import kotlin.random.Random

/**
 *    author : ZFM
 *    date   : 2019/7/810:34
 *    desc   :
 *    version: 1.0
 */
object RandomUtils {


    /**
     * 随机字符串
     */
    fun getRandomString(length: Int): String { //length表示生成字符串的长度
        val base = "abcdefghijklmnopqrstuvwxyz0123456789"
        val sb = StringBuffer()
        for (i in 0 until length) {
            val number = Random.nextInt(base.length)
            sb.append(base[number])
        }
        return sb.toString()
    }
}