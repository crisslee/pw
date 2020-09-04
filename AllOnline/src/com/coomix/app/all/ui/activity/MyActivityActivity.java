package com.coomix.app.all.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.CharacterStyle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.bumptech.GlideApp;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.Constant;
import com.coomix.app.all.GlobalParam;
import com.coomix.app.all.R;
import com.coomix.app.all.data.BaseSubscriber;
import com.coomix.app.all.data.DataEngine;
import com.coomix.app.all.data.ExceptionHandle;
import com.coomix.app.all.data.RxUtils;
import com.coomix.app.all.model.bean.ActPrice;
import com.coomix.app.all.model.bean.MyActivities;
import com.coomix.app.all.model.bean.MyActivity;
import com.coomix.app.all.model.bean.Readpos;
import com.coomix.app.all.model.response.RespMyActList;
import com.coomix.app.all.ui.base.BaseActivity;
import com.coomix.app.all.util.CommunityDateUtil;
import com.coomix.app.all.util.CommunityUtil;
import com.coomix.app.all.util.FooterViewUtils;
import com.coomix.app.all.util.PullToRefreshUtils;
import com.coomix.app.all.util.ViewUtil;
import com.coomix.app.all.widget.MyActionbar;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import io.reactivex.disposables.Disposable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by think<br/>
 * 我的活动列表
 *
 * @since 2017/1/15.
 */
public class MyActivityActivity extends BaseActivity implements ListItemCallback {
    private static final String TAG = MyActivityActivity.class.getSimpleName();

    private PullToRefreshListView mPtrListView;

    private Readpos readpos;

    private List<MyActivity> myActList = new ArrayList<>();

    private static final int ACTIVITY_NUM_PER_PAGE = 15;
    private MyActivityAdapter adapter;
    private Handler mh = new Handler();
    protected FooterViewUtils footerViewUtils;
    private View noMoreFooterView;

