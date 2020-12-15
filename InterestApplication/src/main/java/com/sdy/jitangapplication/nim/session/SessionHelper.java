package com.sdy.jitangapplication.nim.session;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.MsgServiceObserve;
import com.netease.nimlib.sdk.msg.attachment.MsgAttachment;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.LocalAntiSpamResult;
import com.netease.nimlib.sdk.msg.model.RecentContact;
import com.netease.nimlib.sdk.robot.model.RobotAttachment;
import com.netease.nimlib.sdk.uinfo.constant.GenderEnum;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.sdy.jitangapplication.R;
import com.sdy.jitangapplication.common.Constants;
import com.sdy.jitangapplication.nim.DemoCache;
import com.sdy.jitangapplication.nim.activity.MessageInfoActivity;
import com.sdy.jitangapplication.nim.activity.SearchMessageActivity;
import com.sdy.jitangapplication.nim.attachment.AccostGiftAttachment;
import com.sdy.jitangapplication.nim.attachment.ChatDatingAttachment;
import com.sdy.jitangapplication.nim.attachment.ChatHiAttachment;
import com.sdy.jitangapplication.nim.attachment.ChatUpAttachment;
import com.sdy.jitangapplication.nim.attachment.ContactAttachment;
import com.sdy.jitangapplication.nim.attachment.ContactCandyAttachment;
import com.sdy.jitangapplication.nim.attachment.CustomAttachment;
import com.sdy.jitangapplication.nim.attachment.SendCustomTipAttachment;
import com.sdy.jitangapplication.nim.attachment.SendGiftAttachment;
import com.sdy.jitangapplication.nim.attachment.ShareSquareAttachment;
import com.sdy.jitangapplication.nim.attachment.StickerAttachment;
import com.sdy.jitangapplication.nim.attachment.WishHelpAttachment;
import com.sdy.jitangapplication.nim.extension.CustomAttachParser;
import com.sdy.jitangapplication.nim.uikit.api.NimUIKit;
import com.sdy.jitangapplication.nim.uikit.api.model.recent.RecentCustomization;
import com.sdy.jitangapplication.nim.uikit.api.model.session.SessionCustomization;
import com.sdy.jitangapplication.nim.uikit.api.model.session.SessionEventListener;
import com.sdy.jitangapplication.nim.uikit.api.wrapper.NimMessageRevokeObserver;
import com.sdy.jitangapplication.nim.uikit.business.session.helper.MessageListPanelHelper;
import com.sdy.jitangapplication.nim.uikit.business.session.module.MsgForwardFilter;
import com.sdy.jitangapplication.nim.uikit.business.session.module.MsgRevokeFilter;
import com.sdy.jitangapplication.nim.uikit.common.ui.dialog.CustomAlertDialog;
import com.sdy.jitangapplication.nim.uikit.common.ui.dialog.EasyAlertDialogHelper;
import com.sdy.jitangapplication.nim.uikit.common.ui.popupmenu.NIMPopupMenu;
import com.sdy.jitangapplication.nim.uikit.common.ui.popupmenu.PopupMenuItem;
import com.sdy.jitangapplication.nim.uikit.impl.customization.DefaultRecentCustomization;
import com.sdy.jitangapplication.nim.viewholder.MsgViewHolderAccostGift;
import com.sdy.jitangapplication.nim.viewholder.MsgViewHolderChatDating;
import com.sdy.jitangapplication.nim.viewholder.MsgViewHolderChatHi;
import com.sdy.jitangapplication.nim.viewholder.MsgViewHolderChatUp;
import com.sdy.jitangapplication.nim.viewholder.MsgViewHolderContact;
import com.sdy.jitangapplication.nim.viewholder.MsgViewHolderContactCandy;
import com.sdy.jitangapplication.nim.viewholder.MsgViewHolderSendCustomTip;
import com.sdy.jitangapplication.nim.viewholder.MsgViewHolderSendGift;
import com.sdy.jitangapplication.nim.viewholder.MsgViewHolderShareSquare;
import com.sdy.jitangapplication.nim.viewholder.MsgViewHolderTip;
import com.sdy.jitangapplication.nim.viewholder.MsgViewHolderWishHelp;
import com.sdy.jitangapplication.ui.activity.MatchDetailActivity;
import com.sdy.jitangapplication.utils.UserManager;

