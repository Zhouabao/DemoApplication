package com.sdy.jitangapplication.nim.fragment;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSONObject;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.FragmentUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.kotlin.base.data.net.RetrofitFactory;
import com.kotlin.base.data.protocol.BaseResp;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.MsgServiceObserve;
import com.netease.nimlib.sdk.msg.attachment.ImageAttachment;
import com.netease.nimlib.sdk.msg.attachment.VideoAttachment;
import com.netease.nimlib.sdk.msg.constant.MsgDirectionEnum;
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.CustomMessageConfig;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.MessageReceipt;
import com.sdy.baselibrary.utils.RandomUtils;
import com.sdy.jitangapplication.R;
import com.sdy.jitangapplication.api.Api;
import com.sdy.jitangapplication.common.CommonFunction;
import com.sdy.jitangapplication.common.Constants;
import com.sdy.jitangapplication.event.EnablePicEvent;
import com.sdy.jitangapplication.event.HideContactLlEvent;
import com.sdy.jitangapplication.event.NimHeadEvent;
import com.sdy.jitangapplication.event.StarEvent;
import com.sdy.jitangapplication.event.UpdateApproveEvent;
import com.sdy.jitangapplication.event.UpdateSendGiftEvent;
import com.sdy.jitangapplication.event.UpdateStarEvent;
import com.sdy.jitangapplication.model.CustomerMsgBean;
import com.sdy.jitangapplication.model.NimBean;
import com.sdy.jitangapplication.model.ResidueCountBean;
import com.sdy.jitangapplication.model.SendTipBean;
import com.sdy.jitangapplication.nim.attachment.SendCustomTipAttachment;
import com.sdy.jitangapplication.nim.extension.ChatMessageListPanelEx;
import com.sdy.jitangapplication.nim.panel.ChatInputPanel;
import com.sdy.jitangapplication.nim.session.ChatBaseAction;
import com.sdy.jitangapplication.nim.session.ChatChooseGiftAction;
import com.sdy.jitangapplication.nim.session.ChatContactAction;
import com.sdy.jitangapplication.nim.session.ChatPickImageAction;
import com.sdy.jitangapplication.nim.session.ChatTakeImageAction;
import com.sdy.jitangapplication.nim.session.MyLocationAction;
import com.sdy.jitangapplication.nim.uikit.api.model.main.CustomPushContentProvider;
import com.sdy.jitangapplication.nim.uikit.api.model.session.SessionCustomization;
import com.sdy.jitangapplication.nim.uikit.business.session.constant.Extras;
import com.sdy.jitangapplication.nim.uikit.business.session.module.Container;
import com.sdy.jitangapplication.nim.uikit.business.session.module.ModuleProxy;
import com.sdy.jitangapplication.nim.uikit.common.CommonUtil;
import com.sdy.jitangapplication.nim.uikit.common.fragment.TFragment;
import com.sdy.jitangapplication.nim.uikit.impl.NimUIKitImpl;
import com.sdy.jitangapplication.ui.activity.IDVerifyActivity;
import com.sdy.jitangapplication.ui.dialog.AlertCandyEnoughDialog;
import com.sdy.jitangapplication.ui.dialog.ContactCandyReceiveDialog;
import com.sdy.jitangapplication.ui.dialog.LoadingDialog;
import com.sdy.jitangapplication.ui.dialog.ReceiveAccostGiftDialog;
import com.sdy.jitangapplication.ui.dialog.VerifyAddChatDialog;
import com.sdy.jitangapplication.ui.dialog.VideoAddChatTimeDialog;
import com.sdy.jitangapplication.ui.fragment.SnackBarFragment;
import com.sdy.jitangapplication.utils.QNUploadManager;
import com.sdy.jitangapplication.utils.UserManager;
import com.sdy.jitangapplication.widgets.CommonAlertDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 聊天界面基类
 * <p/>
 * Created by huangjun on 2015/2/1.
 */
public class ChatMessageFragment extends TFragment implements ModuleProxy {
    public NimBean nimBean = null;
    private boolean firstIn = true;

    private SessionCustomization customization;

    protected static final String TAG = "MessageActivity";

    // p2p对方Account或者群id
    protected String sessionId;

    protected SessionTypeEnum sessionType;

    // modules
    protected ChatInputPanel inputPanel;
    protected ChatMessageListPanelEx messageListPanel;

