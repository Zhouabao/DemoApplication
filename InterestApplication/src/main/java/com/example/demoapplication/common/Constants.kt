package com.example.demoapplication.common

object Constants {
    const val BaseUrl = ""
    const val DEFAULT_TIME = 500L
    const val READ_TIME = 500L
    const val SPNAME = "mySp"
    //    ppns/文件类型名/用户ID/当前时间戳/16位随机字符串
    const val FILE_NAME_INDEX = "ppns/"
    //上传文件的类型
    const val AVATOR = "avator/" //头像
    const val PUBLISH = "publish/" //发布
    const val USERCENTER = "usecenter/"//个人中心

    //基地址末尾
    const val END_BASE_URL = "/v1.json"
    //pagesize
    const val PAGESIZE = 15
    const val LABEL_MAX_COUNT = 10
    const val TOKEN = "368b7cbd9de3ee4dc1f90f1c24ff3a10"
    const val ACCID = "664eb259a43464123843abbc7488b02b"
    const val QNTOKEN = "qntoken"

    const val ANTI_ALIAS = true
    const val DEFAULT_SIZE = 150F
    const val DEFAULT_START_ANGLE = 270
    const val DEFAULT_SWEEP_ANGLE = 360
    const val DEFAULT_ANIM_TIME = 1000
    const val DEFAULT_MAX_VALUE = 100
    const val DEFAULT_VALUE = 0
    const val DEFAULT_VALUE_SIZE = 7
    const val DEFAULT_ARC_WIDTH = 10


    //    // 为了apiKey,secretKey为您调用百度人脸在线接口的，如注册，比对等。
    //    // 为了的安全，建议放在您的服务端，端把人脸传给服务器，在服务端端
    //    // license为调用sdk的人脸检测功能使用，人脸识别 = 人脸检测（sdk功能）  + 人脸比对（服务端api）
    var apiKey = "FCcKnV8g5FeocjHFrdbQsB58"
    var secretKey = "xtPYk95nmUFR48GVDzCxFZW8qMI6MnBs"
    var licenseID = "demoapplication-face-face-android"
    var licenseFileName = "idl-license.face-android"
}