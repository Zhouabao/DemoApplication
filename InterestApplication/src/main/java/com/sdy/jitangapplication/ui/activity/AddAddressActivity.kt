package com.sdy.jitangapplication.ui.activity

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import androidx.core.view.isVisible
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
import com.sdy.jitangapplication.event.RefreshAddressvent
import com.sdy.jitangapplication.model.AddressBean
import com.sdy.jitangapplication.presenter.AddAddressPresenter
import com.sdy.jitangapplication.presenter.view.AddAddressView
import com.sdy.jitangapplication.ui.dialog.DeleteDialog
import kotlinx.android.synthetic.main.activity_add_address.*
import kotlinx.android.synthetic.main.delete_dialog_layout.*
import kotlinx.android.synthetic.main.layout_actionbar.*
import org.greenrobot.eventbus.EventBus

/**
 * 新建地址
 */
class AddAddressActivity : BaseMvpActivity<AddAddressPresenter>(), AddAddressView, TextWatcher {
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

        hotT1.text = "添加收货地址"
        btnBack.onClick { finish() }
        rightBtn.isVisible = address != null
        rightBtn.text = "删除"
        rightBtn.setTextColor(Color.parseColor("#FF000000"))
        rightBtn.onClick {
            val deleteDialog = DeleteDialog(this)
            deleteDialog.show()
            deleteDialog.title.text = "提示"
            deleteDialog.tip.text = "确认删除该地址？"
            deleteDialog.confirm.onClick {
                mPresenter.delAddress(address!!.id)
                deleteDialog.dismiss()
            }
            deleteDialog.cancel.onClick {
                deleteDialog.dismiss()
            }
        }


        addressName.addTextChangedListener(this)
        addressContact.addTextChangedListener(this)
        addressContent.addTextChangedListener(this)
        addressDetailContent.addTextChangedListener(this)
        addressPostNumber.addTextChangedListener(this)

        initPicker()

        addressContent.onClick {
            KeyboardUtils.hideSoftInput(addressContent)
            cityPicker.showCityPicker()
        }

        saveBtn.onClick {
            if (address != null) {
                params["id"] = address!!.id
            }
            params["nickname"] = addressName.text
            params["phone"] = addressContact.text
            params["postcode"] = addressPostNumber.text
            params["full_address"] = addressDetailContent.text
            //is_default   1设置默认地址   0 不设置
            params["is_default"] = if (switchDefaultAddress.isChecked) {
                1
            } else {
                0
            }
            if (address == null)
                mPresenter.addAddress(params)
            else
                mPresenter.editAddress(params)
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
            switchDefaultAddress.isChecked = address!!.is_default

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

    fun saveBtnEnable() {
        saveBtn.isEnabled = !addressName.text.isNullOrEmpty()
                && !addressContact.text.isNullOrEmpty()
                && !addressContent.text.isNullOrEmpty()
                && !addressDetailContent.text.isNullOrEmpty()
                && !addressPostNumber.text.isNullOrEmpty()

    }

    override fun onAddAddressResult(success: Boolean, addressBean: AddressBean?) {
        if (success) {
            EventBus.getDefault().post(RefreshAddressvent())
            finish()
        }

    }

    override fun delAddressResult(success: Boolean) {
        if (success) {
            EventBus.getDefault().post(RefreshAddressvent())
            finish()

        }
    }

    override fun afterTextChanged(s: Editable?) {
        saveBtnEnable()
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }
}
