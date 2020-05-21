package com.sdy.jitangapplication.nim.attachment;

import com.alibaba.fastjson.JSONObject;
import com.sdy.jitangapplication.nim.extension.CustomAttachmentType;

/**
 * author : ZFM
 * date   : 2019/8/129:32
 * desc   : 联系方式attachment
 * version: 1.0
 */
public class ContactAttachment extends CustomAttachment {
    private final String KEY_CONTACT_WAY = "contactWay";//联系方式
    private final String KEY_CONTACT_CONTENT = "contactContent";//联系号码

    private int contactWay;//联系方式
    private String contactContent;//联系号码

    public ContactAttachment() {
        super(CustomAttachmentType.ChatContact);
    }

    public ContactAttachment(String contactContent, int contactWay) {
        super(CustomAttachmentType.ChatContact);
        this.contactContent = contactContent;
        this.contactWay = contactWay;
    }


    @Override
    protected void parseData(JSONObject data) {
        contactWay = data.getInteger(KEY_CONTACT_WAY);
        contactContent = data.getString(KEY_CONTACT_CONTENT);
    }

    @Override
    protected JSONObject packData() {
        JSONObject data = new JSONObject();
        data.put(KEY_CONTACT_WAY, contactWay);
        data.put(KEY_CONTACT_CONTENT, contactContent);
        return data;
    }

    public int getContactWay() {
        return contactWay;
    }

    public void setContactWay(int contactWay) {
        this.contactWay = contactWay;
    }

    public String getContactContent() {
        return contactContent;
    }

    public void setContactContent(String contactContent) {
        this.contactContent = contactContent;
    }
}
