package com.sdy.jitangapplication.ui.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.LanguageUtils
import com.google.gson.Gson
import com.kotlin.base.ui.activity.BaseActivity
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.model.CountryCodeBean
import com.sdy.jitangapplication.model.LetterCountryCodeComparator
import com.sdy.jitangapplication.ui.adapter.CountryCodeAdapter
import com.sdy.jitangapplication.utils.GetJsonDataUtil
import com.sdy.jitangapplication.widgets.sortcontacts.Cn2Spell
import com.sdy.jitangapplication.widgets.sortcontacts.PinnedHeaderDecoration
import com.sdy.jitangapplication.widgets.sortcontacts.WaveSideBarView
import kotlinx.android.synthetic.main.activity_country_code.*
import kotlinx.android.synthetic.main.layout_actionbar.*
import org.json.JSONArray
import java.util.*

/**
 * 电话号码区号
 */
class CountryCodeActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_country_code)

        initView()
        handler.sendEmptyMessage(MSG_LOAD_DATA)

    }

    private val adapter by lazy { CountryCodeAdapter() }
    private fun initView() {
        hotT1.text = getString(R.string.choose_country_or_region)
        btnBack.clickWithTrigger { finish() }


        countryCodeRv.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        val decoration = PinnedHeaderDecoration()
        decoration.registerTypePinnedHeader(1) { _, _ ->
            true
        }
        countryCodeRv.addItemDecoration(decoration)
        countryCodeRv.adapter = adapter
        adapter.setOnItemClickListener { _, view, position ->
            setResult(Activity.RESULT_OK, intent.putExtra("code", adapter.data[position].code))
            finish()
        }


        indexBar.setOnSelectIndexItemListener(object : WaveSideBarView.OnSelectIndexItemListener {
            override fun onSelectIndexItem(letter: String) {
                for (data in adapter.data.withIndex()) {
                    if (data.value.index.equals(letter)) {
                        (countryCodeRv.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(
                            data.index,
                            0
                        )
                        return
                    }
                }
            }

        })
    }

    companion object {
        private const val MSG_LOAD_DATA = 0x0001
        private const val MSG_LOAD_SUCCESS = 0x0002
        private const val MSG_LOAD_FAILED = 0x0003
    }

    private var thread: Thread? = null
    private val handler by lazy {
        @SuppressLint("HandlerLeak")
        object : Handler() {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    MSG_LOAD_DATA -> {
                        if (thread == null) {
                            thread = Thread(Runnable {
                                // 子线程中解析省市区数据
                                initJsonData()
                            })
                            thread!!.start()
                        }
                    }
                    MSG_LOAD_SUCCESS -> {
                        Collections.sort(cityBeans, LetterCountryCodeComparator())
                        adapter.addData(cityBeans)
                    }
                    MSG_LOAD_FAILED -> {
                    }
                }
            }
        }
    }


    private val cityBeans: ArrayList<CountryCodeBean> = arrayListOf()
    private fun initJsonData() {
        val data = GetJsonDataUtil().getJson(this, "countrycode.json")
        val datas = parseData(data)
        for (tdata in datas) {

            tdata.index = if (LanguageUtils.getSystemLanguage().language == Locale.ENGLISH.language) {
                tdata.en.substring(0, 1)
            } else {
                Cn2Spell.getPinYinFirstLetter(
                    if (tdata.sc.isNullOrEmpty()) {
                        "#"
                    } else {
                        tdata.sc
                    }
                )
            }
        }
        cityBeans.addAll(datas)
        handler.sendEmptyMessage(MSG_LOAD_SUCCESS)
    }


    fun parseData(result: String): ArrayList<CountryCodeBean> {
        val countryCodeBeans = arrayListOf<CountryCodeBean>()
        try {
            val data = JSONArray(result)
            val gson = Gson()
            for (i in 0 until data.length()) {
                val countryCodeBean =
                    gson.fromJson(data.optJSONObject(i).toString(), CountryCodeBean::class.java)
                countryCodeBeans.add(countryCodeBean)
            }
        } catch (e: Exception) {
            handler.sendEmptyMessage(MSG_LOAD_FAILED)
        }

        return countryCodeBeans
    }
}