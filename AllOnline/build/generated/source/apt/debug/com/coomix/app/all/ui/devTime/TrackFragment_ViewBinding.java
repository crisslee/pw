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

public class TrackFragment_ViewBinding implements Unbinder {
  private TrackFragment target;

  @UiThread
  public TrackFragment_ViewBinding(TrackFragment target, View source) {
    this.target = target;

    target.topbar = Utils.findRequiredViewAsType(source, R.id.topbar, "field 'topbar'", MyActionbar.class);
    target.time = Utils.findRequiredViewAsType(source, R.id.time, "field 'time'", EditText.class);
    target.maxIntervalHint = Utils.findRequiredViewAsType(source, R.id.maxIntervalHint, "field 'maxIntervalHint'", TextView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    TrackFragment target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.topbar = null;
    target.time = null;
    target.maxIntervalHint = null;
  }
}
