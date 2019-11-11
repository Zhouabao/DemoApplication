package com.sdy.jitangapplication.nim.panel;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.SystemClock;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.alibaba.fastjson.JSONObject;
import com.blankj.utilcode.constant.PermissionConstants;
import com.blankj.utilcode.util.PermissionUtils;
import com.kotlin.base.data.net.RetrofitFactory;
import com.kotlin.base.data.protocol.BaseResp;
import com.kotlin.base.utils.NetWorkUtils;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.api.UIKitOptions;
import com.netease.nim.uikit.api.model.session.SessionCustomization;
import com.netease.nim.uikit.business.ait.AitTextChangeListener;
import com.netease.nim.uikit.business.session.emoji.EmoticonPickerView;
import com.netease.nim.uikit.business.session.emoji.IEmoticonSelectedListener;
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
import com.netease.nimlib.sdk.msg.attachment.MsgAttachment;
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.CustomMessageConfig;
import com.netease.nimlib.sdk.msg.model.CustomNotification;
import com.netease.nimlib.sdk.msg.model.CustomNotificationConfig;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.sdy.jitangapplication.R;
import com.sdy.jitangapplication.api.Api;
import com.sdy.jitangapplication.common.CommonFunction;
import com.sdy.jitangapplication.event.EnablePicEvent;
import com.sdy.jitangapplication.model.CheckGreetSendBean;
import com.sdy.jitangapplication.nim.session.ChatBaseAction;
import com.sdy.jitangapplication.utils.UserManager;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import java.io.File;
import java.util.HashMap;
import java.util.List;

/**
 * 底部文本编辑，语音等模块
 * Created by hzxuwen on 2015/6/16.
 */
public class ChatInputPanel implements IEmoticonSelectedListener, IAudioRecordCallback, AitTextChangeListener {

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
//    protected View switchToAudioButtonInInputBar;// 语音消息选择按钮
//    protected View emojiButtonInInputBar;// 发送消息按钮


    protected Button sendMessageButtonInInputBar;// 发送消息按钮
    protected View messageInputBar;

    private SessionCustomization customization;

    // 表情
    protected EmoticonPickerView emoticonPickerView;  // 贴图表情控件

    // 语音
    protected AudioRecorder audioMessageHelper;
    private Chronometer recordNimTime;
    protected Button audioRecordBtn; // 录音按钮
    protected TextView audioRecordTv; // 录音按钮
    protected View audioAnimLayout; // 录音动画布局

