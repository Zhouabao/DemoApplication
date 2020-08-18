package com.sdy.jitangapplication.nim.extension;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sdy.jitangapplication.nim.attachment.*;
import com.netease.nimlib.sdk.msg.attachment.MsgAttachment;
import com.netease.nimlib.sdk.msg.attachment.MsgAttachmentParser;

/**
 * Created by zhoujianghua on 2015/4/9.
 */
public class CustomAttachParser implements MsgAttachmentParser {

    private static final String KEY_TYPE = "type";
    private static final String KEY_DATA = "data";

    @Override
    public MsgAttachment parse(String json) {
        CustomAttachment attachment = null;
        try {
            JSONObject object = JSON.parseObject(json);
            int type = object.getInteger(KEY_TYPE);
            JSONObject data = object.getJSONObject(KEY_DATA);
            switch (type) {
                case CustomAttachmentType.ChatHi:
                    attachment = new ChatHiAttachment();
//                case CustomAttachmentType.ChatMatch:
//                    attachment = new ChatMatchAttachment();
                    break;
                case CustomAttachmentType.ShareSquare:
                    attachment = new ShareSquareAttachment();
                    break;
                    case CustomAttachmentType.ChatDating:
                    attachment = new ChatDatingAttachment();
                    break;
                case CustomAttachmentType.SendGift:
                    attachment = new SendGiftAttachment();
                    break;
                case CustomAttachmentType.WishHelp:
                    attachment = new WishHelpAttachment();
                    break;
                case CustomAttachmentType.SendTip:
                    attachment = new SendCustomTipAttachment();
                    break;
                case CustomAttachmentType.ChatContact:
                    attachment = new ContactAttachment();
                    break;
                case CustomAttachmentType.AccostGift:
                    attachment = new AccostGiftAttachment();
                    break;
                    case CustomAttachmentType.ChatContactCandy:
                    attachment = new ContactCandyAttachment();
                    break;
                default:
                    attachment = new DefaultCustomAttachment();
                    break;
            }

            if (attachment != null) {
                attachment.fromJson(data);
            }
        } catch (Exception e) {

        }

        return attachment;
    }

    public static String packData(int type, JSONObject data) {
        JSONObject object = new JSONObject();
        object.put(KEY_TYPE, type);
        if (data != null) {
            object.put(KEY_DATA, data);
        }

        return object.toJSONString();
    }
}