import java.util.ArrayList;
import java.util.List;

/**
 * UIKit自定义消息界面用法展示类
 */
public class SessionHelper {

    private static final int ACTION_HISTORY_QUERY = 0;

    private static final int ACTION_SEARCH_MESSAGE = 1;

    private static final int ACTION_CLEAR_MESSAGE = 2;

    private static final int ACTION_CLEAR_P2P_MESSAGE = 3;

    private static SessionCustomization p2pCustomization;

    private static SessionCustomization myP2pCustomization;

    private static RecentCustomization recentCustomization;

    private static NIMPopupMenu popupMenu;

    private static List<PopupMenuItem> menuItemList;

    public static final boolean USE_LOCAL_ANTISPAM = true;

    public static void init() {
        // 注册自定义消息附件解析器
        NIMClient.getService(MsgService.class).registerCustomAttachmentParser(new CustomAttachParser());
        // 注册各种扩展消息类型的显示ViewHolder
        registerViewHolders();
        // 设置会话中点击事件响应处理
        setSessionListener();
        // 注册消息转发过滤器
        registerMsgForwardFilter();
        // 注册消息撤回过滤器
        registerMsgRevokeFilter();
        // 注册消息撤回监听器
        registerMsgRevokeObserver();
        NimUIKit.setCommonP2PSessionCustomization(getP2pCustomization());
        NimUIKit.setRecentCustomization(getRecentCustomization());
    }

    // 定制化单聊界面。如果使用默认界面，返回null即可
    private static SessionCustomization getP2pCustomization() {
        if (p2pCustomization == null) {
            p2pCustomization = new SessionCustomization() {

                // 由于需要Activity Result， 所以重载该函数。
                @Override
                public void onActivityResult(final Activity activity, int requestCode, int resultCode, Intent data) {
                    super.onActivityResult(activity, requestCode, resultCode, data);

                }

                @Override
                public boolean isAllowSendMessage(IMMessage message) {
                    return checkLocalAntiSpam(message);
                }

                @Override
                public MsgAttachment createStickerAttachment(String category, String item) {
                    return new StickerAttachment(category, item);
                }
            };
            // 背景
            p2pCustomization.backgroundColor = Color.WHITE;
            // p2pCustomization.backgroundUri = "file:///android_asset/xx/bk.jpg";
            // p2pCustomization.backgroundUri = "file:///sdcard/Pictures/bk.png";
            // p2pCustomization.backgroundUri = "android.resource://com.netease.nim.demo/drawable/bk"
            // 定制加号点开后可以包含的操作， 默认已经有图片，视频等消息了
            ArrayList<ChatBaseAction> actions = new ArrayList<>();

            actions.add(new RecordAction());
            actions.add(new EmojAction());
            // p2pCustomization.actions = actions;
            p2pCustomization.withSticker = true;
            // 定制ActionBar右边的按钮，可以加多个
            ArrayList<SessionCustomization.OptionsButton> buttons = new ArrayList<>();
            SessionCustomization.OptionsButton cloudMsgButton = new SessionCustomization.OptionsButton() {

                @Override
                public void onClick(Context context, View view, String sessionId) {
                    // initPopuptWindow(context, view, sessionId, SessionTypeEnum.P2P);
                }
            };
            cloudMsgButton.iconId = R.drawable.icon_more_black;
            SessionCustomization.OptionsButton infoButton = new SessionCustomization.OptionsButton() {

                @Override
                public void onClick(Context context, View view, String sessionId) {
                    MessageInfoActivity.startActivity(context, sessionId,false); // 打开聊天信息
                }
            };
            infoButton.iconId = R.drawable.icon_more_black;
            buttons.add(infoButton);
            p2pCustomization.buttons = buttons;
        }
        return p2pCustomization;
    }

