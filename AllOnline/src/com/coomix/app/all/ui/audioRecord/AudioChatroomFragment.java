package com.coomix.app.all.ui.audioRecord;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.Constant;
import com.coomix.app.all.R;
import com.coomix.app.all.dialog.ProgressDialogEx;
import com.coomix.app.all.model.response.CommunityImageInfo;
import com.coomix.app.all.model.response.CommunityUser;
import com.coomix.app.all.share.PopupWindowUtil;
import com.coomix.app.all.share.TextSet;
import com.coomix.app.all.ui.base.BaseFragment;
import com.coomix.app.all.ui.im.EmLocationActivity;
import com.coomix.app.all.ui.im.GmChatSettingsActivity;
import com.coomix.app.all.ui.im.GmSelectAtUserActivity;
import com.coomix.app.all.ui.wallet.BindWechatActivity;
import com.coomix.app.all.util.GlideUtil;
import com.coomix.app.all.util.ImageCompressUtils;
import com.coomix.app.framework.app.Result;
import com.coomix.app.framework.util.CommonUtil;
import com.coomix.app.redpacket.activity.CreateRedPacketActivity;
import com.coomix.app.redpacket.util.RedPacketConstant;
import com.coomix.app.redpacket.util.RedPacketInfo;
import com.google.gson.JsonObject;
import com.goomeim.GMAppConstant;
import com.goomeim.adapter.GMMessageAdapter;
import com.goomeim.controller.GMChatroomAppListener;
import com.goomeim.controller.GMImManager;
import com.goomeim.controller.GMMessageAppListener;
import com.goomeim.controller.MessageListItemClickListener;
import com.goomeim.controller.MessageSendController;
import com.goomeim.controller.VoicePlayCompletedListener;
import com.goomeim.model.GMAtMessageHelper;
import com.goomeim.model.GMNotifier;
import com.goomeim.ui.GMGroupListener;
import com.goomeim.utils.GMCommonUtils;
import com.goomeim.utils.GMUserUtil;
import com.goomeim.widget.GMAlertDialog;
import com.goomeim.widget.GMChatExtendMenu;
import com.goomeim.widget.chatrow.GMVoicePlayManager;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.muzhi.camerasdk.PhotoPickActivity;
import com.muzhi.camerasdk.model.CameraSdkParameterInfo;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.goome.im.GMError;
import net.goome.im.GMValueCallBack;
import net.goome.im.chat.GMChatManager;
import net.goome.im.chat.GMChatRoom;
import net.goome.im.chat.GMChatRoomManager;
import net.goome.im.chat.GMClient;
import net.goome.im.chat.GMConstant;
import net.goome.im.chat.GMConversation;
import net.goome.im.chat.GMGroup;
import net.goome.im.chat.GMImageMessageBody;
import net.goome.im.chat.GMMessage;
import net.goome.im.util.GMLog;
import net.goome.im.util.PathUtil;

public class AudioChatroomFragment extends BaseFragment implements SensorEventListener {

    private final String TAG = AudioChatroomFragment.class.getCanonicalName();
    public static final String TO_ALPHA = "to_appha";
    protected static final int REQUEST_CODE_MAP = 1;
    protected static final int REQUEST_CODE_CAMERA = 2;
    private final int REQUEST_CODE_CHAT_SETTUNG = 3;
    private static final int REQUEST_CODE_EM_CHAT_SETTINGS = 10000;
    public static final int REQUEST_CODE_EM_AT_SELECT = 10001;
    public static final int CMD_ON_INPUTING = 10002; //命令正在输入
    public static final int INPUTING_REVOKE_DELAY = 3000; //3秒

    private final int TWO_MINUTS = 2 * 65 * 1000; //撤回时间限制--两分钟加10s的误差
    private final int MAX_PER_PAGE_MSG = 100;

    static final int ITEM_TAKE_PICTURE = 1;
    static final int ITEM_PICTURE = 2;
    static final int ITEM_LOCATION = 3;
    static final int ITEM_RED_PACKET = 4;

    protected int[] itemStrings = {R.string.attach_picture, R.string.attach_location, R.string.redpacket};
    protected int[] itemdrawables = {R.drawable.ease_chat_image_selector, R.drawable.ease_chat_location_selector,
            R.drawable.ease_chat_redpacket_selector};
    protected int[] itemIds = {ITEM_PICTURE, ITEM_LOCATION, ITEM_RED_PACKET};

