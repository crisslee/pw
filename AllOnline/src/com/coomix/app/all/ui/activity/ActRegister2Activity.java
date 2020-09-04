package com.coomix.app.all.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.Constant;
import com.coomix.app.all.GlobalParam;
import com.coomix.app.all.R;
import com.coomix.app.all.ui.web.DisclaimerActivity;
import com.coomix.app.all.model.bean.ActOrderInfo;
import com.coomix.app.all.model.bean.ActPrice;
import com.coomix.app.all.model.bean.CommunityActDetail;
import com.coomix.app.all.model.bean.ImageInfo;
import com.coomix.app.all.service.AllOnlineApiClient;
import com.coomix.app.all.widget.MyActionbar;
import com.coomix.app.framework.app.Result;
import com.coomix.app.framework.util.CommonUtil;
import com.coomix.app.all.data.BaseSubscriber;
import com.coomix.app.all.data.DataEngine;
import com.coomix.app.all.data.ExceptionHandle;
import com.coomix.app.all.data.RxUtils;
import com.coomix.app.all.model.response.CommunityImageInfo;
import com.coomix.app.all.model.response.RespRegisterAct;
import com.coomix.app.all.ui.base.BaseActivity;
import com.coomix.app.all.widget.DividerLine;
import com.google.gson.JsonObject;
import com.muzhi.camerasdk.PhotoPickActivity;
import com.muzhi.camerasdk.model.CameraSdkParameterInfo;

import net.goome.im.GMError;
import net.goome.im.GMValueCallBack;
import net.goome.im.chat.GMChatRoom;
import net.goome.im.chat.GMChatRoomManager;
import net.goome.im.chat.GMClient;

import java.io.File;
import java.util.ArrayList;

import io.reactivex.disposables.Disposable;

