// Generated code from Butter Knife. Do not modify!
package com.coomix.app.all.ui.devTime;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.coomix.app.all.R;
import com.coomix.app.all.widget.MyActionbar;
import java.lang.IllegalStateException;
import java.lang.Override;

public class LoopFragment_ViewBinding implements Unbinder {
  private LoopFragment target;

  @UiThread
  public LoopFragment_ViewBinding(LoopFragment target, View source) {
    this.target = target;

    target.topbar = Utils.findRequiredViewAsType(source, R.id.topbar, "field 'topbar'", MyActionbar.class);
    target.start = Utils.findRequiredViewAsType(source, R.id.sDate, "field 'start'", TextView.class);
    target.end = Utils.findRequiredViewAsType(source, R.id.eDate, "field 'end'", TextView.class);
    target.span = Utils.findRequiredViewAsType(source, R.id.time, "field 'span'", EditText.class);
    target.maxIntervalHint = Utils.findRequiredViewAsType(source, R.id.maxIntervalHint, "field 'maxIntervalHint'", TextView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    LoopFragment target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.topbar = null;
    target.start = null;
    target.end = null;
    target.span = null;
    target.maxIntervalHint = null;
  }
}