    private static boolean checkLocalAntiSpam(IMMessage message) {
        if (!USE_LOCAL_ANTISPAM) {
            return true;
        }
        LocalAntiSpamResult result =
                NIMClient.getService(MsgService.class).checkLocalAntiSpam(message.getContent(), "**");
        int operator = result == null ? 0 : result.getOperator();
        switch (operator) {
        case 1: // 替换，允许发送
            message.setContent(result.getContent());
            return true;
        case 2: // 拦截，不允许发送
            return false;
        case 3: // 允许发送，交给服务器
            message.setClientAntiSpam(true);
            return true;
        case 0:
        default:
            break;
        }
        return true;
    }

    private static RecentCustomization getRecentCustomization() {
        if (recentCustomization == null) {
            recentCustomization = new DefaultRecentCustomization() {

                @Override
                public String getDefaultDigest(RecentContact recent) {
                    if (recent.getAttachment() instanceof ChatHiAttachment) {
                        if (((ChatHiAttachment) recent.getAttachment())
                                .getShowType() == ChatHiAttachment.CHATHI_CHATUP_FRIEND) {
                            return DemoCache.getContext().getString(R.string.msg_unlock_chat);
                        }

                    } else if (recent.getAttachment() instanceof ShareSquareAttachment) {
                        return DemoCache.getContext().getString(R.string.msg_share_square);

                    } else if (recent.getAttachment() instanceof SendGiftAttachment) {
                        return DemoCache.getContext().getString(R.string.msg_candy_gift);
                    } else if (recent.getAttachment() instanceof WishHelpAttachment) {
                        return DemoCache.getContext().getString(R.string.msg_want_help);
                    } else if (recent.getAttachment() instanceof AccostGiftAttachment) {
                        return DemoCache.getContext().getString(R.string.msg_chat_up_gift);
                    } else if (recent.getAttachment() instanceof SendCustomTipAttachment) {
                        return ((SendCustomTipAttachment) recent.getAttachment()).getContent();
                    } else if (recent.getAttachment() instanceof ContactCandyAttachment) {
                        return DemoCache.getContext().getString(R.string.msg_unlock_contact);
                    }
                    return super.getDefaultDigest(recent);
                }
            };
        }
        return recentCustomization;
    }

    // 自定义消息界面
    private static void registerViewHolders() {
        // NimUIKit.registerMsgItemViewHolder(ChatMatchAttachment.class, MsgViewHolderMatch.class);
        NimUIKit.registerMsgItemViewHolder(AccostGiftAttachment.class, MsgViewHolderAccostGift.class);
        NimUIKit.registerMsgItemViewHolder(ContactCandyAttachment.class, MsgViewHolderContactCandy.class);
        NimUIKit.registerMsgItemViewHolder(SendGiftAttachment.class, MsgViewHolderSendGift.class);
        NimUIKit.registerMsgItemViewHolder(WishHelpAttachment.class, MsgViewHolderWishHelp.class);
        NimUIKit.registerMsgItemViewHolder(SendCustomTipAttachment.class, MsgViewHolderSendCustomTip.class);
        NimUIKit.registerMsgItemViewHolder(ShareSquareAttachment.class, MsgViewHolderShareSquare.class);
        NimUIKit.registerMsgItemViewHolder(ChatDatingAttachment.class, MsgViewHolderChatDating.class);
        NimUIKit.registerMsgItemViewHolder(ChatHiAttachment.class, MsgViewHolderChatHi.class);
        NimUIKit.registerMsgItemViewHolder(ContactAttachment.class, MsgViewHolderContact.class);
        NimUIKit.registerMsgItemViewHolder(ChatUpAttachment.class, MsgViewHolderChatUp.class);
        NimUIKit.registerTipMsgViewHolder(MsgViewHolderTip.class);
        // NimUIKit.registerMsgItemViewHolder(FileAttachment.class, MsgViewHolderFile.class);
        // NimUIKit.registerMsgItemViewHolder(CustomAttachment.class, MsgViewHolderDefCustom.class);
    }

