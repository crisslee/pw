// Generated code from Butter Knife. Do not modify!
package com.coomix.app.all.ui.login;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.coomix.app.all.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class LoginActivity_ViewBinding implements Unbinder {
  private LoginActivity target;

  @UiThread
  public LoginActivity_ViewBinding(LoginActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public LoginActivity_ViewBinding(LoginActivity target, View source) {
    this.target = target;

    target.register = Utils.findRequiredViewAsType(source, R.id.register, "field 'register'", TextView.class);
    target.title = Utils.findRequiredViewAsType(source, R.id.title, "field 'title'", TextView.class);
    target.statement = Utils.findRequiredViewAsType(source, R.id.statement, "field 'statement'", TextView.class);
    target.phoneOrImei = Utils.findRequiredViewAsType(source, R.id.phoneOrImei, "field 'phoneOrImei'", EditText.class);
    target.password = Utils.findRequiredViewAsType(source, R.id.password, "field 'password'", EditText.class);
    target.getCaptcha = Utils.findRequiredViewAsType(source, R.id.getCaptcha, "field 'getCaptcha'", TextView.class);
    target.login = Utils.findRequiredViewAsType(source, R.id.login, "field 'login'", TextView.class);
    target.forgetPwd = Utils.findRequiredViewAsType(source, R.id.forgetPwd, "field 'forgetPwd'", TextView.class);
    target.loginByCaptcha = Utils.findRequiredViewAsType(source, R.id.loginByCaptcha, "field 'loginByCaptcha'", TextView.class);
    target.imageWx = Utils.findRequiredViewAsType(source, R.id.imageWx, "field 'imageWx'", ImageView.class);
    target.imageQr = Utils.findRequiredViewAsType(source, R.id.imageQr, "field 'imageQr'", ImageView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    LoginActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.register = null;
    target.title = null;
    target.statement = null;
    target.phoneOrImei = null;
    target.password = null;
    target.getCaptcha = null;
    target.login = null;
    target.forgetPwd = null;
    target.loginByCaptcha = null;
    target.imageWx = null;
    target.imageQr = null;
  }
}
