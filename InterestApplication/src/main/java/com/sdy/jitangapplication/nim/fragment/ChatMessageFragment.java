package com.sdy.jitangapplication.nim.fragment;

import android.app.Dialog;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.kotlin.base.data.net.RetrofitFactory;
import com.kotlin.base.data.protocol.BaseResp;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.api.UIKitOptions;
import com.netease.nim.uikit.api.model.main.CustomPushContentProvider;
import com.netease.nim.uikit.api.model.session.SessionCustomization;
import com.netease.nim.uikit.business.ait.AitManager;
import com.netease.nim.uikit.business.session.constant.Extras;
import com.netease.nim.uikit.business.session.module.Container;
import com.netease.nim.uikit.business.session.module.ModuleProxy;
import com.netease.nim.uikit.common.CommonUtil;
import com.netease.nim.uikit.common.fragment.TFragment;
import com.netease.nim.uikit.impl.NimUIKitImpl;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.friend.FriendService;
import com.netease.nimlib.sdk.friend.constant.VerifyType;
import com.netease.nimlib.sdk.friend.model.AddFriendData;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.MsgServiceObserve;
import com.netease.nimlib.sdk.msg.attachment.AudioAttachment;
import com.netease.nimlib.sdk.msg.attachment.ImageAttachment;
import com.netease.nimlib.sdk.msg.attachment.VideoAttachment;
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.CustomMessageConfig;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.MemberPushOption;
import com.netease.nimlib.sdk.msg.model.MessageReceipt;
import com.netease.nimlib.sdk.robot.model.NimRobotInfo;
import com.netease.nimlib.sdk.robot.model.RobotAttachment;
import com.netease.nimlib.sdk.robot.model.RobotMsgType;
import com.netease.nimlib.sdk.uinfo.constant.GenderEnum;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.sdy.jitangapplication.R;
import com.sdy.jitangapplication.api.Api;
import com.sdy.jitangapplication.common.CommonFunction;
import com.sdy.jitangapplication.common.Constants;
import com.sdy.jitangapplication.event.EnablePicEvent;
import com.sdy.jitangapplication.event.NimHeadEvent;
import com.sdy.jitangapplication.event.StarEvent;
import com.sdy.jitangapplication.event.UpdateApproveEvent;
import com.sdy.jitangapplication.event.UpdateContactBookEvent;
import com.sdy.jitangapplication.event.UpdateHiEvent;
import com.sdy.jitangapplication.event.UpdateSendGiftEvent;
import com.sdy.jitangapplication.model.ApproveBean;
import com.sdy.jitangapplication.model.NimBean;
import com.sdy.jitangapplication.model.ResidueCountBean;
import com.sdy.jitangapplication.model.SendTipBean;
import com.sdy.jitangapplication.nim.attachment.ChatHiAttachment;
import com.sdy.jitangapplication.nim.attachment.SendCustomTipAttachment;
import com.sdy.jitangapplication.nim.extension.ChatMessageListPanelEx;
import com.sdy.jitangapplication.nim.panel.ChatInputPanel;
import com.sdy.jitangapplication.nim.session.ChatBaseAction;
import com.sdy.jitangapplication.nim.session.ChatChooseGiftAction;
import com.sdy.jitangapplication.nim.session.ChatPickImageAction;
import com.sdy.jitangapplication.nim.session.ChatTakeImageAction;
import com.sdy.jitangapplication.nim.session.MyLocationAction;
import com.sdy.jitangapplication.ui.dialog.AlertCandyEnoughDialog;
import com.sdy.jitangapplication.ui.dialog.HelpWishReceiveDialog;
import com.sdy.jitangapplication.ui.dialog.LoadingDialog;
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

    protected AitManager aitManager;


    private View rootView;
    private TextView btnMakeFriends;
    private LinearLayout messageActivityBottomLayout;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        parseIntent();
        EventBus.getDefault().register(this);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.chat_nim_message_fragment, container, false);
        btnMakeFriends = rootView.findViewById(R.id.btnMakeFriends);
        messageActivityBottomLayout = rootView.findViewById(R.id.messageActivityBottomLayout);

        //发送消息成为好友
        btnMakeFriends.setOnClickListener(view -> {
            HashMap<String, Object> params = UserManager.INSTANCE.getBaseParams();
            params.put("target_accid", sessionId);
            RetrofitFactory.Companion.getInstance().create(Api.class)
                    .addFriend(UserManager.INSTANCE.getSignParams(params))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new rx.Observer<BaseResp<Object>>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onNext(BaseResp<Object> objectBaseResp) {
                            if (objectBaseResp.getCode() == 200) {
                                //隐藏倒计时控件
                                btnMakeFriends.setVisibility(View.GONE);
                                //发送通知，可以发所有类型的消息
                                EventBus.getDefault().post(new EnablePicEvent(true));
                                EventBus.getDefault().post(new UpdateContactBookEvent());
                                CommonFunction.INSTANCE.toast(objectBaseResp.getMsg());
                                NIMClient.getService(FriendService.class).addFriend(new AddFriendData(sessionId, VerifyType.DIRECT_ADD));

                                //并且发送成为好友消息，
                                IMMessage message = MessageBuilder.createCustomMessage(sessionId, SessionTypeEnum.P2P, "", new ChatHiAttachment(ChatHiAttachment.CHATHI_RFIEND), new CustomMessageConfig());
                                sendMessage(message);

                            } else {
                                CommonFunction.INSTANCE.toast("添加好友失败哦~");
                            }
                        }
                    });


        });


        return rootView;
    }

    /**
     * ***************************** life cycle *******************************
     */

    @Override
    public void onPause() {
        super.onPause();
        NIMClient.getService(MsgService.class).setChattingAccount(MsgService.MSG_CHATTING_ACCOUNT_NONE, SessionTypeEnum.None);
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
            btnMakeFriends.setVisibility(View.GONE);
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
        if (aitManager != null) {
            aitManager.reset();
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

//        initAitManager();

        inputPanel.switchRobotMode(NimUIKitImpl.getRobotInfoProvider().getRobotByAccount(sessionId) != null);

        registerObservers(true);

        if (customization != null) {
            messageListPanel.setChattingBackground(customization.backgroundUri, customization.backgroundColor);
        }


        //如果没有显示过礼物规则提醒
        if (!UserManager.INSTANCE.isShowGuideGiftProtocol() && !sessionId.equals(Constants.ASSISTANT_ACCID)) {
            Dialog dialog = new Dialog(getActivity(), R.style.MyDialog);
            View guideGift = LayoutInflater.from(getActivity()).inflate(R.layout.popupwindow_guide_gift, null);
            guideGift.setOnClickListener(v -> dialog.dismiss());
            dialog.setContentView(guideGift);
            Window window = dialog.getWindow();
            window.setGravity(Gravity.BOTTOM | Gravity.RIGHT);
            WindowManager.LayoutParams attrs = window.getAttributes();
            attrs.width = WindowManager.LayoutParams.MATCH_PARENT;
            attrs.height = WindowManager.LayoutParams.MATCH_PARENT;
            window.setAttributes(attrs);
            dialog.show();
            dialog.setCanceledOnTouchOutside(true);
            dialog.setCancelable(true);
            dialog.setOnDismissListener(dialog1 -> UserManager.INSTANCE.saveShowGuideGiftProtocol(true));
        }
    }


    private void initAitManager() {
        UIKitOptions options = NimUIKitImpl.getOptions();
        if (options.aitEnable) {
            aitManager = new AitManager(getContext(), options.aitTeamMember && sessionType == SessionTypeEnum.Team ? sessionId : null, options.aitIMRobot);
            inputPanel.addAitTextWatcher(aitManager);
            aitManager.setTextChangeListener(inputPanel);
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void updateApproveEvent(UpdateApproveEvent event) {
        getTargetInfo(sessionId);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateApproveEvent(UpdateSendGiftEvent event) {
        messageListPanel.onMsgSend(event.getMessage());
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

        Log.d("message", messages.get(0).toString());
        try {
            if (!sessionId.equals(Constants.ASSISTANT_ACCID)) {
                getTargetInfo(sessionId);
            } else {
                messageActivityBottomLayout.setVisibility(View.VISIBLE);
            }

            if (CommonUtil.isEmpty(messages)) {
                return;
            }
            //新消息来了更新消息状态
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
            //收到已读回执
            messageListPanel.receiveReceipt();
            //收到已读回执,调用接口,改变此时招呼或者消息的状态
            Log.d("已读回执----", "对方已读你的消息，10分钟内对方未回复消息将过期");
            if (!sessionId.equals(Constants.ASSISTANT_ACCID)) {
                getTargetInfo(sessionId);
            }
        }
    };


    /**
     * ********************** implements ModuleProxy *********************
     */

    @Override
    public boolean sendMessage(IMMessage message) {
        Log.d("sendMessage", ".....");
        if (!sessionId.equals(Constants.ASSISTANT_ACCID)
                && !nimBean.is_send_msg()
                && UserManager.INSTANCE.getGender() == 1
                && ((NimUserInfo) NimUIKit.getUserInfoProvider().getUserInfo(sessionId)).getGenderEnum() == GenderEnum.FEMALE) {
            showConfirmSendDialog(message);
        } else {
            if (sendAlready3Msgs())
                if (sessionId.equals(Constants.ASSISTANT_ACCID))
                    sendMsgS(message, false);
                else {
                    if (message.getMsgType() == MsgTypeEnum.text) {
                        sendMsgRequest(message, sessionId);
                    } else {
                        sendMsgS(message, true);
                    }
                }
        }
        return false;
    }

    private Boolean sendAlready3Msgs() {
        //发起方并且次数为0 禁止发送
        if (!sessionId.equals(Constants.ASSISTANT_ACCID) && !nimBean.getIsfriend() && nimBean.getIslimit() && leftGreetCount == 0) {
            if (!sendTip) {
                inputPanel.collapse(true);
                sendTipMessage("你已向对方打了招呼，对方回复后即可继续聊天", SendCustomTipAttachment.CUSTOME_TIP_NORMAL, true);
                sendTip = true;
            }
            return false;
        } else
            return true;
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

    private void appendTeamMemberPush(IMMessage message) {
        if (aitManager == null) {
            return;
        }
        if (sessionType == SessionTypeEnum.Team) {
            List<String> pushList = aitManager.getAitTeamMember();
            if (pushList == null || pushList.isEmpty()) {
                return;
            }
            MemberPushOption memberPushOption = new MemberPushOption();
            memberPushOption.setForcePush(true);
            memberPushOption.setForcePushContent(message.getContent());
            memberPushOption.setForcePushList(pushList);
            message.setMemberPushOption(memberPushOption);
        }
    }

    private IMMessage changeToRobotMsg(IMMessage message) {
        if (aitManager == null) {
            return message;
        }
        if (message.getMsgType() == MsgTypeEnum.robot) {
            return message;
        }
        if (isChatWithRobot()) {
            if (message.getMsgType() == MsgTypeEnum.text && message.getContent() != null) {
                String content = message.getContent().equals("") ? " " : message.getContent();
                message = MessageBuilder.createRobotMessage(message.getSessionId(), message.getSessionType(), message.getSessionId(), content, RobotMsgType.TEXT, content, null, null);
            }
        } else {
            String robotAccount = aitManager.getAitRobot();
            if (TextUtils.isEmpty(robotAccount)) {
                return message;
            }
            String text = message.getContent();
            String content = aitManager.removeRobotAitString(text, robotAccount);
            content = content.equals("") ? " " : content;
            message = MessageBuilder.createRobotMessage(message.getSessionId(), message.getSessionType(), robotAccount, text, RobotMsgType.TEXT, content, null, null);

        }
        return message;
    }

    private boolean isChatWithRobot() {
        return NimUIKitImpl.getRobotInfoProvider().getRobotByAccount(sessionId) != null;
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

    //禁止消息长按操作
    @Override
    public boolean isLongClickEnabled() {
        return !inputPanel.isRecording();
    }

    @Override
    public void onItemFooterClick(IMMessage message) {
        if (aitManager == null) {
            return;
        }
        if (messageListPanel.isSessionMode()) {
            RobotAttachment attachment = (RobotAttachment) message.getAttachment();
            NimRobotInfo robot = NimUIKitImpl.getRobotInfoProvider().getRobotByAccount(attachment.getFromRobotAccount());
            aitManager.insertAitRobot(robot.getAccount(), robot.getName(), inputPanel.getEditSelectionStart());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (aitManager != null) {
            aitManager.onActivityResult(requestCode, resultCode, data);
        }
        inputPanel.onActivityResult(requestCode, resultCode, data);
        messageListPanel.onActivityResult(requestCode, resultCode, data);
    }

    // 操作面板集合
    protected List<ChatBaseAction> getActionList() {
        List<ChatBaseAction> actions = new ArrayList<>();
//        actions.add(new EmojAction());//表情
//        actions.add(new RecordAction());//录音
        actions.add(new ChatPickImageAction()); //图片
        actions.add(new ChatTakeImageAction()); //拍照
        actions.add(new MyLocationAction()); //位置
        actions.add(new ChatChooseGiftAction()); //礼物
//        actions.add(new PhoneCallAction()); //语音通话

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
        RetrofitFactory.Companion.getInstance()
                .create(Api.class)
                .getTargetInfoCandy(UserManager.INSTANCE.getSignParams(params))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new rx.Observer<BaseResp<NimBean>>() {
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
                            new CommonAlertDialog.Builder(getActivity())
                                    .setTitle("提示")
                                    .setContent(nimBeanBaseResp.getMsg())
                                    .setCancelIconIsVisibility(false)
                                    .setConfirmText("知道了")
                                    .setCancelAble(false)
                                    .setOnConfirmListener(dialog -> {
                                        dialog.cancel();
                                        NIMClient.getService(MsgService.class).deleteRecentContact2(sessionId, SessionTypeEnum.P2P);
                                        getActivity().finish();
                                    })
                                    .create()
                                    .show();
                        }
                    }
                });
    }

    /**
     * 设置消息体制内的数据
     */
    private void setTargetInfoData() {
        UserManager.INSTANCE.setApproveBean(new ApproveBean(nimBean.getApprove_time(), nimBean.getIsapprove(), nimBean.getIssend()));
        leftGreetCount = nimBean.getResidue_msg_cnt();
        if (nimBean.getIsfriend()) { //是好友了，按钮消失
            //隐藏倒计时控件
            btnMakeFriends.setVisibility(View.GONE);
            messageActivityBottomLayout.setVisibility(View.VISIBLE);
            //发送通知，可以发所有类型的消息
            EventBus.getDefault().post(new EnablePicEvent(true));
        } else {
            messageActivityBottomLayout.setVisibility(View.VISIBLE);
            if (nimBean.getIsinitiated()) {//非好友并且是自己发起的招呼,按钮消失
                btnMakeFriends.setVisibility(View.GONE);
            } else {//非好友并且是别人发起的招呼,按钮显示
                btnMakeFriends.setVisibility(View.VISIBLE);
            }
        }
        if (!nimBean.getIsfriend())
            EventBus.getDefault().post(new UpdateHiEvent());
        EventBus.getDefault().post(new NimHeadEvent(nimBean));
        EventBus.getDefault().postSticky(new StarEvent(nimBean.getStared(), nimBean.getIsfriend()));
        EventBus.getDefault().postSticky(new EnablePicEvent(nimBean.getIsfriend()));
        messageListPanel.refreshMessageList();
        inputPanel.checkIsSendMsg();
    }

    private int leftGreetCount = 0;
    private boolean sendTip = false;


    private void sendMsgRequest(IMMessage content, String target_accid) {
        HashMap<String, Object> params = UserManager.INSTANCE.getBaseParams();
        params.put("target_accid", target_accid);
        if (content.getMsgType() == MsgTypeEnum.text) {
            params.put("content", content.getContent());
        } else if (content.getMsgType() == MsgTypeEnum.image) {
            params.put("content", ((ImageAttachment) content.getAttachment()).getUrl());
        } else if (content.getMsgType() == MsgTypeEnum.audio) {
            params.put("content", ((AudioAttachment) content.getAttachment()).getUrl());
        } else if (content.getMsgType() == MsgTypeEnum.video) {
            params.put("content", ((VideoAttachment) content.getAttachment()).getUrl());
        }
        params.put("type", content.getMsgType().getValue());
        RetrofitFactory.Companion.getInstance().create(Api.class)
                .sendMsgRequest(UserManager.INSTANCE.getSignParams(params))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new rx.Observer<BaseResp<ResidueCountBean>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(BaseResp<ResidueCountBean> nimBeanBaseResp) {
                        if (nimBeanBaseResp.getCode() == 200 || nimBeanBaseResp.getCode() == 211) {
                            inputPanel.restoreText(true);
                            //如果糖果助力值大于0则证明是回复糖果助力消息，弹助力领取成功弹窗
                            if (nimBeanBaseResp.getData().getGet_help_amount() > 0) {
                                new HelpWishReceiveDialog(nimBeanBaseResp.getData().getGet_help_amount(), getActivity()).show();
                            }

                            leftGreetCount = nimBeanBaseResp.getData().getResidue_msg_cnt();

                            if (content.getMsgType() == MsgTypeEnum.text)
                                sendMsgS(content, false);
                            if (!nimBeanBaseResp.getData().getRet_tips_arr().isEmpty())
                                for (SendTipBean bean : nimBeanBaseResp.getData().getRet_tips_arr())
                                    sendTipMessage(bean.getContent(), bean.getShowType(), bean.getIfSendUserShow());

                            nimBean.setIssended(true);
                            nimBean.set_send_msg(true);
                        } else if (nimBeanBaseResp.getCode() == 409) {//用户被封禁
                            new CommonAlertDialog.Builder(getActivity())
                                    .setTitle("提示")
                                    .setContent(nimBeanBaseResp.getMsg())
                                    .setCancelIconIsVisibility(false)
                                    .setConfirmText("知道了")
                                    .setCancelAble(false)
                                    .setOnConfirmListener(dialog -> {
                                        dialog.cancel();
                                        NIMClient.getService(MsgService.class).deleteRecentContact2(sessionId, SessionTypeEnum.P2P);
                                        getActivity().finish();
                                    })
                                    .create()
                                    .show();
                        } else if (nimBeanBaseResp.getCode() == 410) {
                            sendAlready3Msgs();
                        } else if (nimBeanBaseResp.getCode() == 411) {//糖果余额不足
                            new AlertCandyEnoughDialog(getActivity(), AlertCandyEnoughDialog.Companion.getFROM_SEND_GIFT()).show();
                        } else {
                            CommonFunction.INSTANCE.toast(nimBeanBaseResp.getMsg());
                        }
                    }

                });
    }


    private void aideSendMsg(IMMessage content) {
        HashMap<String, Object> params = UserManager.INSTANCE.getBaseParams();
        if (content.getMsgType() == MsgTypeEnum.text) {
            params.put("content", content.getContent());
        }
        RetrofitFactory.Companion.getInstance().create(Api.class)
                .aideSendMsg(UserManager.INSTANCE.getSignParams(params))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new rx.Observer<BaseResp<ResidueCountBean>>() {
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


    private void sendTipMessage(String msg, int type, boolean ifSendUserShow) {
        // 同时，本地插入被对方拒收的tip消息
        SendCustomTipAttachment attachment = new SendCustomTipAttachment(msg, type, ifSendUserShow);
        IMMessage tip = MessageBuilder.createCustomMessage(sessionId, SessionTypeEnum.P2P, attachment);
        CustomMessageConfig config = new CustomMessageConfig();
        config.enableUnreadCount = false;
        config.enablePush = false;
        tip.setConfig(config);
        sendMsgS(tip, false);
    }


    private void sendMsgS(IMMessage content, boolean requestMsg) {
        appendPushConfig(content);
        // send message to server and save to db
        NIMClient.getService(MsgService.class).sendMessage(content, false).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                if (requestMsg) {
                    sendMsgRequest(content, sessionId);
                }
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
        if (!sessionId.equals(Constants.ASSISTANT_ACCID) && nimBean != null && !nimBean.getIsfriend()) {
            if (nimBean.getIsgreet() && !nimBean.getIsinitiated()) {
                messageActivityBottomLayout.setVisibility(View.VISIBLE);
                EventBus.getDefault().post(new NimHeadEvent(nimBean));
            }
        }
    }


    private void showConfirmSendDialog(final IMMessage message) {
        new CommonAlertDialog.Builder(getActivity())
                .setContent("为提高男性用户质量，将交友机会留给诚意用户。每次发送消息会消耗一个糖果")
                .setTitle("发送消息")
                .setCancelText("取消")
                .setConfirmText("确认发送")
                .setCancelIconIsVisibility(true)
                .setOnConfirmListener(dialog -> {
                    dialog.dismiss();
                    if (sendAlready3Msgs())
                        if (sessionId.equals(Constants.ASSISTANT_ACCID))
                            sendMsgS(message, false);
                        else {
                            if (message.getMsgType() == MsgTypeEnum.text) {
                                sendMsgRequest(message, sessionId);
                            } else {
                                sendMsgS(message, true);
                            }
                        }
                })
                .setOnCancelListener(dialog -> dialog.dismiss())
                .create()
                .show();
    }

}
