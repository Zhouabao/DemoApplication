package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import android.widget.RadioGroup
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.clickWithTrigger
import kotlinx.android.synthetic.main.dialog_choose_character.*

/**
 *    author : ZFM
 *    date   : 2019/6/259:44
 *    desc   :男性落地选择自己的特质
 *    version: 1.0
 */
class ChooseCharacterDialog(val context1: Context) : Dialog(context1, R.style.MyDialog),
    RadioGroup.OnCheckedChangeListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_choose_character)
        initWindow()
        initView()
    }

    private fun initView() {
        rgAdvantage.setOnCheckedChangeListener (this)
        rgGetAlongWay.setOnCheckedChangeListener(this)
        rgProgressiveRelation.setOnCheckedChangeListener(this)

        btnCompleteCharacter.clickWithTrigger {
            //todo 发起网络请求，存储男性的特质
            dismiss()
        }

    }

    fun checkEnable() {
        btnCompleteCharacter.isEnabled =
            rgAdvantage.checkedRadioButtonId != -1 && rgGetAlongWay.checkedRadioButtonId != -1 && rgProgressiveRelation.checkedRadioButtonId != -1
    }


    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.BOTTOM)
        val params = window?.attributes
//        params?.width = ScreenUtils.getScreenWidth() - SizeUtils.dp2px(15F) * 2
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT

        params?.windowAnimations = R.style.MyDialogBottomAnimation
//        params?.y = SizeUtils.dp2px(20F)
        window?.attributes = params
        //点击外部可取消
        setCanceledOnTouchOutside(false)
        setCancelable(false)
    }

    override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
        checkEnable()
    }


}