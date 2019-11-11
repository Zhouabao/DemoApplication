package com.sdy.jitangapplication.nim.fragment;

import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.blankj.utilcode.util.TimeUtils;
import com.kotlin.base.data.net.RetrofitFactory;
import com.kotlin.base.data.protocol.BaseResp;
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
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.MsgServiceObserve;
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
import com.sdy.jitangapplication.R;
import com.sdy.jitangapplication.api.Api;
import com.sdy.jitangapplication.common.CommonFunction;
import com.sdy.jitangapplication.common.Constants;
import com.sdy.jitangapplication.event.*;
import com.sdy.jitangapplication.model.NimBean;
import com.sdy.jitangapplication.nim.attachment.ChatHiAttachment;
import com.sdy.jitangapplication.nim.extension.ChatMessageListPanelEx;
import com.sdy.jitangapplication.nim.panel.ChatInputPanel;
import com.sdy.jitangapplication.nim.session.*;
import com.sdy.jitangapplication.utils.UserManager;
import com.sdy.jitangapplication.widgets.TimeRunTextView;
import org.greenrobot.eventbus.EventBus;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 聊天界面基类
 * <p/>
 * Created by huangjun on 2015/2/1.
 */
public class ChatMessageFragment extends TFragment implements ModuleProxy {
    /**
     * 时间倒计时
     * 招呼头布局
     *
     * @param event
     */
    private int time = 0;
    private boolean pause = false;
    private CountDownTimer countDownTimer;

