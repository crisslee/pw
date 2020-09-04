// Generated code from Butter Knife. Do not modify!
package com.coomix.app.all.ui.detail;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.coomix.app.all.R;
import com.coomix.app.all.widget.MyActionbar;
import java.lang.IllegalStateException;
import java.lang.Override;

public class DistanceStatisticsActivity_ViewBinding implements Unbinder {
  private DistanceStatisticsActivity target;

  @UiThread
  public DistanceStatisticsActivity_ViewBinding(DistanceStatisticsActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public DistanceStatisticsActivity_ViewBinding(DistanceStatisticsActivity target, View source) {
    this.target = target;

    target.bar = Utils.findRequiredViewAsType(source, R.id.actionBar, "field 'bar'", MyActionbar.class);
    target.rvList = Utils.findRequiredViewAsType(source, R.id.rvList, "field 'rvList'", RecyclerView.class);
    target.tvSummary = Utils.findRequiredViewAsType(source, R.id.tvSummary, "field 'tvSummary'", TextView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    DistanceStatisticsActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.bar = null;
    target.rvList = null;
    target.tvSummary = null;
  }
}
