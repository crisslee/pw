// Generated code from Butter Knife. Do not modify!
package com.coomix.app.all.ui.login;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.coomix.app.all.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class RegisterActivity_ViewBinding implements Unbinder {
  private RegisterActivity target;

  @UiThread
  public RegisterActivity_ViewBinding(RegisterActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public RegisterActivity_ViewBinding(RegisterActivity target, View source) {
    this.target = target;

    target.back = Utils.findRequiredViewAsType(source, R.id.back, "field 'back'", TextView.class);
    target.title = Utils.findRequiredViewAsType(source, R.id.title, "field 'title'", TextView.class);
    target.phone = Utils.findRequiredViewAsType(source, R.id.phone, "field 'phone'", EditText.class);
    target.captcha = Utils.findRequiredViewAsType(source, R.id.captcha, "field 'captcha'", EditText.class);
    target.getCaptcha = Utils.findRequiredViewAsType(source, R.id.getCaptcha, "field 'getCaptcha'", TextView.class);
    target.next = Utils.findRequiredViewAsType(source, R.id.next, "field 'next'", TextView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    RegisterActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.back = null;
    target.title = null;
    target.phone = null;
    target.captcha = null;
    target.getCaptcha = null;
    target.next = null;
  }
}
