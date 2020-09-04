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

public class CustomFragment_ViewBinding implements Unbinder {
  private CustomFragment target;

  @UiThread
  public CustomFragment_ViewBinding(CustomFragment target, View source) {
    this.target = target;

    target.topbar = Utils.findRequiredViewAsType(source, R.id.topbar, "field 'topbar'", MyActionbar.class);
    target.rvList = Utils.findRequiredViewAsType(source, R.id.list, "field 'rvList'", RecyclerView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    CustomFragment target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.topbar = null;
    target.rvList = null;
  }
}
