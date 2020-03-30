package com.sdy.jitangapplication.ui.activity

import android.os.Bundle
import android.text.InputFilter
import com.blankj.utilcode.util.KeyboardUtils
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.lljjcoder.Interface.OnCityItemClickListener
import com.lljjcoder.bean.CityBean
import com.lljjcoder.bean.DistrictBean
import com.lljjcoder.bean.ProvinceBean
import com.lljjcoder.style.cityjd.JDCityConfig
import com.lljjcoder.style.cityjd.JDCityPicker
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.event.GetAddressvent
import com.sdy.jitangapplication.model.AddressBean
import com.sdy.jitangapplication.presenter.AddAddressPresenter
import com.sdy.jitangapplication.presenter.view.AddAddressView
import kotlinx.android.synthetic.main.activity_add_address.*
import kotlinx.android.synthetic.main.layout_actionbar.*
import org.greenrobot.eventbus.EventBus

/**
 * 新建地址
 */
class AddAddressActivity : BaseMvpActivity<AddAddressPresenter>(), AddAddressView {
    private val params by lazy { hashMapOf<String, Any>() }

    private val address by lazy { intent.getSerializableExtra("address") as AddressBean? }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_address)
        initView()
    }


    private fun initView() {
        mPresenter = AddAddressPresenter()
        mPresenter.mView = this
        mPresenter.context = this

        hotT1.text = "保存并使用"
        btnBack.onClick { finish() }


        initPicker()
        addressContent.onClick {
            KeyboardUtils.hideSoftInput(addressContent)
            cityPicker.showCityPicker()
        }

        saveBtn.onClick {
            params["nickname"] = addressName.text
            params["phone"] = addressContact.text
            params["postcode"] = addressPostNumber.text
            params["full_address"] = addressDetailContent.text
            mPresenter.addAddress(params)

        }

        addressContact.filters = arrayOf(InputFilter.LengthFilter(11))
        addressPostNumber.filters = arrayOf(InputFilter.LengthFilter(6))
        if (address != null) {
            addressName.setText(address!!.nickname)
            addressName.setSelection(addressName.text.length)
            addressContact.setText(address!!.phone)
            addressContact.setSelection(addressContact.text.length)
            addressContent.setText("${address!!.province_name}${address!!.city_name}${address!!.area_name}")
            addressDetailContent.setText(address!!.full_address)
            addressDetailContent.setSelection(addressDetailContent.text.length)
            addressPostNumber.setText(address!!.postcode)
            addressPostNumber.setSelection(addressPostNumber.text.length)

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
                params["province_name"] = province.name
                params["city_name"] = city.name
                params["area_name"] = district.name

                addressContent.text = "${province.name}${city.name}${district.name}"
            }
        })

    }

    override fun onAddAddressResult(success: Boolean, addressBean: AddressBean?) {
        if (success) {
            EventBus.getDefault().post(GetAddressvent(addressBean!!))
            finish()
        }

    }
}
