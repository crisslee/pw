// Generated code from Butter Knife. Do not modify!
package com.coomix.app.all.ui.devTime;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.coomix.app.all.R;
import com.coomix.app.all.widget.MyActionbar;
import java.lang.IllegalStateException;
import java.lang.Override;

public class WeekFragment_ViewBinding implements Unbinder {
  private WeekFragment target;

  @UiThread
  public WeekFragment_ViewBinding(WeekFragment target, View source) {
    this.target = target;

    target.topbar = Utils.findRequiredViewAsType(source, R.id.topbar, "field 'topbar'", MyActionbar.class);
    target.rlTime = Utils.findRequiredViewAsType(source, R.id.chooseTime, "field 'rlTime'", RelativeLayout.class);
    target.time = Utils.findRequiredViewAsType(source, R.id.time, "field 'time'", TextView.class);
    target.itemMon = Utils.findRequiredViewAsType(source, R.id.itemMon, "field 'itemMon'", TextView.class);
    target.itemTue = Utils.findRequiredViewAsType(source, R.id.itemTue, "field 'itemTue'", TextView.class);
    target.itemWed = Utils.findRequiredViewAsType(source, R.id.itemWed, "field 'itemWed'", TextView.class);
    target.itemThu = Utils.findRequiredViewAsType(source, R.id.itemThu, "field 'itemThu'", TextView.class);
    target.itemFri = Utils.findRequiredViewAsType(source, R.id.itemFri, "field 'itemFri'", TextView.class);
    target.itemSat = Utils.findRequiredViewAsType(source, R.id.itemSat, "field 'itemSat'", TextView.class);
    target.itemSun = Utils.findRequiredViewAsType(source, R.id.itemSun, "field 'itemSun'", TextView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    WeekFragment target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.topbar = null;
    target.rlTime = null;
    target.time = null;
    target.itemMon = null;
    target.itemTue = null;
    target.itemWed = null;
    target.itemThu = null;
    target.itemFri = null;
    target.itemSat = null;
    target.itemSun = null;
  }
}
