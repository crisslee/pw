// Generated code from Butter Knife. Do not modify!
package com.google.zxing.activity;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.coomix.app.all.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class CaptureActivity_ViewBinding implements Unbinder {
  private CaptureActivity target;

  @UiThread
  public CaptureActivity_ViewBinding(CaptureActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public CaptureActivity_ViewBinding(CaptureActivity target, View source) {
    this.target = target;

    target.back = Utils.findRequiredViewAsType(source, R.id.btn_back, "field 'back'", ImageView.class);
    target.btnFlash = Utils.findRequiredViewAsType(source, R.id.flash, "field 'btnFlash'", ImageView.class);
    target.input = Utils.findRequiredViewAsType(source, R.id.input, "field 'input'", ImageView.class);
    target.llManual = Utils.findRequiredViewAsType(source, R.id.ll_manual, "field 'llManual'", LinearLayout.class);
    target.btnAlbum = Utils.findRequiredViewAsType(source, R.id.album, "field 'btnAlbum'", ImageView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    CaptureActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.back = null;
    target.btnFlash = null;
    target.input = null;
    target.llManual = null;
    target.btnAlbum = null;
  }
}