    //    private TextView timerTip;
//    private LinearLayout timerTipContainer;
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
                || audioAnimLayout != null && audioAnimLayout.getVisibility() == View.VISIBLE);

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

    }

    public void setCustomization(SessionCustomization customization) {
        this.customization = customization;
        if (customization != null) {
            emoticonPickerView.setWithSticker(customization.withSticker);
        }
    }

    public void reload(Container container, SessionCustomization customization) {
        this.container = container;
        setCustomization(customization);
    }

    private void initViews() {
        // input bar
        messageActivityBottomLayout = view.findViewById(R.id.messageActivityBottomLayout);
        messageInputBar = view.findViewById(R.id.textMessageLayout);
        sendMessageButtonInInputBar = view.findViewById(R.id.buttonSendMessage);
        messageEditText = view.findViewById(R.id.editTextMessage);

        // 语音
        audioRecordBtn = view.findViewById(R.id.startRecordBtn);
        audioRecordTv = view.findViewById(R.id.recordTv);
        recordNimTime = view.findViewById(R.id.recordTime);
        audioAnimLayout = view.findViewById(R.id.audioLl);


        // 表情
        emoticonPickerView = view.findViewById(R.id.emoticon_picker_view);

    }

    private void initInputBarListener() {
        sendMessageButtonInInputBar.setOnClickListener(clickListener);
    }

    private void initTextEdit() {
        messageEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        messageEditText.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    switchToTextLayout(true);
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
            if (v == sendMessageButtonInInputBar) {
                //检测是否可以发消息
                if (checkSendButtonEnable(messageEditText)) {
                    if (disable) {
                        checkGreetSendMsg(1);
                    } else {
                        onTextMessageSendButtonPressed();
                    }
                    sendMessageButtonInInputBar.setEnabled(false);
                }

            }
        }
    };

    // 点击edittext，切换键盘和更多布局
    private void switchToTextLayout(boolean needShowInput) {
        hideEmojiLayout();
        hideAudioLayout();
        resetActions();

        messageInputBar.setVisibility(View.VISIBLE);

        if (needShowInput) {
            uiHandler.postDelayed(showTextRunnable, SHOW_LAYOUT_DELAY);
        } else {
            hideInputMethod();
        }
    }

    private void resetActions() {
        if (adapter != null) {
            for (int i = 0; i < actions.size(); i++) {
                actions.get(i).setCheck(false);
            }
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * 发送文本消息
     */
    private void onTextMessageSendButtonPressed() {
//        String text = messageEditText.getText().toString();
        String text = messageEditText.getText().toString().trim();
        IMMessage textMessage = createTextMessage(text);

        if (container.proxy.sendMessage(textMessage)) {
            restoreText(true);
        }
    }

    protected IMMessage createTextMessage(String text) {
        return MessageBuilder.createTextMessage(container.account, container.sessionType, text);
    }

    // 切换成音频，收起键盘，按钮切换成键盘
    private void switchToAudioLayout() {
        audioAnimLayout.setVisibility(View.VISIBLE);
        hideInputMethod();
        hideEmojiLayout();
    }

    // 点击表情，切换到表情布局
    private void toggleEmojiLayout() {
        if (emoticonPickerView == null || emoticonPickerView.getVisibility() == View.GONE) {
            showEmojiLayout();
        } else {
            hideEmojiLayout();
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
        audioAnimLayout.setVisibility(View.GONE);
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

        messageEditText.requestFocus();
        uiHandler.postDelayed(showEmojiRunnable, 200);
        emoticonPickerView.setVisibility(View.VISIBLE);
        emoticonPickerView.show(this);
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
        addActionPanelLayout();
        hideEmojiLayout();
        hideInputMethod();

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
            sendMessageButtonInInputBar.setEnabled(true);
            return true;
        } else {
            sendMessageButtonInInputBar.setEnabled(false);
            return false;
        }
    }

    /**
     * *************** IEmojiSelectedListener ***************
     */
    @Override
    public void onEmojiSelected(String key) {
        Editable mEditable = messageEditText.getText();
        if (key.equals("/DEL")) {
            messageEditText.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
        } else {
            int start = messageEditText.getSelectionStart();
            int end = messageEditText.getSelectionEnd();
            start = (start < 0 ? 0 : start);
            end = (start < 0 ? 0 : end);
            mEditable.replace(start, end, key);
        }
    }

    private Runnable hideAllInputLayoutRunnable;

    @Override
    public void onStickerSelected(String category, String item) {
        Log.i("InputPanel", "onStickerSelected, category =" + category + ", sticker =" + item);

        if (customization != null) {
            MsgAttachment attachment = customization.createStickerAttachment(category, item);
            IMMessage stickerMessage = MessageBuilder.createCustomMessage(container.account, container.sessionType, "贴图消息", attachment);
            container.proxy.sendMessage(stickerMessage);
        }
    }

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
                    hideInputMethod();
                    hideEmojiLayout();
                    hideAudioLayout();
                    resetActions();
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
        audioRecordBtn.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
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
            }
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
        audioRecordTv.setText("按住录音");
        recordNimTime.setTextColor(container.activity.getResources().getColor(R.color.colorAccent));
        recordNimTime.setText(R.string.timer_default);
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

    /**
     * 正在进行语音录制和取消语音录制，界面展示
     *
     * @param cancel
     */
    private void updateTimerTip(boolean cancel) {
        if (cancel) {
            audioRecordTv.setText(R.string.recording_cancel_tip);
        } else {
            audioRecordTv.setText(R.string.recording_cancel);
        }
    }

    /**
     * 开始语音录制动画
     */
    private void playAudioRecordAnim() {
        audioAnimLayout.setVisibility(View.VISIBLE);
        recordNimTime.setBase(SystemClock.elapsedRealtime());
        recordNimTime.start();
        recordNimTime.setTextColor(container.activity.getResources().getColor(R.color.colorAccent));
    }

    /**
     * 结束语音录制动画
     */
    private void stopAudioRecordAnim() {
        audioAnimLayout.setVisibility(View.GONE);
        recordNimTime.stop();
        recordNimTime.setBase(SystemClock.elapsedRealtime());
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

        audioRecordTv.setText(R.string.record_audio_end);
        updateTimerTip(false); // 初始化语音动画状态
        playAudioRecordAnim();
    }

    private long audioLength;
    private File audioFile;

    @Override
    public void onRecordSuccess(File audioFile, long audioLength, RecordType recordType) {
        Log.d("OkHttp", "..........onRecordSuccess");
        //检测是否可以发消息
        if (disable) {
            this.audioFile = audioFile;
            this.audioLength = audioLength;
            checkGreetSendMsg(2);
        } else {
            IMMessage audioMessage = MessageBuilder.createAudioMessage(container.account, container.sessionType, audioFile, audioLength);
            container.proxy.sendMessage(audioMessage);
            resetActions();
        }
    }

    @Override
    public void onRecordFail() {
        Log.d("OkHttp", "..........onRecordFail");

        if (started) {
            ToastHelper.showToast(container.activity, R.string.recording_error);
            resetActions();
        }
    }

    @Override
    public void onRecordCancel() {
        resetActions();
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
            sendMessageButtonInInputBar.setEnabled(true);
        } else {
            sendMessageButtonInInputBar.setEnabled(false);
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
        gridView.setNumColumns(5);
        gridView.setSelector(com.netease.nim.uikit.R.color.transparent);
        gridView.setHorizontalSpacing(0);
        gridView.setVerticalSpacing(0);
        gridView.setGravity(Gravity.CENTER);
        gridView.setOnItemClickListener(new GridView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (disable) {
                    if (position != 0 && position != 1) {
                        CommonFunction.INSTANCE.toast("打招呼仅限语音,文本,表情");
                        return;
                    }
                }
                for (int i = 0; i < actions.size(); i++) {
                    if (i == position) {
                        actions.get(i).setCheck(!actions.get(i).isCheck());
                    } else {
                        actions.get(i).setCheck(false);
                    }
                }
                if (actions.get(position).isCheck()) {
                    if (position == 0) {//表情
                        toggleEmojiLayout();
//                        hideAudioLayout();
                    } else if (position == 1) {
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
                                                                switchToAudioLayout();
                                                            }

                                                            @Override
                                                            public void onDenied() {
                                                                CommonFunction.INSTANCE.toast("请开启文件权限后再发送语音.");
                                                                switchToTextLayout(false);
                                                            }
                                                        })
                                                        .request();
                                            } else {
                                                switchToAudioLayout();
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
                            switchToAudioLayout();
                        }

//                        hideEmojiLayout();
                    } else {//定位
                        actions.get(position).onClick();
                        actions.get(position).setCheck(false);
                        hideEmojiLayout();
                        hideAudioLayout();
                    }
                } else {
                    if (position == 0) {//表情
                        hideEmojiLayout();
                    } else if (position == 1) {
                        hideAudioLayout();
                    }
                }

                adapter.notifyDataSetChanged();
            }
        });
    }

    //是否禁用图片、定位、
    private boolean disable = false;

    @Subscribe
    public void EnablePicEvent(EnablePicEvent event) {
        //是好友就不禁用,如果不是好友就禁用
        updateActionsState(event.getEnable());
    }


    //是好友就不禁用,如果不是好友就禁用
    private void updateActionsState(boolean enable) {
        if (enable) {
            disable = false;
            actions.get(2).setEnable(true);
            actions.get(3).setEnable(true);
            actions.get(4).setEnable(true);

            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        } else {
            disable = true;
            actions.get(2).setEnable(false);
            actions.get(2).setIconResIdDisable(R.drawable.send_location_disable);
            actions.get(3).setEnable(false);
            actions.get(3).setIconResIdDisable(R.drawable.icon_send_phone_disable);
            actions.get(4).setEnable(false);
            actions.get(4).setIconResIdDisable(R.drawable.icon_send_pic_disable);

            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }
    }


    /**
     * 判断剩余打招呼次数
     *
     * @param type 1-文本  2-录音
     */

    private boolean sendTip = false;

    private void checkGreetSendMsg(final int type) {
        if (!NetWorkUtils.INSTANCE.isNetWorkAvailable(container.activity)) {
            CommonFunction.INSTANCE.toast("请打开网络");
            return;
        }
        HashMap<String, Object> params = UserManager.INSTANCE.getBaseParams();
        params.put("target_accid", container.account);
        RetrofitFactory.Companion.getInstance().create(Api.class)
                .checkGreetSendMsg(UserManager.INSTANCE.getSignParams(params))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new rx.Observer<BaseResp<CheckGreetSendBean>>() {

                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(BaseResp<CheckGreetSendBean> checkGreetSendBeanBaseResp) {
                        if (checkGreetSendBeanBaseResp != null)
                            if (checkGreetSendBeanBaseResp.getCode() == 200 && checkGreetSendBeanBaseResp.getData() != null) {
                                CheckGreetSendBean checkGreetSendBean = checkGreetSendBeanBaseResp.getData();
                                if (checkGreetSendBean.getIsfriend()) {//是好友，发送好友通知
                                    //todo 发送成为好友通知
                                    //发送通知
                                    updateActionsState(true);
                                    view.findViewById(com.sdy.jitangapplication.R.id.btnMakeFriends).setVisibility(View.GONE);
                                } else {
                                    if (checkGreetSendBean.getResidue_msg_cnt() > 0 || checkGreetSendBean.getIslimit() == false) {//次数大于0就发送消息
                                        if (type == 1) {
                                            onTextMessageSendButtonPressed();
                                        } else if (type == 2) {
                                            if (audioFile != null && audioLength > 0) {
                                                IMMessage audioMessage = MessageBuilder.createAudioMessage(container.account, container.sessionType, audioFile, audioLength);
                                                container.proxy.sendMessage(audioMessage);
                                                audioFile = null;
                                                audioLength = 0;
                                                resetActions();
                                            }
                                        }
                                        if (checkGreetSendBean.getIslimit() && checkGreetSendBean.getResidue_msg_cnt() == 3 && !UserManager.INSTANCE.getHeRead()) {
                                            IMMessage tipMessage = MessageBuilder.createTipMessage(container.account, container.sessionType);
                                            tipMessage.setContent("在收到对方回复前只能发送三条消息");
                                            tipMessage.setStatus(MsgStatusEnum.success);
                                            CustomMessageConfig config = new CustomMessageConfig();
                                            config.enablePush = false;//不推送
                                            config.enableUnreadCount = false;
                                            tipMessage.setConfig(config);
//                                        container.proxy.sendMessage(tipMessage);
                                            NIMClient.getService(MsgService.class).saveMessageToLocal(tipMessage, true);
                                        }
                                    } else if (checkGreetSendBean.getResidue_msg_cnt() == 0) {//次数用尽不能再发消息
                                        if (!sendTip && !UserManager.INSTANCE.getHeRead()) {
                                            resetActions();
                                            IMMessage msg = MessageBuilder.createTipMessage(container.account, container.sessionType);
                                            msg.setContent("你已发送三条消息，请等待对方回复");
                                            msg.setStatus(MsgStatusEnum.success);
                                            CustomMessageConfig config = new CustomMessageConfig();
                                            config.enablePush = false;//不推送
                                            config.enableUnreadCount = false;
                                            msg.setConfig(config);
//                                        container.proxy.sendMessage(msg);

                                            NIMClient.getService(MsgService.class).saveMessageToLocal(msg, true);
                                            sendTip = true;
                                        }
//                                    CustomMessageConfig config = new CustomMessageConfig();
//                                    config.enablePush = false; // 不推送
//                                    msg.setConfig(config);
//                                    container.proxy.sendMessage(msg);
//                                    ToastUtils.showShort("消息次数已用完！");
                                    }
                                }

                            } else {
                                CommonFunction.INSTANCE.toast(checkGreetSendBeanBaseResp.getMsg());
                            }
                    }
                });
    }


}
