// Generated code from Butter Knife. Do not modify!
package com.coomix.app.all.ui.devTime;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.coomix.app.all.R;
import com.coomix.app.all.widget.MyActionbar;
import java.lang.IllegalStateException;
import java.lang.Override;

public class ChooseTimeActivity_ViewBinding implements Unbinder {
  private ChooseTimeActivity target;

  @UiThread
  public ChooseTimeActivity_ViewBinding(ChooseTimeActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public ChooseTimeActivity_ViewBinding(ChooseTimeActivity target, View source) {
    this.target = target;

    target.actionbar = Utils.findRequiredViewAsType(source, R.id.topbar, "field 'actionbar'", MyActionbar.class);
    target.day = Utils.findRequiredViewAsType(source, R.id.day, "field 'day'", NumberPicker.class);
    target.hour = Utils.findRequiredViewAsType(source, R.id.hour, "field 'hour'", NumberPicker.class);
    target.minute = Utils.findRequiredViewAsType(source, R.id.minute, "field 'minute'", NumberPicker.class);
    target.locMode = Utils.findRequiredViewAsType(source, R.id.locMode, "field 'locMode'", ConstraintLayout.class);
    target.modeGps = Utils.findRequiredViewAsType(source, R.id.modeGps, "field 'modeGps'", TextView.class);
    target.modeBase = Utils.findRequiredViewAsType(source, R.id.modeBase, "field 'modeBase'", TextView.class);
    target.modeWifi = Utils.findRequiredViewAsType(source, R.id.modeWifi, "field 'modeWifi'", TextView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    ChooseTimeActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.actionbar = null;
    target.day = null;
    target.hour = null;
    target.minute = null;
    target.locMode = null;
    target.modeGps = null;
    target.modeBase = null;
    target.modeWifi = null;
  }
}
