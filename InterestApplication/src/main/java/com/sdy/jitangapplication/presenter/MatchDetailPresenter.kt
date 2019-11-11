package com.sdy.jitangapplication.presenter

import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.model.GreetBean
import com.sdy.jitangapplication.model.MatchBean
import com.sdy.jitangapplication.model.StatusBean
import com.sdy.jitangapplication.presenter.view.MatchDetailView
import com.sdy.jitangapplication.ui.dialog.HarassmentDialog
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.sdy.jitangapplication.utils.UserManager

/**
 *    author : ZFM
 *    date   : 2019/6/2610:48
 *    desc   :
 *    version: 1.0
 */
class MatchDetailPresenter : BasePresenter<MatchDetailView>() {


    /**
     * 获取详细的用户数据 包括用户的广场信息
     */
    fun getUserDetailInfo(params: HashMap<String, Any>) {
        RetrofitFactory.instance.create(Api::class.java)
            .getMatchUserInfo(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<MatchBean?>>(mView) {
                override fun onStart() {
                }

                override fun onNext(t: BaseResp<MatchBean?>) {
                    if (t.code == 200) {
                        mView.onGetMatchDetailResult(true, t.data)
                    } else {
                        mView.onGetMatchDetailResult(false, null)
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else
                        mView.onGetMatchDetailResult(false, null)
                }
            })

    }

    /**
     * 拉黑用户
     */
    fun shieldingFriend(params: HashMap<String, Any>) {
        if (!checkNetWork()) {
            return
        }
        RetrofitFactory.instance.create(Api::class.java)
            .shieldingFriend(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<Any?>>(mView) {
                override fun onStart() {
                }

                override fun onNext(t: BaseResp<Any?>) {
                    if (t.code == 200) {
                        mView.onGetUserActionResult(true, t.msg)
                    } else {
                        CommonFunction.toast(t.msg)
                        mView.onGetUserActionResult(false, "")
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else {
                        mView.onError(CommonFunction.getErrorMsg(context))
                        mView.onGetUserActionResult(false, null)
                    }
                }
            })

    }

    /**
     * 解除拉黑
     */
    fun removeBlock(params: HashMap<String, Any>) {
        RetrofitFactory.instance.create(Api::class.java)
            .removeBlock(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<Any?>>(mView) {
                override fun onNext(t: BaseResp<Any?>) {
                    if (t.code == 200) {
                        mView.onRemoveBlockResult(true)
                    } else {
                        CommonFunction.toast(t.msg)
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else {
                        mView.onRemoveBlockResult(false)
                    }
                }
            })
    }


    /*
     * 解除匹配
     */
    fun dissolutionFriend(params: HashMap<String, Any>) {
        if (!checkNetWork()) {
            return
        }

        RetrofitFactory.instance.create(Api::class.java)
            .dissolutionFriend(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<Any?>>(mView) {
                override fun onStart() {
                }

                override fun onNext(t: BaseResp<Any?>) {
                    if (t.code == 200) {
                        mView.onGetUserActionResult(true, t.msg)
                    } else {
                        CommonFunction.toast(t.msg)
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else {
                        mView.onError(context.getString(R.string.service_error))
                        mView.onGetUserActionResult(false, null)
                    }
                }
            })

    }


    /**
     * 喜欢
     */
    fun likeUser(params: HashMap<String, Any>) {
        if (!checkNetWork()) {
            return
        }


        RetrofitFactory.instance.create(Api::class.java)
            .addLike(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<StatusBean?>>(mView) {
                override fun onNext(t: BaseResp<StatusBean?>) {
                    if (t.code == 200) {
                        mView.onGetLikeResult(true, t)
                    } else {
                        if (t.code != 201)
                            CommonFunction.toast(t.msg)
                        mView.onGetLikeResult(false, t)
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else
                        mView.onError(context.getString(R.string.service_error))
                }
            })
    }


    /**
     * 打招呼
     */
    fun greet(token: String, accid: String, target_accid: String, tag_id: Int) {
        if (!checkNetWork()) {
            return
        }

        val params = hashMapOf<String, Any>()
        params["tag_id"] = tag_id
        params["target_accid"] = target_accid
        RetrofitFactory.instance.create(Api::class.java)
            .greet(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<StatusBean?>>(mView) {
                override fun onNext(t: BaseResp<StatusBean?>) {
                    if (t.code == 200) {
                        mView.onGreetsResult(true)
                    } else if (t.code == 401) {
                        HarassmentDialog(context, HarassmentDialog.CHATHI).show()
                    } else {
                        CommonFunction.toast(t.msg)
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else
                        mView.onError(context.getString(R.string.service_error))
                }
            })
    }


    /**
     * 判断当前能否打招呼
     */
    fun greetState(token: String, accid: String, target_accid: String) {

        val params = hashMapOf<String, Any>()
        params["target_accid"] = target_accid
        RetrofitFactory.instance.create(Api::class.java)
            .greetState(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<GreetBean?>>(mView) {
                override fun onNext(t: BaseResp<GreetBean?>) {
                    if (t.code == 200) {
                        mView.onGreetStateResult(t.data)
                    } else {
                        mView.onGreetStateResult(t.data)
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else
                        mView.onGreetStateResult(null)
                }
            })
    }
}