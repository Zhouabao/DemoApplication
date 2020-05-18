package com.sdy.jitangapplication.nim.attachment;

import com.alibaba.fastjson.JSONObject;
import com.sdy.jitangapplication.nim.extension.CustomAttachmentType;

/**
 * author : ZFM
 * date   : 2019/8/129:32
 * desc   : 心愿助力attchment
 * version: 1.0
 */
public class WishHelpAttachment extends CustomAttachment {
    private final String KEY_ORDER_ID = "orderId";//礼物的订单id
    private final String KEY_AMOUNT = "amount";//助力数量
    private final String KEY_ICON = "icon";//礼物icon
    private final String KEY_HELP_WISH_STATUS = "wishHelpStatus";//助力状态

    private int orderId;//助力订单id
    private int amount;//助力数量
    private String icon;//礼物icon
    private int wishHelpStatus;//助力状态
    //礼物开启状态
    public final static int WISH_HELP_STATUS_NORMAL = 1;// 开启 待开启
    public final static int WISH_HELP_STATUS_HAS_OPEN = 2; // 已开启
    public final static int WISH_HELP_STATUS_HAS_RETURNED = 3;// 已退回  超时已退回

    public WishHelpAttachment() {
        super(CustomAttachmentType.WishHelp);
    }

    public WishHelpAttachment(String icon, int amount, int orderId,int wishHelpStatus) {
        super(CustomAttachmentType.WishHelp);
        this.orderId = orderId;
        this.amount = amount;
        this.icon = icon;
        this.wishHelpStatus = wishHelpStatus;
    }


    @Override
    protected void parseData(JSONObject data) {
        orderId = data.getInteger(KEY_ORDER_ID);
        amount = data.getInteger(KEY_AMOUNT);
        icon = data.getString(KEY_ICON);
        wishHelpStatus = data.getInteger(KEY_HELP_WISH_STATUS);
    }

    @Override
    protected JSONObject packData() {
        JSONObject data = new JSONObject();
        data.put(KEY_ORDER_ID, orderId);
        data.put(KEY_AMOUNT, amount);
        data.put(KEY_ICON, icon);
        data.put(KEY_HELP_WISH_STATUS, wishHelpStatus);
        return data;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public int getWishHelpStatus() {
        return wishHelpStatus;
    }

    public void setWishHelpStatus(int wishHelpStatus) {
        this.wishHelpStatus = wishHelpStatus;
    }
}
