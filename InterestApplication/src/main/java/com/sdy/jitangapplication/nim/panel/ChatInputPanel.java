package com.sdy.jitangapplication.nim.panel;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.blankj.utilcode.constant.PermissionConstants;
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.api.UIKitOptions;
import com.netease.nim.uikit.api.model.session.SessionCustomization;
import com.netease.nim.uikit.business.ait.AitTextChangeListener;
import com.netease.nim.uikit.business.session.emoji.EmojiManager;
import com.netease.nim.uikit.business.session.emoji.MoonUtil;
import com.netease.nim.uikit.business.session.module.Container;
import com.netease.nim.uikit.common.ToastHelper;
import com.netease.nim.uikit.common.ui.dialog.EasyAlertDialogHelper;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.common.util.string.StringUtil;
import com.netease.nim.uikit.impl.NimUIKitImpl;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.media.record.AudioRecorder;
import com.netease.nimlib.sdk.media.record.IAudioRecordCallback;
import com.netease.nimlib.sdk.media.record.RecordType;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.CustomNotification;
import com.netease.nimlib.sdk.msg.model.CustomNotificationConfig;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.sdy.jitangapplication.R;
import com.sdy.jitangapplication.common.CommonFunction;
import com.sdy.jitangapplication.event.EnablePicEvent;
import com.sdy.jitangapplication.nim.adapter.ChatEmojAdapter;
import com.sdy.jitangapplication.nim.session.ChatBaseAction;
import com.sdy.jitangapplication.ui.dialog.ChatToViplDialog;
import com.sdy.jitangapplication.ui.dialog.HumanVerifyDialog;
import com.sdy.jitangapplication.utils.UserManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.List;

/**
 * 底部文本编辑，语音等模块
 * Created by hzxuwen on 2015/6/16.
 */
public class ChatInputPanel implements IAudioRecordCallback, AitTextChangeListener {

    private static final String TAG = "MsgSendLayout";

    private static final int SHOW_LAYOUT_DELAY = 200;

    protected Container container;
    protected View view;
    protected Handler uiHandler;

    protected View actionPanelBottomLayout; // 更多布局
    protected LinearLayout messageActivityBottomLayout;
    protected EditText messageEditText;// 文本消息编辑框


    //    protected FrameLayout textAudioSwitchLayout; // 切换文本，语音按钮布局
//    protected View switchToTextButtonInInputBar;// 文本消息选择按钮
    protected ImageView switchToAudioButtonInInputBar;// 语音消息选择按钮
    protected ImageView emojiButtonInInputBar;// 表情按钮


    //    protected ImageView sendMessageButtonInInputBar;// 发送消息按钮
//    protected ImageView moreFuntionButtonInInputBar;// 更多消息选择按钮
    protected View messageInputBar, approveView;

    private SessionCustomization customization;

    // 表情
    protected FrameLayout emoticonPickerView;  // 贴图表情控件
    protected RecyclerView emojRv;
    protected TextView sendEmojButton;  // 发送表情
    protected ImageView delEmojButton;  // 表情删除

    // 语音
    protected AudioRecorder audioMessageHelper;
    //    private Chronometer time;
    //    protected Button audioRecordBtn; // 录音按钮
    protected Button audioRecordBtn; // 录音按钮
    protected ConstraintLayout audioAnimLayout; // 录音动画布局
//    protected View audioAnimLayout; // 录音动画布局

    private TextView timerTip;
    private ImageView timerRecordIv;
    private ImageView timerRecordAnimation;
    //    private TextView timerTipContainer;
    private boolean started = false;
    private boolean cancelled = false;
    private boolean touched = false; // 是否按着
    private boolean isKeyboardShowed = true; // 是否显示键盘

    // state
    private boolean actionPanelBottomLayoutHasSetup = false;
    private boolean isTextAudioSwitchShow = true;

    // adapter
    private List<ChatBaseAction> actions;

    // data
    private long typingTime = 0;

    private boolean isRobotSession;

    private TextWatcher aitTextWatcher;
    private ChatActionsGridviewAdapter adapter;