    private View rootView;
    private TextView leftChatTimes, gotoVerifyBtn, unlockContactBtn;
    private LinearLayout messageActivityBottomLayout, verifyLl, unlockContactLl;
    private FrameLayout unlockChatLl;
    private ImageView contactIv, closeContactBtn, giftIcon;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        parseIntent();
        EventBus.getDefault().register(this);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.chat_nim_message_fragment, container, false);
        leftChatTimes = rootView.findViewById(R.id.leftChatTimes);
        gotoVerifyBtn = rootView.findViewById(R.id.gotoVerifyBtn);
        messageActivityBottomLayout = rootView.findViewById(R.id.messageActivityBottomLayout);
        unlockChatLl = rootView.findViewById(R.id.unlockChatLl);
        verifyLl = rootView.findViewById(R.id.verifyLl);
        unlockContactLl = rootView.findViewById(R.id.unlockContactLl);
        unlockContactBtn = rootView.findViewById(R.id.unlockContactBtn);
        contactIv = rootView.findViewById(R.id.contactIv);
        closeContactBtn = rootView.findViewById(R.id.closeContactBtn);
        giftIcon = rootView.findViewById(R.id.giftIcon);

        // 去认证
        gotoVerifyBtn.setOnClickListener(v -> {
            if (!nimBean.getMy_isfaced())
                CommonFunction.INSTANCE.startToFace(getActivity(), IDVerifyActivity.TYPE_ACCOUNT_NORMAL, -1);
            else if (nimBean.getMv_state() == 0)
                CommonFunction.INSTANCE.startToVideoIntroduce(getActivity(), -1);
        });

        // 糖果门槛消费聊天
        unlockChatLl.setOnClickListener(v -> {
            CommonFunction.INSTANCE.checkChat(getActivity(), sessionId);
        });

        // 解锁联系方式
        unlockContactBtn.setOnClickListener(v -> {
            CommonFunction.INSTANCE.checkUnlockContact(getActivity(), sessionId, 1);
        });

        // 关系解锁联系方式
        closeContactBtn.setOnClickListener(v -> {
            unlockContactLl.setVisibility(View.GONE);
        });
        return rootView;
    }

    /**
     * ***************************** life cycle *******************************
     */

    @Override
    public void onPause() {
        super.onPause();
        NIMClient.getService(MsgService.class).setChattingAccount(MsgService.MSG_CHATTING_ACCOUNT_NONE,
                SessionTypeEnum.None);
        inputPanel.onPause();
        messageListPanel.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        inputPanel.onResume();
        messageListPanel.onResume();
        messageListPanel.refreshMessageList();
        if (!sessionId.equals(Constants.ASSISTANT_ACCID))
            if (nimBean == null) {
                getTargetInfo(sessionId);
            } else {
                setTargetInfoData();
            }
        else {
            messageActivityBottomLayout.setVisibility(View.VISIBLE);
        }
        NIMClient.getService(MsgService.class).setChattingAccount(sessionId, sessionType);
        getActivity().setVolumeControlStream(AudioManager.STREAM_VOICE_CALL); // 默认使用听筒播放
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        messageListPanel.onDestroy();
        EventBus.getDefault().unregister(this);
        registerObservers(false);
        if (inputPanel != null) {
            inputPanel.onDestroy();
        }
    }

    public boolean onBackPressed() {
        return inputPanel.collapse(true) || messageListPanel.onBackPressed();
    }

    private void parseIntent() {
        Bundle arguments = getArguments();
        sessionId = arguments.getString(Extras.EXTRA_ACCOUNT);
        sessionType = (SessionTypeEnum) arguments.getSerializable(Extras.EXTRA_TYPE);
        IMMessage anchor = (IMMessage) arguments.getSerializable(Extras.EXTRA_ANCHOR);

        customization = (SessionCustomization) arguments.getSerializable(Extras.EXTRA_CUSTOMIZATION);
        Container container = new Container(getActivity(), sessionId, sessionType, this, true);

        if (messageListPanel == null) {
            messageListPanel = new ChatMessageListPanelEx(container, rootView, anchor, false, true);
        } else {
            messageListPanel.reload(container, anchor);
        }

        if (inputPanel == null) {
            inputPanel = new ChatInputPanel(container, rootView, getActionList());
            inputPanel.setCustomization(customization);
        } else {
            inputPanel.reload(container, customization);
        }

        registerObservers(true);

        if (customization != null) {
            messageListPanel.setChattingBackground(customization.backgroundUri, customization.backgroundColor);
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void updateApproveEvent(UpdateApproveEvent event) {
        getTargetInfo(sessionId);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateSendGiftEvent(UpdateSendGiftEvent event) {
        messageListPanel.onMsgSend(event.getMessage());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void hideContactLlEvent(HideContactLlEvent event) {
        unlockContactLl.setVisibility(View.GONE);
        nimBean.set_unlock_contact(true);

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateStarEvent(UpdateStarEvent event) {
        nimBean.setStared(event.getStar());
    }

    /**
     * ************************* 消息收发 **********************************
     */
    // 是否允许发送消息
    protected boolean isAllowSendMessage(final IMMessage message) {
        return customization.isAllowSendMessage(message);
    }

    private void registerObservers(boolean register) {
        MsgServiceObserve service = NIMClient.getService(MsgServiceObserve.class);
        service.observeReceiveMessage(incomingMessageObserver, register);
        // 已读回执监听
        if (NimUIKitImpl.getOptions().shouldHandleReceipt) {
            service.observeMessageReceipt(messageReceiptObserver, register);
        }
    }

    /**
     * 消息接收观察者
     */
    Observer<List<IMMessage>> incomingMessageObserver = new Observer<List<IMMessage>>() {
        @Override
        public void onEvent(List<IMMessage> messages) {
            onMessageIncoming(messages);
        }
    };

    private void onMessageIncoming(List<IMMessage> messages) {
        try {
            // if (!sessionId.equals(Constants.ASSISTANT_ACCID)) {
            // getTargetInfo(sessionId);
            // } else {
            // messageActivityBottomLayout.setVisibility(View.VISIBLE);
            // }

            if (CommonUtil.isEmpty(messages)) {
                return;
            }
            // 新消息来了更新消息状态
            messageListPanel.onIncomingMessage(messages);
            // 发送已读回执
            messageListPanel.sendReceipt();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 已读回执观察者
     */
    private Observer<List<MessageReceipt>> messageReceiptObserver = new Observer<List<MessageReceipt>>() {
        @Override
        public void onEvent(List<MessageReceipt> messageReceipts) {
            // 收到已读回执
            messageListPanel.receiveReceipt();
            // 收到已读回执,调用接口,改变此时招呼或者消息的状态
            // if (!sessionId.equals(Constants.ASSISTANT_ACCID)) {
            // getTargetInfo(sessionId);
            // }
        }
    };

    /**
     * ********************** implements ModuleProxy *********************
     */
    @Override
    public boolean sendMessage(IMMessage message) {
        Log.d("sendMessage", ".....");
        if (sessionId.equals(Constants.ASSISTANT_ACCID)) {
            sendMsgS(message, false);
            return true;
        } else if (nimBean != null && !nimBean.is_send_msg() && nimBean.getMy_gender() == 1
                && nimBean.getTarget_gender() == 2) {
            showConfirmSendDialog(message);
        } else if (canSendMsg()) {
            // 男性,非会员 弹充值界面
            if (nimBean.getForce_isvip()) {
                CommonFunction.INSTANCE.startToFootPrice(getActivity());
            } else {
                sendMsgRequest(message, sessionId);
            }
        }

        return false;

    }

    private Boolean canSendMsg() {
        // 发起方并且次数为0 禁止发送
        if (nimBean.getIslimit()) {
            // 如果没有认证就弹认证才能聊天,如果认证了就查看是否添加了认证视频
            if (!nimBean.getMy_isfaced())
                new VerifyAddChatDialog(getActivity(), nimBean.getApprove_chat_times()).show();
            else if (nimBean.getMv_state() == 0)
                new VideoAddChatTimeDialog(getActivity()).show();
            return false;
        } else {
            return true;
        }
    }

    // 被对方拉入黑名单后，发消息失败的交互处理
    private void sendFailWithBlackList(int code, IMMessage msg) {
        if (code == ResponseCode.RES_IN_BLACK_LIST) {
            // 如果被对方拉入黑名单，发送的消息前不显示重发红点
            msg.setStatus(MsgStatusEnum.success);
            NIMClient.getService(MsgService.class).updateIMMessageStatus(msg);
            messageListPanel.refreshMessageList();
            // 同时，本地插入被对方拒收的tip消息
            IMMessage tip = MessageBuilder.createTipMessage(msg.getSessionId(), msg.getSessionType());
            tip.setContent(getActivity().getString(R.string.black_list_send_tip));
            tip.setStatus(MsgStatusEnum.success);
            CustomMessageConfig config = new CustomMessageConfig();
            config.enableUnreadCount = false;
            tip.setConfig(config);
            NIMClient.getService(MsgService.class).saveMessageToLocal(tip, true);
        }
    }

    private void appendPushConfig(IMMessage message) {
        CustomPushContentProvider customConfig = NimUIKitImpl.getCustomPushContentProvider();
        if (customConfig == null) {
            return;
        }
        String content = customConfig.getPushContent(message);
        Map<String, Object> payload = customConfig.getPushPayload(message);
        if (!TextUtils.isEmpty(content)) {
            message.setPushContent(content);
        }
        if (payload != null) {
            message.setPushPayload(payload);
        }
        Log.d("OkHttp===", JSONObject.toJSONString(payload.toString()));

    }

    @Override
    public void onInputPanelExpand() {
        messageListPanel.scrollToBottom();
    }

    @Override
    public void shouldCollapseInputPanel() {
        inputPanel.collapse(false);
    }

    // 禁止消息长按操作
    @Override
    public boolean isLongClickEnabled() {
        return !inputPanel.isRecording();
    }

    @Override
    public void onItemFooterClick(IMMessage message) {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        inputPanel.onActivityResult(requestCode, resultCode, data);
    }

    // 操作面板集合
    protected List<ChatBaseAction> getActionList() {
        List<ChatBaseAction> actions = new ArrayList<>();
        // actions.add(new EmojAction());//表情
        // actions.add(new RecordAction());//录音
        actions.add(new ChatPickImageAction()); // 图片
        actions.add(new ChatTakeImageAction()); // 拍照
        actions.add(new MyLocationAction()); // 位置
        actions.add(new ChatContactAction()); // 联系方式
        actions.add(new ChatChooseGiftAction()); // 礼物
        // actions.add(new PhoneCallAction()); //语音通话

        return actions;
    }

    /**
     * 进入聊天界面 获取对方用户的个人信息
     * 并且实时更新招呼的状态
     *
     * @param target_accid
     */
    private LoadingDialog loadingDialog;

    public void getTargetInfo(String target_accid) {
        if (firstIn) {
            if (loadingDialog == null)
                loadingDialog = new LoadingDialog(getActivity());
            loadingDialog.show();
            firstIn = false;
        }
        HashMap<String, Object> params = UserManager.INSTANCE.getBaseParams();
        params.put("target_accid", target_accid);
        RetrofitFactory.Companion.getInstance().create(Api.class)
                .getTargetInfoCandy(UserManager.INSTANCE.getSignParams(params)).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new rx.Observer<BaseResp<NimBean>>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                if (loadingDialog != null && loadingDialog.isShowing()) {
                    loadingDialog.dismiss();
                }
            }

            @Override
            public void onNext(BaseResp<NimBean> nimBeanBaseResp) {
                if (loadingDialog != null && loadingDialog.isShowing()) {
                    loadingDialog.dismiss();
                }
                if (nimBeanBaseResp.getCode() == 200 && nimBeanBaseResp.getData() != null) {
                    nimBean = nimBeanBaseResp.getData();
                    setTargetInfoData();
                } else if (nimBeanBaseResp.getCode() == 409) {
                    new CommonAlertDialog.Builder(getActivity()).setTitle(getString(R.string.tip))
                            .setContent(nimBeanBaseResp.getMsg()).setCancelIconIsVisibility(false)
                            .setConfirmText(getString(R.string.iknow)).setCancelAble(false).setOnConfirmListener(dialog -> {
                        dialog.cancel();
                        NIMClient.getService(MsgService.class).deleteRecentContact2(sessionId,
                                SessionTypeEnum.P2P);
                        getActivity().finish();
                    }).create().show();
                }
            }
        });
    }

    /**
     * 设置消息体制内的数据
     * verifyLl
     */

    private boolean isSendChargePtVip = false; // 是否发送过此条tip
    private boolean isDirectIn = false; // 是否存在回复消息
    private boolean showSendGift = false;// 显示过发送礼物

    private void setTargetInfoData() {
        UserManager.INSTANCE.setShowCandyMessage(nimBean.getChat_expend_amount() > 0);
        UserManager.INSTANCE.setShowCandyTime(nimBean.getChat_expend_time());
        // UserManager.INSTANCE.setApproveBean(new ApproveBean(nimBean.getApprove_time(), nimBean.getIsapprove(),
        // nimBean.getIssend()));
        // 显示提示认证的悬浮
        if (nimBean.getMy_gender() == 2 && (!nimBean.getMy_isfaced() || nimBean.getMv_state() == 0)) {
            verifyLl.setVisibility(View.VISIBLE);
            if (!nimBean.getMy_isfaced()) {
                gotoVerifyBtn.setText(getString(R.string.verify_now));
                if (nimBean.getResidue_msg_cnt() == nimBean.getNormal_chat_times()) {
                    leftChatTimes.setText(getString(R.string.unverify_only_some_can_chat, nimBean.getNormal_chat_times()));
                } else {
                    leftChatTimes.setText(getString(R.string.unverify_residue_count, nimBean.getResidue_msg_cnt()));
                }
            } else {
                gotoVerifyBtn.setText(getString(R.string.video_introduce));
                leftChatTimes.setText(getString(R.string.unverify_today_residue_count, nimBean.getResidue_msg_cnt()));
            }
        } else {
            verifyLl.setVisibility(View.INVISIBLE);
        }

        // 显示糖果解锁聊天
        if (nimBean.getLockbtn()) {
            unlockChatLl.setVisibility(View.VISIBLE);
        } else {
            unlockChatLl.setVisibility(View.GONE);
        }

        // 显示解锁联系方式
        // 0没有留下联系方式 1 电话 2 微信 3 qq 99隐藏
        if (nimBean.getUnlock_contact_way() != 0 && !nimBean.is_unlock_contact()) {
            unlockContactLl.setVisibility(View.VISIBLE);
            if (nimBean.getUnlock_contact_way() == 1) {
                contactIv.setImageResource(R.drawable.icon_phone_circle);
                unlockContactBtn.setBackgroundResource(R.drawable.shape_rectangle_orange_15dp);
            } else if (nimBean.getUnlock_contact_way() == 2) {
                contactIv.setImageResource(R.drawable.icon_wechat_circle);
                unlockContactBtn.setBackgroundResource(R.drawable.shape_rectangle_green_15);
            } else if (nimBean.getUnlock_contact_way() == 3) {
                contactIv.setImageResource(R.drawable.icon_qq_circle);
                unlockContactBtn.setBackgroundResource(R.drawable.shape_rectangle_blue_15);
            }
        } else {
            unlockContactLl.setVisibility(View.GONE);
        }

        EventBus.getDefault().post(new NimHeadEvent(nimBean, nimBean.getMy_gender() == 2 && !nimBean.getMy_isfaced()));
        EventBus.getDefault().postSticky(new StarEvent(nimBean.getStared()));
        messageListPanel.refreshMessageList();
        // inputPanel.checkIsSendMsg();

        if (!nimBean.getUnlock_popup_str().isEmpty()) {
            new ContactCandyReceiveDialog(sessionId, nimBean.getUnlock_popup_str(), getActivity()).show();
            nimBean.setUnlock_popup_str("");
        }

        if (!sessionId.equals(Constants.ASSISTANT_ACCID) && UserManager.INSTANCE.getGender() == 2
                && !nimBean.getPrivate_chat_state() && !isSendChargePtVip && !messageListPanel.getItems().isEmpty()
                && messageListPanel.getItems().get(messageListPanel.getItems().size() - 1)
                .getDirect() == MsgDirectionEnum.In) {
            ArrayList<SendTipBean> tips = new ArrayList<>();
            tips.add(new SendTipBean(getString(R.string.hide_message_if_gold_vip), true,
                    SendCustomTipAttachment.CUSTOME_TIP_PRIVICY_SETTINGS));
            CommonFunction.INSTANCE.sendTips(sessionId, tips);
            isSendChargePtVip = true;
        } else {
            isSendChargePtVip = true;
        }

        if (UserManager.INSTANCE.getGender() == 1) {
            for (int i = 0; i < messageListPanel.getItems().size(); i++) {
                IMMessage message = messageListPanel.getItems().get(i);
                if (message.getDirect() == MsgDirectionEnum.In) {
                    isDirectIn = true;
                    break;
                }
            }
            for (int i = 0; i < messageListPanel.getItems().size(); i++) {
                IMMessage message = messageListPanel.getItems().get(i);
                if (message.getAttachment() instanceof SendCustomTipAttachment
                        && ((SendCustomTipAttachment) message.getAttachment())
                        .getShowType() == SendCustomTipAttachment.CUSTOME_TIP_CHARGE_PT_VIP) {
                    isSendChargePtVip = true;
                    break;
                }
            }

            if (nimBean.getMy_gender() == 1 && nimBean.getTarget_gender() == 2 && nimBean.getTarget_ishoney()) {
                EventBus.getDefault().post(new EnablePicEvent(5));
                if (!nimBean.getLockbtn() && !nimBean.is_send_msg() && !showSendGift) {
                    showSendGiftEvent();
                    // EventBus.getDefault().post(new ShowSendGiftEvent(true));
                    showSendGift = true;
                }
            }

        }

    }


    private void uploadImgToQN(IMMessage content, String target_accid, String imageUrl) {
        if (loadingDialog == null)
            loadingDialog = new LoadingDialog(getActivity());
        loadingDialog.show();
        String key = Constants.FILE_NAME_INDEX + Constants.CHATCHECK + UserManager.INSTANCE.getAccid()
                + System.currentTimeMillis() + RandomUtils.INSTANCE.getRandomString(16);
        QNUploadManager.INSTANCE.getInstance().put(imageUrl, key, SPUtils.getInstance(Constants.SPNAME).getString("qntoken"), (key1, info, response) -> {
            Log.d("sendMessage", "response===" + response.toString());
            Log.d("sendMessage", "key1===" + key1);
            if (info.isOK()) {
                sendMsgRequest(content, target_accid, key1);
            } else {
                loadingDialog.dismiss();
                CommonFunction.INSTANCE.toast("消息发送失败");
            }
        }, null);
    }

    private void sendMsgRequest(IMMessage content, String target_accid, String qnMediaUrl) {
        if (loadingDialog == null)
            loadingDialog = new LoadingDialog(getActivity());
        loadingDialog.show();
        HashMap<String, Object> params = UserManager.INSTANCE.getBaseParams();
        params.put("target_accid", target_accid);
        if (content.getMsgType() == MsgTypeEnum.text) {
            params.put("content", content.getContent());
        } else {
            params.put("content", qnMediaUrl);
        }

        params.put("type", content.getMsgType().getValue());
        RetrofitFactory.Companion.getInstance().create(Api.class)
                .sendMsgRequest(UserManager.INSTANCE.getSignParams(params)).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new rx.Observer<BaseResp<ResidueCountBean>>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                if (loadingDialog != null && loadingDialog.isShowing()) {
                    loadingDialog.dismiss();
                }
            }

            @Override
            public void onNext(BaseResp<ResidueCountBean> nimBeanBaseResp) {
                if (loadingDialog != null && loadingDialog.isShowing()) {
                    loadingDialog.dismiss();
                }
                if (nimBeanBaseResp.getCode() == 200 || nimBeanBaseResp.getCode() == 211) {
                    inputPanel.restoreText(true);
                    // 搭讪礼物如果返回不为空，就代表成功领取对方的搭讪礼物
                    if (nimBeanBaseResp.getData().getRid_data() != null
                            && !nimBeanBaseResp.getData().getRid_data().getIcon().isEmpty()) {
                        new ReceiveAccostGiftDialog(getActivity(), nimBeanBaseResp.getData().getRid_data())
                                .show();
                    }
                    sendMsgS(content, false);
                    if (!nimBeanBaseResp.getData().getRet_tips_arr().isEmpty())
                        CommonFunction.INSTANCE.sendTips(sessionId,
                                nimBeanBaseResp.getData().getRet_tips_arr());
                    nimBean.set_send_msg(true);

                    if (UserManager.INSTANCE.getGender() == 1 && !isSendChargePtVip
                            && !sessionId.equals(Constants.ASSISTANT_ACCID) && !nimBean.getIsplatinum()) {
                        ArrayList<SendTipBean> tips = new ArrayList<>();
                        tips.add(new SendTipBean(getString(R.string.charge_to_free), true,
                                SendCustomTipAttachment.CUSTOME_TIP_CHARGE_PT_VIP));
                        CommonFunction.INSTANCE.sendTips(sessionId, tips);
                        isSendChargePtVip = true;
                    }
                } else if (nimBeanBaseResp.getCode() == 409) {// 用户被封禁

                    new CommonAlertDialog.Builder(getActivity()).setTitle(getString(R.string.tip))
                            .setContent(nimBeanBaseResp.getMsg()).setCancelIconIsVisibility(false)
                            .setConfirmText(getString(R.string.iknow)).setCancelAble(false).setOnConfirmListener(dialog -> {
                        dialog.cancel();
                        NIMClient.getService(MsgService.class).deleteRecentContact2(sessionId,
                                SessionTypeEnum.P2P);
                        getActivity().finish();
                    }).create().show();
                } else if (nimBeanBaseResp.getCode() == 411) {// 糖果余额不足
                    new AlertCandyEnoughDialog(getActivity(),
                            AlertCandyEnoughDialog.Companion.getFROM_SEND_GIFT()).show();
                } else if (nimBeanBaseResp.getCode() == 201) {// 门槛会员充值
                    CommonFunction.INSTANCE.startToFootPrice(getActivity());
                } else {
                    inputPanel.restoreText(true);
                    FragmentUtils.add(
                            ((AppCompatActivity) ActivityUtils.getTopActivity()).getSupportFragmentManager(),
                            new SnackBarFragment(new CustomerMsgBean(SnackBarFragment.SEND_FAILED, getString(R.string.send_failed), nimBeanBaseResp.getMsg(), R.drawable.icon_notice)),
                            android.R.id.content);
//                    CommonFunction.INSTANCE.toast(nimBeanBaseResp.getMsg());
                }
            }

        });
    }

    private void sendMsgRequest(IMMessage content, String target_accid) {
//        if (content.getMsgType() == MsgTypeEnum.audio) {
//            uploadImgToQN(content, target_accid, ((AudioAttachment) content.getAttachment()).getPath());
//        } else
        if (content.getMsgType() == MsgTypeEnum.image) {
            uploadImgToQN(content, target_accid, ((ImageAttachment) content.getAttachment()).getPath());
        } else if (content.getMsgType() == MsgTypeEnum.video) {
            uploadImgToQN(content, target_accid, ((VideoAttachment) content.getAttachment()).getPath());
        } else {
            sendMsgRequest(content, target_accid, "");
        }
    }

    private void aideSendMsg(IMMessage content) {
        HashMap<String, Object> params = UserManager.INSTANCE.getBaseParams();
        if (content.getMsgType() == MsgTypeEnum.text) {
            params.put("content", content.getContent());
        }
        RetrofitFactory.Companion.getInstance().create(Api.class)
                .aideSendMsg(UserManager.INSTANCE.getSignParams(params)).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new rx.Observer<BaseResp<ResidueCountBean>>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(BaseResp<ResidueCountBean> nimBeanBaseResp) {

            }

        });
    }

    private void sendMsgS(IMMessage content, boolean requestMsg) {
        appendPushConfig(content);
        // send message to server and save to db

        NIMClient.getService(MsgService.class).sendMessage(content, false).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // if (requestMsg) {
                // sendMsgRequest(content, sessionId);
                // }
                if (sessionId.equals(Constants.ASSISTANT_ACCID) && content.getMsgType() == MsgTypeEnum.text) {
                    aideSendMsg(content);
                }
            }

            @Override
            public void onFailed(int i) {

            }

            @Override
            public void onException(Throwable throwable) {

            }
        });
        messageListPanel.onMsgSend(content);
    }

    private void showConfirmSendDialog(final IMMessage message) {
        new CommonAlertDialog.Builder(getActivity()).setContent(getString(R.string.cost_one_candy_for_quanlity)).setTitle(getString(R.string.send_messgae))
                .setCancelText(getString(R.string.cancel)).setConfirmText(getString(R.string.confirm_send)).setCancelIconIsVisibility(true)
                .setOnConfirmListener(dialog -> {
                    dialog.dismiss();
                    if (canSendMsg()) {
                        sendMsgRequest(message, sessionId);
                    }
                }).setOnCancelListener(dialog -> dialog.dismiss()).create().show();
    }

    public void showSendGiftEvent() {
        // 初始化底部面板
        giftIcon.setVisibility(View.VISIBLE);
        ObjectAnimator trans = ObjectAnimator.ofFloat(giftIcon, "translationY", SizeUtils.dp2px(-5F),
                SizeUtils.dp2px(0F), SizeUtils.dp2px(-5F));
        trans.setDuration(800);
        trans.setRepeatCount(-1);
        trans.setInterpolator(new LinearInterpolator());
        trans.start();
        giftIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                giftIcon.clearAnimation();
                giftIcon.setVisibility(View.GONE);
            }
        });

    }

}
