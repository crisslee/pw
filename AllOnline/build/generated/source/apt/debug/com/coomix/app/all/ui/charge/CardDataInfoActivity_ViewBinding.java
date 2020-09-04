// Generated code from Butter Knife. Do not modify!
package com.coomix.app.all.ui.charge;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.coomix.app.all.R;
import com.coomix.app.all.widget.MyActionbar;
import java.lang.IllegalStateException;
import java.lang.Override;

public class CardDataInfoActivity_ViewBinding implements Unbinder {
  private CardDataInfoActivity target;

  @UiThread
  public CardDataInfoActivity_ViewBinding(CardDataInfoActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public CardDataInfoActivity_ViewBinding(CardDataInfoActivity target, View source) {
    this.target = target;

    target.topbar = Utils.findRequiredViewAsType(source, R.id.topbar, "field 'topbar'", MyActionbar.class);
    target.cardNo = Utils.findRequiredViewAsType(source, R.id.cardNo, "field 'cardNo'", TextView.class);
    target.cardIsp = Utils.findRequiredViewAsType(source, R.id.cardIsp, "field 'cardIsp'", TextView.class);
    target.cardIccid = Utils.findRequiredViewAsType(source, R.id.cardIccid, "field 'cardIccid'", TextView.class);
    target.cardExp = Utils.findRequiredViewAsType(source, R.id.cardExp, "field 'cardExp'", TextView.class);
    target.dataTotal = Utils.findRequiredViewAsType(source, R.id.dataTotal, "field 'dataTotal'", TextView.class);
    target.dataLeft = Utils.findRequiredViewAsType(source, R.id.dataLeft, "field 'dataLeft'", TextView.class);
    target.dataUsed = Utils.findRequiredViewAsType(source, R.id.dataUsed, "field 'dataUsed'", TextView.class);
    target.cardCharge = Utils.findRequiredViewAsType(source, R.id.cardCharge, "field 'cardCharge'", TextView.class);
    target.dataCharge = Utils.findRequiredViewAsType(source, R.id.dataCharge, "field 'dataCharge'", TextView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    CardDataInfoActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.topbar = null;
    target.cardNo = null;
    target.cardIsp = null;
    target.cardIccid = null;
    target.cardExp = null;
    target.dataTotal = null;
    target.dataLeft = null;
    target.dataUsed = null;
    target.cardCharge = null;
    target.dataCharge = null;
  }
}
