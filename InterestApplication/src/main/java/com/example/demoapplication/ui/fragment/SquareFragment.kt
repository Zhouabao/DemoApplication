package com.example.demoapplication.ui.fragment


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.demoapplication.R
import com.example.demoapplication.api.Api
import com.example.demoapplication.presenter.SquarePresenter
import com.example.demoapplication.presenter.view.SquareView
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.ext.excute
import com.kotlin.base.ext.onClick
import com.kotlin.base.rx.BaseSubscriber
import com.kotlin.base.ui.fragment.BaseMvpFragment
import kotlinx.android.synthetic.main.fragment_square.*
import okhttp3.ResponseBody

/**
 * 广场
 *
 */
class SquareFragment : BaseMvpFragment<SquarePresenter>(), SquareView {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_square, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mPresenter = SquarePresenter()
        mPresenter.mView = this

        tv1.onClick {
            RetrofitFactory.instance.create(Api::class.java)
                .getFileFromNet("http://rsrc1.futrueredland.com.cn/safty/ppsns")
                .excute(object : BaseSubscriber<ResponseBody>(mPresenter.mView) {
                    override fun onNext(t: ResponseBody) {
                        super.onNext(t)
                        Log.i("tag", t.string())
                    }
                })
        }
    }


}