    private static void setSessionListener() {
        SessionEventListener listener = new SessionEventListener() {

            @Override
            public void onAvatarClicked(Context context, IMMessage message) {
                // 一般用于打开用户资料页面
                if (message.getFromAccount().equals(UserManager.INSTANCE.loginInfo().getAccount())) {
                    // context.startActivity(new Intent(context, UserCenterActivity.class));
                } else if (!message.getFromAccount().equals(Constants.ASSISTANT_ACCID))
                    MatchDetailActivity.start(context, message.getFromAccount(), -1, -1);
            }

            @Override
            public void onAvatarLongClicked(Context context, IMMessage message) {
                // 一般用于群组@功能，或者弹出菜单，做拉黑，加好友等功能
            }

            @Override
            public boolean isUserVip() {
                return UserManager.INSTANCE.isUserVip();
            }

            @Override
            public boolean isShowGiftIcon(IMMessage message) {
                if (message.getSessionId().equals(Constants.ASSISTANT_ACCID)) {
                    return false;
                } else {
                    boolean sendMan =
                            ((NimUserInfo) NimUIKit.getUserInfoProvider().getUserInfo(message.getFromAccount()))
                                    .getGenderEnum() == GenderEnum.MALE;
                    boolean differentGender =
                            ((NimUserInfo) NimUIKit.getUserInfoProvider().getUserInfo(message.getSessionId()))
                                    .getGenderEnum() != ((NimUserInfo) NimUIKit.getUserInfoProvider()
                                            .getUserInfo(UserManager.INSTANCE.getAccid())).getGenderEnum();
                    if (!message.getFromAccount().equals(Constants.ASSISTANT_ACCID)
                            && message.getMsgType() != MsgTypeEnum.tip && message.getMsgType() != MsgTypeEnum.custom
                            && sendMan && differentGender && UserManager.INSTANCE.getShowCandyMessage()
                            && message.getTime() > UserManager.INSTANCE.getShowCandyTime()) {
                        return true;
                    } else {
                        return false;
                    }
                }
            }

            @Override
            public String robotAccount() {
                return Constants.ASSISTANT_ACCID;
            }
        };
        NimUIKit.setSessionListener(listener);
    }

    /**
     * 消息转发过滤器
     */
    private static void registerMsgForwardFilter() {
        NimUIKit.setMsgForwardFilter(new MsgForwardFilter() {

            @Override
            public boolean shouldIgnore(IMMessage message) {
                if (message.getMsgType() == MsgTypeEnum.custom) {
                    // 白板消息和阅后即焚消息，红包消息 不允许转发
                    return true;
                } else if (message.getMsgType() == MsgTypeEnum.robot && message.getAttachment() != null
                        && ((RobotAttachment) message.getAttachment()).isRobotSend()) {
                    return true; // 如果是机器人发送的消息 不支持转发
                }
                return false;
            }
        });
    }

    /**
     * 消息撤回过滤器
     */
    private static void registerMsgRevokeFilter() {
        NimUIKit.setMsgRevokeFilter(new MsgRevokeFilter() {

            @Override
            public boolean shouldIgnore(IMMessage message) {
                if (DemoCache.getAccount().equals(message.getSessionId())) {
                    // 发给我的电脑 不允许撤回
                    return true;
                } else if (message.getAttachment() instanceof CustomAttachment) {
                    return true;
                }
                return false;
            }
        });
    }

    private static void registerMsgRevokeObserver() {
        NIMClient.getService(MsgServiceObserve.class).observeRevokeMessage(new NimMessageRevokeObserver(), true);
    }

    private static void initPopuptWindow(Context context, View view, String sessionId,
            SessionTypeEnum sessionTypeEnum) {
        if (popupMenu == null) {
            menuItemList = new ArrayList<>();
            popupMenu = new NIMPopupMenu(context, menuItemList, listener);
        }
        menuItemList.clear();
        menuItemList.addAll(getMoreMenuItems(context, sessionId, sessionTypeEnum));
        popupMenu.notifyData();
        popupMenu.show(view);
    }

