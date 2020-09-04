package com.coomix.app.all.ui.mine;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.Constant;
import com.coomix.app.all.R;
import com.coomix.app.all.model.bean.Account;
import com.coomix.app.all.model.bean.Token;
import com.coomix.app.all.service.ServiceAdapter;
import com.coomix.app.all.service.ServiceAdapter.ServiceAdapterCallback;
import com.coomix.app.all.ui.base.BaseActivity;
import com.coomix.app.all.ui.login.LoginActivity;
import com.coomix.app.all.widget.MyActionbar;
import com.coomix.app.all.manager.ActivityStateManager;
import com.coomix.app.framework.app.Result;
import com.coomix.app.framework.util.Encryption;
import com.coomix.app.framework.util.KeyboardUtils;
import com.coomix.app.all.dialog.ProgressDialogEx;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ModifyPwdActivity extends BaseActivity implements OnClickListener, ServiceAdapterCallback {
    private EditText oldPwdEt, newPwdEt, newPwd2Et;
    private Button okBtn;
    private ServiceAdapter serviceAdapter;
    private ProgressDialogEx dialog;

    private Boolean fromLogin;
    private Boolean modifyPassword = false;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_modify_pwd);

        oldPwdEt = (EditText) findViewById(R.id.old_pwd);
        newPwdEt = (EditText) findViewById(R.id.new_pwd);
        newPwd2Et = (EditText) findViewById(R.id.new_pwd2);

        okBtn = (Button) findViewById(R.id.ok_button);
        okBtn.setOnClickListener(this);

        serviceAdapter = ServiceAdapter.getInstance(this);
        serviceAdapter.registerServiceCallBack(this);

        fromLogin = getIntent().getBooleanExtra("fromLogin", false);

        MyActionbar actionbar = (MyActionbar) findViewById(R.id.myActionbar);
        actionbar.initActionbar(true, R.string.modify_pwd, R.string.community_done, 0);
        actionbar.setLeftImageClickListener(view -> {
            if (fromLogin) {
                Intent intent = new Intent();
                intent.putExtra("modifyPassword", modifyPassword);
                setResult(RESULT_OK, intent);
                finish();
            } else {
                finish();
            }
        });
        actionbar.setRightTextClickListener(view -> doAction());
    }

    private void doAction() {
        KeyboardUtils.hide(getApplicationContext(), getCurrentFocus());
        String oldpwdStr = oldPwdEt.getText().toString().trim();
        String newpwdStr = newPwdEt.getText().toString().trim();
        String newpwd2Str = newPwd2Et.getText().toString().trim();
        if (TextUtils.isEmpty(oldpwdStr)) {
            Toast.makeText(this, getString(R.string.old_pwd), Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(newpwdStr)) {
            Toast.makeText(this, getString(R.string.new_pwd), Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(newpwd2Str)) {
            Toast.makeText(this, getString(R.string.new_pwd2), Toast.LENGTH_SHORT).show();
            return;
        }
        if (!newpwdStr.equals(newpwd2Str)) {
            Toast.makeText(this, getString(R.string.pwd_different), Toast.LENGTH_SHORT).show();
            return;
        }

        if (oldpwdStr.equals(newpwdStr)) {
            Toast.makeText(this, getString(R.string.pwd_same), Toast.LENGTH_SHORT).show();
            return;
        }

        if (!TextUtils.isEmpty(newpwdStr) && newpwdStr.length() < 2) {
            Toast.makeText(this, R.string.pwd_low, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!TextUtils.isEmpty(oldpwdStr) && oldpwdStr.length() < 2) {
            Toast.makeText(this, R.string.pwd_low, Toast.LENGTH_SHORT).show();
            return;
        }

        dialog = ProgressDialogEx.show(this, "", getString(R.string.loading), false, 0, null);
        serviceAdapter.modifyPwd(this.hashCode(), AllOnlineApp.sToken.access_token.trim(), AllOnlineApp.sAccount.trim(),
            oldPwdEt.getText().toString().trim(), encrypt(newpwdStr));
    }

    @Override
    public void onClick(View v) {
        if (v == okBtn) {
            doAction();
        }
    }

    @Override
    public void onBackPressed() {
        if (fromLogin) {
            Intent intent = new Intent();
            intent.putExtra("modifyPassword", modifyPassword);
            setResult(RESULT_OK, intent);
            finish();
        } else {
            finish();
        }
        super.onBackPressed();
    }

    /**
     * 密码加密
     */
    public static String encrypt(String sSrc) {
        StringBuilder sb = new StringBuilder();
        int length = sSrc.length();
        for (int i = 0; i < length; i++) {
            byte c = (byte) (sSrc.charAt(i) ^ 20000);
            sb.append((char) c);
        }
        return sb.toString();
    }

    @Override
    protected void onDestroy() {
        if (serviceAdapter != null) {
            serviceAdapter.unregisterServiceCallBack(this);
        }
        super.onDestroy();
    }

    @Override
    public void callback(int messageId, Result result) {
        if (result.statusCode == Result.ERROR_NETWORK) {
            if (dialog != null) {
                dialog.dismiss();
            }
            Toast.makeText(this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
            return;
        }

        if (result.apiCode == Constant.FM_APIID_MODIFY_PWD) {
            if (dialog != null) {
                dialog.dismiss();
            }
            if (result.statusCode == Result.OK) {
                Toast.makeText(this, getString(R.string.msg_modify_pwd_success), Toast.LENGTH_SHORT).show();

                modifyPassword = true;

                Cursor cursor = AllOnlineApp.mDb.getAccount(AllOnlineApp.sAccount);
                cursor.moveToFirst();
                Account account = Account.parseCursor(cursor);

                if (cursor != null) {
                    cursor.close();
                }

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                AllOnlineApp.sPassword = new Encryption()
                    .encrypt(Constant.SECRET_KEY, newPwdEt.getText().toString().trim());
                if (!TextUtils.isEmpty(account.password)) {
                    account.password = AllOnlineApp.sPassword;
                } else {
                    account.password = "";
                }
                account.time = dateFormat.format(new Date());
                AllOnlineApp.mDb.insertAccount(account);

                AllOnlineApp.sToken = ((Token) result.mResult);
                if (AllOnlineApp.sToken == null) {
                    Toast.makeText(getApplicationContext(), getString(R.string.token_expiry), Toast.LENGTH_SHORT)
                        .show();
                    ActivityStateManager.finishAll();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.putExtra("fromBoot", false);
                    startActivity(intent);
                }

                if (fromLogin) {
                    Intent intent = new Intent();
                    intent.putExtra("modifyPassword", modifyPassword);
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    finish();
                }
            } else {
                Toast.makeText(this, result.errorMessage, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
