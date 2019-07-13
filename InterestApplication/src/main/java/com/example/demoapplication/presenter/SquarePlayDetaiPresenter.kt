package com.example.demoapplication.presenter

import android.app.Activity
import com.example.demoapplication.api.Api
import com.example.demoapplication.model.SquareBean
import com.example.demoapplication.presenter.view.SquarePlayDetailView
import com.example.demoapplication.utils.UserManager
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseSubscriber

/**
 *    author : ZFM
 *    date   : 2019/7/417:57
 *    desc   :
 *    version: 1.0
 */
class SquarePlayDetaiPresenter : BasePresenter<SquarePlayDetailView>() {

    fun getRencentlySquares() {
        mView.onGetRecentlySquaresResults(
            mutableListOf(
                SquareBean(
                    isvip = 1,
                    icon = null,
                    accid = "664eb259a43464123843abbc7488b02b",
                    audio_json = null,
                    avatar = "http://rsrc1.futrueredland.com.cn/",
                    city_name = "成都",
                    comment_cnt = 0,
                    create_time = "2019-06-27 09:42:47",
                    descr = "这是一个测试广场发布消息4",
                    id = 2,
                    isliked = 0,
                    iscollected = 0,
                    like_cnt = 10,
                    member_level = 1,
                    nickname = "32321",
                    out_time = "06-27 09:42",
                    photo_json = mutableListOf(
                        "http://rsrc1.futrueredland.com.cn/ppns/headImage/0ca42c0d253ebee3f2bb197fbfcc5527/1562740286/0uBnhoxs4yRnWl39",
                        "http://rsrc1.futrueredland.com.cn/ppns/avator/e3a623fbef21dd5fc00b189cb9949ade/1562754134044/ehjjqedmm107wsz3.jpg",
                        "http://rsrc1.futrueredland.com.cn/ppns/avator/e3a623fbef21dd5fc00b189cb9949ade/1562754134044/ehjjqedmm107wsz3.jpg"
                    ),
                    province_name = "四川",
                    share_cnt = 0,
                    tag_id = 1,
                    title = "精选",
                    video_json = null,
                    type = 1,
                    duration = 0
                ),
                SquareBean(
                    isvip = 1,
                    icon = null,
                    accid = "0ca42c0d253ebee3f2bb197fbfcc5527",
                    audio_json = mutableListOf("http://up.mcyt.net/down/47541.mp3"),
                    avatar = "",
                    city_name = "乐山",
                    comment_cnt = 3,
                    create_time = "2019-07-08 16:20:47",
                    descr = "这是一个测试消息1",
                    id = 3,
                    isliked = 0,
                    iscollected = 0,
                    like_cnt = -5,
                    member_level = 1,
                    nickname = "",
                    out_time = "3天前",
                    photo_json = null,
                    province_name = "四川",
                    share_cnt = 0,
                    tag_id = 1,
                    title = "精选",
                    video_json = null, type = 3, duration = 0
                ),
                SquareBean(
                    isvip = 1,
                    icon = null,
                    accid = "0ca42c0d253ebee3f2bb197fbfcc5527",
                    audio_json = null,
                    avatar = "",
                    city_name = "乐山",
                    comment_cnt = 3,
                    create_time = "2019-07-08 16:20:47",
                    descr = "这是一个测试消息1",
                    id = 3,
                    isliked = 0,
                    iscollected = 0,
                    like_cnt = -5,
                    member_level = 1,
                    nickname = "",
                    out_time = "3天前",
                    photo_json = null,
                    province_name = "四川",
                    share_cnt = 0,
                    tag_id = 1,
                    title = "精选",
                    video_json = mutableListOf("http://9890.vod.myqcloud.com/9890_4e292f9a3dd011e6b4078980237cc3d3.f20.mp4"),
                    type = 2
                ), SquareBean(
                    isvip = 1,
                    icon = null,
                    accid = "664eb259a43464123843abbc7488b02b",
                    audio_json = null,
                    avatar = "http://rsrc1.futrueredland.com.cn/",
                    city_name = "成都",
                    comment_cnt = 0,
                    create_time = "2019-06-27 09:42:47",
                    descr = "这是一个测试广场发布消息4",
                    id = 2,
                    isliked = 0,
                    iscollected = 0,
                    like_cnt = 10,
                    member_level = 1,
                    nickname = "32321",
                    out_time = "06-27 09:42",
                    photo_json = mutableListOf(
                        "http://rsrc1.futrueredland.com.cn/ppns/headImage/0ca42c0d253ebee3f2bb197fbfcc5527/1562740286/0uBnhoxs4yRnWl39",
                        "http://rsrc1.futrueredland.com.cn/ppns/avator/e3a623fbef21dd5fc00b189cb9949ade/1562754134044/ehjjqedmm107wsz3.jpg",
                        "http://rsrc1.futrueredland.com.cn/ppns/avator/e3a623fbef21dd5fc00b189cb9949ade/1562754134044/ehjjqedmm107wsz3.jpg"
                    ),
                    province_name = "四川",
                    share_cnt = 0,
                    tag_id = 1,
                    title = "精选",
                    video_json = null,
                    type = 1,
                    duration = 0
                ),
                SquareBean(
                    isvip = 1,
                    icon = null,
                    accid = "0ca42c0d253ebee3f2bb197fbfcc5527",
                    audio_json = mutableListOf("http://up.mcyt.net/down/47541.mp3"),
                    avatar = "",
                    city_name = "乐山",
                    comment_cnt = 3,
                    create_time = "2019-07-08 16:20:47",
                    descr = "这是一个测试消息1",
                    id = 3,
                    isliked = 0,
                    iscollected = 0,
                    like_cnt = -5,
                    member_level = 1,
                    nickname = "",
                    out_time = "3天前",
                    photo_json = null,
                    province_name = "四川",
                    share_cnt = 0,
                    tag_id = 1,
                    title = "精选",
                    video_json = null, type = 3, duration = 0
                ),
                SquareBean(
                    isvip = 1,
                    icon = null,
                    accid = "0ca42c0d253ebee3f2bb197fbfcc5527",
                    audio_json = null,
                    avatar = "",
                    city_name = "乐山",
                    comment_cnt = 3,
                    create_time = "2019-07-08 16:20:47",
                    descr = "这是一个测试消息1",
                    id = 3,
                    isliked = 0,
                    iscollected = 0,
                    like_cnt = -5,
                    member_level = 1,
                    nickname = "",
                    out_time = "3天前",
                    photo_json = null,
                    province_name = "四川",
                    share_cnt = 0,
                    tag_id = 1,
                    title = "精选",
                    video_json = mutableListOf("http://9890.vod.myqcloud.com/9890_4e292f9a3dd011e6b4078980237cc3d3.f20.mp4"),
                    type = 2
                )
            )
        )
    }


