package com.coomix.app.all.ui.installer;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.GlideApp;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.GlobalParam;
import com.coomix.app.all.R;
import com.coomix.app.all.data.BaseSubscriber;
import com.coomix.app.all.data.DataEngine;
import com.coomix.app.all.data.ExceptionHandle;
import com.coomix.app.all.data.RxUtils;
import com.coomix.app.all.dialog.InputDialog;
import com.coomix.app.all.model.response.RespPlateInfo;
import com.coomix.app.all.model.response.RespServiceProvider;
import com.coomix.app.all.model.response.RespUploadImage;
import com.coomix.app.all.share.PopupWindowUtil;
import com.coomix.app.all.share.TextSet;
import com.coomix.app.all.ui.base.BaseActivity;
import com.coomix.app.all.widget.MyActionbar;
import com.coomix.app.framework.util.CommonUtil;
import com.coomix.app.framework.util.PreferenceUtil;
import com.coomix.app.all.util.PermissionUtil;
import com.google.gson.Gson;
import com.google.zxing.activity.CaptureActivity;
import com.google.zxing.util.UriUtil;
import com.google.zxing.util.ZConstant;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import org.json.JSONArray;

/**
 * 安装工信息上传
 *
 * @author goome
 */
public class UploadInfoActivity extends BaseActivity {
    private static final int REQUEST_CODE_SCAN_GALLERY = 100;
    private static final int REQUEST_TAKE_PHOTO_CODE = 101;
    private static final int REQUEST_SCAN_CODE = 102;
    private static final int CLICK_IMEI = 0;
    private static final int CLICK_CAR_NUMBER = 1;
    private static final int CLICK_LOCATION = 2;
    private static final int CLICK_HOST = 3;
    private static final String PRE_HOST = "pre_host";
    private TextView textImei, textCarNum, textHost;
    private ImageView imageCarNumber, imageSetLocation;
    private String setLocationImagePath, carNumberPath;
    private String locImageUrl, carImageUrl;

    private File photoFile = null;

