package com.coomix.app.all.ui.im;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.Constant;
import com.coomix.app.all.R;
import com.coomix.app.all.dialog.ProgressDialogEx;
import com.coomix.app.all.manager.ServerTimeManager;
import com.coomix.app.all.model.response.CommunityImageInfo;
import com.coomix.app.all.model.response.CommunityUser;
import com.coomix.app.all.share.PopupWindowUtil;
import com.coomix.app.all.share.TextSet;
import com.coomix.app.all.ui.base.BaseActivity;
import com.coomix.app.all.ui.login.LoginActivity;
import com.coomix.app.all.ui.wallet.BindWechatActivity;
import com.coomix.app.all.ui.web.BusAdverActivity;
import com.coomix.app.all.util.CommunityUtil;
import com.coomix.app.all.util.GlideUtil;
import com.coomix.app.all.util.ImageCompressUtils;
import com.coomix.app.all.manager.SettingDataManager;
import com.coomix.app.all.widget.MyActionbar;
import com.coomix.app.framework.app.Result;
import com.coomix.app.framework.util.CommonUtil;
import com.coomix.app.framework.util.TimeUtil;
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
import com.goomeim.domain.GMEmojicon;
import com.goomeim.model.GMAtMessageHelper;
import com.goomeim.model.GMNotifier;
import com.goomeim.ui.GMGroupListener;
import com.goomeim.utils.GMCommonUtils;
import com.goomeim.utils.GMHyperLinkValue;
import com.goomeim.utils.GMSmileUtils;
import com.goomeim.utils.GMUserUtil;
import com.goomeim.widget.GMAlertDialog;
import com.goomeim.widget.GMChatExtendMenu;
import com.goomeim.widget.GMChatInputMenu;
import com.goomeim.widget.GMChatPrimaryMenuBase;
import com.goomeim.widget.GMVoiceRecorderView;
import com.goomeim.widget.chatrow.GMChatRowVoicePlayClickListener;
import com.goomeim.widget.chatrow.GMVoicePlayManager;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.muzhi.camerasdk.PhotoPickActivity;
import com.muzhi.camerasdk.PreviewActivity;
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
import net.goome.im.chat.GMChatroomMemberInfo;
import net.goome.im.chat.GMClient;
import net.goome.im.chat.GMConstant.ConversationDirection;
import net.goome.im.chat.GMConstant.ConversationType;
import net.goome.im.chat.GMConstant.MsgBodyType;
import net.goome.im.chat.GMConstant.MsgDirection;
import net.goome.im.chat.GMConstant.MsgStatus;
import net.goome.im.chat.GMConversation;
import net.goome.im.chat.GMGroup;
import net.goome.im.chat.GMImageMessageBody;
import net.goome.im.chat.GMMessage;
import net.goome.im.chat.GMPageResult;
import net.goome.im.chat.GMTextMessageBody;
import net.goome.im.util.GMLog;
import net.goome.im.util.PathUtil;

/**
 * Created by ssl
 *
 * @since 2017/6/8.
 */
public class GMChatActivity extends BaseActivity implements View.OnClickListener, SensorEventListener
{
    private final String TAG = GMChatActivity.class.getSimpleName();
    public static final String TO_ALPHA = "to_appha";
    protected static final int REQUEST_CODE_MAP = 1;
    protected static final int REQUEST_CODE_CAMERA = 2;
    private final int REQUEST_CODE_CHAT_SETTUNG = 3;
    private static final int REQUEST_CODE_EM_CHAT_SETTINGS = 10000;
    public static final int REQUEST_CODE_EM_AT_SELECT = 10001;
    public static final int CMD_ON_INPUTING = 10002; //命令正在输入
    public static final int INPUTING_REVOKE_DELAY = 3000; //3秒

    private final int TWO_MINUTS = 2*65*1000; //撤回时间限制--两分钟加10s的误差
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
    private GMChatInputMenu inputMenu;
    private GMVoiceRecorderView voiceRecorderView;
    protected MyItemClickListener extendMenuItemClickListener;
    private File cameraFile;
    protected GMMessageAdapter messageAdapter;
    private long toChatUserId;
    private ConversationType chatType = ConversationType.CHAT;
    private boolean bKeyboardForceShow = false;
    private boolean bShowedLongClick = false;
    private View rootView;
    private ProgressDialogEx progressDialogEx;
    private CommunityUser mUser = null;
    private MessageSendController msgSendController;
    // 调用距离传感器，控制屏幕
    private SensorManager mManager;// 传感器管理对象
    //private ChatRoomListener chatRoomListener;
    private GroupListener groupListener;
    private GMConversation conversation;
    //当类型是聊天室时，存储聊天室信息
    private GMChatRoom room;
    //去社区
    private ImageView toSection;
    //转发到社区
    private TextView textShare2Community;
    private GMChatHandler mh;
    private MyActionbar actionbar;