    /**
     * 点赞 取消点赞
     * 1 点赞 2取消点赞
     */
    fun getSquareLike(params: HashMap<String, Any>, position: Int) {
        RetrofitFactory.instance.create(Api::class.java)
            .getSquareLike(params)
            .excute(object : BaseSubscriber<BaseResp<Any?>>(mView) {
                override fun onNext(t: BaseResp<Any?>) {
                    super.onNext(t)
                    if (t.code == 200) {
                        mView.onGetSquareLikeResult(position, true)
                    } else if (t.code == 403) {
                        UserManager.startToLogin(context as Activity)
                    } else {
                        mView.onGetSquareLikeResult(position, false)
                    }

                }
            })
    }

    /**
     * 点赞 取消点赞
     * 1 收藏 2取消收藏
     */
    fun getSquareCollect(params: HashMap<String, Any>, position: Int) {
        RetrofitFactory.instance.create(Api::class.java)
            .getSquareCollect(params)
            .excute(object : BaseSubscriber<BaseResp<Any?>>(mView) {
                override fun onNext(t: BaseResp<Any?>) {
                    super.onNext(t)
                    if (t.code == 200)
                        mView.onGetSquareCollectResult(position, t)
                    else if (t.code == 403) {
                        UserManager.startToLogin(context as Activity)
                    } else {
                        mView.onGetSquareCollectResult(position, t)
                    }
                }
            })
    }
}