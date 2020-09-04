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

public class SetPwdActivity_ViewBinding implements Unbinder {
  private SetPwdActivity target;

  @UiThread
  public SetPwdActivity_ViewBinding(SetPwdActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public SetPwdActivity_ViewBinding(SetPwdActivity target, View source) {
    this.target = target;

    target.close = Utils.findRequiredViewAsType(source, R.id.back, "field 'close'", ImageView.class);
    target.title = Utils.findRequiredViewAsType(source, R.id.title, "field 'title'", TextView.class);
    target.etPwd = Utils.findRequiredViewAsType(source, R.id.password, "field 'etPwd'", EditText.class);
    target.etConfirmPwd = Utils.findRequiredViewAsType(source, R.id.confirmPwd, "field 'etConfirmPwd'", EditText.class);
    target.etDevice = Utils.findRequiredViewAsType(source, R.id.deviceNick, "field 'etDevice'", EditText.class);
    target.tvRegister = Utils.findRequiredViewAsType(source, R.id.register, "field 'tvRegister'", TextView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    SetPwdActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.close = null;
    target.title = null;
    target.etPwd = null;
    target.etConfirmPwd = null;
    target.etDevice = null;
    target.tvRegister = null;
  }
}
