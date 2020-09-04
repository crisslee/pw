// Generated code from Butter Knife. Do not modify!
package com.coomix.app.all.ui.devTime;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.View;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.coomix.app.all.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class DevTimeActivity_ViewBinding implements Unbinder {
  private DevTimeActivity target;

  @UiThread
  public DevTimeActivity_ViewBinding(DevTimeActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public DevTimeActivity_ViewBinding(DevTimeActivity target, View source) {
    this.target = target;

    target.vp = Utils.findRequiredViewAsType(source, R.id.vp, "field 'vp'", ViewPager.class);
    target.tab = Utils.findRequiredViewAsType(source, R.id.tab, "field 'tab'", TabLayout.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    DevTimeActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.vp = null;
    target.tab = null;
  }
}
