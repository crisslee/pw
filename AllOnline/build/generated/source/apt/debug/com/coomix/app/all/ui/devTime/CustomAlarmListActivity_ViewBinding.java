// Generated code from Butter Knife. Do not modify!
package com.coomix.app.all.ui.devTime;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.coomix.app.all.R;
import com.coomix.app.all.widget.MyActionbar;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuRecyclerView;
import java.lang.IllegalStateException;
import java.lang.Override;

public class CustomAlarmListActivity_ViewBinding implements Unbinder {
  private CustomAlarmListActivity target;

  @UiThread
  public CustomAlarmListActivity_ViewBinding(CustomAlarmListActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public CustomAlarmListActivity_ViewBinding(CustomAlarmListActivity target, View source) {
    this.target = target;

    target.actionbar = Utils.findRequiredViewAsType(source, R.id.topbar, "field 'actionbar'", MyActionbar.class);
    target.rvList = Utils.findRequiredViewAsType(source, R.id.rvList, "field 'rvList'", SwipeMenuRecyclerView.class);
    target.add = Utils.findRequiredViewAsType(source, R.id.add, "field 'add'", TextView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    CustomAlarmListActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.actionbar = null;
    target.rvList = null;
    target.add = null;
  }
}
