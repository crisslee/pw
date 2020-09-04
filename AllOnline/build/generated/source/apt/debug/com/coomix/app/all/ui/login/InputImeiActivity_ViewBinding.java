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

public class InputImeiActivity_ViewBinding implements Unbinder {
  private InputImeiActivity target;

  @UiThread
  public InputImeiActivity_ViewBinding(InputImeiActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public InputImeiActivity_ViewBinding(InputImeiActivity target, View source) {
    this.target = target;

    target.back = Utils.findRequiredViewAsType(source, R.id.back, "field 'back'", TextView.class);
    target.etImei = Utils.findRequiredViewAsType(source, R.id.inputImei, "field 'etImei'", EditText.class);
    target.bind = Utils.findRequiredViewAsType(source, R.id.bindImei, "field 'bind'", TextView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    InputImeiActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.back = null;
    target.etImei = null;
    target.bind = null;
  }
}
