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
    private final String KEY_IF_VIRTUAL = "ifVirtual";//是否是虚拟礼物

    private int orderId;//分享的广场id
    private boolean ifVirtual;//是否是虚拟礼物
    //礼物开启状态
    public final static int GiftReceiveStatusNormal = 0;// 开启 待开启
    public final static int GiftReceiveStatusHasOpen = 1; // 已开启
    public final static int GiftReceiveStatusHasReturned = 2;// 已退回  超时已退回

    public SendGiftAttachment() {
        super(CustomAttachmentType.SendGift);
    }

    public SendGiftAttachment(int orderId, boolean ifVirtual) {
        super(CustomAttachmentType.SendGift);
        this.orderId = orderId;
        this.ifVirtual = ifVirtual;
    }

    public boolean isIfVirtual() {
        return ifVirtual;
    }

    public void setIfVirtual(boolean ifVirtual) {
        this.ifVirtual = ifVirtual;
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
        ifVirtual = data.getBoolean(KEY_IF_VIRTUAL);
    }

    @Override
    protected JSONObject packData() {
        JSONObject data = new JSONObject();
        data.put(KEY_ORDER_ID, orderId);
        data.put(KEY_IF_VIRTUAL, ifVirtual);
        return data;
    }
}