    private PullToRefreshListView mPtrListView;
    protected MyItemClickListener extendMenuItemClickListener;
    private File cameraFile;
    protected GMMessageAdapter messageAdapter;
    private long toChatUserId;
    private GMConstant.ConversationType chatType = GMConstant.ConversationType.CAROLVOICEROOM;
    private boolean bKeyboardForceShow = false;
    private boolean bShowedLongClick = false;
    private View rootView;
    private ProgressDialogEx progressDialogEx;
    private CommunityUser mUser = null;
    private MessageSendController msgSendController;
    // 调用距离传感器，控制屏幕
    private SensorManager mManager;// 传感器管理对象
    private GroupListener groupListener;
    private GMConversation conversation;
    //当类型是聊天室时，存储聊天室信息
    private GMChatRoom room;
    private GMChatHandler mh;

    private static final int GET_CHATROOM_DONE = 0;
    private static final int GET_CONVERSATION_DONE = 1;
    private static final int RE_GET_CONVERSATION_DONE = 2;
    public static CommunityUser ALPHA_USER = null;
    private boolean bNotGoToAlpha = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle b = getArguments();
        if (b != null) {
            toChatUserId = b.getLong(GMAppConstant.EXTRA_USER_ID, 0);
            chatType = (GMConstant.ConversationType) b.getSerializable(GMAppConstant.EXTRA_CHAT_TYPE);
        }

        Object obj = getActivity().getSystemService(Context.SENSOR_SERVICE);
        if (obj != null && obj instanceof SensorManager) {
            mManager = (SensorManager) obj;
            mManager.registerListener(this, mManager.getDefaultSensor(Sensor.TYPE_PROXIMITY), // 距离感应器
                SensorManager.SENSOR_DELAY_NORMAL);// 注册传感器，第一个参数为距离监听器，第二个是传感器类型，第三个是延迟类型
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_audio_chatroom, container, false);
        initViews(rootView);
        mh = new GMChatHandler(this);

        msgSendController = new MessageSendController(getContext(), toChatUserId, chatType);
        msgSendController.setMessageRefreshListener(refreshListener);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (messageAdapter != null) {
            messageAdapter.refresh();
        }

        if (isChatroom()) {
            GMAtMessageHelper.get().removeAtMeGroup(String.valueOf(toChatUserId));
        }

        try {
            //重置角标计数
            GMNotifier gmNotifier = GMImManager.getInstance().getNotifier();
            if (gmNotifier != null) {
                gmNotifier.reset();
            }
            // 清空角标
            //BadgeUtil.resetBadgeCount(getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        //如果语音消息在播放，停止
        if (GMVoicePlayManager.isPlaying) {
            GMVoicePlayManager.getInstance().stopVoice();
        }
    }

    @Override
    public void onDestroy() {
        release();
        if (mh != null) {
            mh.removeCallbacksAndMessages(null);
        }
        if (isChatroom()) {
            GMAtMessageHelper.get().removeAtMeGroup(String.valueOf(toChatUserId));
            GMAtMessageHelper.get().cleanToAtUserList();
        }

        super.onDestroy();
    }

    public static AudioChatroomFragment newInstance(long chatId, GMConstant.ConversationType type) {
        AudioChatroomFragment fragment = new AudioChatroomFragment();
        Bundle b = new Bundle();
        b.putLong(GMAppConstant.EXTRA_USER_ID, chatId);
        b.putSerializable(GMAppConstant.EXTRA_CHAT_TYPE, type);
        fragment.setArguments(b);
        return fragment;
    }

    private void initViews(View v) {
        mPtrListView = (PullToRefreshListView) v.findViewById(R.id.listViewChat);
        mPtrListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);

        if (chatType == GMConstant.ConversationType.CHAT) {
            // imageRight.setImageResource(R.drawable.chat_right);
        } else if (chatType == GMConstant.ConversationType.GROUP) {
            // imageRight.setImageResource(R.drawable.chat_right);
        } else if (isChatroom()) {
        }


        mPtrListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                //下拉加载之前的消息记录，请求之前的消息
                requestPreviousMsg();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                //没有上拉
            }
        });

        if (mPtrListView.getRefreshableView() != null) {
            mPtrListView.getRefreshableView().setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
        }
    }

    private boolean isChatroom() {
        return (chatType == GMConstant.ConversationType.CHATROOM
                || chatType == GMConstant.ConversationType.CLASSICROOM
                || chatType == GMConstant.ConversationType.CAROLVOICEROOM);
    }


    static class GMChatHandler extends Handler {
        private WeakReference<AudioChatroomFragment> mActivity;

        GMChatHandler(AudioChatroomFragment fragment) {
            mActivity = new WeakReference<AudioChatroomFragment>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            AudioChatroomFragment m = mActivity.get();
            if (m == null) {
                return;
            }
            switch (msg.what) {
                case GET_CHATROOM_DONE:
//                    m.showChatroomInfo();
                    break;

                case GET_CONVERSATION_DONE:
                    m.initAdapter();
                    break;

                case RE_GET_CONVERSATION_DONE:
                    m.refreshList();
                    break;

                default:
                    break;
            }
        }
    }

    private void requestPreviousMsg() {
        if (conversation == null) {
            mPtrListView.onRefreshComplete();
            return;
        }

        GMUserUtil.getInstance().getSonWorkHandler().post(new Runnable() {
            @Override
            public void run() {
                GMMessage message = messageAdapter.getItem(0);
                final List<GMMessage> listNewMsgs;
                if (message != null) {
                    listNewMsgs = conversation.loadMoreMsgFromDB(message.getMsgId(), MAX_PER_PAGE_MSG);
                } else {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mPtrListView.onRefreshComplete();
                        }
                    });
                    return;
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mPtrListView.onRefreshComplete();
                        if (listNewMsgs == null || listNewMsgs.size() <= 0) {
                            mPtrListView.setMode(PullToRefreshBase.Mode.DISABLED);
                            View view = LayoutInflater.from(getContext()).inflate(R.layout.footer_no_more_data, null);
                            TextView textView = (TextView) view.findViewById(R.id.textView);
                            textView.setText(R.string.no_previous_data);
                            mPtrListView.getRefreshableView().addHeaderView(view);
                        } else {
                            if (messageAdapter != null) {
                                messageAdapter.refreshSeekTo(listNewMsgs.size());
                            }
                        }
                    }
                });
            }
        });
    }

    private void initData() {
        if (isChatroom()) {
            initChatroomInfo(toChatUserId);
        }

        initConversation();

        if (chatType == GMConstant.ConversationType.CHAT) {

        } else if (chatType == GMConstant.ConversationType.GROUP) {
            GMGroup group = GMClient.getInstance().groupManager().getGroup(String.valueOf(toChatUserId));

            // listen the event that user moved out group or group is dismissed
            groupListener = new GroupListener();
            GMClient.getInstance().groupManager().addGroupChangeListener(groupListener);
        } else if (isChatroom()) {
            GMImManager.getInstance().addChatroomListener(roomListener);
        }
        //Todo 增加新聊天类型
    }

    private void initChatroomInfo(final long roomid) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                GMChatRoomManager mgr = GMClient.getInstance().chatroomManager();
                if (mgr == null) {
                    return;
                }
                room = mgr.getChatroomInfoFromDB(roomid);
                mh.sendEmptyMessage(GET_CHATROOM_DONE);
                if (room == null) {
                    mgr.getChatroomSpecificationFromServerWithId(roomid, new GMValueCallBack<GMChatRoom>() {
                        @Override
                        public void onSuccess(GMChatRoom aroom) {
                            if (aroom != null) {
                                room = aroom;
                            }
                            mh.sendEmptyMessage(GET_CHATROOM_DONE);
                        }

                        @Override
                        public void onError(GMError gmError) {
                            Log.e(TAG, gmError.errMsg());
                        }
                    });
                }
            }
        }).start();
    }

    private void initConversation() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                GMChatManager chatManager = GMClient.getInstance().chatManager();
                if (chatManager == null) {
                    return;
                }
                conversation = chatManager.getConversation(GMCommonUtils.getConversationIdByUserId(
                    String.valueOf(toChatUserId), chatType), chatType, false);
                mh.sendEmptyMessage(GET_CONVERSATION_DONE);
                if (conversation != null) {
                    conversation.markAllMessagesAsRead();
                }
            }
        }).start();
        GMImManager.getInstance().addGMMessageListener(gmMessageAppListener);
    }

    private void initAdapter() {
        messageAdapter = new GMMessageAdapter(getContext(), String.valueOf(toChatUserId), chatType, conversation,
                mPtrListView.getRefreshableView());
        messageAdapter.setShowAvatar(true);
        if (false/*isChatroom()*/) {
            messageAdapter.setShowUserNick(true);
        } else {
            messageAdapter.setShowUserNick(false);
        }
        messageAdapter.setItemClickListener(messageListItemClickListener);
        GMVoicePlayManager.getInstance().setAdapter(messageAdapter);
        GMVoicePlayManager.getInstance().setVoicePlayCompletedListener(voicePlayListener);
        mPtrListView.setAdapter(messageAdapter);
    }

    private VoicePlayCompletedListener voicePlayListener = new VoicePlayCompletedListener()
    {
        @Override
        public void onVoicePlayCompleted(int position)
        {
            int next = findNextUnlistenedVoice(position);
            if (next > 0)
            {
                GMMessage message = messageAdapter.getItem(next);
                GMVoicePlayManager.getInstance().playVoice(next, message, null);
                messageAdapter.notifyDataSetChanged();
            }
        }
    };

    private int findNextUnlistenedVoice(int position)
    {
        int size = messageAdapter.getCount();
        for (int i = position + 1; i < size; i++)
        {
            GMMessage message = messageAdapter.getItem(i);
            if (message != null && message.getDirection() == GMConstant.MsgDirection.RECEIVE && message.getBodyType() ==
                    GMConstant.MsgBodyType.VOICE && GMCommonUtils.getMessageExtInt(message, GMAppConstant.GOOME_VOICE_LISTENED, 0)
                    == 0)
            {
                return i;
            }
        }
        return -1;
    }

    private MessageSendController.OnMessageRefreshListener refreshListener
            = new MessageSendController.OnMessageRefreshListener() {
        @Override
        public void onRefresh() {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (messageAdapter != null) {
                        messageAdapter.refresh();
                    }
                }
            });
        }

        @Override
        public void onRefreshAndSelectLast() {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (messageAdapter != null) {
                        messageAdapter.refreshSelectLast();
                    }
                }
            });
        }
    };

    private GMMessageAppListener gmMessageAppListener = new GMMessageAppListener() {
        @Override
        public boolean onMessageReceived(List<GMMessage> messages) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && AudioChatroomFragment.this.isDetached()) {
                return false;
            }

            boolean isCurrentChat = false;
            GMMessage curMessage = null;
            for (final GMMessage message : messages) {
                if (message == null) {
                    continue;
                }
                long userId = 0;
                if (message.getChatType() == GMConstant.ConversationType.CHATROOM
                        || message.getChatType() == GMConstant.ConversationType.CLASSICROOM
                        || message.getChatType() == GMConstant.ConversationType.CAROLVOICEROOM) {
                    userId = message.getTo();
                } else {
                    // single chat message
                    userId = message.getFrom();
                }

                if (userId == toChatUserId) {
                    curMessage = message;
                    isCurrentChat = true;
                    break;
                }
            }

            if (isCurrentChat) {
                // 当前会话则刷新列表
                refreshAdapterList();
            }

            return true;
        }

        @Override
        public boolean onMessageRead(List<GMMessage> messages) {
            refreshAdapterList();
            return true;
        }

        @Override
        public boolean onMessageDelivered(List<GMMessage> messages) {
            refreshAdapterList();
            return true;
        }

        @Override
        public boolean onMessageChanged(GMMessage message, Object change) {
            refreshAdapterList();
            return true;
        }

        @Override
        public boolean onCmdMessageReceived(List<GMMessage> messages, boolean bInputing) {
            refreshAdapterList();
            return true;
        }
    };


    private GMChatroomAppListener roomListener = new GMChatroomAppListener() {
        @Override
        public void onChatRoomChaged() {
            refreshAdapterList();
            initChatroomInfo(toChatUserId);
        }
    };

    private void refreshAdapterList() {
        if (conversation == null) {
            reInitConversation();
            return;
        }
        //如果当前处于会话底部很少的距离（ 两条信息）则直接拉到底部，其他则只刷新
        if (messageAdapter != null) {
            if (mPtrListView.getRefreshableView() != null
                    && mPtrListView.getRefreshableView().getLastVisiblePosition() + 2 >= messageAdapter.getCount()) {
                messageAdapter.refreshSelectLast();
            } else {
                messageAdapter.refresh();
            }
        }
    }

    private void reInitConversation() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                GMChatManager chatManager = GMClient.getInstance().chatManager();
                if (chatManager == null) {
                    return;
                }
                conversation = chatManager.getConversation(
                        GMCommonUtils.getConversationIdByUserId(String.valueOf(toChatUserId), chatType), chatType, false);
                mh.sendEmptyMessage(RE_GET_CONVERSATION_DONE);
                if (conversation != null) {
                    conversation.markAllMessagesAsRead();
                }
            }
        }).start();
    }

    private void refreshList() {
        if (messageAdapter != null && conversation != null) {
            messageAdapter.setConversation(conversation);
        }
        if (conversation != null) {
            refreshAdapterList();
        }
    }

    private void showWaitInfo(String msg) {
        if (progressDialogEx == null) {
            progressDialogEx = new ProgressDialogEx(getContext());
            progressDialogEx.setAutoDismiss(true);
            progressDialogEx.setDuration(30000);
        }

        try {
            progressDialogEx.show(msg);
        } catch (Exception e) {

        }
    }

    private void dismissWait() {
        if (progressDialogEx != null && progressDialogEx.isShowing()) {
            progressDialogEx.dismiss();
        }
    }

    private MessageListItemClickListener messageListItemClickListener = new MessageListItemClickListener() {
        @Override
        public void onUserAvatarClick(String uid) {
        }

        @Override
        public void onUserAvatarLongClick(GMMessage message, long uid) {
        }

        @Override
        public void onResendClick(final GMMessage message) {
            new GMAlertDialog(getContext(), R.string.resend, R.string.confirm_resend, null,
                    new GMAlertDialog.AlertDialogUser() {
                        @Override
                        public void onResult(boolean confirmed, Bundle bundle) {
                            if (!confirmed) {
                                return;
                            }
                            resendMessage(message);
                        }
                    }, true).show();
        }

        @Override
        public void onBubbleLongClick(GMMessage message) {
            showPopMsgLongClick(message);
        }

        @Override
        public boolean onBubbleClick(GMMessage message) {
            if (message != null && message.getBodyType() == GMConstant.MsgBodyType.IMAGE) {
                showImagePreviewActivity(message);
            }
            return false;
        }

        @Override
        public void onTextAutoLinkClick(GMMessage message, String data, int type) {
        }
    };

    private void showImagePreviewActivity(final GMMessage curMessage) {
        showWaitInfo(getString(R.string.please_wait));
        GMUserUtil.getInstance().getSonWorkHandler().post(new Runnable() {
            @Override
            public void run() {
                GMChatManager chatManager = GMClient.getInstance().chatManager();
                if (chatManager == null) {
                    return;
                }
                conversation = chatManager.getConversation(
                        GMCommonUtils.getConversationIdByUserId(String.valueOf(toChatUserId), chatType), chatType, false);
                if (conversation == null) {
                    return;
                }
                GMMessage lastMessage = conversation.getLastMessage();
                if (chatType == GMConstant.ConversationType.CLASSICROOM) {
                    if (messageAdapter != null) {
                        lastMessage = messageAdapter.getLastMessage();
                    }
                }
                if (lastMessage == null) {
                    return;
                }

                List<GMMessage> listMessages = conversation.searchMsgWithType(GMConstant.MsgBodyType.IMAGE,
                        lastMessage.getMsgId() + 1, Integer.MAX_VALUE, -1, GMConstant.ConversationDirection.UP);
                if (listMessages != null && listMessages.size() > 0) {
                    int position = 0;
                    final ArrayList<String> image_list = new ArrayList<String>();
                    final HashMap<String, String> thumbnail_map = new HashMap<String, String>();
                    for (GMMessage message : listMessages) {
                        if (message != null && message.getBodyType() == GMConstant.MsgBodyType.IMAGE) {
                            GMImageMessageBody imgBody = (GMImageMessageBody) message.getMsgBody();
                            if (imgBody == null) {
                                continue;
                            }
                            if (message.getMsgId() == curMessage.getMsgId()) {
                                position = image_list.size();
                            }

                            String thumbPath = null;
                            if (message.getDirection() == GMConstant.MsgDirection.SEND) {
                                thumbPath = GlideUtil.getGlideCachePath(imgBody.getLocalPath());
                                image_list.add(imgBody.getLocalPath());
                                if (!TextUtils.isEmpty(thumbPath)) {
                                    thumbPath = GlideUtil.getGlideCachePath(imgBody.getRemotePath());
                                }
                            } else {
                                image_list.add(imgBody.getRemotePath());
                                thumbPath = GlideUtil.getGlideCachePath(imgBody.getRemotePath());
                            }

                            thumbnail_map.put(imgBody.getRemotePath(), thumbPath);
                        }
                    }

                    //主线程跳转
                    final int curPos = position;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dismissWait();
//                            goToPreviewActivity(image_list, thumbnail_map, curPos);
                        }
                    });
                } else {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dismissWait();
                        }
                    });
                }
            }
        });
    }

    private void showPopMsgLongClick(final GMMessage message) {
        if (message != null && message.getBodyType() == GMConstant.MsgBodyType.VOICE)
        {
            if (GMVoicePlayManager.isPlaying)
            {
                GMVoicePlayManager.getInstance().stopVoice();
            }
        }
        if (message != null && message.getMsgBody() != null)
        {
            ArrayList<TextSet> list = new ArrayList<TextSet>();
            TextSet set;
            //删除
            TextSet setDelete = new TextSet(R.string.em_message_delete, false, new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    showDeleteComfirm(message);
                }
            });
            TextSet revokeSet = null;
            switch (message.getBodyType())
            {
                case VOICE:
                    //VOICE --- 撤回，删除
                    if (revokeSet != null)
                    {
                        list.add(revokeSet);
                    }
                    list.add(setDelete);
                    break;

                default:
                    list.add(setDelete);
                    break;
            }
            bShowedLongClick = true;
            PopupWindowUtil.showPopWindow(getContext(), rootView, 0, list, true);
        }
    }

    private void showDeleteComfirm(final GMMessage message) {
        if (message != null && message.getMsgBody() != null) {
            TextSet setDelete = new TextSet(R.string.em_message_delete_sure, true, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        deleteMessageFromDB(message);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), R.string.Delete_failed, Toast.LENGTH_SHORT).show();
                    }
                }
            });
            PopupWindowUtil.showPopWindow(getContext(), rootView, R.string.em_message_delete_confirm, setDelete,
                    null, true);
        }
    }

    private void deleteMessageFromDB(final GMMessage message) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (message == null) {
                    return;
                }
                if (conversation != null) {
                    conversation.removeMessage(message.getMsgId());
                }
                if (messageAdapter != null) {
                    messageAdapter.refresh();
                }
            }
        }).start();
    }

    private void addShareToCommunity(ArrayList<TextSet> list) {
    }

    class MyItemClickListener implements GMChatExtendMenu.GMChatExtendMenuItemClickListener {
        @Override
        public void onClick(int itemId, View view) {

            Intent intent = null;
            switch (itemId) {
                case ITEM_TAKE_PICTURE:
                    selectPicFromCamera();
                    break;

                case ITEM_PICTURE:
                    selectPicFromLocal();
                    break;

                case ITEM_LOCATION:
                    intent = new Intent(getContext(), EmLocationActivity.class);
                    intent.putExtra("from", EmLocationActivity.FROM_CHAT_ACTION);
                    startActivityForResult(intent, REQUEST_CODE_MAP);
                    break;

                case ITEM_RED_PACKET:
                    //先绑定微信
                    if (isWechatBinded() == false) {
                        Intent intentBindWechat = new Intent(getContext(), BindWechatActivity.class);
                        intentBindWechat.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                        startActivity(intentBindWechat);
                        return;
                    }
                    //去包红包
                    if (chatType == GMConstant.ConversationType.CHAT) {
                        intent = new Intent(getContext(), CreateRedPacketActivity.class);
                        intent.putExtra(CreateRedPacketActivity.EXTRA_TYPE_DISPLAY,
                                RedPacketConstant.RP_DISPLAY_CHAT_SINGLE);
                        intent.putExtra(CreateRedPacketActivity.EXTRA_SPECIFIED_ID, String.valueOf(toChatUserId));
                        startActivity(intent);
                    } else if (room != null && isChatroom()) {
                        intent = new Intent(getContext(), CreateRedPacketActivity.class);
                        intent.putExtra(CreateRedPacketActivity.EXTRA_TYPE_DISPLAY, RedPacketConstant
                                .RP_DISPLAY_CHAT_GROUP);
                        intent.putExtra(CreateRedPacketActivity.EXTRA_PEOPLE_COUNT, room.getOccupantsCount());
                        intent.putExtra(CreateRedPacketActivity.EXTRA_SPECIFIED_ID, String.valueOf(toChatUserId));
                        startActivity(intent);
                    }
                    break;

                default:
                    break;
            }
        }
    }

    private boolean isWechatBinded() {
//        CommunityUser user = AllOnlineApp.getCommunityUser();
//        if (!user.isWechatBinded()) {
//            return false;
//        } else {
//            return true;
//        }
        return true;
    }

    protected void selectPicFromLocal() {
        CameraSdkParameterInfo mCameraSdkParameterInfo = new CameraSdkParameterInfo();
        mCameraSdkParameterInfo.setSingle_mode(false);
        mCameraSdkParameterInfo.setShow_camera(true);
        mCameraSdkParameterInfo.setMax_image(9);
        mCameraSdkParameterInfo.setCroper_image(true);
        mCameraSdkParameterInfo.setFilter_image(false);
        Intent intent = new Intent(getContext(), PhotoPickActivity.class);

        Bundle b = new Bundle();
        b.putSerializable(CameraSdkParameterInfo.EXTRA_PARAMETER, mCameraSdkParameterInfo);
        b.putBoolean(PhotoPickActivity.FROM_IM, true);
        intent.putExtras(b);
        startActivityForResult(intent, CameraSdkParameterInfo.TAKE_PICTURE_FROM_GALLERY);
    }

    protected void selectPicFromCamera() {
        if (!GMCommonUtils.isSdcardExist()) {
            Toast.makeText(getContext(), R.string.sd_card_does_not_exist, Toast.LENGTH_SHORT).show();
            return;
        }

        cameraFile = new File(PathUtil.getInstance().getImagePath(),
                GMClient.getInstance().getCurrentUserId() + System.currentTimeMillis() + ".jpg");
        cameraFile.getParentFile().mkdirs();
        startActivityForResult(
                new Intent(MediaStore.ACTION_IMAGE_CAPTURE).putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(cameraFile)),
                REQUEST_CODE_CAMERA);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_CAMERA) {
                // capture new image
                if (cameraFile != null && cameraFile.exists()) {
                    GMUserUtil.getInstance().getSonWorkHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            CommunityImageInfo imageInfo = new CommunityImageInfo();
                            imageInfo.setSource_image(cameraFile.getAbsolutePath());
                            ImageCompressUtils.compress(getContext(), imageInfo);
                            sendImageMessage(imageInfo.getSource_image());
                        }
                    });
                }
            } else if (requestCode == CameraSdkParameterInfo.TAKE_PICTURE_FROM_GALLERY) {
                if (data != null) {
                    Bundle bundle = data.getExtras();
                    if (bundle != null) {
                        CameraSdkParameterInfo mCameraSdkParameterInfo = (CameraSdkParameterInfo) bundle
                                .getSerializable(CameraSdkParameterInfo.EXTRA_PARAMETER);
                        final ArrayList<String> list = mCameraSdkParameterInfo.getImage_list();
                        if (list != null) {
                            for (String path : list) {
                                if (!TextUtils.isEmpty(path)) {
                                    sendImageMessage(path);
                                }
                            }
                        }
                    }
                }
            } else if (requestCode == REQUEST_CODE_MAP) {
                // location
                double latitude = data.getDoubleExtra("latitude", 0);
                double longitude = data.getDoubleExtra("longitude", 0);
                String locationAddress = data.getStringExtra("textAddress");
                if ((int) latitude == 0 && (int) longitude == 0) {
                    //无效的地理位置信息
                    Toast.makeText(getContext(), R.string.unable_to_get_loaction, Toast.LENGTH_SHORT).show();
                } else {
                    sendLocationMessage(latitude, longitude, locationAddress);
                }
            } else if (requestCode == REQUEST_CODE_EM_AT_SELECT) {
                if (data != null && data.hasExtra(GmSelectAtUserActivity.SELECT_AT_USER_NAME)) {
                    //弹出用户列表选择了用户后返回
                    long uid = data.getLongExtra(GmSelectAtUserActivity.SELECT_AT_USER_ID, -1);
                    String nick = data.getStringExtra(GmSelectAtUserActivity.SELECT_AT_USER_NAME);
                }
            } else if (requestCode == REQUEST_CODE_CHAT_SETTUNG) {
            }
        } else if (resultCode == GmChatSettingsActivity.RESULT_AT_CONTENT) {
            if (requestCode == REQUEST_CODE_CHAT_SETTUNG) {
                if (data != null) {
                    String content = data.getStringExtra(Constant.DATA_TEXT);
                    if (content == null) {
                        content = "";
                    }
                    content = getString(R.string.group_at_all) + " " + content;
                    sendAtMessage(content);
                }
            }
        } else if (resultCode == GmChatSettingsActivity.RESULT_EXIT_GROUP) {
            //退群
            if (requestCode == REQUEST_CODE_CHAT_SETTUNG) {
                deleteConversation();
//                finish();
            }
        }
    }

    private void deleteConversation() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean delete = GMClient.getInstance().chatManager().deleteConversation(String.valueOf(toChatUserId),
                        true);
                if (!delete) {
                    GMLog.i(TAG, "delete conversation fail id=" + toChatUserId);
                }
            }
        }).start();
    }

    private void toChatroomSetting() {
        Intent intent = new Intent(getContext(), GmChatSettingsActivity.class);
        intent.putExtra(GMAppConstant.EXTRA_USER_ID, toChatUserId);
        startActivityForResult(intent, REQUEST_CODE_CHAT_SETTUNG);
    }

    private void startToSetting() {
    }

    /**********************************************/
    protected void sendTextMessage(String content) {
        msgSendController.sendTextMessage(content);
    }

    /**
     * send @ message, only support group chat message
     *
     * @param content
     */
    public void sendAtMessage(String content) {
        if (!isChatroom()) {
            return;
        }
        msgSendController.sendAtMessage(content);
    }

    //发送表情消息
    protected void sendBigExpressionMessage(String name, String identityCode) {
        msgSendController.sendBigExpressionMessage(name, identityCode);
    }

    //发送语音消息
    protected void sendVoiceMessage(String filePath, int length) {
        msgSendController.sendVoiceMessage(filePath, length);
    }

    //发送图片消息
    protected void sendImageMessage(String imagePath) {
        msgSendController.sendImageMessage(imagePath);
    }

    /***********发送红包信息***********/
    protected void sendRedPacketMessage(int redPacketType, String redpacket_id, String title) {
        msgSendController.sendRedPacketMessage(redPacketType, redpacket_id, title);
    }

    //发送地址消息-
    protected void sendLocationMessage(double latitude, double longitude, String locationAddress) {
        msgSendController.sendLocationMessage(latitude, longitude, locationAddress);
    }

    //发送视频消息
    protected void sendVideoMessage(String videoPath, String thumbPath, int videoLength) {
        msgSendController.sendVideoMessage(videoPath, thumbPath, videoLength);
    }

    //发送文件消息
    protected void sendFileMessage(String filePath) {
        msgSendController.sendFileMessage(filePath);
    }

    //发送发送消息
    protected void sendMessage(GMMessage message, JsonObject jsonObject) {
        msgSendController.sendMessage(message, jsonObject);
    }

    /***********发送领取了红包的提醒信息***********/
    private void sendRedpacketCMDMessage(final String redpacketHostUid, final String redpacketHostName,
                                         String redpacket_id, int status) {
        msgSendController.sendRedpacketCMDMessage(redpacketHostUid, redpacketHostName, redpacket_id, status);
    }

    //重新发送消息
    public void resendMessage(GMMessage message) {
        msgSendController.resendMessage(message);
    }

    //发送图片消息
    protected void sendPicByUri(Uri selectedImage) {
        msgSendController.sendPicByUri(selectedImage);
    }

    //发送文件消息
    protected void sendFileByUri(Uri uri) {
        msgSendController.sendFileByUri(uri);
    }

    //消息撤回
    private void revokeMessage(GMMessage message) {
        msgSendController.revokeMessage(message);
    }

    private void sendInputingCMDMessage() {
        if (chatType != GMConstant.ConversationType.CHAT) {
            return;
        }
        msgSendController.sendInputingCMDMessage();
    }

    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event == null) {
            return;
        }
        float[] its = event.values;
        if (its != null && its.length > 0 && event.sensor != null && event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            int mode = AudioManager.MODE_NORMAL;
            // 经过测试，当手贴近距离感应器的时候its[0]返回值为0.0，当手离开时返回1.0
            if (its[0] == 0.0) {
                // 贴近手机
                GMImManager.getInstance().setSpeakerOpened(false);
                mode = AudioManager.MODE_IN_COMMUNICATION;
            } else {
                // 远离手机
                GMImManager.getInstance().setSpeakerOpened(true);
                mode = AudioManager.MODE_NORMAL;
            }
            if (GMVoicePlayManager.isPlaying && !GMVoicePlayManager.getInstance().isWiredHeadsetOn()) {
                GMVoicePlayManager.getInstance().updatePlayMode(mode);
            }
        }
    }

    private void sendRedPacket(final RedPacketInfo redPacketInfo) {
        if (redPacketInfo != null && (redPacketInfo.getDisplay_type() == RedPacketConstant.RP_DISPLAY_CHAT_GROUP ||
                (redPacketInfo.getDisplay_type() == RedPacketConstant.RP_DISPLAY_CHAT_SINGLE
                        && redPacketInfo.getToChatId().equals(String.valueOf(toChatUserId))))) {
            sendRedPacketMessage(redPacketInfo.getPacket_type(), redPacketInfo.getRedpacket_id(),
                    redPacketInfo.getHello_words());
            //发送命令给后台，便于标记状态
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Result result = AllOnlineApp.mApiClient.sendRedPacket(this.hashCode(), CommonUtil.getTicket(),
                            redPacketInfo.getRedpacket_id());
                }
            }).start();
        }
    }

    private void release() {
        if (GMUserUtil.getInstance().getSonWorkHandler() != null) {
            GMUserUtil.getInstance().getSonWorkHandler().removeCallbacksAndMessages(null);
        }
        if (groupListener != null) {
            GMClient.getInstance().groupManager().removeGroupChangeListener(groupListener);
        }
        if (roomListener != null) {
            GMImManager.getInstance().removeChatroomListener(roomListener);
        }
        if (mManager != null) {
            mManager.unregisterListener(this);// 注销传感器监听
            GMImManager.getInstance().setSpeakerOpened(true);
        }
        GMImManager.getInstance().removeGMMessageListener(gmMessageAppListener);
        gmMessageAppListener = null;
    }

    /**
     * listen the group event
     */
    class GroupListener extends GMGroupListener {
        @Override
        public void onUserRemoved(final String groupId, String groupName) {
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    if (groupId.equals(String.valueOf(toChatUserId))) {
                        Toast.makeText(getContext(), R.string.you_are_group, Toast.LENGTH_LONG).show();
                        if (!getActivity().isFinishing()) {
                            getActivity().finish();
                        }
                    }
                }
            });
        }

        @Override
        public void onGroupDestroyed(final String groupId, String groupName) {
            // prompt group is dismissed and finish this activity
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    if (groupId.equals(String.valueOf(toChatUserId))) {
                        Toast.makeText(getContext(), R.string.the_current_group_destroyed, Toast.LENGTH_LONG)
                                .show();
                        if (!getActivity().isFinishing()) {
                            getActivity().finish();
                        }
                    }
                }
            });
        }
    }

}

