package com.sdy.jitangapplication.nim.attachment;

import com.alibaba.fastjson.JSONObject;
import com.sdy.jitangapplication.nim.extension.CustomAttachmentType;

/**
 * author : ZFM
 * date   : 2019/8/129:32
 * desc   : 广场分享的attachment
 * version: 1.0
 */
public class SendGiftAttachment extends CustomAttachment {
    private final String KEY_ORDER_ID = "orderId";//礼物的订单id
    private final String KEY_GIFT_STATUS = "giftStatus";//礼物状态

    private int orderId;//礼物的订单id
    private int giftStatus;//礼物状态
    //礼物开启状态
    public final static int GIFT_RECEIVE_STATUS_NORMAL = 1;// 开启 待开启  （待领取状态）
    public final static int GIFT_RECEIVE_STATUS_HAS_OPEN = 2; // 已开启（领取成功 or 发送成功）
    public final static int GIFT_RECEIVE_STATUS_HAS_RETURNED = 3;// 已退回  超时已退回（超时退回）

    public SendGiftAttachment() {
        super(CustomAttachmentType.SendGift);
    }

    public SendGiftAttachment(int orderId, int giftStatus) {
        super(CustomAttachmentType.SendGift);
        this.orderId = orderId;
        this.giftStatus = giftStatus;
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

    @Override
    protected void parseData(JSONObject data) {
        orderId = data.getInteger(KEY_ORDER_ID);
        giftStatus = data.getInteger(KEY_GIFT_STATUS);
    }

    @Override
    protected JSONObject packData() {
        JSONObject data = new JSONObject();
        data.put(KEY_ORDER_ID, orderId);
        data.put(KEY_GIFT_STATUS, giftStatus);
        return data;
    }
}
