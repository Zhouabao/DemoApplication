package com.example.demoapplication.utils

import android.content.Context
import cn.sharesdk.onekeyshare.OnekeyShare

/**
 *    author : ZFM
 *    date   : 2019/7/211:19
 *    desc   :分享工具类
 *    version: 1.0
 */
object ShareUtils {
    fun showShare(
        context: Context,
        platform: String?,
        title: String?,
        titleUrl: String?,
        text: String?,
        imagePath: String?,
        url: String?
    ) {
        val oks = OnekeyShare()
        if (platform != null) {
            oks.setPlatform(platform)
        }
        //关闭sso授权
        oks.disableSSOWhenAuthorize()
        //title标题：微信，QQ和QQ空间等平台使用
        oks.setTitle(title)
        //titleUrl:QQ和QQ空间跳转链接
        oks.setTitleUrl(titleUrl)
        //text分享文本：所有平台都需要这个字段
        oks.text = text
        //imagePath:图片本地路径，Linked-In以外的平台都支持此参数
        oks.setImagePath(imagePath)
        //url在微信、微博、Facebook等平台使用
        oks.setUrl(url)
        oks.show(context)
    }
}