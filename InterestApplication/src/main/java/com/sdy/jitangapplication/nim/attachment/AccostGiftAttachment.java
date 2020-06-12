package com.sdy.jitangapplication.nim.attachment;

import com.alibaba.fastjson.JSONObject;
import com.sdy.jitangapplication.nim.extension.CustomAttachmentType;

/**
 * author : ZFM
 * date   : 2019/8/129:32
 * desc   : 广场分享的attachment
 * version: 1.0
 */
public class AccostGiftAttachment extends CustomAttachment {
    private final String KEY_ORDER_ID = "orderId";//礼物的订单id
    private final String KEY_GIFT_STATUS = "status";//礼物状态
    private final String KEY_GIFT_NAME = "title";//礼物名字
    private final String KEY_GIFT_ICON = "icon";//礼物icon
    private final String KEY_GIFT_AMOUNT = "amount";//礼物价值

    private int orderId;//礼物的订单id
    private int giftStatus;//礼物状态
    private String giftName;//礼物名字
    private String giftIcon;//礼物icon
    private int giftAmount;//糖果
    //礼物开启状态
    public final static int GIFT_RECEIVE_STATUS_NORMAL = 1;// 开启 待开启  （待领取状态）
    public final static int GIFT_RECEIVE_STATUS_HAS_OPEN = 2; // 已开启（领取成功 or 发送成功）
    public final static int GIFT_RECEIVE_STATUS_HAS_RETURNED = 3;// 已退回  超时已退回（超时退回）

    public AccostGiftAttachment() {
        super(CustomAttachmentType.AccostGift);
    }

    public AccostGiftAttachment(int orderId, int giftStatus, String icon, String title, int giftAmount) {
        super(CustomAttachmentType.AccostGift);
        this.orderId = orderId;
        this.giftStatus = giftStatus;
        this.giftIcon = icon;
        this.giftName = title;
        this.giftAmount = giftAmount;
    }

    public int getGiftStatus() {
        return giftStatus;
    }

    public void setGiftStatus(int giftStatus) {
        this.giftStatus = giftStatus;
    }

    public int getId() {
        return orderId;
    }

    public void setId(int id) {
        this.orderId = id;
    }

    public String getGiftName() {
        return giftName;
    }

    public void setGiftName(String giftName) {
        this.giftName = giftName;
    }

    public String getGiftIcon() {
        return giftIcon;
    }

    public void setGiftIcon(String giftIcon) {
        this.giftIcon = giftIcon;
    }

    @Override
    protected void parseData(JSONObject data) {
        orderId = data.getInteger(KEY_ORDER_ID);
        giftStatus = data.getInteger(KEY_GIFT_STATUS);
        giftIcon = data.getString(KEY_GIFT_ICON);
        giftName = data.getString(KEY_GIFT_NAME);
        giftAmount = data.getInteger(KEY_GIFT_AMOUNT);
    }

    @Override
    protected JSONObject packData() {
        JSONObject data = new JSONObject();
        data.put(KEY_ORDER_ID, orderId);
        data.put(KEY_GIFT_STATUS, giftStatus);
        data.put(KEY_GIFT_ICON, giftIcon);
        data.put(KEY_GIFT_NAME, giftName);
        data.put(KEY_GIFT_AMOUNT, giftAmount);
        return data;
    }
}