    public NimBean nimBean = null;


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
    private TimeRunTextView outdateTimeText;
    private ProgressBar outdateTime;
    private LinearLayout messageActivityBottomLayout;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        parseIntent();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.chat_nim_message_fragment, container, false);
        btnMakeFriends = rootView.findViewById(R.id.btnMakeFriends);
        outdateTimeText = rootView.findViewById(R.id.outdateTimeText);
        outdateTime = rootView.findViewById(R.id.outdateTime);
        messageActivityBottomLayout = rootView.findViewById(R.id.messageActivityBottomLayout);


        //发送消息成为好友
        btnMakeFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                                    outdateTimeText.stopTime();
                                    outdateTimeText.setVisibility(View.GONE);
                                    outdateTime.setVisibility(View.GONE);
                                    //发送通知，可以发所有类型的消息
                                    EventBus.getDefault().post(new EnablePicEvent(true));
                                    EventBus.getDefault().post(new UpdateContactBookEvent());
                                    CommonFunction.INSTANCE.toast(objectBaseResp.getMsg());

                                    //并且发送成为好友消息，
                                    IMMessage message = MessageBuilder.createCustomMessage(sessionId, SessionTypeEnum.P2P, "", new ChatHiAttachment(null, ChatHiAttachment.CHATHI_RFIEND), new CustomMessageConfig());
                                    sendMessage(message);

                                } else {
                                    CommonFunction.INSTANCE.toast("添加好友失败哦~");
                                }
                            }
                        });


            }
        });
        return rootView;
    }

    /**
     * ***************************** life cycle *******************************
     */

    @Override
    public void onPause() {
        super.onPause();
        pause = true;
        NIMClient.getService(MsgService.class).setChattingAccount(MsgService.MSG_CHATTING_ACCOUNT_NONE, SessionTypeEnum.None);
        inputPanel.onPause();
        messageListPanel.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        pause = false;
        inputPanel.onResume();
        messageListPanel.onResume();
        if (!sessionId.equals(Constants.ASSISTANT_ACCID)) {
            getTargetInfo(sessionId);
        } else {
            messageActivityBottomLayout.setVisibility(View.VISIBLE);
        }
        NIMClient.getService(MsgService.class).setChattingAccount(sessionId, sessionType);
        getActivity().setVolumeControlStream(AudioManager.STREAM_VOICE_CALL); // 默认使用听筒播放
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        messageListPanel.onDestroy();
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
//             btnMakeFriends = rootView.findViewById(R.id.btnMakeFriends);
//        outdateTimeText = rootView.findViewById(R.id.outdateTimeText);
//        outdateTime = rootView.findViewById(R.id.outdateTime);
//        messageActivityBottomLayout = rootView.findViewById(R.id.messageActivityBottomLayout);
            messageListPanel = new ChatMessageListPanelEx(container, rootView, anchor, false, false);
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


    }


    private void initAitManager() {
        UIKitOptions options = NimUIKitImpl.getOptions();
        if (options.aitEnable) {
            aitManager = new AitManager(getContext(), options.aitTeamMember && sessionType == SessionTypeEnum.Team ? sessionId : null, options.aitIMRobot);
            inputPanel.addAitTextWatcher(aitManager);
            aitManager.setTextChangeListener(inputPanel);
        }
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
            if (CommonUtil.isEmpty(messages)) {
                return;
            }
            //新消息来了更新消息状态
            messageListPanel.onIncomingMessage(messages);

            //新消息来了请求接口，更新键盘啊头布局等数据。
            if (sessionId.equals(Constants.ASSISTANT_ACCID)) {
                messageActivityBottomLayout.setVisibility(View.VISIBLE);
            } else if (nimBean == null || !nimBean.getIsfriend()) {
                getTargetInfo(sessionId);
            }

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

            if (!sessionId.equals(Constants.ASSISTANT_ACCID) && (nimBean == null || !nimBean.getIsfriend())) {
                getTargetInfo(sessionId);
            }
        }
    };


    /**
     * ********************** implements ModuleProxy *********************
     */
    //判断当前招呼中是否有自己的消息
    private boolean hasMineMsg = false;

    @Override
    public boolean sendMessage(IMMessage message) {
        if (isAllowSendMessage(message)) {
            appendPushConfig(message);
            // send message to server and save to db
            NIMClient.getService(MsgService.class).sendMessage(message, false);
            messageListPanel.onMsgSend(message);
            //var type: Int = 0,//类型1，新消息 2，倒计时 3，普通样式 4 过期
            if (!sessionId.equals(Constants.ASSISTANT_ACCID) && nimBean != null && !nimBean.getIsfriend()) {
                if (nimBean.getType() == 2 && !nimBean.getIsinitiated()) {
                    nimBean.setType(3);
                    outdateTime.setVisibility(View.GONE);
                    outdateTimeText.stopTime();
                    outdateTimeText.setVisibility(View.GONE);
                    messageActivityBottomLayout.setVisibility(View.VISIBLE);
                    EventBus.getDefault().post(new NimHeadEvent(nimBean));


                    //在没有发过6的前提下
                    if (!UserManager.INSTANCE.getSecondReply()) {
                        //在没有发过6的前提下，循环会话列表是否有自己的会话
                        checkHasMine();
                        //第二轮回复提示，  判断当前消息中有没有自己回复过的，并且发过5
                        if (!nimBean.getIsinitiated() && hasMineMsg && UserManager.INSTANCE.getStopTime()) {
                            IMMessage tipMessage = MessageBuilder.createTipMessage(sessionId, sessionType);
                            tipMessage.setContent(getActivity().getResources().getString(R.string.make_friend_tip));
                            tipMessage.setStatus(MsgStatusEnum.success);
                            CustomMessageConfig config = new CustomMessageConfig();
                            config.enablePush = false;//不推送
                            config.enableUnreadCount = false;
                            tipMessage.setConfig(config);
                            NIMClient.getService(MsgService.class).saveMessageToLocal(tipMessage, true);
                            UserManager.INSTANCE.saveSecondReply(true);
                            ScaleAnimation scaleAnimation = (ScaleAnimation) AnimationUtils.loadAnimation(getActivity(), R.anim.anim_scale);
                            btnMakeFriends.startAnimation(scaleAnimation);
                        }

                        //发送停止倒数tip  判断当前消息中有没有自己回复过的，并且没发过5
                        if (!nimBean.getIsinitiated() && hasMineMsg && !UserManager.INSTANCE.getStopTime()) {
                            IMMessage tipMessage = MessageBuilder.createTipMessage(sessionId, sessionType);
                            tipMessage.setContent(getActivity().getResources().getString(R.string.stop_count_down_tip));
                            tipMessage.setStatus(MsgStatusEnum.success);
                            CustomMessageConfig config = new CustomMessageConfig();
                            config.enablePush = false;//不推送
                            config.enableUnreadCount = false;
                            tipMessage.setConfig(config);
                            NIMClient.getService(MsgService.class).saveMessageToLocal(tipMessage, true);
                            UserManager.INSTANCE.saveStopTime(true);
                        }
                    }
                }
            }

        } else {
            // 替换成tip
            message = MessageBuilder.createTipMessage(message.getSessionId(), message.getSessionType());
            message.setContent("该消息无法发送");
            message.setStatus(MsgStatusEnum.success);
            NIMClient.getService(MsgService.class).saveMessageToLocal(message, false);
        }

        if (aitManager != null) {
            aitManager.reset();
        }
        return true;
    }

    /**
     * 检查当前会话是否有我的消息
     */
    private void checkHasMine() {
        for (int i = messageListPanel.getItems().size() - 1; i >= 0; i--) {
            IMMessage message = messageListPanel.getItems().get(i);
            if (messageListPanel.isMyMessage(message) && message.getMsgType() != MsgTypeEnum.tip) {
                hasMineMsg = true;
                break;
            }
        }
    }


    /**
     * 检查是否有tip消息
     *
     * @param tip
     */
    private boolean checkHasTip(String tip) {
        for (int i = messageListPanel.getItems().size() - 1; i >= 0; i--) {
            IMMessage message = messageListPanel.getItems().get(i);
            if (messageListPanel.isMyMessage(message) && message.getContent().equals(tip)) {
                return true;
            }
        }

        return false;
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
        actions.add(new EmojAction());//表情
        actions.add(new RecordAction());//录音
        actions.add(new MyLocationAction()); //位置
        actions.add(new PhoneCallAction()); //语音通话
        actions.add(new ChatPickImageAction()); //图片

        return actions;
    }


    /**
     * 进入聊天界面 获取对方用户的个人信息
     * 并且实时更新招呼的状态
     *
     * @param target_accid
     */
    public void getTargetInfo(String target_accid) {
        HashMap<String, Object> params = UserManager.INSTANCE.getBaseParams();
        params.put("target_accid", target_accid);
        RetrofitFactory.Companion.getInstance()
                .create(Api.class)
                .getTargetInfo(UserManager.INSTANCE.getSignParams(params))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new rx.Observer<BaseResp<NimBean>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(BaseResp<NimBean> nimBeanBaseResp) {
                        time = 0;
                        if (nimBeanBaseResp.getCode() == 200 && nimBeanBaseResp.getData() != null) {
                            nimBean = nimBeanBaseResp.getData();
                            if (nimBean.getIsfriend()) { //是好友了，按钮消失
                                //隐藏倒计时控件
                                btnMakeFriends.setVisibility(View.GONE);
                                outdateTimeText.stopTime();
                                outdateTimeText.setVisibility(View.GONE);
                                outdateTime.setVisibility(View.GONE);
                                messageActivityBottomLayout.setVisibility(View.VISIBLE);
                                //发送通知，可以发所有类型的消息
                                EventBus.getDefault().post(new EnablePicEvent(true));
                            } else {

                                //1，新消息 2，倒计时 3，普通样式 4 过期
                                if (nimBean.getType() == 2) { //倒计时消息
                                    messageActivityBottomLayout.setVisibility(View.VISIBLE);
                                    if (nimBean.getIsinitiated()) {//非好友并且是自己发起的招呼,按钮消失
                                        btnMakeFriends.setVisibility(View.GONE);
                                    } else {//非好友并且是别人发起的招呼,按钮显示
                                        btnMakeFriends.setVisibility(View.VISIBLE);
                                    }

                                    if (nimBean.getCountdown() > 0) {
                                        //倒计时进度条
                                        outdateTime.setVisibility(View.VISIBLE);
                                        outdateTimeText.setVisibility(View.VISIBLE);
                                        //文本倒计时
                                        if (!pause) {
                                            outdateTimeText.startTime(nimBean.getCountdown(), "2", "后过期");
                                        }
                                        outdateTime.setMax(nimBean.getCountdown_total());
                                        outdateTime.setProgress(nimBean.getCountdown());
                                        if (countDownTimer != null) {
                                            countDownTimer.cancel();
                                            //防止new出多个导致时间跳动加速
                                            countDownTimer = null;
                                        }
                                        countDownTimer = new CountDownTimer((nimBean.getCountdown()) * 1000, 1000) {

                                            @Override
                                            public void onTick(long l) {
                                                if (!pause) {
                                                    time++;
                                                    outdateTime.setProgress((nimBean.getCountdown() - time));
                                                }
                                            }

                                            @Override
                                            public void onFinish() {
                                                outdateTimeText.setText("消息已于 " + TimeUtils.getNowString() + " 过期");
                                                //过期消息不展示消息面板
                                                inputPanel.collapse(true);
                                                messageActivityBottomLayout.setVisibility(View.GONE);
                                                outdateTime.setProgress(0);
                                                btnMakeFriends.setVisibility(View.GONE);

                                            }
                                        }.start();
                                    }
                                } else if (nimBean.getType() == 4) {//过期消息
                                    //过期消息不展示消息面板
                                    inputPanel.collapse(true);
                                    messageActivityBottomLayout.setVisibility(View.GONE);
                                    btnMakeFriends.setVisibility(View.GONE);
                                    outdateTime.setVisibility(View.VISIBLE);
                                    outdateTime.setProgress(0);
                                    outdateTimeText.setVisibility(View.VISIBLE);
                                    outdateTimeText.stopTime();
                                    if (nimBean.getTimeout_time().isEmpty()) {
                                        outdateTimeText.setText("招呼已过期");
                                    } else {
                                        outdateTimeText.setText("招呼已于 " + nimBean.getTimeout_time() + " 过期");
                                    }
                                } else {
                                    if (nimBean.getIsinitiated()) {//非好友并且是自己发起的招呼,按钮消失
                                        btnMakeFriends.setVisibility(View.GONE);
                                    } else {//非好友并且是别人发起的招呼,按钮显示
                                        btnMakeFriends.setVisibility(View.VISIBLE);
                                    }
                                    outdateTime.setVisibility(View.GONE);
                                    outdateTimeText.stopTime();
                                    outdateTimeText.setVisibility(View.GONE);
                                    messageActivityBottomLayout.setVisibility(View.VISIBLE);
                                }

                            }
                            EventBus.getDefault().post(new UpdateHiEvent());
                            EventBus.getDefault().post(new NimHeadEvent(nimBeanBaseResp.getData()));
                            EventBus.getDefault().postSticky(new StarEvent(nimBeanBaseResp.getData().getStared(), nimBeanBaseResp.getData().getIsfriend()));
                            EventBus.getDefault().postSticky(new EnablePicEvent(nimBeanBaseResp.getData().getIsfriend()));


                            //已读对方的消息  判断有没有发送过二次回复，再判断当前会话里面有没有发送过这个tip，发送过就不再发
                            if (!sessionId.equals(Constants.ASSISTANT_ACCID) && nimBean != null && !nimBean.getIsfriend() && !nimBean.getIsinitiated()
                                    && (!UserManager.INSTANCE.getSecondReply() && !checkHasTip(getActivity().getResources().getString(R.string.has_read_hi_tip)))) {
                                IMMessage tipMessage = MessageBuilder.createTipMessage(sessionId, sessionType);
                                tipMessage.setContent(getActivity().getResources().getString(R.string.has_read_hi_tip));
                                tipMessage.setStatus(MsgStatusEnum.success);
                                CustomMessageConfig config = new CustomMessageConfig();
                                config.enablePush = false;//不推送
                                config.enableUnreadCount = false;
                                tipMessage.setConfig(config);
//                                        container.proxy.sendMessage(tipMessage);
                                NIMClient.getService(MsgService.class).saveMessageToLocal(tipMessage, true);
                                UserManager.INSTANCE.saveReadHe(true);
                            }

                            //对方已读招呼
                            if (!sessionId.equals(Constants.ASSISTANT_ACCID) && nimBean != null && !nimBean.getIsfriend() && nimBean.getIsinitiated() && nimBean.getType() == 2
                                    && nimBean.getIsread() && !UserManager.INSTANCE.getHeRead()) {
                                IMMessage tipMessage = MessageBuilder.createTipMessage(sessionId, sessionType);
                                tipMessage.setContent(getActivity().getResources().getString(R.string.he_has_read_hi_tip));
                                tipMessage.setStatus(MsgStatusEnum.success);
                                CustomMessageConfig config = new CustomMessageConfig();
                                config.enablePush = false;//不推送
                                config.enableUnreadCount = false;
                                tipMessage.setConfig(config);
//                                        container.proxy.sendMessage(tipMessage);
                                NIMClient.getService(MsgService.class).saveMessageToLocal(tipMessage, true);
                                UserManager.INSTANCE.saveHeRead(true);

                            }
                        }
                    }
                });

    }


}
