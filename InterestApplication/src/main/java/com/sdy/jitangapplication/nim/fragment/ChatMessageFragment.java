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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.kotlin.base.data.net.RetrofitFactory;
import com.kotlin.base.data.protocol.BaseResp;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.MsgServiceObserve;
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.CustomMessageConfig;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.MessageReceipt;
import com.sdy.jitangapplication.R;
import com.sdy.jitangapplication.api.Api;
import com.sdy.jitangapplication.common.CommonFunction;
import com.sdy.jitangapplication.common.Constants;
import com.sdy.jitangapplication.event.HideContactLlEvent;
import com.sdy.jitangapplication.event.NimHeadEvent;
import com.sdy.jitangapplication.event.StarEvent;
import com.sdy.jitangapplication.event.UpdateApproveEvent;
import com.sdy.jitangapplication.event.UpdateSendGiftEvent;
import com.sdy.jitangapplication.model.ChatUpBean;
import com.sdy.jitangapplication.model.NimBean;
import com.sdy.jitangapplication.model.ResidueCountBean;
import com.sdy.jitangapplication.model.SendTipBean;
import com.sdy.jitangapplication.nim.attachment.SendCustomTipAttachment;
import com.sdy.jitangapplication.nim.extension.ChatMessageListPanelEx;
import com.sdy.jitangapplication.nim.panel.ChatInputPanel;
import com.sdy.jitangapplication.nim.session.ChatBaseAction;
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
import com.sdy.jitangapplication.ui.dialog.ChatUpOpenPtVipDialog;
import com.sdy.jitangapplication.ui.dialog.ContactCandyReceiveDialog;
import com.sdy.jitangapplication.ui.dialog.HelpWishReceiveDialog;
import com.sdy.jitangapplication.ui.dialog.LoadingDialog;
import com.sdy.jitangapplication.ui.dialog.OpenVipActivity;
import com.sdy.jitangapplication.ui.dialog.ReceiveAccostGiftDialog;
import com.sdy.jitangapplication.ui.dialog.VerifyAddChatDialog;
import com.sdy.jitangapplication.ui.dialog.VideoAddChatTimeDialog;
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
    private ImageView contactIv, closeContactBtn;

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

        // 去认证
        gotoVerifyBtn.setOnClickListener(v -> {
            if (!nimBean.getMy_isfaced())
                CommonFunction.INSTANCE.startToFace(getActivity(), IDVerifyActivity.TYPE_ACCOUNT_NORMAL, -1);
            else if (nimBean.getMv_state() == 0)
                CommonFunction.INSTANCE.startToVideoIntroduce(getActivity(), -1);
        });

        // 糖果门槛消费聊天
        unlockChatLl.setOnClickListener(v -> {
            new ChatUpOpenPtVipDialog(getActivity(), sessionId, ChatUpOpenPtVipDialog.TYPE_LOCK_CHATUP,
                    new ChatUpBean(nimBean.getChatup_amount(), nimBean.getPlat_cnt(), false, 0, "", 0,
                            nimBean.getAvatar(), nimBean.getIsplatinum()),
                    "").show();
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

        // 如果没有显示过礼物规则提醒
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

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void updateApproveEvent(UpdateApproveEvent event) {
        getTargetInfo(sessionId);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateApproveEvent(UpdateSendGiftEvent event) {
        messageListPanel.onMsgSend(event.getMessage());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void hideContactLlEvent(HideContactLlEvent event) {
        unlockContactLl.setVisibility(View.GONE);
        nimBean.set_unlock_contact(true);

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
            Log.d("已读回执----", "对方已读你的消息，10分钟内对方未回复消息将过期");
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
                OpenVipActivity.Companion.start(getActivity(), null, OpenVipActivity.FROM_P2P_CHAT, -1);
            } else {
                // if (message.getMsgType() == MsgTypeEnum.text) {
                sendMsgRequest(message, sessionId);
                // } else {
                // sendMsgS(message, true);
                // }
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
//        actions.add(new ChatChooseGiftAction()); // 礼物
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
                            new CommonAlertDialog.Builder(getActivity()).setTitle("提示")
                                    .setContent(nimBeanBaseResp.getMsg()).setCancelIconIsVisibility(false)
                                    .setConfirmText("知道了").setCancelAble(false).setOnConfirmListener(dialog -> {
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
    private void setTargetInfoData() {
        // todo 获取服务器展示糖果的时间
        UserManager.INSTANCE.setShowCandyMessage(nimBean.getChat_expend_amount() > 0);
        UserManager.INSTANCE.setShowCandyTime(nimBean.getChat_expend_time());
        // UserManager.INSTANCE.setApproveBean(new ApproveBean(nimBean.getApprove_time(), nimBean.getIsapprove(),
        // nimBean.getIssend()));
        // 显示提示认证的悬浮
        if (nimBean.getMy_gender() == 2 && (!nimBean.getMy_isfaced() || nimBean.getMv_state() == 0)) {
            verifyLl.setVisibility(View.VISIBLE);
            if (!nimBean.getMy_isfaced()) {
                gotoVerifyBtn.setText("立即认证");
                if (nimBean.getResidue_msg_cnt() == nimBean.getNormal_chat_times()) {
                    leftChatTimes.setText("未认证每天仅能和" + nimBean.getNormal_chat_times() + "个用户聊天");
                } else {
                    leftChatTimes.setText("还有" + nimBean.getResidue_msg_cnt() + "次聊天机会，认证后增加");
                }
            } else {
                gotoVerifyBtn.setText("视频介绍");
                leftChatTimes.setText("今日还有" + nimBean.getResidue_msg_cnt() + "次聊天机会，追加视频聊天不设限");
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
    }

    private void sendMsgRequest(IMMessage content, String target_accid) {
        HashMap<String, Object> params = UserManager.INSTANCE.getBaseParams();
        params.put("target_accid", target_accid);
        if (content.getMsgType() == MsgTypeEnum.text) {
            params.put("content", content.getContent());
        }
        // else if (content.getMsgType() == MsgTypeEnum.image) {
        // params.put("content", ((ImageAttachment) content.getAttachment()).getUrl());
        // } else if (content.getMsgType() == MsgTypeEnum.audio) {
        // params.put("content", ((AudioAttachment) content.getAttachment()).getUrl());
        // } else if (content.getMsgType() == MsgTypeEnum.video) {
        // params.put("content", ((VideoAttachment) content.getAttachment()).getUrl());
        // }
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
                    }

                    @Override
                    public void onNext(BaseResp<ResidueCountBean> nimBeanBaseResp) {
                        if (nimBeanBaseResp.getCode() == 200 || nimBeanBaseResp.getCode() == 211) {
                            inputPanel.restoreText(true);
                            // 如果糖果助力值大于0则证明是回复糖果助力消息，弹助力领取成功弹窗
                            if (nimBeanBaseResp.getData().getGet_help_amount() > 0) {
                                new HelpWishReceiveDialog(nimBeanBaseResp.getData().getGet_help_amount(), getActivity())
                                        .show();
                            }
                            // 搭讪礼物如果返回不为空，就代表成功领取对方的搭讪礼物
                            if (nimBeanBaseResp.getData().getRid_data() != null
                                    && !nimBeanBaseResp.getData().getRid_data().getIcon().isEmpty()) {
                                new ReceiveAccostGiftDialog(getActivity(), nimBeanBaseResp.getData().getRid_data())
                                        .show();
                            }
                            // if (content.getMsgType() == MsgTypeEnum.text)
                            sendMsgS(content, false);
                            if (!nimBeanBaseResp.getData().getRet_tips_arr().isEmpty())
                                for (SendTipBean bean : nimBeanBaseResp.getData().getRet_tips_arr())
                                    sendTipMessage(bean.getContent(), bean.getShowType(), bean.getIfSendUserShow());

                            nimBean.set_send_msg(true);

                        } else if (nimBeanBaseResp.getCode() == 409) {// 用户被封禁
                            new CommonAlertDialog.Builder(getActivity()).setTitle("提示")
                                    .setContent(nimBeanBaseResp.getMsg()).setCancelIconIsVisibility(false)
                                    .setConfirmText("知道了").setCancelAble(false).setOnConfirmListener(dialog -> {
                                        dialog.cancel();
                                        NIMClient.getService(MsgService.class).deleteRecentContact2(sessionId,
                                                SessionTypeEnum.P2P);
                                        getActivity().finish();
                                    }).create().show();
                        } else if (nimBeanBaseResp.getCode() == 411) {// 糖果余额不足
                            new AlertCandyEnoughDialog(getActivity(),
                                    AlertCandyEnoughDialog.Companion.getFROM_SEND_GIFT()).show();
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
        new CommonAlertDialog.Builder(getActivity()).setContent("为提高男性用户质量，将交友机会留给诚意用户。每次发送消息会消耗一个糖果").setTitle("发送消息")
                .setCancelText("取消").setConfirmText("确认发送").setCancelIconIsVisibility(true)
                .setOnConfirmListener(dialog -> {
                    dialog.dismiss();
                    if (canSendMsg()) {
                        // if (message.getMsgType() == MsgTypeEnum.text) {
                        sendMsgRequest(message, sessionId);
                        // } else {
                        // sendMsgS(message, true);
                        // }
                    }
                }).setOnCancelListener(dialog -> dialog.dismiss()).create().show();
    }

}