    private static NIMPopupMenu.MenuItemClickListener listener = new NIMPopupMenu.MenuItemClickListener() {

        @Override
        public void onItemClick(final PopupMenuItem item) {
            switch (item.getTag()) {
            case ACTION_HISTORY_QUERY:
                // MessageHistoryActivity.start(item.getContext(), item.getSessionId(), item.getSessionTypeEnum()); //
                // 漫游消息查询
                SearchMessageActivity.start(item.getContext(), item.getSessionId(), item.getSessionTypeEnum());// 搜索聊天记录

                break;
            case ACTION_SEARCH_MESSAGE:
                SearchMessageActivity.start(item.getContext(), item.getSessionId(), item.getSessionTypeEnum());// 搜索聊天记录
                break;
            case ACTION_CLEAR_MESSAGE:
                EasyAlertDialogHelper.createOkCancelDiolag(item.getContext(), null, "确定要清空吗？", true,
                        new EasyAlertDialogHelper.OnDialogActionListener() {

                            @Override
                            public void doCancelAction() {
                            }

                            @Override
                            public void doOkAction() {
                                NIMClient.getService(MsgService.class).clearServerHistory(item.getSessionId(),
                                        item.getSessionTypeEnum(),true);
                                MessageListPanelHelper.getInstance().notifyClearMessages(item.getSessionId());
                            }
                        }).show();
                break;
            case ACTION_CLEAR_P2P_MESSAGE:
                String title = item.getContext().getString(R.string.message_p2p_clear_tips);
                CustomAlertDialog alertDialog = new CustomAlertDialog(item.getContext());
                alertDialog.setTitle(title);
                alertDialog.addItem("确定", new CustomAlertDialog.onSeparateItemClickListener() {

                    @Override
                    public void onClick() {
                        NIMClient.getService(MsgService.class).clearServerHistory(item.getSessionId(),
                                item.getSessionTypeEnum(),true);
                        MessageListPanelHelper.getInstance().notifyClearMessages(item.getSessionId());
                    }
                });
                String itemText = item.getContext().getString(R.string.sure_keep_roam);
                alertDialog.addItem(itemText, new CustomAlertDialog.onSeparateItemClickListener() {

                    @Override
                    public void onClick() {
                        NIMClient.getService(MsgService.class).clearServerHistory(item.getSessionId(),
                                item.getSessionTypeEnum(),false);
                        MessageListPanelHelper.getInstance().notifyClearMessages(item.getSessionId());
                    }
                });
                alertDialog.addItem("取消", new CustomAlertDialog.onSeparateItemClickListener() {

                    @Override
                    public void onClick() {
                    }
                });
                alertDialog.show();
                break;
            }
        }
    };

    private static List<PopupMenuItem> getMoreMenuItems(Context context, String sessionId,
            SessionTypeEnum sessionTypeEnum) {
        List<PopupMenuItem> moreMenuItems = new ArrayList<PopupMenuItem>();
        moreMenuItems.add(new PopupMenuItem(context, ACTION_HISTORY_QUERY, sessionId, sessionTypeEnum,
                DemoCache.getContext().getString(R.string.message_history_query)));
        moreMenuItems.add(new PopupMenuItem(context, ACTION_SEARCH_MESSAGE, sessionId, sessionTypeEnum,
                DemoCache.getContext().getString(R.string.message_search_title)));
        moreMenuItems.add(new PopupMenuItem(context, ACTION_CLEAR_MESSAGE, sessionId, sessionTypeEnum,
                DemoCache.getContext().getString(R.string.message_clear)));
        if (sessionTypeEnum == SessionTypeEnum.P2P) {
            moreMenuItems.add(new PopupMenuItem(context, ACTION_CLEAR_P2P_MESSAGE, sessionId, sessionTypeEnum,
                    DemoCache.getContext().getString(R.string.message_p2p_clear)));
        }
        return moreMenuItems;
    }
}