    public ChatInputPanel(Container container, View view, List<ChatBaseAction> actions, boolean isTextAudioSwitchShow) {
        this.container = container;
        this.view = view;
        this.actions = actions;
        this.uiHandler = new Handler();
        this.isTextAudioSwitchShow = isTextAudioSwitchShow;
        EventBus.getDefault().register(this);
        init();
    }

    public ChatInputPanel(Container container, View view, List<ChatBaseAction> actions) {
        this(container, view, actions, false);
    }

    public void onPause() {
        // 停止录音
        if (audioMessageHelper != null) {
            onEndAudioRecord(true);
        }
        collapse(false);
    }


    public void onResume() {
        switchToTextLayout(false);
    }

    /**
     * 判断是否能发送消息
     */
    public void checkIsSendMsg() {
        //0 不验证  1去认证 2去开通会员  3去认证+去会员  4去会员+去认证
        if (UserManager.INSTANCE.getApproveBean() != null && UserManager.INSTANCE.getApproveBean().getIssend() == false) {
            approveView.setVisibility(View.VISIBLE);
            messageEditText.setBackgroundResource(R.drawable.shape_rectangle_gray_24dp);
            messageEditText.setHintTextColor(Color.parseColor("#C0C5CB"));
            approveView.setOnClickListener(v -> {
                if (UserManager.INSTANCE.getApproveBean().getIsapprove() == 2 || UserManager.INSTANCE.getApproveBean().getIsapprove() == 4) {
                    new ChatToViplDialog(container.activity).show();
                } else if (UserManager.INSTANCE.getApproveBean().getIsapprove() == 1 || UserManager.INSTANCE.getApproveBean().getIsapprove() == 3) {
                    new HumanVerifyDialog(container.activity).show();
                }
            });
        } else {
            approveView.setVisibility(View.GONE);
            messageEditText.setBackgroundResource(R.drawable.shape_rectangle_white_24dp);
            messageEditText.setHintTextColor(Color.parseColor("#C9C9C9"));
        }

    }

    public void onDestroy() {
        // release
        if (audioMessageHelper != null) {
            audioMessageHelper.destroyAudioRecorder();
        }
        EventBus.getDefault().unregister(this);

    }

    /**
     * 收起来
     *
     * @param immediately
     * @return
     */
    public boolean collapse(boolean immediately) {
        boolean respond = (emoticonPickerView != null && emoticonPickerView.getVisibility() == View.VISIBLE
                || audioRecordBtn != null && audioRecordBtn.getVisibility() == View.VISIBLE
//                || actionPanelBottomLayout != null && actionPanelBottomLayout.getVisibility() == View.VISIBLE
        );

        hideAllInputLayout(immediately);

        return respond;
    }

    public void addAitTextWatcher(TextWatcher watcher) {
        aitTextWatcher = watcher;
    }

    private void init() {
        initViews();
        for (int i = 0; i < actions.size(); ++i) {
            actions.get(i).setIndex(i);
            actions.get(i).setContainer(container);
        }

        //打开输入框
        showActionPanelLayout();

        initInputBarListener();
        initTextEdit();
        initAudioRecordButton();
        restoreText(false);
        //展示四个那啥
        showActionPanelLayout();
        //初始化表情包
        initEmojViews();

    }


    public void setCustomization(SessionCustomization customization) {
        this.customization = customization;
    }

    public void reload(Container container, SessionCustomization customization) {
        this.container = container;
        setCustomization(customization);
    }