public class ActRegister2Activity extends BaseActivity
        implements View.OnClickListener {

    public static final String ACT_DETAIL_INFO = "act_detailinfo";

    public static final String TAG = "ActRegister2Activity";


    private TextView btnCommit;
    //    private Button btnCommit;
    private CheckBox cbAgrement;
    private RecyclerView recyclerView;
    private CommunityActDetail commActDetailInfo = null;
    private ActRegister2Adapter actRegister2Adapter;

    public static final String SIGN_TYPE = "sign_type";
    public static final int REG_CANCEL = 0;// 取消报名
    public static final int REG_SIGN = 1;// 活动报名
    public static final int REG_EDIT = 2;// 编辑报名
    public static final int REG_SIGN_PRE = 3;// 预约报名
    private int registerStatus = REG_SIGN;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_register2);
        if (getIntent() != null) {
            commActDetailInfo = (CommunityActDetail) getIntent().getSerializableExtra(ACT_DETAIL_INFO);
        }
        initView();
        if (commActDetailInfo != null && commActDetailInfo.getDisplay() != null) {
            actRegister2Adapter.setData(commActDetailInfo.getDisplay());
            actRegister2Adapter.loadDataFromFile();
            actRegister2Adapter.notifyDataSetChanged();

        }

        requestMyRegesteredAct();
    }


    @Override
    public void onClick(View v) {
        if (ifClickTooFast())
            return;

        switch (v.getId()) {
            case R.id.btnCommit:
                commitOrCancel();
                break;
            case R.id.textViewLiability:
                // 显示免责声明
                Intent intent = new Intent(this, DisclaimerActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    private void commitOrCancel() {
        if (!CommonUtil.isLogin()) {
            Toast.makeText(this, "您尚未登录", Toast.LENGTH_SHORT).show();
            CommonUtil.goToLogin(this);
            return;
        }

        if (!cbAgrement.isChecked()) {
            Toast.makeText(this, R.string.act_disclaimer_unselect, Toast.LENGTH_SHORT).show();
            return;
        }

        String errMsg = actRegister2Adapter.checkParams();
        if (!TextUtils.isEmpty(errMsg)) {
            showToast(errMsg);
            return;
        }

        showProgressDialog("正在上传，请稍等");
        uploadPicsThenCommit();
    }

    private void initView() {
        //action bar
        MyActionbar actionbar = (MyActionbar) findViewById(R.id.mineActionbar);
        actionbar.initActionbar(true, R.string.activity_register, 0, 0);

        //免责声明
        findViewById(R.id.textViewLiability).setOnClickListener(this);
        btnCommit = (TextView) findViewById(R.id.btnCommit);
        btnCommit.setOnClickListener(this);

        cbAgrement = (CheckBox) findViewById(R.id.cbAgrement);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        actRegister2Adapter = new ActRegister2Adapter(this);
        recyclerView.setAdapter(actRegister2Adapter);
        DividerLine dividerLine = new DividerLine(DividerLine.VERTICAL);
        dividerLine.setSize(1);
        dividerLine.setColor(getResources().getColor(R.color.line_show_thf));
        recyclerView.addItemDecoration(dividerLine);


        if (commActDetailInfo != null && commActDetailInfo.getSigned() == 1) {
            registerStatus = REG_CANCEL;

            btnCommit.setText(R.string.register_cancel);
        }

        if (registerStatus == REG_CANCEL || registerStatus == REG_EDIT) {
            btnCommit.setText(R.string.register_cancel);
        }

        if (getIntent() != null && getIntent().hasExtra(SIGN_TYPE)) {
            registerStatus = getIntent().getIntExtra(SIGN_TYPE, REG_SIGN);
        }
    }

    private void requestMyRegesteredAct() {
        if (commActDetailInfo == null || commActDetailInfo.getSigned() != 1)
            return;

        Disposable disposable = DataEngine.getCommunityApi()
                .getMyRegisteredAct(
                        GlobalParam.getInstance().getCommonParas(),
                        commActDetailInfo.getId(),
                        null)
                .compose(RxUtils.toMain())
                .compose(RxUtils.businessTransformer())
                .subscribeWith(new BaseSubscriber<JsonObject>(null) {

                    @Override
                    public void onNext(JsonObject jsonObject) {
                        showErr(jsonObject.toString());
                    }

                    @Override
                    public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                        showErr(e.getErrCodeMessage());
                    }
                });
        subscribeRx(disposable);
    }

    private void registerAct() {
        Disposable disposable = DataEngine.getCommunityApi()
                .registerAct(
                        GlobalParam.getInstance().getCommonParas(),
                        registerStatus,
                        commActDetailInfo.getId(),
                        -1,
                        actRegister2Adapter.getInputData())
                .compose(RxUtils.toMain())
                .compose(RxUtils.businessTransformer())
                .subscribeWith(new BaseSubscriber<RespRegisterAct>(ActRegister2Activity.this) {

                    @Override
                    public void onNext(RespRegisterAct response) {
                        CommActDetailActivity.bReRequestNewData = true;
                        if (registerStatus == REG_SIGN_PRE) {
                            showToast(getString(R.string.order_success));
                            finish();
                        } else {
                            parseReturnSigned(response.getData());
                        }
                    }

                    @Override
                    public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                        showErr(e.getErrCodeMessage());
                    }
                });

        subscribeRx(disposable);

    }

    private final int IMAGE_UPLOAD_FAIL = -1;
    private final int IMAGE_UPLOAD_SUCC = 0;

    private void uploadPicsThenCommit() {
        final AllOnlineApiClient client = new AllOnlineApiClient(ActRegister2Activity.this);
        final ArrayList<ImageInfo> imageInfos = actRegister2Adapter.getAllImage();
        if (imageInfos.size() == 0) {//no pics that need to be uploaded
            registerAct();
        } else { // uploads pics in background
            new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean succ = true;
                    for (ImageInfo info : imageInfos) {
                        succ = succ && uploadPic(client, info);
                    }
                    if (!succ) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showToast(getString(R.string.error_upload_pic_failed));
                                dismissProgressDialog();
                            }
                        });
                    } else {
                        registerAct();
                    }
                }
            }).start();
        }
    }

    /**
     * @param client
     * @param imageInfo
     * @return true if succ
     */
    private boolean uploadPic(AllOnlineApiClient client, ImageInfo imageInfo) {
        if (imageInfo == null || imageInfo.isNet()) {
            return true;
        }

        Result result = client.pictureUpload(AllOnlineApp.sToken.ticket, imageInfo.getSource_image(), CommonUtil.getFileMimeType(imageInfo.getSource_image()));
        if (result != null && result.success) {
            CommunityImageInfo resultImage = (CommunityImageInfo) result.mResult;
            if (resultImage != null) {
                //String picUrl = resultImage.getDomain() + resultImage.getImg_path();
                imageInfo.setImg_path(resultImage.getImg_path());
                imageInfo.setDomain(resultImage.getDomain());
                imageInfo.setNet(true);
                try {
                    new File(imageInfo.getSource_image()).delete();
                } catch (Exception e) {
                }
            }
        } else {
            return false;
        }
        return true;
    }


    private void parseReturnSigned(ActOrderInfo actOrderInfo) {
        if (actRegister2Adapter != null) {
            actRegister2Adapter.saveDataToFile();
        }

        if (actOrderInfo != null) {
            if (registerStatus == REG_CANCEL) {
                // registerStatus = REG_SIGN;
                showToast(getString(R.string.register_cancel_success));
                finish();
            } else if (registerStatus == REG_EDIT) {
                showToast(getString(R.string.register_edit_success));
                finish();
            }else {
                showToast(getString(R.string.register_success));
                if (actOrderInfo.getOrder_id() > 0) {
                    goToOrderActivity(actOrderInfo.getOrder_id());
                }
            }
        } else {
            showToast(R.string.register_fail);
        }
    }

    //报名加群成功后就初始化群的基本信息
    private void initChatroomInfo(final long roomId) {
        final GMChatRoomManager chatroomMgr = GMClient.getInstance().chatroomManager();
        if (chatroomMgr == null) {
            return;
        }
        GMChatRoom room = chatroomMgr.getChatroomInfoFromDB(roomId);
        if (room == null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    chatroomMgr.getChatroomSpecificationFromServerWithId(roomId, new GMValueCallBack<GMChatRoom>() {
                        @Override
                        public void onSuccess(GMChatRoom room) {
                            chatroomMgr.getPushServiceEnableFromServer(roomId, new GMValueCallBack<Boolean>() {
                                @Override
                                public void onSuccess(Boolean aBoolean) {
                                }

                                @Override
                                public void onError(GMError gmError) {
                                }
                            });
                        }

                        @Override
                        public void onError(GMError gmError) {
                        }
                    });
                }
            }).start();
        }
    }

    private void goToOrderActivity(long iOrderId) {
        if (commActDetailInfo != null) {
            ActPrice actPrice = commActDetailInfo.getPrice();
            if (actPrice != null) {
                if (actPrice.getType() == ActPrice.PRICE_FASTEN) {
                    //固定价格
                    if (actPrice.getFixed_price() > 0) {
                        //去支付
                        Intent intent = new Intent(this, MyOrderInfoActivity.class);
                        //intent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                        intent.putExtra(Constant.NATIVE_ACTIVITY_ID, commActDetailInfo.getId());
                        intent.putExtra(MyOrderInfoActivity.ORDER_DETAIL_OR_PAYING, MyOrderInfoActivity.ORDER_PAYING);
                        intent.putExtra(MyOrderInfoActivity.ORDER_ID, iOrderId);
                        intent.putExtra(MyOrderInfoActivity.ORDER_FROM_SIGNED, true);
                        startActivity(intent);
                        finish();
                        return;
                    }
                }
            }

            //线下支付或免费，不需要支付，直接去报了名的活动详情
            Intent intent = new Intent(this, MyOrderInfoActivity.class);
            intent.putExtra(Constant.NATIVE_ACTIVITY_ID, commActDetailInfo.getId());
            intent.putExtra(MyOrderInfoActivity.ORDER_DETAIL_OR_PAYING, MyOrderInfoActivity.ORDER_DETAIL);
            intent.putExtra(MyOrderInfoActivity.ORDER_ID, iOrderId);
            startActivity(intent);
            finish();
        }
    }


    private void showToast(int msgId) {
        Toast.makeText(this, msgId, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void finish() {
        // TODO Auto-generated method stub
        // 在CommActDetailActivity.class中判断
        this.setResult(Activity.RESULT_OK);
        super.finish();
    }


    // 下面的代码处理图片
    private CameraSdkParameterInfo mCameraSdkParameterInfo = new CameraSdkParameterInfo();

    public void toPhotoPick(int maxPicNum) {
        mCameraSdkParameterInfo.setSingle_mode(false);
        mCameraSdkParameterInfo.setShow_camera(true);
        mCameraSdkParameterInfo.setMax_image(maxPicNum);
        mCameraSdkParameterInfo.setCroper_image(true);
        mCameraSdkParameterInfo.setFilter_image(false);

        Intent intent = new Intent();
        intent.setClassName(getApplication(), PhotoPickActivity.class.getName());

        Bundle b = new Bundle();
        b.putSerializable(CameraSdkParameterInfo.EXTRA_PARAMETER, mCameraSdkParameterInfo);
        intent.putExtras(b);
        startActivityForResult(intent, CameraSdkParameterInfo.TAKE_PICTURE_FROM_GALLERY);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CameraSdkParameterInfo.TAKE_PICTURE_FROM_GALLERY) {
            if (data != null) {
                actRegister2Adapter.parsePicResult(data.getExtras(), true);
            }
        } else if (requestCode == CameraSdkParameterInfo.TAKE_PICTURE_PREVIEW) {
            if (data != null) {
                actRegister2Adapter.parsePicResult(data.getExtras(), false);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