    private static final int GET_CHATROOM_DONE = 0;
    private static final int GET_CONVERSATION_DONE = 1;
    private static final int RE_GET_CONVERSATION_DONE = 2;
    public static CommunityUser ALPHA_USER = null;
    private boolean bNotGoToAlpha = false;
    private Handler mHandler = new Handler(Looper.getMainLooper())
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case CMD_ON_INPUTING:
                    if(chatType == ConversationType.CHAT && mUser != null)
                    {
                        actionbar.setTitle(mUser.getName());
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        rootView = LayoutInflater.from(this).inflate(R.layout.activity_goome_chat, null);
        setContentView(rootView);

        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        mh = new GMChatHandler(this);

        if (getIntent() != null)
        {
            initIntentData(getIntent());
        }

        initUser();

        initViews();

        msgSendController = new MessageSendController(this, toChatUserId, chatType);
        msgSendController.setMessageRefreshListener(refreshListener);

        initData();

        Object obj = getSystemService(Context.SENSOR_SERVICE);
        if (obj != null && obj instanceof SensorManager)
        {
            mManager = (SensorManager) obj;
            mManager.registerListener(this, mManager.getDefaultSensor(Sensor.TYPE_PROXIMITY), // 距离感应器
                SensorManager.SENSOR_DELAY_NORMAL);// 注册传感器，第一个参数为距离监听器，第二个是传感器类型，第三个是延迟类型
        }

        registerReceiver();

        hideKeyBoard();

        if (getIntent() != null && getIntent().getBooleanExtra(TO_ALPHA, false))
        {
            //从广告或特殊帖跳到聊天的，要发送一个通知消息给对方（这种情况一般都是跟小酷聊天）
            msgSendController.sendAlphaCMDMessage();
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (messageAdapter != null)
        {
            messageAdapter.refresh();
        }

        if (isChatroom())
        {
            GMAtMessageHelper.get().removeAtMeGroup(String.valueOf(toChatUserId));
        }

        try
        {
            //重置角标计数
            GMNotifier gmNotifier = GMImManager.getInstance().getNotifier();
            if (gmNotifier != null)
            {
                gmNotifier.reset();
            }
            // 清空角标
            //BadgeUtil.resetBadgeCount(getApplicationContext());
        }
        catch (Exception e)
        {

        }
    }

    @Override
    protected void onDestroy()
    {
        release();
        if (mh != null)
        {
            mh.removeCallbacksAndMessages(null);
        }
        super.onDestroy();
    }

    private void initIntentData(Intent intent)
    {
        if (intent.hasExtra(GMAppConstant.EXTRA_USER_ID))
        {
            String groupId = intent.getStringExtra(GMAppConstant.EXTRA_USER_ID);
            toChatUserId = Long.valueOf(groupId);
        }
        if (intent.hasExtra(GMAppConstant.EXTRA_CHAT_TYPE))
        {
            chatType = (ConversationType) intent.getSerializableExtra(GMAppConstant.EXTRA_CHAT_TYPE);
        }
    }

    private void initUser()
    {
        if (getIntent() != null && getIntent().hasExtra(Constant.USER_DATA))
        {
            mUser = (CommunityUser) getIntent().getSerializableExtra(Constant.USER_DATA);
            if (mUser != null)
            {
                toChatUserId = Long.parseLong(mUser.getUid());
                if (chatType == ConversationType.CHAT && TextUtils.isEmpty(mUser.getName()))
                {
                    GMUserUtil.getInstance().getUserSync(mUser.getUid(), null, new GMUserUtil.GMUserUtilCallBackSingnle()
                    {
                        @Override
                        public void callbackUser(CommunityUser user)
                        {
                            mUser = user;
                            if(mUser != null)
                            {
                                actionbar.setTitle(mUser.getName());
                            }
                        }
                    });
                }
            }
            else
            {
                finish();
            }
        }
    }

    private void initViews()
    {
        actionbar = (MyActionbar) findViewById(R.id.myActionbar);
        actionbar.initActionbar(true, R.string.chat_images, 0, R.drawable.alarm_setting);
        actionbar.setRightImageClickListener(view -> {
            if (chatType == ConversationType.CHAT)
            {
                startToSetting();
            }
            else if (isChatroom())
            {
                toChatroomSetting();
            }
        });

        voiceRecorderView = (GMVoiceRecorderView) findViewById(R.id.voice_recorder);
        mPtrListView = (PullToRefreshListView) findViewById(R.id.listViewChat);
        mPtrListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        inputMenu = (GMChatInputMenu) findViewById(R.id.input_menu);

        if (chatType == ConversationType.CHAT)
        {
            // imageRight.setImageResource(R.drawable.chat_right);
        }
        else if (chatType == ConversationType.GROUP)
        {
            // imageRight.setImageResource(R.drawable.chat_right);
        }
        else if (isChatroom())
        {
            actionbar.setRightImageResource(R.drawable.ease_group_topbar_icon);
        }

        initInputMenu();

        mPtrListView.getRefreshableView().setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if (bKeyboardForceShow)
                {
                    return true;
                }

                hideKeyBoard();
                inputMenu.hideExtendMenuContainer();
                return false;
            }
        });

        mPtrListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>()
        {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView)
            {
                //下拉加载之前的消息记录，请求之前的消息
                requestPreviousMsg();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView)
            {
                //没有上拉
            }
        });

        //社区相关
        toSection = (ImageView) findViewById(R.id.to_community_section_Button);
        toSection.setOnClickListener(this);
        toSection.setVisibility(View.GONE);
        textShare2Community = (TextView) findViewById(R.id.share_to_community);

        if(mPtrListView.getRefreshableView() != null)
        {
            mPtrListView.getRefreshableView().setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
        }
    }

    private boolean isChatroom()
    {
        return (chatType == ConversationType.CHATROOM || chatType == ConversationType.CLASSICROOM);
    }

    static class GMChatHandler extends Handler
    {
        private WeakReference<GMChatActivity> mActivity;

        GMChatHandler(GMChatActivity activity)
        {
            mActivity = new WeakReference<GMChatActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg)
        {
            GMChatActivity m = mActivity.get();
            if (m == null)
            {
                return;
            }
            switch (msg.what)
            {
                case GET_CHATROOM_DONE:
                    m.showChatroomInfo();
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

    private void showChatroomInfo()
    {
        if (room == null)
        {
            return;
        }
        else
        {
            if (messageAdapter != null)
            {
                messageAdapter.setChatOwnerId(room.getOwner());
            }
            //显示标题
            actionbar.setTitle(room.getSubject() + "(" + room.getOccupantsCount() + ")");
            //社区转发
            toSection.setVisibility(View.GONE);

            //更新群成员
            updateChatroomMemeberList();
        }
    }

    private void updateChatroomMemeberList()
    {
        //5天全量更新一次member list
        if (room != null
            && System.currentTimeMillis() - room.getLastTimeToGetMemberListFromServer() > TimeUtil.SECONDS_IN_A_DAY * 5)
        {
            Log.i("felix", "room not null and time="+(System.currentTimeMillis()-room.getLastTimeToGetMemberListFromServer()));
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    getMemberList(room.getChatroomId(), 0);
                }
            }).start();
        }
    }

    private void getMemberList(final long roomid, final int pos)
    {
        GMChatRoomManager mgr = GMClient.getInstance().chatroomManager();
        if (mgr == null)
        {
            return;
        }
        mgr.getChatroomMemberListFromServerWithId(roomid, pos, 500,
            new GMValueCallBack<GMPageResult<GMChatroomMemberInfo>>()
            {
                @Override
                public void onSuccess(GMPageResult<GMChatroomMemberInfo> result)
                {
                    int currPos = result.getNextPos();
                    if (currPos != 0)
                    {
                        getMemberList(roomid, currPos);
                    }
                    else
                    {
                        GMLog.i(TAG, "get chatroom member list done");
                    }
                }

                @Override
                public void onError(GMError gmError)
                {
                }
            });
    }

    private void requestPreviousMsg()
    {
        if (conversation == null)
        {
            mPtrListView.onRefreshComplete();
            return;
        }

        GMUserUtil.getInstance().getSonWorkHandler().post(new Runnable()
        {
            @Override
            public void run()
            {
                GMMessage message = messageAdapter.getItem(0);
                final List<GMMessage> listNewMsgs;
                if (message != null)
                {
                    listNewMsgs = conversation.loadMoreMsgFromDB(message.getMsgId(), MAX_PER_PAGE_MSG);
                }
                else
                {
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            mPtrListView.onRefreshComplete();
                        }
                    });
                    return;
                }

                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        mPtrListView.onRefreshComplete();
                        if (listNewMsgs == null || listNewMsgs.size() <= 0)
                        {
                            mPtrListView.setMode(PullToRefreshBase.Mode.DISABLED);
                            View view = LayoutInflater.from(GMChatActivity.this).inflate(R.layout.footer_no_more_data,
                                null);
                            TextView textView = (TextView) view.findViewById(R.id.textView);
                            textView.setText(R.string.no_previous_data);
                            mPtrListView.getRefreshableView().addHeaderView(view);
                        }
                        else
                        {
                            if (messageAdapter != null)
                            {
                                messageAdapter.refreshSeekTo(listNewMsgs.size());
                            }
                        }
                    }
                });
            }
        });
    }

    private void initData()
    {
        if (isChatroom())
        {
            initChatroomInfo(toChatUserId);
        }

        initConversation();

        if (chatType == ConversationType.CHAT)
        {
            if (mUser != null)
            {
                actionbar.setTitle(mUser.getName());
            }
        }
        else if (chatType == ConversationType.GROUP)
        {
            GMGroup group = GMClient.getInstance().groupManager().getGroup(String.valueOf(toChatUserId));
            if (group != null)
            {
                actionbar.setTitle(group.getGroupName() + "(" + group.getMemberCount() + ")");
            }
            // listen the event that user moved out group or group is dismissed
            groupListener = new GroupListener();
            GMClient.getInstance().groupManager().addGroupChangeListener(groupListener);
        }
        else if (isChatroom())
        {
            GMImManager.getInstance().addChatroomListener(roomListener);
        }
        //Todo 增加新聊天类型
    }

    private void initChatroomInfo(final long roomid)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                GMChatRoomManager mgr = GMClient.getInstance().chatroomManager();
                if (mgr == null)
                {
                    return;
                }
                room = mgr.getChatroomInfoFromDB(roomid);
                mh.sendEmptyMessage(GET_CHATROOM_DONE);
                if (room == null)
                {
                    mgr.getChatroomSpecificationFromServerWithId(roomid, new GMValueCallBack<GMChatRoom>()
                    {
                        @Override
                        public void onSuccess(GMChatRoom aroom)
                        {
                            if (aroom != null)
                            {
                                room = aroom;
                            }
                            mh.sendEmptyMessage(GET_CHATROOM_DONE);
                        }

                        @Override
                        public void onError(GMError gmError)
                        {

                        }
                    });
                }
            }
        }).start();
    }

    private void initConversation()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                GMChatManager chatManager = GMClient.getInstance().chatManager();
                if (chatManager == null)
                {
                    return;
                }
                conversation = chatManager.getConversation(
                    GMCommonUtils.getConversationIdByUserId(String.valueOf(toChatUserId), chatType), chatType, false);
                mh.sendEmptyMessage(GET_CONVERSATION_DONE);
                if (conversation != null)
                {
                    conversation.markAllMessagesAsRead();
                }
            }
        }).start();
        GMImManager.getInstance().addGMMessageListener(gmMessageAppListener);
    }

    private void initAdapter()
    {
        messageAdapter = new GMMessageAdapter(this, String.valueOf(toChatUserId), chatType, conversation,
            mPtrListView.getRefreshableView());
        messageAdapter.setShowAvatar(true);
        if (isChatroom())
        {
            messageAdapter.setShowUserNick(true);
        }
        else
        {
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
            if (message != null && message.getDirection() == MsgDirection.RECEIVE && message.getBodyType() ==
                    MsgBodyType.VOICE && GMCommonUtils.getMessageExtInt(message, GMAppConstant.GOOME_VOICE_LISTENED, 0)
                    == 0)
            {
                return i;
            }
        }
        return -1;
    }
    private MessageSendController.OnMessageRefreshListener refreshListener
        = new MessageSendController.OnMessageRefreshListener()
    {
        @Override
        public void onRefresh()
        {
            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    if (messageAdapter != null)
                    {
                        messageAdapter.refresh();
                    }
                }
            });
        }

        @Override
        public void onRefreshAndSelectLast()
        {
            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    if (messageAdapter != null)
                    {
                        messageAdapter.refreshSelectLast();
                    }
                }
            });
        }
    };

    private GMMessageAppListener gmMessageAppListener = new GMMessageAppListener()
    {
        @Override
        public boolean onMessageReceived(List<GMMessage> messages)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && isDestroyed())
            {
                return false;
            }

            boolean isCurrentChat = false;
            GMMessage curMessage = null;
            for (final GMMessage message : messages)
            {
                if (message == null)
                {
                    continue;
                }
                long userId = 0;
                if (message.getChatType() == ConversationType.CHATROOM
                    || message.getChatType() == ConversationType.CLASSICROOM)
                {
                    userId = message.getTo();
                }
                else
                {
                    // single chat message
                    userId = message.getFrom();
                }

                if (userId == toChatUserId)
                {
                    curMessage = message;
                    isCurrentChat = true;
                    break;
                }
            }

            if(isCurrentChat)
            {
                // 当前会话则刷新列表
                refreshAdapterList();

                //响铃震动控制
                //GMImManager.getInstance().getNotifier().vibrateAndPlayTone(message);
                if (curMessage != null && curMessage.getChatType() == ConversationType.CHAT
                        && curMessage.getDirection() == MsgDirection.RECEIVE)
                {
                    //如果是私聊，没有获取到对方的昵称，则利用收到消息中携带的昵称展示
                    final String name = GMCommonUtils.getMessageExtString(curMessage, GMAppConstant.GOOME_NICKNAME, "");
                    if(TextUtils.isEmpty(mUser.getName()) && !TextUtils.isEmpty(name))
                    {
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                if(mUser == null)
                                {
                                    mUser = new CommunityUser();
                                    mUser.setUid(String.valueOf(toChatUserId));
                                }
                                mUser.setName(name);
                                actionbar.setTitle(mUser.getName());
                            }
                        });
                    }
                }
            }

            return true;
        }

        @Override
        public boolean onMessageRead(List<GMMessage> messages)
        {
            refreshAdapterList();
            return true;
        }

        @Override
        public boolean onMessageDelivered(List<GMMessage> messages)
        {
            refreshAdapterList();
            return true;
        }

        @Override
        public boolean onMessageChanged(GMMessage message, Object change)
        {
            refreshAdapterList();
            return true;
        }

        @Override
        public boolean onCmdMessageReceived(List<GMMessage> messages, boolean bInputing)
        {
            if(bInputing)
            {
                //显示正在输入
                showOnInputing();
            }
            else
            {
                refreshAdapterList();
            }
            return true;
        }
    };

    private void showOnInputing()
    {
        if(chatType != ConversationType.CHAT)
        {
            return;
        }

        if(mHandler.hasMessages(CMD_ON_INPUTING))
        {
            mHandler.removeMessages(CMD_ON_INPUTING);
        }
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                actionbar.setTitle(R.string.chat_inputing);
            }
        });
        mHandler.sendEmptyMessageDelayed(CMD_ON_INPUTING, INPUTING_REVOKE_DELAY);
    }

    private GMChatroomAppListener roomListener = new GMChatroomAppListener()
    {
        @Override
        public void onChatRoomChaged()
        {
            refreshAdapterList();
            initChatroomInfo(toChatUserId);
        }
    };

    private void refreshAdapterList()
    {
        if (conversation == null)
        {
            reInitConversation();
            return;
        }
        //如果当前处于会话底部很少的距离（ 两条信息）则直接拉到底部，其他则只刷新
        if (messageAdapter != null)
        {
            if (mPtrListView.getRefreshableView() != null
                && mPtrListView.getRefreshableView().getLastVisiblePosition() + 2 >= messageAdapter.getCount())
            {
                messageAdapter.refreshSelectLast();
            }
            else
            {
                messageAdapter.refresh();
            }
        }
    }

    private void reInitConversation()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                GMChatManager chatManager = GMClient.getInstance().chatManager();
                if (chatManager == null)
                {
                    return;
                }
                conversation = chatManager.getConversation(
                    GMCommonUtils.getConversationIdByUserId(String.valueOf(toChatUserId), chatType), chatType, false);
                mh.sendEmptyMessage(RE_GET_CONVERSATION_DONE);
                if (conversation != null)
                {
                    conversation.markAllMessagesAsRead();
                }
            }
        }).start();
    }

    private void refreshList()
    {
        if (messageAdapter != null && conversation != null)
        {
            messageAdapter.setConversation(conversation);
        }
        if (conversation != null)
        {
            refreshAdapterList();
        }
    }

    private void showWaitInfo(String msg)
    {
        if (progressDialogEx == null)
        {
            progressDialogEx = new ProgressDialogEx(this);
            progressDialogEx.setAutoDismiss(true);
            progressDialogEx.setDuration(30000);
        }

        try
        {
            progressDialogEx.show(msg);
        }
        catch (Exception e)
        {

        }
    }

    private void dismissWait()
    {
        if (progressDialogEx != null && progressDialogEx.isShowing())
        {
            progressDialogEx.dismiss();
        }
    }

    private void initInputMenu()
    {
        registerExtendMenuItem();
        inputMenu.init(null);
        inputMenu.setChatInputMenuListener(new GMChatInputMenu.ChatInputMenuListener()
        {
            @Override
            public void onSendMessage(String content)
            {
                sendTextMessage(content);
            }

            @Override
            public boolean onPressToSpeakBtnTouch(View v, MotionEvent event)
            {
                return voiceRecorderView.onPressToSpeakBtnTouch(v, event,
                    new GMVoiceRecorderView.EaseVoiceRecorderCallback()
                    {
                        @Override
                        public void onVoiceRecordComplete(String voiceFilePath, int voiceTimeLength)
                        {
                            sendVoiceMessage(voiceFilePath, voiceTimeLength);
                        }
                    });
            }

            @Override
            public void onBigExpressionClicked(GMEmojicon emojicon)
            {
                sendBigExpressionMessage(emojicon.getName(), emojicon.getIdentityCode());
            }

            @Override
            public void onKeyboardShow()
            {
                if (messageAdapter != null)
                {
                    messageAdapter.selectLastDelay(200);
                }
            }

            @Override
            public void onInputing()
            {
                //发送正在输入的透传消息
                sendInputingCMDMessage();
            }
        });

        inputMenu.getPrimaryMenu().setOnAtTriggerListener(new GMChatPrimaryMenuBase.OnAtTriggerListener()
        {
            @Override
            public void onAtTrigger()
            {
                hideKeyBoard();
                Intent intent = new Intent(GMChatActivity.this, GmSelectAtUserActivity.class);
                intent.putExtra(GMAppConstant.EXTRA_USER_ID, toChatUserId);
                startActivityForResult(intent, REQUEST_CODE_EM_AT_SELECT);
                try
                {
                    overridePendingTransition(R.anim.push_bottom_in, 0);
                }
                catch (Exception e)
                {
                }
            }
        });
    }

    protected void registerExtendMenuItem()
    {
        extendMenuItemClickListener = new MyItemClickListener();
        for (int i = 0; i < itemStrings.length; i++)
        {
            inputMenu.registerExtendMenuItem(itemStrings[i], itemdrawables[i], itemIds[i], extendMenuItemClickListener);
        }
    }

    protected void hideKeyBoard()
    {
        if (inputMenu != null)
        {
            inputMenu.hideKeyboard();
        }
    }

    private void showKeyboard()
    {
        if (inputMenu != null)
        {
            inputMenu.showKeyboard();
        }
    }

    private MessageListItemClickListener messageListItemClickListener = new MessageListItemClickListener()
    {
        @Override
        public void onUserAvatarClick(String uid)
        {
            if (uid != null)
            {
                if (!uid.equals(String.valueOf(AllOnlineApp.sToken.community_id)))
                {
                    //进入别人主页，可能存在拉黑的可能，拉黑后需要自动结束此页面
                    GMImManager.getInstance().setChatActivity(GMChatActivity.this);
                }
            }
        }

        @Override
        public void onUserAvatarLongClick(GMMessage message, long uid)
        {
            String username = "";
            if (message != null)
            {
                username = GMCommonUtils.getMessageExtString(message, GMAppConstant.GOOME_NICKNAME, "");
            }
            inputAtUsername(message, uid, username, true, true);
        }

        @Override
        public void onResendClick(final GMMessage message)
        {
            new GMAlertDialog(GMChatActivity.this, R.string.resend, R.string.confirm_resend, null,
                new GMAlertDialog.AlertDialogUser()
                {
                    @Override
                    public void onResult(boolean confirmed, Bundle bundle)
                    {
                        if (!confirmed)
                        {
                            return;
                        }
                        resendMessage(message);
                    }
                }, true).show();
        }

        @Override
        public void onBubbleLongClick(GMMessage message)
        {
            if (inputMenu.getVisibility() == View.VISIBLE)
            {
                hideKeyBoard();
            }
            showPopMsgLongClick(message);
        }

        @Override
        public boolean onBubbleClick(GMMessage message)
        {
            if (message != null && message.getBodyType() == MsgBodyType.IMAGE)
            {
                showImagePreviewActivity(message);
            }
            return false;
        }

        @Override
        public void onTextAutoLinkClick(GMMessage message, String data, int type)
        {
            if (!bShowedLongClick)
            {
                if (type == GMSmileUtils.AUTO_LINK_HYPER)
                {
                    //带超链接的信息
                    ArrayList<GMHyperLinkValue> listLinkValue = GMCommonUtils.getMessageHyperLinkValueList(message);
                    for (GMHyperLinkValue linkValue : listLinkValue)
                    {
                        if (linkValue != null && data.equals(linkValue.getNickName()))
                        {
                            // if (linkValue.getJumpType() == GMAppConstant.GM_EXT_JUMP_TO_CHAT)
                            // {
                            //     CommunityUser user = new CommunityUser();
                            //     user.setUid(String.valueOf(linkValue.getUid()));
                            //     user.setListen(1);
                            //     user.setName(linkValue.getNickName());
                            //
                            //     //关注，然后发消息
                            //     ServerRequestController.getInstance(GMChatActivity.this).userFollow(0, user.getUid(),
                            //         user.getListen(),
                            //         CommonUtil.getTicket());
                            //
                            //     //从alpha小酷切换到某人的聊天，保存alpha的信息然后返回时候返回到小酷
                            //     CommonUtil.switch2ChatActivity(GMChatActivity.this, user);
                            //     ALPHA_USER = mUser;
                            // }
                            // else if (linkValue.getJumpType() == GMAppConstant.GM_EXT_JUMP_TO_PERSONNAL_PAGE)
                            // {
                            //     CommonUtil.switch2CommunityPageActivity(GMChatActivity.this,
                            //         String.valueOf(linkValue.getUid()));
                            // }
                            // break;
                        }
                    }
                }
                else
                {
                    //普通的link点击，web，emal，tel等
                    showPopForAutoLink(data, type);
                }
            }
        }
    };

    private void showPopForAutoLink(final String data, final int type)
    {
        if (!TextUtils.isEmpty(data))
        {
            hideKeyBoard();
            if (type == GMSmileUtils.AUTO_LINK_WEB)
            {
                //网页直接跳转
                String url = data;
                if (!url.startsWith("http"))
                {
                    url = "http://" + url;
                }
                //网页直接跳转
                CommunityUtil.switch2WebViewActivity(GMChatActivity.this, data, true, BusAdverActivity.INTENT_FROM_CHAT);
                return;
            }

            String title = "";
            ArrayList<TextSet> listItems = new ArrayList<TextSet>();
            TextSet textSet = new TextSet(R.string.copy, false, new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    final android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(
                        Context.CLIPBOARD_SERVICE);
                    clipboard.setText(data);
                }
            });
            listItems.add(textSet);

            if (type == GMSmileUtils.AUTO_LINK_TEL)
            {
                title = String.format(getString(R.string.phone_num_title),
                    data.substring(data.indexOf(":") + 1, data.length()));
                textSet = new TextSet(R.string.call, false, new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        //拨打电话
                        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + data));
                        startActivity(intent);
                    }
                });
                listItems.add(0, textSet);

                textSet = new TextSet(R.string.add_to_contacts, false, new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        //添加到手机通讯录
                        Intent addIntent = new Intent(Intent.ACTION_INSERT,
                            Uri.withAppendedPath(Uri.parse("content://com.android.contacts"), "contacts"));
                        addIntent.setType("vnd.android.cursor.dir/person");
                        addIntent.setType("vnd.android.cursor.dir/contact");
                        addIntent.setType("vnd.android.cursor.dir/raw_contact");
                        addIntent.putExtra(android.provider.ContactsContract.Intents.Insert.PHONE,
                            data.substring(data.indexOf(":") + 1, data.length()));
                        startActivity(addIntent);
                        //Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(data));
                        //this.startActivity(intent);
                    }
                });
                listItems.add(textSet);
            }
            else if (type == GMSmileUtils.AUTO_LINK_EMAIL)
            {
                title = String.format(getString(R.string.mail_title), data);
                textSet = new TextSet(R.string.use_default_email_count, false, new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        //发送邮件
                        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + data));
                        startActivity(intent);
                    }
                });
                listItems.add(0, textSet);
            }
            PopupWindowUtil.showPopWindow(this, rootView, title, listItems, true);
        }
    }

    private void showImagePreviewActivity(final GMMessage curMessage)
    {
        showWaitInfo(getString(R.string.please_wait));
        GMUserUtil.getInstance().getSonWorkHandler().post(new Runnable()
        {
            @Override
            public void run()
            {
                GMChatManager chatManager = GMClient.getInstance().chatManager();
                if (chatManager == null)
                {
                    return;
                }
                conversation = chatManager.getConversation(
                    GMCommonUtils.getConversationIdByUserId(String.valueOf(toChatUserId), chatType), chatType, false);
                if (conversation == null)
                {
                    return;
                }
                GMMessage lastMessage = conversation.getLastMessage();
                if(chatType == ConversationType.CLASSICROOM)
                {
                    if(messageAdapter != null)
                    {
                        lastMessage = messageAdapter.getLastMessage();
                    }
                }
                if(lastMessage == null)
                {
                    return;
                }

                List<GMMessage> listMessages = conversation.searchMsgWithType(MsgBodyType.IMAGE,
                    lastMessage.getMsgId() + 1, Integer.MAX_VALUE, -1, ConversationDirection.UP);
                if (listMessages != null && listMessages.size() > 0)
                {
                    int position = 0;
                    final ArrayList<String> image_list = new ArrayList<String>();
                    final HashMap<String, String> thumbnail_map = new HashMap<String, String>();
                    for (GMMessage message : listMessages)
                    {
                        if (message != null && message.getBodyType() == MsgBodyType.IMAGE)
                        {
                            GMImageMessageBody imgBody = (GMImageMessageBody) message.getMsgBody();
                            if (imgBody == null)
                            {
                                continue;
                            }
                            if (message.getMsgId() == curMessage.getMsgId())
                            {
                                position = image_list.size();
                            }

                            String thumbPath = null;
                            if (message.getDirection() == MsgDirection.SEND)
                            {
                                thumbPath = GlideUtil.getGlideCachePath(imgBody.getLocalPath());
                                image_list.add(imgBody.getLocalPath());
                                if (!TextUtils.isEmpty(thumbPath))
                                {
                                    thumbPath = GlideUtil.getGlideCachePath(imgBody.getRemotePath());
                                }
                            }
                            else
                            {
                                image_list.add(imgBody.getRemotePath());
                                thumbPath = GlideUtil.getGlideCachePath(imgBody.getRemotePath());
                            }

                            thumbnail_map.put(imgBody.getRemotePath(), thumbPath);
                        }
                    }

                    //主线程跳转
                    final int curPos = position;
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            dismissWait();
                            goToPreviewActivity(image_list, thumbnail_map, curPos);
                        }
                    });
                }
                else
                {
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            dismissWait();
                        }
                    });
                }
            }
        });
    }

    private void goToPreviewActivity(ArrayList<String> image_list, HashMap<String, String> thumbnail_map, int position)
    {
        // 图片预览
        Intent intent = new Intent();
        intent.setClass(this, PreviewActivity.class);
        CameraSdkParameterInfo info = new CameraSdkParameterInfo();
        info.setPosition(position);
        info.setImage_list(image_list);
        intent.putExtra(CameraSdkParameterInfo.EXTRA_PARAMETER, info);
        intent.putExtra(PreviewActivity.INTENT_TOP_TYPE, PreviewActivity.TOP_GRID);
        intent.putExtra(PreviewActivity.INTENT_THUMBNAIL_MAP, thumbnail_map);
        startActivity(intent);
        try
        {
            overridePendingTransition(R.anim.zoom_enter, 0);
        }
        catch (Exception e)
        {
        }
    }

    public void inputAtUsername(GMMessage message, long userId, String userName, boolean bLongClicked,
                                boolean autoAddAtSymbol)
    {
        if (userId == -1 || GMClient.getInstance().getCurrentUserId() == userId || !isChatroom())
        {
            return;
        }
        String content = "";
        if (inputMenu != null && inputMenu.getPrimaryMenu() != null && inputMenu.getPrimaryMenu().getEditText() != null)
        {
            content = inputMenu.getPrimaryMenu().getEditText().getText().toString();
            bKeyboardForceShow = true;
            inputMenu.showAtEndKeyboard();
            mHandler.postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    bKeyboardForceShow = false;
                }
            }, 1500);
        }

        GMAtMessageHelper.get().addAtUser(userId, userName);
        if (autoAddAtSymbol)
        {
            inputMenu.insertText("@" + userName + " ");
        }
        else
        {
            inputMenu.insertText(userName + " ");
        }
    }

    private void showPopMsgLongClick(final GMMessage message)
    {
        if (message != null && message.getBodyType() == MsgBodyType.VOICE)
        {
            if (GMChatRowVoicePlayClickListener.currentPlayListener != null
                && GMChatRowVoicePlayClickListener.currentPlayListener.isPlaying)
            {
                GMChatRowVoicePlayClickListener.currentPlayListener.stopPlayVoice();
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

            //撤回
            TextSet revokeSet = null;
            if (message.getStatus() == MsgStatus.SUCCESSED && message.getFrom() == AllOnlineApp.sToken.community_id
                && ServerTimeManager.getInatnce().isLaterThanSomeTime(message.getTimestamp(), TWO_MINUTS))
            {
                revokeSet = new TextSet(R.string.revoke_message, false, new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        //消息撤回
                        revokeMessage(message);
                    }
                });
            }

            switch (message.getBodyType())
            {
                case TEXT:
                    //TXT --- 复制，撤回，删除，社区
                    set = new TextSet(R.string.em_message_copy, false, new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            final android.text.ClipboardManager clipboard
                                = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                            clipboard.setText(((GMTextMessageBody) message.getMsgBody()).getMessage());
                        }
                    });
                    list.add(set);
                    if (revokeSet != null)
                    {
                        list.add(revokeSet);
                    }
                    list.add(setDelete);
                    addShareToCommunity(list);
                    break;

                case CUSTOM:
                    if (GMCommonUtils.isRedPacketMessage(message))
                    {
                        list.add(setDelete);
                    }
                    break;

                case IMAGE:
                    //IMAGE --- 撤回，删除，社区
                    if (revokeSet != null)
                    {
                        list.add(revokeSet);
                    }
                    list.add(setDelete);
                    addShareToCommunity(list);
                    break;

                case VOICE:
                    //VOICE --- 撤回，删除
                    if (revokeSet != null)
                    {
                        list.add(revokeSet);
                    }
                    list.add(setDelete);
                    break;

                case LOCATION:
                    //LOCATION --- 撤回，删除
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
            PopupWindowUtil.showPopWindow(GMChatActivity.this, rootView, 0, list, true,
                new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        bShowedLongClick = false;
                    }
                }
            );
        }
    }

    private void showDeleteComfirm(final GMMessage message)
    {
        if (message != null && message.getMsgBody() != null)
        {
            TextSet setDelete = new TextSet(R.string.em_message_delete_sure, true, new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    try
                    {
                        deleteMessageFromDB(message);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        Toast.makeText(GMChatActivity.this, R.string.Delete_failed, Toast.LENGTH_SHORT).show();
                    }
                }
            });
            PopupWindowUtil.showPopWindow(this, rootView, R.string.em_message_delete_confirm, setDelete,
                null, true);
        }
    }

    private void deleteMessageFromDB(final GMMessage message)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                if (message == null)
                {
                    return;
                }
                if (conversation != null)
                {
                    conversation.removeMessage(message.getMsgId());
                }
                if (messageAdapter != null)
                {
                    messageAdapter.refresh();
                }
            }
        }).start();
    }

    private void addShareToCommunity(ArrayList<TextSet> list)
    {
        // final Section section = getSection();
        // if (section != null)
        // {
        //     // 该城市开通了社区功能，且该群聊对应板块所属城市（GmChatActivity.gmCommunityAct）
        //     // 是 当前所选城市（BusOnlineApp.CITY.code）
        //     // 显示转发到社区
        //     TextSet set = new TextSet(R.string.em_message_share_to_community, false, new View.OnClickListener()
        //     {
        //         @Override
        //         public void onClick(View v)
        //         {
        //             setLeftBtn(false, new View.OnClickListener()
        //             {
        //                 @Override
        //                 public void onClick(View v)
        //                 {
        //                     // 点击取消按钮，取消分享
        //                     closeShare();
        //                 }
        //             });
        //             inputMenu.setVisibility(View.GONE);
        //             messageAdapter.setCheckBoxVisiable(true);
        //             setShare2Community(true, new View.OnClickListener()
        //             {
        //                 @Override
        //                 public void onClick(View v)
        //                 {
        //                     // 点击分享到社区
        //                     shareToCommunity(section);
        //                     closeShare();
        //                 }
        //             });
        //         }
        //     });
        //     list.add(set);
        // }
    }

    /**
     * 是否返回按钮
     *
     * @param isBack   true:返回按钮，false:取消按钮
     * @param listener 按钮回调,默认为退出{@link}
     */
    public void setLeftBtn(boolean isBack, final View.OnClickListener... listener)
    {
        if (isBack)
        {
            actionbar.setLeftText(0);
            actionbar.setLeftImageVisibility(View.VISIBLE);
        }
        else
        {
            actionbar.setLeftText(R.string.cancel);
            actionbar.setLeftImageVisibility(View.GONE);
        }
        if (listener != null && listener.length > 0)
        {
            actionbar.setLeftTextCLickListener(listener[0]);
        }
    }

    private void closeShare()
    {
        setLeftBtn(true);
        setShare2Community(false);
        inputMenu.setVisibility(View.VISIBLE);
        messageAdapter.setCheckBoxVisiable(false);
    }

    /**
     * 是否返回按钮
     *
     * @param isVisiable true:显示转发到社区按钮，false:不显示
     * @param listener   按钮回调,默认为不带Intent参数跳往{@link }
     */
    public void setShare2Community(boolean isVisiable, final View.OnClickListener... listener)
    {
        textShare2Community.setVisibility(isVisiable ? View.VISIBLE : View.GONE);
        if (listener != null && listener.length > 0)
        {
            textShare2Community.setOnClickListener(listener[0]);
        }
        else
        {
            textShare2Community.setOnClickListener(this);
        }
    }

    /**
     * 转发到帖子
     */
    // private void shareToCommunity(Section section)
    // {
    //     boolean isShare = false;
    //     final Intent intent = new Intent(this, CommunityAddTopicActivity.class);
    //     String msg = messageAdapter.getCheckedMessages();
    //     if (!TextUtils.isEmpty(msg))
    //     {
    //         intent.putExtra(CommunityAddTopicActivity.INTENT_ADDTOPIC_TEXT, msg);
    //         isShare = true;
    //     }
    //     ArrayList<String> pics = messageAdapter.getCheckedImages();
    //     if (pics != null && pics.size() > 0)
    //     {
    //         intent.putExtra(CommunityAddTopicActivity.INTENT_ADDTOPIC_PIC_URL, pics);
    //         isShare = true;
    //     }
    //     boolean hasOtherMsg = messageAdapter.hasCheckedOtherMessage();
    //     intent.putExtra(Constant.SECTION_DATA, section);
    //     if (hasOtherMsg)
    //     {
    //         // 含有不能转发的消息类型，弹窗提示
    //         showConfirmDialog(intent, isShare);
    //     }
    //     else if (isShare)
    //     {
    //         // 仅包含可以转发的消息, 直接转发
    //         startActivity(intent);
    //     }
    //     else
    //     {
    //         // 没有选择任何消息
    //         Toast.makeText(this, R.string.em_message_checked_no, Toast.LENGTH_SHORT).show();
    //     }
    // }

    /**
     * @param intent  转发的intent
     * @param isShare 是否需要转发
     */
    private void showConfirmDialog(final Intent intent, final boolean isShare)
    {
        // final Dialog dialog = new AlertDialog.Builder(this).create();
        // dialog.show();
        // Window window = dialog.getWindow();
        // window.setContentView(R.layout.dialog2);
        // window.findViewById(R.id.dialog_log).setVisibility(View.GONE);
        // ((TextView) window.findViewById(R.id.dialog_text)).setText(R.string.em_message_checked_other_message_hint);
        // // 为确认按钮添加事件,执行退出应用操作
        // window.findViewById(R.id.dialog_sure).setOnClickListener(new View.OnClickListener()
        // {
        //     public void onClick(View v)
        //     {
        //         if (isShare)
        //         {
        //             // 转发
        //             startActivity(intent);
        //         }
        //         dialog.dismiss();
        //     }
        // });
        //
        // // 关闭alert对话框架
        // window.findViewById(R.id.dialog_cancel).setOnClickListener(new View.OnClickListener()
        // {
        //     public void onClick(View v)
        //     {
        //         dialog.dismiss();
        //     }
        // });
    }

    class MyItemClickListener implements GMChatExtendMenu.GMChatExtendMenuItemClickListener
    {
        @Override
        public void onClick(int itemId, View view)
        {
            if (inputMenu != null)
            {
                inputMenu.hideExtendMenuContainer();
            }
            Intent intent = null;
            switch (itemId)
            {
                case ITEM_TAKE_PICTURE:
                    selectPicFromCamera();
                    break;

                case ITEM_PICTURE:
                    selectPicFromLocal();
                    break;

                case ITEM_LOCATION:
                    intent = new Intent(GMChatActivity.this, EmLocationActivity.class);
                    intent.putExtra("from", EmLocationActivity.FROM_CHAT_ACTION);
                    startActivityForResult(intent, REQUEST_CODE_MAP);
                    break;

                case ITEM_RED_PACKET:
                    //先绑定微信
                    if(isWechatBinded() == false){
                        Intent intentBindWechat = new Intent(GMChatActivity.this, BindWechatActivity.class);
                        intentBindWechat.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                        startActivity(intentBindWechat);
                        return;
                    }
                    //去包红包
                    if (chatType == ConversationType.CHAT)
                    {
                        intent = new Intent(GMChatActivity.this, CreateRedPacketActivity.class);
                        intent.putExtra(CreateRedPacketActivity.EXTRA_TYPE_DISPLAY,
                            RedPacketConstant.RP_DISPLAY_CHAT_SINGLE);
                        intent.putExtra(CreateRedPacketActivity.EXTRA_SPECIFIED_ID, String.valueOf(toChatUserId));
                        startActivity(intent);
                        overridePendingTransition(R.anim.push_bottom_in, 0);
                    }
                    else if (room != null && isChatroom())
                    {
                        intent = new Intent(GMChatActivity.this, CreateRedPacketActivity.class);
                        intent.putExtra(CreateRedPacketActivity.EXTRA_TYPE_DISPLAY, RedPacketConstant
                            .RP_DISPLAY_CHAT_GROUP);
                        intent.putExtra(CreateRedPacketActivity.EXTRA_PEOPLE_COUNT, room.getOccupantsCount());
                        intent.putExtra(CreateRedPacketActivity.EXTRA_SPECIFIED_ID, String.valueOf(toChatUserId));
                        startActivity(intent);
                        overridePendingTransition(R.anim.push_bottom_in, 0);
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
    protected void selectPicFromLocal()
    {
        CameraSdkParameterInfo mCameraSdkParameterInfo = new CameraSdkParameterInfo();
        mCameraSdkParameterInfo.setSingle_mode(false);
        mCameraSdkParameterInfo.setShow_camera(true);
        mCameraSdkParameterInfo.setMax_image(9);
        mCameraSdkParameterInfo.setCroper_image(true);
        mCameraSdkParameterInfo.setFilter_image(false);
        Intent intent = new Intent(this, PhotoPickActivity.class);

        Bundle b = new Bundle();
        b.putSerializable(CameraSdkParameterInfo.EXTRA_PARAMETER, mCameraSdkParameterInfo);
        b.putBoolean(PhotoPickActivity.FROM_IM, true);
        intent.putExtras(b);
        startActivityForResult(intent, CameraSdkParameterInfo.TAKE_PICTURE_FROM_GALLERY);
    }

    protected void selectPicFromCamera()
    {
        if (!GMCommonUtils.isSdcardExist())
        {
            Toast.makeText(this, R.string.sd_card_does_not_exist, Toast.LENGTH_SHORT).show();
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
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK)
        {
            if (requestCode == REQUEST_CODE_CAMERA)
            {
                // capture new image
                if (cameraFile != null && cameraFile.exists())
                {
                    GMUserUtil.getInstance().getSonWorkHandler().post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            CommunityImageInfo imageInfo = new CommunityImageInfo();
                            imageInfo.setSource_image(cameraFile.getAbsolutePath());
                            ImageCompressUtils.compress(GMChatActivity.this, imageInfo);
                            sendImageMessage(imageInfo.getSource_image());
                        }
                    });
                }
            }
            else if (requestCode == CameraSdkParameterInfo.TAKE_PICTURE_FROM_GALLERY)
            {
                if (data != null)
                {
                    Bundle bundle = data.getExtras();
                    if (bundle != null)
                    {
                        CameraSdkParameterInfo mCameraSdkParameterInfo = (CameraSdkParameterInfo) bundle
                            .getSerializable(CameraSdkParameterInfo.EXTRA_PARAMETER);
                        final ArrayList<String> list = mCameraSdkParameterInfo.getImage_list();
                        if (list != null)
                        {
                            for (String path : list)
                            {
                                if (!TextUtils.isEmpty(path))
                                {
                                    sendImageMessage(path);
                                }
                            }
                        }
                    }
                }
            }
            else if (requestCode == REQUEST_CODE_MAP)
            {
                // location
                double latitude = data.getDoubleExtra("latitude", 0);
                double longitude = data.getDoubleExtra("longitude", 0);
                String locationAddress = data.getStringExtra("textAddress");
                if ((int)latitude == 0 && (int)longitude == 0)
                {
                    //无效的地理位置信息
                    Toast.makeText(this, R.string.unable_to_get_loaction, Toast.LENGTH_SHORT).show();
                }
                else
                {
                    sendLocationMessage(latitude, longitude, locationAddress);
                }
            }
            else if (requestCode == REQUEST_CODE_EM_AT_SELECT)
            {
                if (data != null && data.hasExtra(GmSelectAtUserActivity.SELECT_AT_USER_NAME))
                {
                    //弹出用户列表选择了用户后返回
                    long uid = data.getLongExtra(GmSelectAtUserActivity.SELECT_AT_USER_ID, -1);
                    String nick = data.getStringExtra(GmSelectAtUserActivity.SELECT_AT_USER_NAME);
                    inputAtUsername(null, uid, nick, false, false);
                }
                mHandler.postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        showKeyboard();
                    }
                }, 200);
            }
            else if (requestCode == REQUEST_CODE_CHAT_SETTUNG)
            {
                finish();
            }
        }
        else if (resultCode == GmChatSettingsActivity.RESULT_AT_CONTENT)
        {
            if (requestCode == REQUEST_CODE_CHAT_SETTUNG)
            {
                if (data != null)
                {
                    String content = data.getStringExtra(Constant.DATA_TEXT);
                    if (content == null)
                    {
                        content = "";
                    }
                    content = getString(R.string.group_at_all) + " " + content;
                    sendAtMessage(content);
                }
            }
        }
        else if (resultCode == GmChatSettingsActivity.RESULT_EXIT_GROUP)
        {
            //退群
            if (requestCode == REQUEST_CODE_CHAT_SETTUNG)
            {
                deleteConversation();
                finish();
            }
        }
    }

    private void deleteConversation()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                boolean delete = GMClient.getInstance().chatManager().deleteConversation(String.valueOf(toChatUserId),
                    true);
                if (!delete)
                {
                    GMLog.i(TAG, "delete conversation fail id=" + toChatUserId);
                }
            }
        }).start();
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.to_community_section_Button:
                // 前往对应社区板块发帖
                // Section section2 = getSection();
                // if (section2 != null)
                // {
                //     Intent intent2Section = new Intent(this, CommunitySectionActivityNew.class);
                //     intent2Section.putExtra(Constant.SECTION_DATA, section2);
                //     CommonUtil.switchActivity(this, intent2Section);
                // }
                break;
        }
    }

    //    @Override
    //    public void onBackPressed()
    //    {
    //        super.onBackPressed();
    //        if (isChatroom())
    //        {
    //            GMAtMessageHelper.get().removeAtMeGroup(String.valueOf(toChatUserId));
    //        }
    //        GMAtMessageHelper.get().cleanToAtUserList();
    //    }

    private void toChatroomSetting()
    {
        Intent intent = new Intent(this, GmChatSettingsActivity.class);
        intent.putExtra(GMAppConstant.EXTRA_USER_ID, toChatUserId);
        startActivityForResult(intent, REQUEST_CODE_CHAT_SETTUNG);
    }

    private void startToSetting()
    {
        // Intent intent = new Intent(this, ChatPersonSettingActivity.class);
        // intent.putExtra(Constant.USER_DATA, mUser);
        // startActivityForResult(intent, REQUEST_CODE_CHAT_SETTUNG);
    }

    /**********************************************/
    protected void sendTextMessage(String content)
    {
        msgSendController.sendTextMessage(content);
    }

    /**
     * send @ message, only support group chat message
     *
     * @param content
     */
    public void sendAtMessage(String content)
    {
        if (!isChatroom())
        {
            return;
        }
        msgSendController.sendAtMessage(content);
    }

    //发送表情消息
    protected void sendBigExpressionMessage(String name, String identityCode)
    {
        msgSendController.sendBigExpressionMessage(name, identityCode);
    }

    //发送语音消息
    protected void sendVoiceMessage(String filePath, int length)
    {
        msgSendController.sendVoiceMessage(filePath, length);
    }

    //发送图片消息
    protected void sendImageMessage(String imagePath)
    {
        hideExtendMenu();
        msgSendController.sendImageMessage(imagePath);
    }

    /***********发送红包信息***********/
    protected void sendRedPacketMessage(int redPacketType, String redpacket_id, String title)
    {
        hideExtendMenu();
        msgSendController.sendRedPacketMessage(redPacketType, redpacket_id, title);
    }

    //发送地址消息-
    protected void sendLocationMessage(double latitude, double longitude, String locationAddress)
    {
        hideExtendMenu();
        msgSendController.sendLocationMessage(latitude, longitude, locationAddress);
    }

    //发送视频消息
    protected void sendVideoMessage(String videoPath, String thumbPath, int videoLength)
    {
        msgSendController.sendVideoMessage(videoPath, thumbPath, videoLength);
    }

    //发送文件消息
    protected void sendFileMessage(String filePath)
    {
        msgSendController.sendFileMessage(filePath);
    }

    //发送发送消息
    protected void sendMessage(GMMessage message, JsonObject jsonObject)
    {
        msgSendController.sendMessage(message, jsonObject);
    }

    /***********发送领取了红包的提醒信息***********/
    private void sendRedpacketCMDMessage(final String redpacketHostUid, final String redpacketHostName,
                                         String redpacket_id, int status)
    {
        msgSendController.sendRedpacketCMDMessage(redpacketHostUid, redpacketHostName, redpacket_id, status);
    }

    //重新发送消息
    public void resendMessage(GMMessage message)
    {
        msgSendController.resendMessage(message);
    }

    //发送图片消息
    protected void sendPicByUri(Uri selectedImage)
    {
        msgSendController.sendPicByUri(selectedImage);
    }

    //发送文件消息
    protected void sendFileByUri(Uri uri)
    {
        msgSendController.sendFileByUri(uri);
    }

    //消息撤回
    private void revokeMessage(GMMessage message)
    {
        msgSendController.revokeMessage(message);
    }

    private void sendInputingCMDMessage()
    {
        if(chatType != ConversationType.CHAT)
        {
            return;
        }
        msgSendController.sendInputingCMDMessage();
    }

    private void hideExtendMenu()
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                if (inputMenu != null)
                {
                    inputMenu.hideExtendMenuContainer();
                }
            }
        });
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (intent != null && intent.getAction() != null)
            {
                String action = intent.getAction();
                if (action.equals(RedPacketConstant.OPEN_REDPACKET_ACTION))
                {
                    //领取红包的广播
                    if (intent.hasExtra(RedPacketConstant.REDPACKET_DATA))
                    {
                        RedPacketInfo redPacketInfo = (RedPacketInfo)intent.getSerializableExtra(RedPacketConstant.REDPACKET_DATA);
                        if(redPacketInfo != null && redPacketInfo.getToChatId().equals(String.valueOf(toChatUserId)))
                        {
                            //发送领取了红包的透传信息
                            sendRedpacketCMDMessage(redPacketInfo.getUid(), redPacketInfo.getName(), redPacketInfo.getRedpacket_id(), redPacketInfo.getStatus());
                        }
                    }
                }
                else if (action.equals(RedPacketConstant.REDPACKET_WILL_SEND_ACTION))
                {
                    //红包支付完成，去查询刚新建的没有发送的红包的广播，然后发送
                    if (intent.hasExtra(RedPacketConstant.REDPACKET_DATA))
                    {
                        Object object = intent.getSerializableExtra(RedPacketConstant.REDPACKET_DATA);
                        if (object != null && object instanceof RedPacketInfo)
                        {
                            sendRedPacket((RedPacketInfo) object);
                        }
                    }

                    mHandler.postDelayed(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            if (messageAdapter != null)
                            {
                                messageAdapter.selectLastNow();
                            }
                        }
                    }, 900);
                }
            }
        }
    };

    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1)
    {
    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        if (event == null)
        {
            return;
        }
        float[] its = event.values;
        if (its != null && its.length > 0 && event.sensor != null && event.sensor.getType() == Sensor.TYPE_PROXIMITY)
        {
            // 经过测试，当手贴近距离感应器的时候its[0]返回值为0.0，当手离开时返回1.0
            if (its[0] == 0.0)
            {
                // 贴近手机
                GMImManager.getInstance().setSpeakerOpened(false);
            }
            else
            {
                // 远离手机
                GMImManager.getInstance().setSpeakerOpened(true);
            }
        }
    }

    private void sendRedPacket(final RedPacketInfo redPacketInfo)
    {
        if (redPacketInfo != null && (redPacketInfo.getDisplay_type() == RedPacketConstant.RP_DISPLAY_CHAT_GROUP ||
            (redPacketInfo.getDisplay_type() == RedPacketConstant.RP_DISPLAY_CHAT_SINGLE
                    && redPacketInfo.getToChatId().equals(String.valueOf(toChatUserId)))))
        {
            sendRedPacketMessage(redPacketInfo.getPacket_type(), redPacketInfo.getRedpacket_id(),
                redPacketInfo.getHello_words());
            //发送命令给后台，便于标记状态
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = AllOnlineApp.mApiClient.sendRedPacket(this.hashCode(), CommonUtil.getTicket(),
                        redPacketInfo.getRedpacket_id());
                }
            }).start();
        }
    }

    private void registerReceiver()
    {
        if (broadcastReceiver != null)
        {
            IntentFilter intentFilter = new IntentFilter(RedPacketConstant.OPEN_REDPACKET_ACTION);
            intentFilter.addAction(RedPacketConstant.REDPACKET_WILL_SEND_ACTION);
            registerReceiver(broadcastReceiver, intentFilter);
        }
    }

    private void unRegisterReceiver()
    {
        if (broadcastReceiver != null)
        {
            unregisterReceiver(broadcastReceiver);
        }
    }

    private void release()
    {
        unRegisterReceiver();

        if(GMUserUtil.getInstance().getSonWorkHandler() != null)
        {
            GMUserUtil.getInstance().getSonWorkHandler().removeCallbacksAndMessages(null);
        }

        if (groupListener != null)
        {
            GMClient.getInstance().groupManager().removeGroupChangeListener(groupListener);
        }

        if (roomListener != null)
        {
            GMImManager.getInstance().removeChatroomListener(roomListener);
        }

        if (mManager != null)
        {
            mManager.unregisterListener(this);// 注销传感器监听
            GMImManager.getInstance().setSpeakerOpened(true);
        }
        if(mHandler != null)
        {
            mHandler.removeCallbacksAndMessages(null);
        }
        GMImManager.getInstance().removeGMMessageListener(gmMessageAppListener);
        gmMessageAppListener = null;
    }

    /**
     * listen the group event
     */
    class GroupListener extends GMGroupListener
    {
        @Override
        public void onUserRemoved(final String groupId, String groupName)
        {
            runOnUiThread(new Runnable()
            {
                public void run()
                {
                    if (groupId.equals(String.valueOf(toChatUserId)))
                    {
                        Toast.makeText(GMChatActivity.this, R.string.you_are_group, Toast.LENGTH_LONG).show();
                        if (!GMChatActivity.this.isFinishing())
                        {
                            GMChatActivity.this.finish();
                        }
                    }
                }
            });
        }

        @Override
        public void onGroupDestroyed(final String groupId, String groupName)
        {
            // prompt group is dismissed and finish this activity
            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    if (groupId.equals(String.valueOf(toChatUserId)))
                    {
                        Toast.makeText(GMChatActivity.this, R.string.the_current_group_destroyed, Toast.LENGTH_LONG)
                            .show();
                        if (!GMChatActivity.this.isFinishing())
                        {
                            GMChatActivity.this.finish();
                        }
                    }
                }
            });
        }
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        long userId = -1;
        if (intent != null && intent.hasExtra(Constant.USER_DATA))
        {
            CommunityUser user = null;
            user = (CommunityUser) intent.getSerializableExtra(Constant.USER_DATA);
            if (user != null)
            {
                userId = Long.parseLong(user.getUid());
                this.mUser = user;
            }
        }
        else if (intent.hasExtra(GMAppConstant.EXTRA_USER_ID))
        {
            String groupId = intent.getStringExtra(GMAppConstant.EXTRA_USER_ID);
            userId = Long.valueOf(groupId);
        }

        //因为此activity是singletask，所以再跳转到到新的聊天需要先结束当前的任务
        if (toChatUserId >= 0 && toChatUserId == userId)
        {
            super.onNewIntent(intent);
        }
        else
        {
            bNotGoToAlpha = true;
            finish();
            bNotGoToAlpha = false;
            startActivity(intent);
        }
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        hideKeyBoard();
        inputMenu.hideExtendMenuContainer();
    }

    @Override
    public void finish()
    {
        super.finish();

        if (isChatroom())
        {
            GMAtMessageHelper.get().removeAtMeGroup(String.valueOf(toChatUserId));
            GMAtMessageHelper.get().cleanToAtUserList();
        }

        if (getIntent() != null && getIntent().getBooleanExtra(Constant.WHERE_FROM_WELCOME, false))
        {
            if (!CommonUtil.isLogin()) {
                //去登陆
                startActivity(new Intent(this, LoginActivity.class));
            }else{
                SettingDataManager.getInstance(this).goToMainByMap(this, null);
            }
        }
        else if (!bNotGoToAlpha && ALPHA_USER != null)
        {
            // CommonUtil.switch2ChatActivity(this, ALPHA_USER);
            ALPHA_USER = null;
        }
    }
}