    private void initViews() {
        // input bar
        messageActivityBottomLayout = view.findViewById(R.id.messageActivityBottomLayout);
        messageInputBar = view.findViewById(R.id.textMessageLayout);
//        sendMessageButtonInInputBar = view.findViewById(R.id.buttonSendMessage);
//        moreFuntionButtonInInputBar = view.findViewById(R.id.buttonMoreFuntionInText);
        switchToAudioButtonInInputBar = view.findViewById(R.id.buttonAudioMessage);
        emojiButtonInInputBar = view.findViewById(R.id.emoji_button);
        messageEditText = view.findViewById(R.id.editTextMessage);

        // 语音
        audioRecordBtn = view.findViewById(R.id.recordStateTv);
        audioAnimLayout = view.findViewById(R.id.layoutPlayAudio);
//        time = view.findViewById(R.id.timer);
        timerTip = view.findViewById(R.id.timer_tip);
        timerRecordIv = view.findViewById(R.id.recordIv);
        timerRecordAnimation = view.findViewById(R.id.recordAnima);

        // 表情
        emoticonPickerView = view.findViewById(R.id.emoticon_picker_view1);
        emojRv = view.findViewById(R.id.emojRv);
        //表情发送
        sendEmojButton = view.findViewById(R.id.sendEmojButton);
        //表情删除
        delEmojButton = view.findViewById(R.id.delEmojButton);

        //认证弹窗
        approveView = view.findViewById(R.id.approveView);

    }


    /****************初始化表情控件******************/
    private void initEmojViews() {
        emojRv.setLayoutManager(new GridLayoutManager((container.activity), 8));
        ChatEmojAdapter adapter = new ChatEmojAdapter();
        adapter.addFooterView(inflateFootView());
        emojRv.setAdapter(adapter);
        for (int i = 0; i < EmojiManager.getDisplayCount(); i++) {
            adapter.addData(i);
        }
        adapter.setOnItemClickListener((adapter1, view, position) -> {
            Editable mEditable = messageEditText.getText();
            int start = messageEditText.getSelectionStart();
            int end = messageEditText.getSelectionEnd();
            start = (start < 0 ? 0 : start);
            end = (start < 0 ? 0 : end);
            mEditable.replace(start, end, EmojiManager.getDisplayText(position));
        });

        sendEmojButton.setOnClickListener(v -> {
            //检测是否可以发消息
            if (checkSendButtonEnable(messageEditText)) {
                onTextMessageSendButtonPressed();
            }
        });
        delEmojButton.setOnClickListener(v -> {
            messageEditText.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
        });
    }

    private View inflateFootView() {
        View view = new View(container.activity);
        RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, SizeUtils.dp2px(62F));
        view.setLayoutParams(params);
        view.setBackgroundColor(Color.parseColor("#F4F4F4"));
        return view;
    }

    private void initInputBarListener() {

//        sendMessageButtonInInputBar.setOnClickListener(clickListener);
//        moreFuntionButtonInInputBar.setOnClickListener(clickListener);
        switchToAudioButtonInInputBar.setOnClickListener(clickListener);
        emojiButtonInInputBar.setOnClickListener(clickListener);
    }

    private void initTextEdit() {
//        messageEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        messageEditText.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    switchToTextLayout(true);
                    return true;
                }
                return false;
            }

        });


        messageEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                messageEditText.setHint("");
                checkSendButtonEnable(messageEditText);
            }
        });

        messageEditText.addTextChangedListener(new TextWatcher() {
            private int start;
            private int count;

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                this.start = start;
                this.count = count;
                if (aitTextWatcher != null) {
                    aitTextWatcher.onTextChanged(s, start, before, count);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (aitTextWatcher != null) {
                    aitTextWatcher.beforeTextChanged(s, start, count, after);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                checkSendButtonEnable(messageEditText);
                MoonUtil.replaceEmoticons(container.activity, s, start, count);

                int editEnd = messageEditText.getSelectionEnd();
                messageEditText.removeTextChangedListener(this);
                while (StringUtil.counterChars(s.toString()) > NimUIKitImpl.getOptions().maxInputTextLength && editEnd > 0) {
                    s.delete(editEnd - 1, editEnd);
                    editEnd--;
                }
                messageEditText.setSelection(editEnd);
                messageEditText.addTextChangedListener(this);

                if (aitTextWatcher != null) {
                    aitTextWatcher.afterTextChanged(s);
                }

                sendTypingCommand();
            }
        });


        messageEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    //检测是否可以发消息
                    if (checkSendButtonEnable(messageEditText)) {
                        onTextMessageSendButtonPressed();
//                        messageEditText.setEnabled(false);
                    }

                    return true;
                }
                return false;
            }
        });
    }


    /**
     * 发送“正在输入”通知
     */
    private void sendTypingCommand() {
        if (container.account.equals(NimUIKit.getAccount())) {
            return;
        }

        if (container.sessionType == SessionTypeEnum.Team || container.sessionType == SessionTypeEnum.ChatRoom) {
            return;
        }

        if (System.currentTimeMillis() - typingTime > 5000L) {
            typingTime = System.currentTimeMillis();
            CustomNotification command = new CustomNotification();
            command.setSessionId(container.account);
            command.setSessionType(container.sessionType);
            CustomNotificationConfig config = new CustomNotificationConfig();
            config.enablePush = false;
            config.enableUnreadCount = false;
            command.setConfig(config);

            JSONObject json = new JSONObject();
            json.put("id", "1");
            command.setContent(json.toString());

            NIMClient.getService(MsgService.class).sendCustomNotification(command);
        }
    }

    /**
     * ************************* 键盘布局切换 *******************************
     */

    private View.OnClickListener clickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
