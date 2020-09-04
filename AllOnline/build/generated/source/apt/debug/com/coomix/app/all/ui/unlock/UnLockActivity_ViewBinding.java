// Generated code from Butter Knife. Do not modify!
package com.coomix.app.all.ui.unlock;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.coomix.app.all.R;
import com.coomix.app.all.widget.MyActionbar;
import com.handmark.pulltorefresh.library.PullToRefreshRecyclerView;
import java.lang.IllegalStateException;
import java.lang.Override;

public class UnLockActivity_ViewBinding implements Unbinder {
  private UnLockActivity target;

  @UiThread
  public UnLockActivity_ViewBinding(UnLockActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public UnLockActivity_ViewBinding(UnLockActivity target, View source) {
    this.target = target;

    target.mActionbar = Utils.findRequiredViewAsType(source, R.id.mineActionbar, "field 'mActionbar'", MyActionbar.class);
    target.mContentLayout = Utils.findRequiredView(source, R.id.content_layout, "field 'mContentLayout'");
    target.mRecyclerViewWrapper = Utils.findRequiredViewAsType(source, R.id.recycler_view_wrap, "field 'mRecyclerViewWrapper'", PullToRefreshRecyclerView.class);
    target.mBottomLayout = Utils.findRequiredView(source, R.id.bottom_layout, "field 'mBottomLayout'");
    target.mCheckBox = Utils.findRequiredViewAsType(source, R.id.cbSelectAll, "field 'mCheckBox'", CheckBox.class);
    target.mLockBtn = Utils.findRequiredViewAsType(source, R.id.btnLock, "field 'mLockBtn'", Button.class);
    target.mUnLockBtn = Utils.findRequiredViewAsType(source, R.id.btnUnLock, "field 'mUnLockBtn'", Button.class);
    target.mEmptyView = Utils.findRequiredView(source, R.id.empty_layout, "field 'mEmptyView'");
    target.mEmptyTxt = Utils.findRequiredViewAsType(source, R.id.empty, "field 'mEmptyTxt'", TextView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    UnLockActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.mActionbar = null;
    target.mContentLayout = null;
    target.mRecyclerViewWrapper = null;
    target.mBottomLayout = null;
    target.mCheckBox = null;
    target.mLockBtn = null;
    target.mUnLockBtn = null;
    target.mEmptyView = null;
    target.mEmptyTxt = null;
  }
}
