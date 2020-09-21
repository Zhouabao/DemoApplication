package com.sdy.jitangapplication.common

object Constants {
    //云信新账号
//    const val NIM_APP_KEY = "8556d54f7f6f3453efd7713dba23f852"//正式
//    const val NIM_APP_KEY = "197b98d4853be1582aec4f2a9c6a6645"//测试

    const val NIM_APP_KEY = "789f6cf551ec789fbe39b0a4ea68c951"//测试1
    //是否处于测试环境
    const val TEST = false



    const val BaseUrl = ""
    const val DEFAULT_TIME = 500L
    const val READ_TIME = 500L
    const val SPNAME = "mySp"
    //    ppns/文件类型名/用户ID/当前时间戳/16位随机字符串
    const val FILE_NAME_INDEX = "ppsns/"
    //上传文件的类型
    const val AVATOR = "avator/" //头像
    const val PUBLISH = "publish/" //发布
    const val DATING = "dating/" //约会
    const val USERCENTER = "usecenter/"//个人中心
    const val REPORTUSER = "report/"//舉報用户
    const val CANDYPRODUCT = "candyproduct/"//舉報用户
    const val FEEDBACK = "feedback/"//问题反馈
    const val VIDEOFACE = "verifyVideo/"//认证视频
    const val DEFAULT_AVATAR = "/meta/default_avatar.jpg"//默认图
    const val DEFAULT_EMPTY_AVATAR = "/ppsns_default_avatar.png"//默认头像


    const val CACHE_DIR = "/jitangapplicaiton"//缓存文件夹

    //基地址末尾
    const val END_BASE_URL = "/v1.json"
    //pagesize
    const val PAGESIZE = 15
    const val LABEL_MAX_COUNT = 10
    const val TOKEN = "368b7cbd9de3ee4dc1f90f1c24ff3a10"
    const val ACCID = "664eb259a43464123843abbc7488b02b"
    const val QNTOKEN = "qntoken"


    //官方助手id
    const val ASSISTANT_ACCID = "01"


    //百度人脸认证
    //    // 为了apiKey,secretKey为您调用百度人脸在线接口的，如注册，比对等。
    //    // 为了的安全，建议放在您的服务端，端把人脸传给服务器，在服务端端
    //    // license为调用sdk的人脸检测功能使用，人脸识别 = 人脸检测（sdk功能）  + 人脸比对（服务端api）
    var apiKey = "FCcKnV8g5FeocjHFrdbQsB58"
    var secretKey = "xtPYk95nmUFR48GVDzCxFZW8qMI6MnBs"
    var licenseID = "jitangapplication-face-android"
    var licenseFileName = "idl-license.face-android"


    //友盟配置
    //key
    const val UMENG_APPKEY = "5d5a69330cafb26766000a29"
    const val UMENG_SECRET = "7bc4c2d146e193981b4e7fde5081ffee"
    //微信支付appid
    const val WECHAT_APP_ID = "wx4a3210c3cc8e44fb"
    const val WECHAT_APP_KEY = "a6c9ff7bd2cc9029fd47c02ff2e021ab"
    //支付宝支付appid
    const val ALIPAY_APP_ID = "2088631087441932"
    const val ALIPAY_APP_KEY = "a6c9ff7bd2cc9029fd47c02ff2e021ab"
    //新浪
    const val SINA_APP_KEY = "3069931526"
    const val SINA_APP_SECRET = "57acb0e0282c250ebd92de38a7912536"
    //QQ
    const val QQ_APP_KEY = "1109743471"
    const val QQ_APP_SECRET = "KxByI7XUslBAh82c"
    //bugly
    const val BUGLY_APP_ID = "5218ec6f7d"


    //数美
    const val SM_ORGANIZATION = "2WzKdLM1nFYXpxZB5HX0"
    const val SM_PUBLICKEY = "MIIDLzCCAhegAwIBAgIBMDANBgkqhkiG9w0BAQUFADAyMQswCQYDVQQGEwJDTjELMAkGA1UECwwCU00xFjAUBgNVBAMMDWUuaXNodW1laS5jb20wHhcNMTkxMTA4MDYzMTA5WhcNMzkxMTAzMDYzMTA5WjAyMQswCQYDVQQGEwJDTjELMAkGA1UECwwCU00xFjAUBgNVBAMMDWUuaXNodW1laS5jb20wggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQC863Gs04OQP+63cPm43EdEnTPEcXh2y5per1OZGjUsyNRhJMG7+ePBE0vZKdtt+RLBwf8C59mdbnozMCwyu58Pt7ooSyEevkW72w4qP1U6DYP6R5PjrgTOU674z8KsmwIhbOimk1Gi+4X3pEVZlKNCfScjVKMmsypYgN343Yae2MLH4H6XyycZc6NWRCIXd1gAYOSBOzkb/kWRAsirrbYlHNbVFBfNr2SAk/F3x45rWS3CeZJZSeMImeomweLG+ya6l70osu1lVLu7aS5LhvCb4xNGiKsY63y0XsEZNYaQJJM6wMEPM1Ihhts8PkM5niJBLKj2n/wm//JF5Kj/S0cPAgMBAAGjUDBOMB0GA1UdDgQWBBTB7clijKNQxalsnJqe3M8qRIAPJDAfBgNVHSMEGDAWgBTB7clijKNQxalsnJqe3M8qRIAPJDAMBgNVHRMEBTADAQH/MA0GCSqGSIb3DQEBBQUAA4IBAQAQf3IQPlaxfc8T68QlWbrHX1cz1jjlZz6G+wKVKOT5PMn0RCu3Rw9Aarfl04rsZDF66AZjzNLVxqvRPZLWP1U4CQr74ErqgJtEXUU1WVz5NP1sJiVifUpbGKUJ36ucO22XPZspgFR84EA5Y0DVXtSvuXjHLurxC/qmLGKv0UFEZjzySmLOS/X3m2PMKlGZuDtO7a4t3ikkg0SFPbM3/3xQrJK533sr2FyUCzOfHocUe+bT45A4IDSd0ecPMHhkj+55JjlwfvXp9fGLBq6Iv57eKKpbY9vX8qRFLFIBGBmdpRSnpwNZ1rtBZrMgGTczpTQg527mpaTh68r1XIjaAknZ"
    const val SM_AINFOKEY = "cyhDpMYLMOWsrzgiCQTKPqHQYWqsEdEEVyaLTACvwYpJehLfFTUZIzfdiWoIzPjV"

    //闪验
    const val SY_APP_ID="Rr9a0DYd"
    const val SY_APP_KEY="0jsXDyqa"


    //小米
    // AppID 2882303761518151078
    //AppKey  5591815169078
    //AppSecret    fgsswM8jRV+rBPFZr/0ofA==

}