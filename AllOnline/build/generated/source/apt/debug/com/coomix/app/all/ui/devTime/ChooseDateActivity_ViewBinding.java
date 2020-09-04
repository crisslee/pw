// Generated code from Butter Knife. Do not modify!
package com.coomix.app.all.ui.devTime;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.coomix.app.all.R;
import com.coomix.app.all.widget.MyActionbar;
import java.lang.IllegalStateException;
import java.lang.Override;

public class ChooseDateActivity_ViewBinding implements Unbinder {
  private ChooseDateActivity target;

  @UiThread
  public ChooseDateActivity_ViewBinding(ChooseDateActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public ChooseDateActivity_ViewBinding(ChooseDateActivity target, View source) {
    this.target = target;

    target.rvList = Utils.findRequiredViewAsType(source, R.id.list, "field 'rvList'", RecyclerView.class);
    target.actionbar = Utils.findRequiredViewAsType(source, R.id.topbar, "field 'actionbar'", MyActionbar.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    ChooseDateActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.rvList = null;
    target.actionbar = null;
  }
}
