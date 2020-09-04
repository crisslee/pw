package com.coomix.app.all.ui.im;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.Constant;
import com.coomix.app.all.R;
import com.coomix.app.all.dialog.AskDialog;
import com.coomix.app.all.dialog.ProgressDialogEx;
import com.coomix.app.all.ui.base.BaseActivity;
import com.coomix.app.all.widget.MyActionbar;
import com.coomix.app.framework.util.PreferenceUtil;
import com.goomeim.GMAppConstant;
import java.util.ArrayList;
import java.util.List;
import net.goome.im.GMError;
import net.goome.im.GMValueCallBack;
import net.goome.im.chat.GMChatRoom;
import net.goome.im.chat.GMChatRoomManager;
import net.goome.im.chat.GMChatroomMemberInfo;
import net.goome.im.chat.GMClient;
import net.goome.im.chat.GMConstant.ChatroomMode;
import net.goome.im.chat.GMPageResult;
import net.goome.im.util.GMLog;

/**
 * File Description:
 *
 * @author felixqiu
 * @since 2017/8/8.
 */
public class GmChatSettingsActivity extends BaseActivity implements View.OnClickListener,
    CompoundButton.OnCheckedChangeListener {

    private static final String TAG = GmChatSettingsActivity.class.getSimpleName();
    private long toChatGroupId;
    private GMChatRoom room;
    private ListView listview;
    private GmChatroomMembersAdapter adapter;
    private ToggleButton notifyTBtn;
    private TextView textGroupName;
    private ProgressDialogEx progressDialogEx = null;
    private LinearLayout layoutAtAll;
    private LinearLayout layoutUnnotify;

    public static final int RESULT_EXIT_GROUP = 1000;
    public static final int RESULT_AT_CONTENT = 1001;
    private static final int REFRESH_USER_LIST = 10001;
    private static final int GET_CHATROOM_INFO_DONE = 10002;
    private long chatroomOwnerId = -1;
    private final int TIME_OUT = 60000;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @SuppressWarnings("unchecked")
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GET_CHATROOM_INFO_DONE:
                    showChatroomInfo();
                    break;
                case REFRESH_USER_LIST:
                    dismissWait();
                    List<GMChatroomMemberInfo> userList = null;
                    if (msg.obj != null && msg.obj instanceof List) {
                        userList = (List<GMChatroomMemberInfo>) msg.obj;
                    }
                    if (adapter == null) {
                        ChatroomMode mode = ChatroomMode.NORMAL;
                        if (room != null) {
                            mode = room.getModeType();
                        }
                        adapter = new GmChatroomMembersAdapter(GmChatSettingsActivity.this, userList, chatroomOwnerId,
                            mode);
                        listview.setAdapter(adapter);
                    } else {
                        adapter.refreshUserInfo(userList);
                    }
                    break;

                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gm_chat_settings);

        if (getIntent() != null && getIntent().getExtras() != null) {
            toChatGroupId = getIntent().getLongExtra(GMAppConstant.EXTRA_USER_ID, -1);

            initViews();

            if (toChatGroupId != -1) {
                initChatroomInfo();

                showWaitInfo();
            } else {
                finish();
            }
        } else {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        super.onDestroy();
    }

    private void initViews() {
        MyActionbar actionbar = (MyActionbar) findViewById(R.id.myActionbar);
        actionbar.initActionbar(true, R.string.em_chat_group_settings, 0, 0);

        layoutUnnotify = (LinearLayout) findViewById(R.id.ll_unnotify);
        boolean unNotify = PreferenceUtil.getBoolean(GMAppConstant.GROUP_MSG_UNNOTIFY +
            GMClient.getInstance().getCurrentUserId() + toChatGroupId, false);
        notifyTBtn = (ToggleButton) findViewById(R.id.em_group_member_notify);
        notifyTBtn.setChecked(unNotify);
        notifyTBtn.setOnCheckedChangeListener(this);
        findViewById(R.id.em_clear_and_exit).setOnClickListener(this);
        listview = (ListView) findViewById(R.id.em_group_member_list);
        textGroupName = (TextView) findViewById(R.id.em_group_name);
        listview.setOnItemClickListener(itemClickListener);
    }

    private void initAtAllViews(long ownerId) {
        if (Constant.IS_DEBUG_MODE || (ownerId != -1 && ownerId == AllOnlineApp.sToken.community_id)) {
            layoutAtAll = (LinearLayout) findViewById(R.id.layoutAtAll);
            layoutAtAll.setOnClickListener(this);
            layoutAtAll.setVisibility(View.VISIBLE);
            findViewById(R.id.layoutAtAllLine).setVisibility(View.VISIBLE);
        }
    }

    private void initChatroomInfo() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                room = GMClient.getInstance().chatroomManager().getChatroomInfoFromDB(toChatGroupId);
                mHandler.sendEmptyMessage(GET_CHATROOM_INFO_DONE);
            }
        }).start();
    }

    private void showChatroomInfo() {
        if (room == null) {
            Toast.makeText(this, "获取聊天室信息出错", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            chatroomOwnerId = room.getOwner();
            // at all
            initAtAllViews(chatroomOwnerId);
            // 聊天室主题
            textGroupName.setText(room.getSubject());

            // 获取成员列表
            getGroupMemberUserInfo();
        }
    }

    private void getGroupMemberUserInfo() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (room.getLastTimeToGetMemberListFromServer() > 0) {
                    List<GMChatroomMemberInfo> list = GMClient.getInstance().chatroomManager()
                        .getChatroomMemberListFromDB(toChatGroupId);
                    List<GMChatroomMemberInfo> sortList = new ArrayList<>();
                    if (list != null) {
                        for (GMChatroomMemberInfo m : list) {
                            if (m.getUid() == chatroomOwnerId) {
                                sortList.add(m);
                                list.remove(m);
                                break;
                            }
                        }
                        sortList.addAll(list);
                    }
                    Message msg = mHandler.obtainMessage();
                    msg.what = REFRESH_USER_LIST;
                    msg.obj = sortList;
                    mHandler.sendMessage(msg);
                } else {
                    getMemberListFromServer(toChatGroupId, 0);
                }
            }
        }).start();
    }

    private void getMemberListFromServer(final long roomid, final int pos) {
        GMChatRoomManager mgr = GMClient.getInstance().chatroomManager();
        if (mgr == null) {
            return;
        }
        mgr.getChatroomMemberListFromServerWithId(roomid, pos, 500,
            new GMValueCallBack<GMPageResult<GMChatroomMemberInfo>>() {
                @Override
                public void onSuccess(GMPageResult<GMChatroomMemberInfo> result) {
                    int currPos = result.getNextPos();
                    if (currPos != 0) {
                        getMemberListFromServer(roomid, currPos);
                    } else {
                        GMLog.i(TAG, "get chatroom member list done");
                        initChatroomInfo();
                    }
                }

                @Override
                public void onError(GMError gmError) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(GmChatSettingsActivity.this, "获取聊天室成员出错", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                }
            });
    }

    private void showWaitInfo() {
        progressDialogEx = new ProgressDialogEx(this);
        progressDialogEx.setAutoDismiss(true);
        progressDialogEx.setDuration(TIME_OUT);

        try {
            progressDialogEx.show(getString(R.string.loading));
        } catch (Exception e) {
            e.printStackTrace();
        }
        mHandler.postDelayed(showMsgRunnable, TIME_OUT + 1000);
    }

    private void dismissWait() {
        if (progressDialogEx != null && progressDialogEx.isShowing()) {
            progressDialogEx.dismiss();
        }
        if (mHandler != null) {
            mHandler.removeCallbacks(showMsgRunnable);
        }
    }

    private Runnable showMsgRunnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(GmChatSettingsActivity.this, R.string.failed_to_load_data, Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.em_clear_and_exit:
                askExitGroup();
                break;

            case R.id.layoutAtAll:
                break;

            default:
                break;
        }
    }

    private void askExitGroup() {
        final AskDialog askDialog = new AskDialog(this);
        askDialog.setShowMsg(getString(R.string.em_clear_and_exit) + "?");
        askDialog.setNoText(R.string.cancel);
        askDialog.setYesText(R.string.em_message_delete_sure);
        askDialog.show();
        askDialog.setOnYesClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askDialog.dismiss();
                exitGroup();
            }
        });
    }

    private Runnable leaveRunnable = new Runnable() {
        public void run() {
            try {
                GMClient.getInstance().chatroomManager().leaveChatRoom(toChatGroupId, new GMValueCallBack<GMError>() {
                    @Override
                    public void onSuccess(GMError error) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                dismissWait();
                                setResult(RESULT_EXIT_GROUP);
                                finish();
                            }
                        });
                    }

                    @Override
                    public void onError(GMError error) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                dismissWait();
                                setResult(RESULT_EXIT_GROUP);
                                finish();
                            }
                        });
                    }
                });
            } catch (Exception e) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        dismissWait();
                        setResult(RESULT_EXIT_GROUP);
                        finish();
                    }
                });
                e.printStackTrace();
            }
        }
    };

    private void exitGroup() {
        showWaitInfo();
        new Thread(leaveRunnable).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.em_group_member_notify:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        GMClient.getInstance().chatroomManager().updatePushService(toChatGroupId, !isChecked,
                            new GMValueCallBack<GMChatRoom>() {
                                @Override
                                public void onSuccess(GMChatRoom room) {
                                    PreferenceUtil.commitBoolean(GMAppConstant.GROUP_MSG_UNNOTIFY +
                                        GMClient.getInstance().getCurrentUserId() + toChatGroupId, isChecked);
                                }

                                @Override
                                public void onError(GMError gmError) {
                                    Log.i("felix", "updatePushService error=" + gmError.toString());
                                }
                            });
                    }
                }).start();
                if (isChecked) {
                    // MobclickAgent.onEvent(GmChatSettingsActivity.this, Constant.UmengEvent.EM_GROUP_UNNOTIFY);
                } else {
                    // MobclickAgent.onEvent(GmChatSettingsActivity.this, Constant.UmengEvent.EM_GROUP_NOTIFY);
                }
                break;
            default:
                break;
        }
    }

    private AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (adapter != null && position >= 0 && position < adapter.getCount()) {
            }
        }
    };
}
