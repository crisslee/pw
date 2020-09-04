// Generated code from Butter Knife. Do not modify!
package com.coomix.app.all.ui.history;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.coomix.app.all.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class HistoryParentActivity_ViewBinding implements Unbinder {
  private HistoryParentActivity target;

  @UiThread
  public HistoryParentActivity_ViewBinding(HistoryParentActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public HistoryParentActivity_ViewBinding(HistoryParentActivity target, View source) {
    this.target = target;

    target.back = Utils.findRequiredViewAsType(source, R.id.back, "field 'back'", TextView.class);
    target.txtTotalTime = Utils.findRequiredViewAsType(source, R.id.txtTotalTime, "field 'txtTotalTime'", TextView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    HistoryParentActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.back = null;
    target.txtTotalTime = null;
  }
}
