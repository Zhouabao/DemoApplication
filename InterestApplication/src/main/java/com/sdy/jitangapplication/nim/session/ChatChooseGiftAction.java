package com.sdy.jitangapplication.nim.session;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;

import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.tools.SdkVersionUtils;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.sdy.jitangapplication.R;
import com.sdy.jitangapplication.nim.uikit.business.session.constant.RequestCode;
import com.sdy.jitangapplication.nim.uikit.business.uinfo.UserInfoHelper;
import com.sdy.jitangapplication.nim.uikit.common.util.string.MD5;
import com.sdy.jitangapplication.ui.dialog.ChatSendGiftDialog;

import java.io.File;
import java.util.List;

/**
 *
 */
public class ChatChooseGiftAction extends ChatBaseAction {
    private static final int PICK_IMAGE_COUNT = 9;

    public ChatChooseGiftAction() {
        super(R.drawable.send_gift_uncheck, R.drawable.send_gift_uncheck, R.string.input_panel_photo);
    }

    @Override
    public void onClick() {
        new ChatSendGiftDialog(UserInfoHelper.getUserTitleName(getAccount(), SessionTypeEnum.P2P),
                UserInfoHelper.getAvatar(getAccount()), getAccount(), getActivity()).show();

        // CommonFunction.INSTANCE.toast("礼物正在开发中");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RequestCode.PICK_IMAGE) {
            if (data != null) {
                List<LocalMedia> selectList = PictureSelector.obtainMultipleResult(data);
                for (int i = 0; i < selectList.size(); i++) {
                    LocalMedia media = selectList.get(i);
                    if (PictureMimeType.eqImage(media.getMimeType())) {// 发送图片
                        if (SdkVersionUtils.checkedAndroid_Q())
                            if (media.getAndroidQToPath() != null && !media.getAndroidQToPath().isEmpty())
                                sendImageAfterSelfImagePicker(new File(media.getAndroidQToPath()));
                            else
                                sendImageAfterSelfImagePicker(new File(media.getCompressPath()));
                        else
                            sendImageAfterSelfImagePicker(new File(media.getCompressPath()));
                    } else if (PictureMimeType.eqVideo(media.getMimeType())) {// 发送视频
                        if (SdkVersionUtils.checkedAndroid_Q())
                            if (media.getAndroidQToPath() != null && !media.getAndroidQToPath().isEmpty())
                                sendVideo(new File(media.getAndroidQToPath()),
                                        MD5.getStreamMD5(media.getAndroidQToPath()));
                            else
                                sendVideo(new File(Uri.parse(media.getPath()).getPath()),
                                        MD5.getStreamMD5(media.getPath()));
                        else
                            sendVideo(new File(media.getPath()), MD5.getStreamMD5(media.getPath()));
                    }
                }
            }
        }
    }

    /**
     * 发送图片
     */
    private void sendImageAfterSelfImagePicker(File file) {
        IMMessage message = MessageBuilder.createImageMessage(getAccount(), getSessionType(), file, file.getName());
        sendMessage(message);
    }

    /**
     * 获取视频mediaPlayer
     *
     * @param file 视频文件
     * @return mediaPlayer
     */
    private MediaPlayer getVideoMediaPlayer(File file) {
        try {
            return MediaPlayer.create(getActivity(), Uri.fromFile(file));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 发送视频
     *
     * @param file
     * @param md5
     */
    private void sendVideo(File file, String md5) {
        MediaPlayer mediaPlayer = getVideoMediaPlayer(file);
        long duration = mediaPlayer == null ? 0 : mediaPlayer.getDuration();
        int height = mediaPlayer == null ? 0 : mediaPlayer.getVideoHeight();
        int width = mediaPlayer == null ? 0 : mediaPlayer.getVideoWidth();
        IMMessage message =
                MessageBuilder.createVideoMessage(getAccount(), getSessionType(), file, duration, width, height, md5);
        sendMessage(message);
    }

}
