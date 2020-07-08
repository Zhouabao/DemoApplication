package com.sdy.jitangapplication.nim.uikit.business.session.viewholder;

import com.netease.nimlib.sdk.msg.attachment.VideoAttachment;
import com.sdy.jitangapplication.R;
import com.sdy.jitangapplication.nim.uikit.business.session.activity.WatchVideoActivity;
import com.sdy.jitangapplication.nim.uikit.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter;
import com.sdy.jitangapplication.nim.uikit.common.util.media.BitmapDecoder;

/**
 * Created by zhoujianghua on 2015/8/5.
 */
public class MsgViewHolderVideo extends MsgViewHolderThumbBase {

    public MsgViewHolderVideo(BaseMultiItemFetchLoadAdapter adapter) {
        super(adapter);
    }

    @Override
    protected int getContentResId() {
        return R.layout.nim_message_item_video;
    }

    @Override
    protected void onItemClick() {
        WatchVideoActivity.start(context, message);
    }

    @Override
    protected String thumbFromSourceFile(String path) {
        VideoAttachment attachment = (VideoAttachment) message.getAttachment();
        String thumb = attachment.getThumbPathForSave();
        return BitmapDecoder.extractThumbnail(path, thumb) ? thumb : null;
    }
}