//            if (v == sendMessageButtonInInputBar) {
//                检测是否可以发消息
//                if (checkSendButtonEnable(messageEditText)) {
//                    onTextMessageSendButtonPressed();
//                    sendMessageButtonInInputBar.setEnabled(false);
//                }
//            }
//            else if (v == moreFuntionButtonInInputBar) {
//                toggleActionPanelLayout();
//            }
//            else
            if (v == switchToAudioButtonInInputBar) {
                if (audioRecordBtn.getVisibility() == View.GONE)
                    switchToAudioLayout();
                else
                    switchToTextLayout(true);
//                    hideAudioLayout();
            } else if (v == emojiButtonInInputBar) {
                toggleEmojiLayout();
            }
        }
    };


    // 点击“+”号按钮，切换更多布局和键盘
    private void toggleActionPanelLayout() {
        if (actionPanelBottomLayout == null || actionPanelBottomLayout.getVisibility() == View.GONE) {
            showActionPanelLayout();
        } else {
            hideActionPanelLayout();
        }
    }


    // 隐藏更多布局
    private void hideActionPanelLayout() {
//        moreFuntionButtonInInputBar.setImageResource(R.drawable.nim_message_input_more_clickopen);

//        uiHandler.removeCallbacks(showMoreFuncRunnable);
//        if (actionPanelBottomLayout != null) {
//            actionPanelBottomLayout.setVisibility(View.GONE);
//        }
    }

    private Runnable showMoreFuncRunnable = new Runnable() {
        @Override
        public void run() {
            actionPanelBottomLayout.setVisibility(View.VISIBLE);
        }
    };

    // 点击edittext，切换键盘和更多布局
    private void switchToTextLayout(boolean needShowInput) {
        hideEmojiLayout();
        hideAudioLayout();
        hideActionPanelLayout();

        messageInputBar.setVisibility(View.VISIBLE);

        if (needShowInput) {
            uiHandler.postDelayed(showTextRunnable, SHOW_LAYOUT_DELAY);
        } else {
            hideInputMethod();
        }
    }