    private RespServiceProvider.ServiceProvider spInfo;
    private int iClickType = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_upload_info);

        initViews();
    }

    private void initViews() {
        MyActionbar actionbar = (MyActionbar) findViewById(R.id.mineActionbar);
        actionbar.initActionbar(true, R.string.upload_info, 0, 0);

        textImei = (TextView) findViewById(R.id.textViewImei);
        textHost = (TextView) findViewById(R.id.textViewHost);
        textCarNum = (TextView) findViewById(R.id.textViewCarNum);

        imageCarNumber = (ImageView) findViewById(R.id.imageViewCarNumber);
        imageSetLocation = (ImageView) findViewById(R.id.imageViewSetLocation);
        imageCarNumber.setVisibility(View.GONE);
        imageSetLocation.setVisibility(View.GONE);

        findViewById(R.id.layoutImei).setOnClickListener(view -> itemClickAction(CLICK_IMEI));
        findViewById(R.id.layoutCarNumber).setOnClickListener(view -> itemClickAction(CLICK_CAR_NUMBER));
        findViewById(R.id.layoutDevLocation).setOnClickListener(view -> itemClickAction(CLICK_LOCATION));
        findViewById(R.id.layoutHost).setOnClickListener(view -> itemClickAction(CLICK_HOST));
        findViewById(R.id.textViewCommit).setOnClickListener(view -> commitData());
    }

    private void itemClickAction(int iType) {
        iClickType = iType;
        ArrayList<TextSet> list = new ArrayList<TextSet>();
        final String title;
        if (iType == CLICK_IMEI) {
            title = "IMEI";
            TextSet textSet0 = new TextSet(R.string.to_scan, false, view -> {
                //扫描imei
                goToScanCode();
            });

            TextSet textSet1 = new TextSet(R.string.input_man, false, view -> {
                //输入imei
                inputDialod(iType, InputType.TYPE_CLASS_NUMBER, title);
            });
            list.add(textSet0);
            list.add(textSet1);
        } else if (iType == CLICK_HOST) {
            title = "服务器域名";
            TextSet textSet0 = new TextSet(R.string.history, false, view -> {
                //历史host
                showHostHistory();
            });

            TextSet textSet1 = new TextSet(R.string.input_man, false, view -> {
                //输入host
                inputDialod(iType, InputType.TYPE_CLASS_TEXT, title);
            });

            list.add(textSet0);
            list.add(textSet1);
        } else if (iType == CLICK_CAR_NUMBER) {
            title = "车牌号";
            //车牌
            TextSet textSet0 = new TextSet(R.string.photos, false, view -> {
                //相册
                goToPictures();
            });

            TextSet textSet1 = new TextSet(R.string.attach_take_pic, false, view -> {
                //拍照
                goToTakePhotos();
            });
            TextSet textSet2 = new TextSet(R.string.input_man, false, view -> {
                //输入
                inputDialod(iType, InputType.TYPE_CLASS_TEXT, title);
            });
            list.add(textSet0);
            list.add(textSet1);
            list.add(textSet2);
        } else if (iType == CLICK_LOCATION) {
            title = "安装位置";
            //安装位置
            TextSet textSet0 = new TextSet(R.string.photos, false, view -> {
                //相册
                goToPictures();
            });

            TextSet textSet1 = new TextSet(R.string.attach_take_pic, false, view -> {
                //拍照
                goToTakePhotos();
            });
            list.add(textSet0);
            list.add(textSet1);
        } else {
            return;
        }

        PopupWindowUtil.showPopWindow(this, getWindow().getDecorView(), title, list, false);
    }

    private void goToScanCode() {
        Intent i = new Intent(this, CaptureActivity.class);
        i.putExtra(CaptureActivity.SHOW_MANUAL, true);
        startActivityForResult(i, REQUEST_SCAN_CODE);
    }

    @SuppressWarnings("CheckResult")
    private void goToPictures() {
        rxPermissions.requestEach(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(permission -> {
                    if (permission.granted) {
                        goGallery();
                    } else if (permission.shouldShowRequestPermissionRationale) {
                        showSettingDlg(getString(R.string.per_stg_hint), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                PermissionUtil.goIntentSetting(UploadInfoActivity.this);
                            }
                        });
                    }
                });
    }

    private void goGallery() {
        //打开相册
        Intent innerIntent = new Intent(Intent.ACTION_GET_CONTENT);
        innerIntent.setType("image/*");
        startActivityForResult(innerIntent, REQUEST_CODE_SCAN_GALLERY);
    }

    @SuppressWarnings("CheckResult")
    private void goToTakePhotos() {
        rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        if (aBoolean) {
                            takePhoto();//拍照逻辑
                        } else {
                            showSettingDlg(getString(R.string.no_stg_camera_hint), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    PermissionUtil.goIntentSetting(UploadInfoActivity.this);
                                }
                            });
                        }
                    }
                });
    }

    private void takePhoto() {
        //调取系统拍照
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(this.getPackageManager()) != null) {
            // 设置系统相机拍照后的输出路径
            // 创建临时文件
            photoFile = createTmpFile(this);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
            startActivityForResult(cameraIntent, REQUEST_TAKE_PHOTO_CODE);
        } else {
            Toast.makeText(this, R.string.camerasdk_msg_no_camera, Toast.LENGTH_SHORT).show();
        }
    }

    public File createTmpFile(Context context) {
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            // 已挂载
            File pic = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(new Date());
            String fileName = "upload_image_" + timeStamp + "";
            File tmpFile = new File(pic, fileName + ".jpg");
            return tmpFile;
        } else {
            File cacheDir = context.getCacheDir();
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(new Date());
            String fileName = "upload_image_" + timeStamp + "";
            File tmpFile = new File(cacheDir, fileName + ".jpg");
            return tmpFile;
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_SCAN_CODE:
                if (resultCode != RESULT_OK) {
                    return;
                }
                Bundle b = data.getExtras();
                if (b == null) {
                    return;
                }
                if (b.containsKey(ZConstant.INTENT_EXTRA_KEY_QR_SCAN)) {
                    String imei = b.getString(ZConstant.INTENT_EXTRA_KEY_QR_SCAN);
                    textImei.setText(imei);
                }
                break;

            case REQUEST_CODE_SCAN_GALLERY:
                if (resultCode != RESULT_OK) {
                    return;
                }
                String path = UriUtil.getRealPathFromUri(this, data.getData());
                if (TextUtils.isEmpty(path)) {
                    return;
                }
                if (iClickType == CLICK_CAR_NUMBER) {
                    //车牌
                    carNumberPath = path;
                    imageCarNumber.setVisibility(View.VISIBLE);
                    textCarNum.setText("");
                    GlideApp.with(UploadInfoActivity.this).load(path).placeholder(R.drawable.image_default).error(R.drawable.image_default_error).into(imageCarNumber);
                } else if (iClickType == CLICK_LOCATION) {
                    //安装位置
                    setLocationImagePath = path;
                    imageSetLocation.setVisibility(View.VISIBLE);
                    GlideApp.with(UploadInfoActivity.this).load(path).placeholder(R.drawable.image_default).error(R.drawable.image_default_error).into(imageSetLocation);
                }
                break;

            case REQUEST_TAKE_PHOTO_CODE:
                if (resultCode == RESULT_OK) {
                    if (photoFile != null) {
                        String path1 = photoFile.getAbsolutePath();
                        if (iClickType == CLICK_CAR_NUMBER) {
                            //车牌
                            carNumberPath = path1;
                            imageCarNumber.setVisibility(View.VISIBLE);
                            GlideApp.with(UploadInfoActivity.this).load(path1).placeholder(R.drawable.image_default).error(R.drawable.image_default_error).into(imageCarNumber);
                        } else if (iClickType == CLICK_LOCATION) {
                            //安装位置
                            setLocationImagePath = path1;
                            imageSetLocation.setVisibility(View.VISIBLE);
                            GlideApp.with(UploadInfoActivity.this).load(path1).placeholder(R.drawable.image_default).error(R.drawable.image_default_error).into(imageSetLocation);
                        }
                    }
                } else if (photoFile != null && photoFile.exists()) {
                    photoFile.delete();
                }
                break;

            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void inputDialod(final int iType, int inputType, String title) {
        final InputDialog inputDialog = new InputDialog(this);
        int iMaxLen = 0;
        if(iType == CLICK_IMEI){
            iMaxLen = 15;
        }
        inputDialog.setStyle(title, inputType, iMaxLen, new InputDialog.ResultCallback() {
            @Override
            public void callback(String data) {
                if (iType == CLICK_IMEI) {
                    textImei.setText(data);
                } else if (iType == CLICK_CAR_NUMBER) {
                    if(TextUtils.isEmpty(data) || isCarNumner(data)) {
                        textCarNum.setText(data);
                    }else{
                        showToast("输入的车牌号有误，请重新输入！");
                    }
                } else if (iType == CLICK_HOST) {
                    textHost.setText(data);
                }
            }
        });
        inputDialog.show();
    }

    private void showHostHistory() {
        final ArrayList<String> list = getHostHistory();
        if (list == null || list.size() <= 0) {
            showToast("暂无历史域名！请重新输入");
            return;
        }

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_host_history, null);
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(view);
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = AllOnlineApp.screenWidth * 4 / 5;
        params.height = AllOnlineApp.screenHeight * 4 / 5;
        dialog.getWindow().setAttributes(params);
        dialog.show();

        ListView listView = (ListView) view.findViewById(R.id.listViewHis);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.upload_host_history_item, list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (position >= 0 && position < list.size()) {
                    textHost.setText(list.get(position));
                }
                dialog.dismiss();
            }
        });
    }

    private void uploadPicture(String filePath, final int iType) {
        if(TextUtils.isEmpty(filePath)){
            return;
        }
        showLoading("正在上传图片...");
        File file = new File(filePath);
        RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("name", file.getName())
                .addFormDataPart("content", file.getName(), RequestBody.create(MediaType.parse("image/*"), file))
                .build();
        Disposable d = DataEngine.getUploadInfoApi()
                .uploadPicture(GlobalParam.getInstance().getCommonParas(), AllOnlineApp.sToken.access_token, requestBody)
                .compose(RxUtils.toMain())
                .compose(RxUtils.businessTransformer())
                .subscribeWith(new BaseSubscriber<RespUploadImage>() {
                    @Override
                    public void onNext(RespUploadImage imageInfo) {
                        if (imageInfo.getData() != null) {
                            if (iType == CLICK_CAR_NUMBER) {
                                carImageUrl = imageInfo.getData().getUrl();
                                uploadPicture(setLocationImagePath, CLICK_LOCATION);
                            } else {
                                hideLoading();
                                locImageUrl = imageInfo.getData().getUrl();
                                if (bInputCarNum) {
                                    commitAllData(textCarNum.getText().toString());
                                } else {
                                    hyperlpr(carImageUrl);
                                }
                            }
                        }
                    }

                    @Override
                    public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                        hideLoading();
                        showToast(getString(R.string.image_upload_fail));
                    }
                });
        subscribeRx(d);
    }

    /***车牌识别***/
    private void hyperlpr(final String imageUrl) {
        showLoading("正在自动识别车牌号...");
        Disposable d = DataEngine.getUploadInfoApi()
                .hyperlpr(GlobalParam.getInstance().getCommonParas(), AllOnlineApp.sToken.access_token, imageUrl)
                .compose(RxUtils.toMain())
                .compose(RxUtils.businessTransformer())
                .subscribeWith(new BaseSubscriber<RespPlateInfo>() {
                    @Override
                    public void onNext(RespPlateInfo respPlateInfo) {
                        if (respPlateInfo != null && respPlateInfo.getData() != null && !TextUtils.isEmpty(respPlateInfo.getData().getPlate())) {
                            if (respPlateInfo.getData().getPlate().contains("recognize") || !isCarNumner(respPlateInfo.getData().getPlate())) {
                                //识别失败调取ali的识别
                                aliHyperlpr(imageUrl);
                            } else {
                                hideLoading();
                                textCarNum.setText(respPlateInfo.getData().getPlate());
                                commitAllData(respPlateInfo.getData().getPlate());
                            }
                        }else{
                            //识别失败调取ali的识别
                            aliHyperlpr(imageUrl);
                        }
                    }

                    @Override
                    public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                        showToast("车牌号识别失败，请重新拍照识别");
                        hideLoading();
                    }
                });
        subscribeRx(d);
    }

    /***车牌识别 ali***/
    private void aliHyperlpr(String imageUrl) {
        showLoading("正在自动识别车牌号...");
        Disposable d = DataEngine.getUploadInfoApi()
                .aliHyperlpr(GlobalParam.getInstance().getCommonParas(), AllOnlineApp.sToken.access_token, imageUrl)
                .compose(RxUtils.toMain())
                .compose(RxUtils.businessTransformer())
                .subscribeWith(new BaseSubscriber<RespPlateInfo>() {
                    @Override
                    public void onNext(RespPlateInfo respPlateInfo) {
                        hideLoading();
                        if (respPlateInfo != null && respPlateInfo.getData() != null && !TextUtils.isEmpty(respPlateInfo.getData().getPlate())
                                && isCarNumner(respPlateInfo.getData().getPlate())) {
                            textCarNum.setText(respPlateInfo.getData().getPlate());
                            commitAllData(respPlateInfo.getData().getPlate());
                        }else{
                            showToast("车牌号识别失败，请重新拍照识别");
                        }
                    }

                    @Override
                    public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                        showToast("车牌号识别失败，请重新拍照识别");
                        hideLoading();
                    }
                });
        subscribeRx(d);
    }

    private void commitAllData(String carNumber) {
        showLoading("正在提交所有数据...");
        String host = textHost.getText().toString();
        String imei = textImei.getText().toString();
        Disposable d = DataEngine.getUploadInfoApi()
                .enterInstallInfo(GlobalParam.getInstance().getCommonParas(), AllOnlineApp.sToken.access_token, imei, carNumber, host, carImageUrl)
                .compose(RxUtils.toMain())
                .compose(RxUtils.businessTransformer())
                .subscribeWith(new BaseSubscriber<RespPlateInfo>() {
                    @Override
                    public void onNext(RespPlateInfo respPlateInfo) {
                        hideLoading();
                        showToast("数据提交成功!");
                        saveHostHistory();
                    }

                    @Override
                    public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                        showToast("数据提交失败请重试");
                        hideLoading();
                    }
                });
        subscribeRx(d);
    }

    private boolean bInputCarNum = false;

    private void commitData() {
        bInputCarNum = false;
        if (textImei.getText() == null || TextUtils.isEmpty(textImei.getText().toString()) || textImei.getText().length() != 15) {
            showToast("请输入准确的IMEI号信息");
            return;
        }

        if (TextUtils.isEmpty(carNumberPath) && (textCarNum.getText() == null || TextUtils.isEmpty(textCarNum.getText().toString()))) {
            showToast("请先拍取车牌号照片或输入车牌号");
            return;
        }

        if (TextUtils.isEmpty(setLocationImagePath)) {
            showToast("请先拍取定位器的安装位置照片");
            return;
        }

        if (textHost.getText() == null || TextUtils.isEmpty(textHost.getText().toString())) {
            showToast("请先完善域名信息");
            return;
        }

        if (textCarNum.getText() != null && !TextUtils.isEmpty(textCarNum.getText().toString())) {
            bInputCarNum = true;
            uploadPicture(setLocationImagePath, CLICK_LOCATION);
        } else {
            uploadPicture(carNumberPath, CLICK_CAR_NUMBER);
        }
    }

    public void saveHostHistory() {
        if (textHost.getText() == null || TextUtils.isEmpty(textHost.getText().toString())) {
            return;
        }
        String host = textHost.getText().toString();
        ArrayList<String> list = getHostHistory();
        if (list.contains(host)) {
            list.remove(host);
        }
        list.add(0, host);

        String data = new Gson().toJson(list);
        PreferenceUtil.commitString(PRE_HOST, data);
    }

    public ArrayList<String> getHostHistory() {
        String data = PreferenceUtil.getString(PRE_HOST, null);
        if (TextUtils.isEmpty(data)) {
            return new ArrayList<String>();
        }
        ArrayList<String> list = new ArrayList<String>();
        try {
            JSONArray jsonArray = new JSONArray(data);
            for (int i = 0; i < jsonArray.length(); i++) {
                list.add(jsonArray.get(i).toString());
            }
        } catch (Exception e) {
        }
        return list;
    }

    private boolean isCarNumner(String number)
    {
        final String match = "^(([京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z](([0-9]{5}[DF])|([DF]([A-HJ-NP-Z0-9])[0-9]{4})))" +
                "|([京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z][A-HJ-NP-Z0-9]{4}[A-HJ-NP-Z0-9挂学警港澳使领]))$";
        return CommonUtil.isMatched(match, number);
    }
}
