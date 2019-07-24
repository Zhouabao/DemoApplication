package com.example.demoapplication.model

/**
 *    author : ZFM
 *    date   : 2019/7/2314:52
 *    desc   :
 *    version: 1.0
 */
data class MediaBean(
    val id :Int = 0,
    val fileType: TYPE = TYPE.IMAGE,
    var filePath: String = "",
    var fileName: String = "",
    var thumbnail: String = "",
    var duration: Int = 0,
    var size: Long = 0L,
    var ischecked: Boolean = false
) {
    enum class TYPE { IMAGE, VIDEO }

}