//    public void resetActions() {
//        if (adapter != null) {
//            for (int i = 0; i < actions.size(); i++) {
//                actions.get(i).setCheck(false);
//            }
//            adapter.notifyDataSetChanged();
//        }
//    }

    /**
     * 发送文本消息
     */
    private void onTextMessageSendButtonPressed() {
//        String text = messageEditText.getText().toString();
        String text = messageEditText.getText().toString().trim();
        if (!text.isEmpty()) {
            IMMessage textMessage = createTextMessage(text);
            if (container.proxy.sendMessage(textMessage)) {
                restoreText(true);
            }
        }
    }

    protected IMMessage createTextMessage(String text) {
        return MessageBuilder.createTextMessage(container.account, container.sessionType, text);
    }

    // 切换成音频，收起键盘，按钮切换成键盘
    private void switchToAudioLayout() {
        //获取权限后录语音
        if (!PermissionUtils.isGranted(PermissionConstants.MICROPHONE) || !PermissionUtils.isGranted(PermissionConstants.STORAGE)) {
            PermissionUtils.permission(PermissionConstants.MICROPHONE)
                    .callback(new PermissionUtils.SimpleCallback() {
                        @Override
                        public void onGranted() {
                            if (!PermissionUtils.isGranted(PermissionConstants.STORAGE)) {
                                PermissionUtils.permission(PermissionConstants.STORAGE)
                                        .callback(new PermissionUtils.SimpleCallback() {
                                            @Override
                                            public void onGranted() {
                                                switchToAudioButtonInInputBar.setImageResource(R.drawable.icon_message_input_keyboard);
                                                messageEditText.setVisibility(View.INVISIBLE);
                                                audioRecordBtn.setVisibility(View.VISIBLE);
                                                hideInputMethod();
                                                hideEmojiLayout();
                                                hideActionPanelLayout();
                                            }

                                            @Override
                                            public void onDenied() {
                                                CommonFunction.INSTANCE.toast("请开启文件权限后再发送语音.");
                                                switchToTextLayout(false);
                                            }
                                        })
                                        .request();
                            } else {
                                switchToAudioButtonInInputBar.setImageResource(R.drawable.icon_message_input_keyboard);
                                messageEditText.setVisibility(View.INVISIBLE);
                                audioRecordBtn.setVisibility(View.VISIBLE);
                                hideInputMethod();
                                hideEmojiLayout();
                                hideActionPanelLayout();
                            }
                        }

                        @Override
                        public void onDenied() {
                            CommonFunction.INSTANCE.toast("请开启录音权限后再发送语音.");
                            switchToTextLayout(false);
                        }
                    })
                    .request();
        } else {
            switchToAudioButtonInInputBar.setImageResource(R.drawable.icon_message_input_keyboard);
            messageEditText.setVisibility(View.INVISIBLE);
            audioRecordBtn.setVisibility(View.VISIBLE);
            hideInputMethod();
            hideEmojiLayout();
            hideActionPanelLayout();
        }

    }

    // 点击表情，切换到表情布局
    private void toggleEmojiLayout() {
        if (emoticonPickerView == null || emoticonPickerView.getVisibility() == View.GONE) {
            emojiButtonInInputBar.setImageResource(R.drawable.icon_message_input_keyboard);
            showEmojiLayout();
        } else {
            emojiButtonInInputBar.setImageResource(R.drawable.icon_message_input_emo);
            switchToTextLayout(true);
//            hideEmojiLayout();
        }
    }

    // 隐藏表情布局
    private void hideEmojiLayout() {
        uiHandler.removeCallbacks(showEmojiRunnable);
        if (emoticonPickerView != null) {
            emoticonPickerView.setVisibility(View.GONE);
        }
    }

    // 隐藏键盘布局
    private void hideInputMethod() {
        isKeyboardShowed = false;
        uiHandler.removeCallbacks(showTextRunnable);
        InputMethodManager imm = (InputMethodManager) container.activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(messageEditText.getWindowToken(), 0);
        messageEditText.clearFocus();
    }

    // 隐藏语音布局
    private void hideAudioLayout() {
        switchToAudioButtonInInputBar.setImageResource(R.drawable.icon_message_input_record);
        audioRecordBtn.setVisibility(View.GONE);
        messageEditText.setVisibility(View.VISIBLE);

    }


    // 显示键盘布局
    private void showInputMethod(EditText editTextMessage) {
        editTextMessage.requestFocus();
        //如果已经显示,则继续操作时不需要把光标定位到最后
        if (!isKeyboardShowed) {
            editTextMessage.setSelection(editTextMessage.getText().length());
            isKeyboardShowed = true;
        }

        InputMethodManager imm = (InputMethodManager) container.activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editTextMessage, 0);

        container.proxy.onInputPanelExpand();
    }


    // 显示表情布局
    private void showEmojiLayout() {
        hideInputMethod();
        hideAudioLayout();
        hideActionPanelLayout();

        messageEditText.requestFocus();
        uiHandler.postDelayed(showEmojiRunnable, 200);
        emoticonPickerView.setVisibility(View.VISIBLE);

        container.proxy.onInputPanelExpand();
    }

    // 初始化更多布局
    private void addActionPanelLayout() {
        if (actionPanelBottomLayout == null) {
            actionPanelBottomLayout = view.findViewById(R.id.actionsLayout);
            actionPanelBottomLayoutHasSetup = false;
        }

        // 初始化具体more layout中的项目
        if (actionPanelBottomLayoutHasSetup) {
            return;
        }

        //初始化底部面板
        initBottomActionPanel(view, actions);
        actionPanelBottomLayoutHasSetup = true;
    }

    // 显示更多布局
    private void showActionPanelLayout() {
//        moreFuntionButtonInInputBar.setImageResource(R.drawable.nim_message_input_more_clickclose);
        addActionPanelLayout();
        hideEmojiLayout();
        hideAudioLayout();
        hideInputMethod();

        uiHandler.postDelayed(showMoreFuncRunnable, SHOW_LAYOUT_DELAY);
        container.proxy.onInputPanelExpand();
    }


    private Runnable showEmojiRunnable = new Runnable() {
        @Override
        public void run() {
            emoticonPickerView.setVisibility(View.VISIBLE);
        }
    };

    private Runnable showTextRunnable = new Runnable() {
        @Override
        public void run() {
            showInputMethod(messageEditText);
        }
    };

    private void restoreText(boolean clearText) {
        if (clearText) {
            messageEditText.setText("");
        }

        checkSendButtonEnable(messageEditText);
    }

    /**
     * 显示发送或更多
     *
     * @param editText
     */
    private Boolean checkSendButtonEnable(EditText editText) {
        if (isRobotSession) {
            return false;
        }
        String textMessage = editText.getText().toString();
        if (!TextUtils.isEmpty(StringUtil.removeBlanks(textMessage))) {
//            sendMessageButtonInInputBar.setEnabled(true);
//            moreFuntionButtonInInputBar.setVisibility(View.GONE);
//            sendMessageButtonInInputBar.setVisibility(View.VISIBLE);
            return true;
        } else {
//            sendMessageButtonInInputBar.setEnabled(false);
//            moreFuntionButtonInInputBar.setVisibility(View.VISIBLE);
//            sendMessageButtonInInputBar.setVisibility(View.GONE);
            return false;
        }
    }


    private Runnable hideAllInputLayoutRunnable;


    @Override
    public void onTextAdd(String content, int start, int length) {
        if (messageEditText.getVisibility() != View.VISIBLE ||
                (emoticonPickerView != null && emoticonPickerView.getVisibility() == View.VISIBLE)) {
            switchToTextLayout(true);
        } else {
            uiHandler.postDelayed(showTextRunnable, SHOW_LAYOUT_DELAY);
        }
        messageEditText.getEditableText().insert(start, content);
    }

    @Override
    public void onTextDelete(int start, int length) {
        if (messageEditText.getVisibility() != View.VISIBLE) {
            switchToTextLayout(true);
        } else {
            uiHandler.postDelayed(showTextRunnable, SHOW_LAYOUT_DELAY);
        }
        int end = start + length - 1;
        messageEditText.getEditableText().replace(start, end, "");
    }

    public int getEditSelectionStart() {
        return messageEditText.getSelectionStart();
    }


    /**
     * 隐藏所有输入布局
     */
    private void hideAllInputLayout(boolean immediately) {
        if (hideAllInputLayoutRunnable == null) {
            hideAllInputLayoutRunnable = new Runnable() {

                @Override
                public void run() {
                    emojiButtonInInputBar.setImageResource(R.drawable.icon_message_input_emo);
                    switchToAudioButtonInInputBar.setImageResource(R.drawable.icon_message_input_record);
                    hideInputMethod();
                    hideEmojiLayout();
                    hideAudioLayout();
                    hideActionPanelLayout();
                }
            };
        }
        long delay = immediately ? 0 : ViewConfiguration.getDoubleTapTimeout();
        uiHandler.postDelayed(hideAllInputLayoutRunnable, delay);
    }

    /**
     * ****************************** 语音 ***********************************
     */
    private void initAudioRecordButton() {
        audioRecordBtn.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                touched = true;
                initAudioRecord();
                onStartAudioRecord();
            } else if (event.getAction() == MotionEvent.ACTION_CANCEL
                    || event.getAction() == MotionEvent.ACTION_UP) {
                touched = false;
                onEndAudioRecord(isCancelled(v, event));
            } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                touched = true;
                cancelAudioRecord(isCancelled(v, event));
            }

            return false;
        });
    }

    // 上滑取消录音判断
    private static boolean isCancelled(View view, MotionEvent event) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);

        if (event.getRawX() < location[0] || event.getRawX() > location[0] + view.getWidth()
                || event.getRawY() < location[1] - 40) {
            return true;
        }

        return false;
    }

    /**
     * 初始化AudioRecord
     */
    private void initAudioRecord() {
        if (audioMessageHelper == null) {
            UIKitOptions options = NimUIKitImpl.getOptions();
            audioMessageHelper = new AudioRecorder(container.activity, options.audioRecordType, options.audioRecordMaxTime, this);
        }
    }

    /**
     * 开始语音录制
     */
    private void onStartAudioRecord() {
        container.activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        audioMessageHelper.startRecord();
        cancelled = false;
    }

    /**
     * 结束语音录制
     *
     * @param cancel
     */
    private void onEndAudioRecord(boolean cancel) {
        started = false;
        container.activity.getWindow().setFlags(0, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        audioMessageHelper.completeRecord(cancel);
        audioRecordBtn.setText(R.string.record_audio);
        audioRecordBtn.setBackgroundResource(R.drawable.shape_rectangle_white_24dp);
        stopAudioRecordAnim();
    }

    /**
     * 取消语音录制
     *
     * @param cancel
     */
    private void cancelAudioRecord(boolean cancel) {
        // reject
        if (!started) {
            return;
        }
        // no change
        if (cancelled == cancel) {
            return;
        }

        cancelled = cancel;
        updateTimerTip(cancel);
    }

//    private void play() {

//    }
//
//    private void stop() {

//    }

    /**
     * 正在进行语音录制和取消语音录制，界面展示
     *
     * @param cancel
     */

    private void updateTimerTip(boolean cancel) {
        if (cancel) {
            timerTip.setText(com.netease.nim.uikit.R.string.recording_cancel_tip);
            timerTip.setTextColor(Color.parseColor("#FFFD4417"));
            timerRecordIv.setImageResource(R.drawable.icon_voice_revert);
            timerRecordAnimation.setVisibility(View.GONE);
        } else {
            timerTip.setText(com.netease.nim.uikit.R.string.recording_cancel);
            timerTip.setTextColor(Color.parseColor("#FF8B8B8B"));
            timerRecordIv.setImageResource(R.drawable.icon_voice_record);
            timerRecordAnimation.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 开始语音录制动画
     */
    private void playAudioRecordAnim() {
        audioAnimLayout.setVisibility(View.VISIBLE);
        if (timerRecordAnimation.getBackground() instanceof AnimationDrawable) {
            AnimationDrawable animation = (AnimationDrawable) timerRecordAnimation.getBackground();
            animation.start();
        }
    }

    /**
     * 结束语音录制动画
     */
    private void stopAudioRecordAnim() {
        audioAnimLayout.setVisibility(View.GONE);
        if (timerRecordAnimation.getBackground() instanceof AnimationDrawable) {
            AnimationDrawable animation = (AnimationDrawable) timerRecordAnimation.getBackground();
            animation.stop();
        }
    }

    // 录音状态回调
    @Override
    public void onRecordReady() {
        Log.d("OkHttp", "..........ready");
    }

    @Override
    public void onRecordStart(File audioFile, RecordType recordType) {
        Log.d("OkHttp", "..........start");

        started = true;
        if (!touched) {
            return;
        }


        audioRecordBtn.setText(com.netease.nim.uikit.R.string.record_audio_end);
        audioRecordBtn.setBackgroundResource(R.drawable.shape_rectangle_gray_f6_24dp);
        updateTimerTip(false); // 初始化语音动画状态
        playAudioRecordAnim();
    }

    @Override
    public void onRecordSuccess(File audioFile, long audioLength, RecordType recordType) {
        Log.d("OkHttp", "..........onRecordSuccess");
        //检测是否可以发消息
        IMMessage audioMessage = MessageBuilder.createAudioMessage(container.account, container.sessionType, audioFile, audioLength);
        container.proxy.sendMessage(audioMessage);

    }

    @Override
    public void onRecordFail() {
        Log.d("OkHttp", "..........onRecordFail");

        if (started) {
            ToastHelper.showToast(container.activity, R.string.recording_error);
        }
    }

    @Override
    public void onRecordCancel() {
    }

    @Override
    public void onRecordReachedMaxTime(final int maxTime) {
        stopAudioRecordAnim();
        EasyAlertDialogHelper.createOkCancelDiolag(container.activity, "", container.activity.getString(R.string.recording_max_time), false, new EasyAlertDialogHelper.OnDialogActionListener() {
            @Override
            public void doCancelAction() {
            }

            @Override
            public void doOkAction() {
                audioMessageHelper.handleEndRecord(true, maxTime);
            }
        }).show();
    }

    public boolean isRecording() {
        return audioMessageHelper != null && audioMessageHelper.isRecording();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        int index = (requestCode << 16) >> 24;
        if (index != 0) {
            index--;
            if (index < 0 | index >= actions.size()) {
                LogUtil.d(TAG, "request code out of actions' range");
                return;
            }
            ChatBaseAction action = actions.get(index);
            if (action != null) {
                action.onActivityResult(requestCode & 0xff, resultCode, data);
            }
        }
    }

    public void switchRobotMode(boolean isRobot) {
        isRobotSession = isRobot;
        if (isRobot) {
//            sendMessageButtonInInputBar.setEnabled(true);
//            sendMessageButtonInInputBar.setVisibility(View.VISIBLE);
//            moreFuntionButtonInInputBar.setVisibility(View.GONE);
        } else {
//            sendMessageButtonInInputBar.setEnabled(false);
//            sendMessageButtonInInputBar.setVisibility(View.GONE);
//            moreFuntionButtonInInputBar.setVisibility(View.VISIBLE);
        }
    }


    /**
     * 初始化底部面板
     *
     * @param view
     * @param actions
     */
    public void initBottomActionPanel(View view, final List<ChatBaseAction> actions) {
        GridView gridView = view.findViewById(R.id.viewPager);
        adapter = new ChatActionsGridviewAdapter(view.getContext(), actions);
        gridView.setAdapter(adapter);
        gridView.setNumColumns(4);
        gridView.setSelector(R.color.transparent);
        gridView.setHorizontalSpacing(0);
        gridView.setVerticalSpacing(0);
        gridView.setGravity(Gravity.CENTER);
        //图片、拍照、位置
        gridView.setOnItemClickListener((parent, view1, position, id) -> {
            if (disable && position != 3) {
                CommonFunction.INSTANCE.toast("打招呼仅限语音,文本,表情");
                return;
            }
            actions.get(position).onClick();
//                hideEmojiLayout();
//                hideAudioLayout();
        });
    }

    //是否禁用图片、定位、
    private boolean disable = false;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void EnablePicEvent(EnablePicEvent event) {
        //是好友就不禁用,如果不是好友就禁用
        disable = !event.getEnable();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (i != adapter.getCount() - 1)
                ((ChatBaseAction) adapter.getItem(i)).setEnable(event.getEnable());
        }
        adapter.notifyDataSetChanged();
    }

}
