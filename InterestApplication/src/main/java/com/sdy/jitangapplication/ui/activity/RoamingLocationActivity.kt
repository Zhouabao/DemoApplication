package com.sdy.jitangapplication.ui.activity

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Message
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.SPUtils
import com.google.gson.Gson
import com.kotlin.base.ui.activity.BaseActivity
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.event.SetRoamingLocationEvent
import com.sdy.jitangapplication.event.UpdateRoamingLocationEvent
import com.sdy.jitangapplication.model.CityBean
import com.sdy.jitangapplication.model.LetterLocationComparator
import com.sdy.jitangapplication.model.ProviceBean
import com.sdy.jitangapplication.ui.adapter.RoamingLocationAdapter
import com.sdy.jitangapplication.utils.AMapManager
import com.sdy.jitangapplication.utils.GetJsonDataUtil
import com.sdy.jitangapplication.utils.UserManager
import com.sdy.jitangapplication.widgets.sortcontacts.Cn2Spell
import com.sdy.jitangapplication.widgets.sortcontacts.PinnedHeaderDecoration
import com.sdy.jitangapplication.widgets.sortcontacts.WaveSideBarView
import kotlinx.android.synthetic.main.activity_roaming_location.*
import kotlinx.android.synthetic.main.activity_roaming_location.currentLocation
import kotlinx.android.synthetic.main.dialog_match_filter.*
import kotlinx.android.synthetic.main.layout_actionbar.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.startActivity
import org.json.JSONArray
import java.util.*

/**
 * 地址漫游
 */
class RoamingLocationActivity : BaseActivity() {

    private val roaming by lazy { intent.getStringExtra("roaming") }

    companion object {
        private const val MSG_LOAD_DATA = 0x0001
        private const val MSG_LOAD_SUCCESS = 0x0002
        private const val MSG_LOAD_FAILED = 0x0003

        fun startToRoaming(context: Context, roaming: String) {
            context.startActivity<RoamingLocationActivity>("roaming" to roaming)
        }
    }


    private var thread: Thread? = null
    private var isLoaded = false

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
                        Collections.sort(cityBeans, LetterLocationComparator())
                        adapter.addData(cityBeans)
                        isLoaded = true
                    }
                    MSG_LOAD_FAILED -> {
                    }
                }
            }
        }
    }
    private val adapter by lazy { RoamingLocationAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_roaming_location)

        initView()
        handler.sendEmptyMessage(MSG_LOAD_DATA)
        AMapManager.initLocation(this)

    }


    private val cityBeans: ArrayList<CityBean> = arrayListOf()

    private fun initJsonData() {
        val data = GetJsonDataUtil().getJson(this, "province.json")
        val proviceBean = parseData(data)

        for (data in proviceBean) {
            for (data1 in data.city) {
                cityBeans.add(
                    CityBean(
                        data1.name, data.name, Cn2Spell.getPinYinFirstLetter(data1.name)
                    )
                )
            }
        }
        handler.sendEmptyMessage(MSG_LOAD_SUCCESS)
    }


    fun parseData(result: String): ArrayList<ProviceBean> {
        val provinceBeans = arrayListOf<ProviceBean>()
        try {
            val data = JSONArray(result)
            val gson = Gson()
            for (i in 0 until data.length()) {
                val province =
                    gson.fromJson(data.optJSONObject(i).toString(), ProviceBean::class.java)
                provinceBeans.add(province)
            }
        } catch (e: Exception) {
            handler.sendEmptyMessage(MSG_LOAD_FAILED)
        }

        return provinceBeans
    }

    private fun initView() {
        EventBus.getDefault().register(this)

        hotT1.text = getString(R.string.location_roaming)
        btnBack.clickWithTrigger {
            finish()
        }

        val manager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        roamingLocationRv.layoutManager = manager
        val decoration = PinnedHeaderDecoration()
        decoration.registerTypePinnedHeader(1) { _, _ ->
            true
        }
        roamingLocationRv.addItemDecoration(decoration)
        roamingLocationRv.adapter = adapter
        adapter.setOnItemClickListener { _, view, position ->
            SPUtils.getInstance(Constants.SPNAME).put("roaming_city", "${adapter.data[position].provinceName},${adapter.data[position].name}")
            EventBus.getDefault().post(SetRoamingLocationEvent(adapter.data[position]))
            finish()
        }

        currentLocation.clickWithTrigger {
            EventBus.getDefault().post(SetRoamingLocationEvent(CityBean()))
            SPUtils.getInstance(Constants.SPNAME).remove("roaming_city")
            finish()
        }

        roamingLocation.clickWithTrigger {
            EventBus.getDefault().post(
                SetRoamingLocationEvent(
                    CityBean(
                        roaming.split(",")[1],
                        roaming.split(",")[0]
                    )
                )
            )
            finish()
        }


        indexBar.setOnSelectIndexItemListener(object : WaveSideBarView.OnSelectIndexItemListener {
            override fun onSelectIndexItem(letter: String) {
                for (data in adapter.data.withIndex()) {
                    if (data.value.index.equals(letter)) {
                        (roamingLocationRv.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(
                            data.index,
                            0
                        )
                        return
                    }
                }
            }

        })

        searchLocation.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchResult(query)
                KeyboardUtils.hideSoftInput(searchLocation)
                searchLocation.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (newText.isNullOrEmpty()) {
                    searchResult(newText)
                }
//                searchResult(newText)
                return true
            }

        })


        if (roaming.isNotEmpty()) {
            roamingDivider.isVisible = true
            roamingLocation.isVisible = true
            roamingLocation.text = getString(R.string.current_roaming,roaming)
        } else {
            roamingDivider.isVisible = false
            roamingLocation.isVisible = false
        }
    }


    private fun searchResult(query: String?) {
        if (!query.isNullOrEmpty()) {
            //每次改变输入就清空数据重新查询
            adapter.data.clear()
            adapter.notifyDataSetChanged()
            for (data in cityBeans) {
                val pinyin = Cn2Spell.getPinYin(data.name)
                if (!query.isNullOrEmpty() && (pinyin.contains(query) || data.name.contains(query))) {
                    adapter.addData(data)
                }
            }
        } else {
            adapter.data.clear()
            adapter.addData(cityBeans)
            adapter.notifyDataSetChanged()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun updateRoamingLocationEvent(event: UpdateRoamingLocationEvent) {
        currentLocation.text = getString(R.string.current_location) +"\t${UserManager.getCity()}," + UserManager.getDistrict()
        if (roaming.isNullOrEmpty()) {
            currentLocation.setTextColor(resources.getColor(R.color.colorOrange))
            currentLocation.setCompoundDrawablesWithIntrinsicBounds(
                resources.getDrawable(
                    R.drawable.icon_location_orange
                ),
                null,
                null,
                null
            )
        } else {
            currentLocation.setTextColor(resources.getColor(R.color.colorBlack19))
            currentLocation.setCompoundDrawablesWithIntrinsicBounds(
                resources.getDrawable(
                    R.drawable.icon_location_roaming
                ),
                null,
                null,
                null
            )
        }

    }
}