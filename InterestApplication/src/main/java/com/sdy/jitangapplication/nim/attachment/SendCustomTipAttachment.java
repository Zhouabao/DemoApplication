package com.sdy.jitangapplication.nim.attachment;

import com.alibaba.fastjson.JSONObject;
import com.sdy.jitangapplication.nim.extension.CustomAttachmentType;

/**
 * author : ZFM
 * date   : 2019/8/129:32b
 * desc   : 发送自定义的tip消息
 * version: 1.0
 */
public class SendCustomTipAttachment extends CustomAttachment {
    private final String KEY_CONTENT = "content"; //发送tip的内容
    private final String KEY_TYPE = "showType"; //发送tip的类型
    private final String KEY_IF_SEND_USER_SHOW = "ifSendUserShow"; //是否是发送方显示
    public final static int CUSTOM_TIP_NO_MONEY_MAN = 1;//男方发送消息余额不足
    public final static int CUSTOME_TIP_RECEIVE_VERIFY_WOMAN = 2;//认证后的女方收到第一条消息（仅显示一次，切换用户不重复发送）
    public final static int CUSTOME_TIP_RECEIVE_NOT_VERIFY_WOMAN = 3;//未认证的女方收到第一条消息（切换用户重复发送）
    public final static int CUSTOME_TIP_WOMAN_CHAT_COUNT = 4;//女方聊天条数到达设定条数时,没有心愿礼物
    public final static int CUSTOME_TIP_MAN_BIGGER_CHAT_COUNT_HAS_GIFT = 5;//男方 双方聊天数>设定聊天条数  女方有心愿礼物
    public final static int CUSTOME_TIP_MAN_BIGGER_CHAT_COUNT_NOT_HAS_GIFT = 6;//男方 双方聊天数>设定聊天条数 女方没有心愿礼物
    public final static int CUSTOME_TIP_RECEIVED_GIFT = 7;//已领取对方赠送的糖果礼物，可直接用于兑换商品和提现
    public final static int CUSTOME_TIP_EXCHANGE_PRODUCT = 8;//已满足兑换所需糖果，立即兑换
    public final static int CUSTOME_TIP_EXCHANGE_FOR_ASSISTANT = 9;//小助手发聊天糖果退回警告（针对女方）
    public final static int CUSTOME_TIP_NORMAL = 10;//常规的tip
//    public final static int CUSTOME_TIP_RECEIVE_NOT_HUMAN = 8;//发件方 收件方非真人
//    public final static int CUSTOM_TIP_FIRST_SEND_MAN = 1;//男方发送第一条消息（仅第一次发送消息会显示，切换用户不重复发送）

    private String content;//发送tip的内容
    private int showType;//是什么类型的tip
    private Boolean ifSendUserShow;//是否是发送方显示

    public SendCustomTipAttachment() {
        super(CustomAttachmentType.SendTip);
    }

    public SendCustomTipAttachment(String content, int type, boolean ifSendUserShow) {
        super(CustomAttachmentType.SendTip);
        this.content = content;
        this.showType = type;
        this.ifSendUserShow = ifSendUserShow;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getShowType() {
        return showType;
    }

    public void setShowType(int showType) {
        this.showType = showType;
    }


    public Boolean getIfSendUserShow() {
        return ifSendUserShow;
    }

    public void setIfSendUserShow(Boolean ifSendUserShow) {
        this.ifSendUserShow = ifSendUserShow;
    }


    @Override
    protected void parseData(JSONObject data) {
        content = data.getString(KEY_CONTENT);
        showType = data.getInteger(KEY_TYPE);
        ifSendUserShow = data.getBoolean(KEY_IF_SEND_USER_SHOW);
    }

    @Override
    protected JSONObject packData() {
        JSONObject data = new JSONObject();
        data.put(KEY_CONTENT, content);
        data.put(KEY_TYPE, showType);
        data.put(KEY_IF_SEND_USER_SHOW, ifSendUserShow);
        return data;
    }
}