    public static boolean shouldRefresh = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_activity);

        initViews();

        getMyActList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (shouldRefresh) {
            shouldRefresh = false;
            getMyActList();
        }
    }

    private void initViews() {
        MyActionbar actionbar = (MyActionbar) findViewById(R.id.mineActionbar);
        actionbar.initActionbar(true, R.string.my_activity, 0, 0);

        // list rootView
        mPtrListView = (PullToRefreshListView) findViewById(R.id.my_activity_list);
        adapter = new MyActivityAdapter(this, myActList, this);
        mPtrListView.setAdapter(adapter);
        footerViewUtils = new FooterViewUtils(this, mPtrListView.getRefreshableView());
        initActList();
    }

    private void initActList() {
        mPtrListView.setMode(Mode.PULL_FROM_END);
        ViewUtil.setPtrStateText(mPtrListView);
        mPtrListView.getRefreshableView().setDivider(new ColorDrawable(getResources().getColor(R.color.color_bg)));
        mPtrListView.getRefreshableView().setDividerHeight(getResources().getDimensionPixelSize(R.dimen.space_2x));
        mPtrListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                getMyActList();
            }
        });

        mPtrListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //跳往活动信息
                position -= 1;
                MyActivity activity = (MyActivity) adapter.getItem(position);
                Intent intent = new Intent(MyActivityActivity.this, MyOrderInfoActivity.class);
                intent.putExtra(Constant.NATIVE_ACTIVITY_ID, activity.getId());
                intent.putExtra(MyOrderInfoActivity.ORDER_DETAIL_OR_PAYING, MyOrderInfoActivity.ORDER_DETAIL);
                intent.putExtra(MyOrderInfoActivity.ORDER_ID, activity.getPay().getOrder_id());
                startActivity(intent);
            }
        });
    }

    private void getMyActList() {
        if (readpos == null) {
            readpos = new Readpos();
        }
        showLoading(getString(R.string.please_wait));
        Disposable disposable = DataEngine.getCommunityApi().getMyActivityList(
                GlobalParam.getInstance().getCommonParas(),
                AllOnlineApp.sToken.ticket, readpos.getPointer(), readpos.getId(), ACTIVITY_NUM_PER_PAGE)
                .compose(RxUtils.toMain())
                .compose(RxUtils.businessTransformer())
                .subscribeWith(new BaseSubscriber<RespMyActList>() {
                    @Override
                    public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                        showErr(e.getErrCodeMessage());
                        hideLoading();
                        mPtrListView.onRefreshComplete();
                        if (adapter == null || adapter.getCount() == 0) {
                            // 列表，无内容，且网络访问出错时
                            footerViewUtils.showEmptyView(R.drawable.icon_hint_no_net, R.string.hint_no_net, onClickListener, 10000);
                        }
                    }

                    @Override
                    public void onNext(RespMyActList respMyActList) {
                        hideLoading();
                        if (footerViewUtils != null && (10000 == footerViewUtils.getTag() || myActList.size() > 0)) {
                            footerViewUtils.dismiss();
                        }
                        mPtrListView.onRefreshComplete();
                        if (respMyActList != null) {
                            parseMyActivityList(respMyActList.getData());
                        } else {
                            if (adapter == null || adapter.getCount() == 0) {
                                // 列表，无内容
                                footerViewUtils.showEmptyView(R.drawable.icon_hint_no_net, R.string.hint_no_content, onClickListener, 10000);
                            }
                        }
                    }
                });
        subscribeRx(disposable);
    }

    private void parseMyActivityList(MyActivities data) {
        readpos = data.getReadpos();
        //removeNoMoreDataFootView();//没有下拉刷新
        ArrayList<MyActivity> listActivities = data.getActivity();
        for (MyActivity myActivity : listActivities) {
            if (myActivity.getGeneral_status() != MyActivity.ENDED) {//已结束活动不显示
                myActList.add(myActivity);
            }
        }
        //myActList.addAll(listActivities);
        adapter.refresh(myActList);

        handleActsCountChanged();

        if (listActivities.size() <= 0) {
            PullToRefreshUtils.disablePullUp(mPtrListView);
            if (adapter.getCount() > 0) {
                mPtrListView.getRefreshableView().addFooterView(getNoMoreDataFootView());
            }
        }
    }

    private View getNoMoreDataFootView() {
        if (noMoreFooterView == null) {
            noMoreFooterView = FooterViewUtils.getNoMoreDataFooterView(this);
        }
        return noMoreFooterView;
    }

    private void removeNoMoreDataFootView() {
        if (noMoreFooterView != null) {
            mPtrListView.getRefreshableView().removeFooterView(noMoreFooterView);
            noMoreFooterView = null;
        }
    }

    public void handleActsCountChanged() {
        try {
            if (myActList.size() <= 0) {
                footerViewUtils.showEmptyView(R.drawable.hint_noconetent, R.string.hint_no_content, onClickListener);
            } else {
                footerViewUtils.dismiss();
                if (noMoreFooterView == null) {
                    mPtrListView.setMode(Mode.PULL_FROM_END);
                }
            }
        } catch (Exception e) {
            //LogUtil.e(":" + e.getMessage());
            e.printStackTrace();
        }
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            getMyActList();
        }
    };

    @Override
    public void onInnerClick(View v) {
        //跳往确认支付
        int position = (int) v.getTag();
        MyActivity activity = (MyActivity) adapter.getItem(position);
        Intent intent = new Intent(MyActivityActivity.this, MyOrderInfoActivity.class);
        intent.putExtra(Constant.NATIVE_ACTIVITY_ID, activity.getId());
        intent.putExtra(MyOrderInfoActivity.ORDER_DETAIL_OR_PAYING, MyOrderInfoActivity.ORDER_PAYING);
        intent.putExtra(MyOrderInfoActivity.ORDER_ID, activity.getPay().getOrder_id());
        startActivity(intent);
    }

    private class MyActivityAdapter extends BaseAdapter implements View.OnClickListener {
        private Context mContext;

        private List<MyActivity> actList;

        private ListItemCallback mCallback;

        public MyActivityAdapter(Context context, List<MyActivity> actList, ListItemCallback callback) {
            this.mContext = context;
            this.actList = actList;
            this.mCallback = callback;
        }

        public void refresh(List<MyActivity> list) {
            this.actList = list;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return actList.size();
        }

        @Override
        public Object getItem(int position) {
            return actList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            MyActivityHolder holder = null;
            if (convertView == null) {
                holder = new MyActivityHolder();
                convertView = LayoutInflater.from(MyActivityActivity.this).inflate(R.layout.my_activity_item, null);
                holder.title = (TextView) convertView.findViewById(R.id.activity_title);
                holder.time = (TextView) convertView.findViewById(R.id.activity_time);
                holder.location = (TextView) convertView.findViewById(R.id.activity_location);
                holder.price = (TextView) convertView.findViewById(R.id.activity_price);
                holder.bookTime = (TextView) convertView.findViewById(R.id.activity_book_time);
                holder.generalStatus = (TextView) convertView.findViewById(R.id.general_status);
                holder.gotoPay = (TextView) convertView.findViewById(R.id.goto_pay);
                holder.llGotoPay = (LinearLayout) convertView.findViewById(R.id.ll_goto_pay);
                holder.pic = (ImageView) convertView.findViewById(R.id.activity_pic);

                convertView.setTag(R.layout.my_activity_item, holder);
            } else {
                holder = (MyActivityHolder) convertView.getTag(R.layout.my_activity_item);
            }

            setData(holder, position);

            return convertView;
        }

        private class MyActivityHolder {
            TextView title;
            TextView time;
            TextView location;
            TextView price;
            TextView bookTime;
            TextView generalStatus;
            TextView gotoPay;
            ImageView pic;

            LinearLayout llGotoPay;
        }

        private void setData(final MyActivityHolder holder, int position) {
            MyActivity act = null;
            if (position < actList.size()) {
                act = actList.get(position);
            }
            if (holder == null || act == null) {
                return;
            }

            holder.title.setText(act.getTitle());
            long beginTime = act.getBegtime();
            long endTime = act.getEndtime();
            holder.time.setText(CommunityDateUtil.formatActTime(mContext, beginTime, endTime, true));
            holder.bookTime.setText(
                    CommunityDateUtil.formatTime(act.getPay().getPay_order_create_time() * 1000, "yyyy.MM.dd HH:mm"));
            holder.location.setText(act.getLocation());
            //设置价格
            long orderId = act.getPay().getOrder_id();
            holder.price.setVisibility(View.GONE);
            if (orderId > 0 && act.getPrice() != null) {
                holder.price.setVisibility(View.VISIBLE);
                int type = act.getPrice().getType();
                if (type == ActPrice.PRICE_OFFLINE) {
                    holder.price.setText(R.string.pay_offline);
                    holder.price.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                            getResources().getDimensionPixelSize(R.dimen.text_xl));
                } else if (type == ActPrice.PRICE_FASTEN) {
                    int orderPrice = act.getPay().getTotal_fee();
                    if (orderPrice > 0) {
                        holder.price.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                                getResources().getDimensionPixelSize(R.dimen.textsize_xxsmall));
                        String rmbPrice = CommunityUtil.getMoneyStrByIntDecimal(MyActivityActivity.this, orderPrice, 2);
                        SpannableString str = new SpannableString(rmbPrice);
                        CharacterStyle size = new AbsoluteSizeSpan(getResources().getDimensionPixelSize(R.dimen.text_xl));
                        int start = rmbPrice.indexOf(getResources().getString(R.string.money_unit));
                        int end = rmbPrice.indexOf(".");
                        if (start >= 0 && end > 0 && end >= start) {
                            str.setSpan(size, start + 1, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                        holder.price.setText(str);
                    } else {
                        holder.price.setText(R.string.free);
                        holder.price.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                                getResources().getDimensionPixelSize(R.dimen.text_xl));
                    }
                } else if (type == ActPrice.PRICE_FREE) {
                    holder.price.setText(R.string.free);
                    holder.price.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                            getResources().getDimensionPixelSize(R.dimen.text_xl));
                }
            }
            //支付按钮
            holder.llGotoPay.setVisibility(View.GONE);
            int genStatus = act.getGeneral_status();
            switch (genStatus) {
                case MyActivity.PAY_WAITTING:
                    //待支付
                    if (orderId > 0) {
                        holder.llGotoPay.setVisibility(View.VISIBLE);
                        holder.llGotoPay.setOnClickListener(this);
                        holder.llGotoPay.setTag(position);
                    }
                    holder.generalStatus.setText(R.string.activity_not_paid);
                    holder.generalStatus.setTextColor(getResources().getColor(R.color.orange_pay_status));
                    holder.price.setTextColor(getResources().getColor(R.color.orange_pay_status));
                    break;
                case MyActivity.PAYED:
                    //进行中
                    holder.llGotoPay.setVisibility(View.GONE);
                    holder.generalStatus.setText(R.string.activity_on_going);
                    holder.generalStatus.setTextColor(getResources().getColor(R.color.color_main));
                    holder.price.setTextColor(getResources().getColor(R.color.orange_pay_status));
                    break;
                case MyActivity.ENDED:
                    //已结束
                    holder.llGotoPay.setVisibility(View.GONE);
                    holder.generalStatus.setText(R.string.activity_finished);
                    holder.generalStatus.setTextColor(getResources().getColor(R.color.dark_gray));
                    holder.price.setTextColor(getResources().getColor(R.color.dark_gray));
                    break;
                case MyActivity.CANCELED:
                    //已取消
                    holder.llGotoPay.setVisibility(View.GONE);
                    holder.generalStatus.setText(R.string.activity_cancelled);
                    holder.generalStatus.setTextColor(getResources().getColor(R.color.dark_gray));
                    holder.price.setTextColor(getResources().getColor(R.color.dark_gray));
                    break;
            }

            GlideApp.with(MyActivityActivity.this).load(act.getPic()).error(R.drawable.image_default_error).placeholder(
                    R.drawable.image_default).centerCrop().into(new SimpleTarget<Drawable>() {
                @Override
                public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                    holder.pic.setImageDrawable(resource);

                }

            });
        }

        @Override
        public void onClick(View v) {
            mCallback.onInnerClick(v);
        }
    }

    @Override
    public void finish() {
        setResult(RESULT_OK);
        super.finish();
    }
}
