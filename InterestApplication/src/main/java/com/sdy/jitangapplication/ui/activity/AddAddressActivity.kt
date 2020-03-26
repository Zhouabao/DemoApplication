package com.sdy.jitangapplication.ui.activity

import android.os.Bundle
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.lljjcoder.Interface.OnCityItemClickListener
import com.lljjcoder.bean.CityBean
import com.lljjcoder.bean.DistrictBean
import com.lljjcoder.bean.ProvinceBean
import com.lljjcoder.style.cityjd.JDCityConfig
import com.lljjcoder.style.cityjd.JDCityPicker
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.presenter.AddAddressPresenter
import com.sdy.jitangapplication.presenter.view.AddAddressView
import kotlinx.android.synthetic.main.activity_add_address.*
import kotlinx.android.synthetic.main.layout_actionbar.*

/**
 * 新建地址
 */
class AddAddressActivity : BaseMvpActivity<AddAddressPresenter>(), AddAddressView {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_address)
        initView()
    }

    private fun initView() {
        hotT1.text = "保存并使用"
        btnBack.onClick { finish() }


        initPicker()
        addressContent.onClick {
            cityPicker.showCityPicker()
        }
    }

    private val wheelType = JDCityConfig.ShowType.PRO_CITY_DIS
    private val cityConfig by lazy { JDCityConfig.Builder().build() }
    private val cityPicker by lazy { JDCityPicker() }
    fun initPicker() {
        cityConfig.showType = wheelType
        cityPicker.init(this)
        cityPicker.setConfig(cityConfig)
        cityPicker.setOnCityItemClickListener(object : OnCityItemClickListener() {
            override fun onSelected(
                province: ProvinceBean,
                city: CityBean,
                district: DistrictBean
            ) {

                addressContent.text = "${province.name}${city.name}${district.name}"
            }
        })

    }
}
