package com.coomix.app.all.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.R;
import com.coomix.app.all.model.bean.ActCommitItem;
import com.coomix.app.all.model.bean.ActCommitItemOption;
import java.util.ArrayList;

/**
 * Created by xxx on 2017/1/1.
 */
public class PopSelectorDialog extends Dialog implements View.OnClickListener {
    private TextView textTitle;
    private TextView textSelect1, textSelect2, textSelect3;
    private ListView listViewPop;
    private int iSelect1 = -1;
    private int iSelect2 = -1;
    private int iSelect3 = -1;
    private PopItemAdapter popItemAdapter = null;
    private Animation animation = null;
    private OnLastSelectListener lastSelectListener = null;
    private ActCommitItem actCommitItem = null;

    public PopSelectorDialog(Context context, ActCommitItem actCommitItem) {
        this(context, R.style.add_dialog, actCommitItem);
    }

    public PopSelectorDialog(Context context, int theme, ActCommitItem actCommitItem) {
        super(context, theme);

        this.actCommitItem = actCommitItem;
        if (actCommitItem == null || actCommitItem.getOptions() == null) {
            return;
        }

        initViews(context);

        initData();
    }

    private void initViews(Context context) {
        View mainView = LayoutInflater.from(context).inflate(R.layout.dialog_address_selector, null);
        setContentView(mainView);

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.height = AllOnlineApp.screenHeight * 3 / 5;
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.x = 0;
        params.y = 0;
        params.windowAnimations = R.style.AnimBottom;
        params.gravity = Gravity.BOTTOM;
        getWindow().setAttributes(params);

        textTitle = (TextView) mainView.findViewById(R.id.textViewTitle);
        textSelect1 = (TextView) mainView.findViewById(R.id.textViewSelect1);
        textSelect2 = (TextView) mainView.findViewById(R.id.textViewSelect2);
        textSelect3 = (TextView) mainView.findViewById(R.id.textViewSelect3);
        listViewPop = (ListView) mainView.findViewById(R.id.listViewAddress);
        findViewById(R.id.popImageViewClose).setOnClickListener(this);
        textSelect1.setOnClickListener(this);
        textSelect2.setOnClickListener(this);
        textSelect3.setOnClickListener(this);
        listViewPop.setOnItemClickListener(onItemClickListener);
    }

    @Override
    public void show() {
        if (actCommitItem == null) {
            return;
        }
        super.show();
    }

    private void initData() {
        iSelect1 = -1;
        iSelect2 = -1;
        iSelect3 = -1;
        textSelect1.setText(getContext().getString(R.string.pls_select));
        textSelect1.setVisibility(View.VISIBLE);
        textSelect1.setSelected(true);
        textSelect2.setVisibility(View.GONE);
        textSelect3.setVisibility(View.GONE);

        popItemAdapter = new PopItemAdapter();
        listViewPop.setAdapter(popItemAdapter);

        if (actCommitItem != null && actCommitItem.getOptions() != null) {
            textTitle.setText(actCommitItem.getName());
            boolean bHasDefaultValue = false;
            if (actCommitItem.getValue() != null) {
                bHasDefaultValue = isHaveValue(actCommitItem.getValue());
            }
            if (!bHasDefaultValue && actCommitItem.getDefault_option() != null) {
                bHasDefaultValue = isHaveValue(actCommitItem.getDefault_option());
            }

            if (bHasDefaultValue) {
                ActCommitItemOption option = null;
                ArrayList<ActCommitItemOption> listOptions;
                if (actCommitItem.getLevel_num() > 0) {
                    if (actCommitItem.getOptions() != null && iSelect1 >= 0 && iSelect1 < actCommitItem.getOptions()
                        .size()) {
                        option = actCommitItem.getOptions().get(iSelect1);
                        if (option != null) {
                            textSelect1.setText(option.getName());
                            if (actCommitItem.getLevel_num() == 1) {
                                textSelect1.setSelected(true);
                                popItemAdapter.setListDatas(actCommitItem.getOptions());
                                listViewPop.setSelection(iSelect1);
                            }
                        } else {
                            return;
                        }
                    } else {
                        return;
                    }
                }
                if (actCommitItem.getLevel_num() > 1) {
                    if (option.getNext_level() != null && iSelect2 >= 0 && iSelect2 < option.getNext_level().size()) {
                        listOptions = option.getNext_level();
                        option = option.getNext_level().get(iSelect2);
                        if (option != null) {
                            textSelect2.setVisibility(View.VISIBLE);
                            textSelect2.setText(option.getName());
                            if (actCommitItem.getLevel_num() == 2) {
                                textSelect1.setSelected(false);
                                textSelect2.setSelected(true);
                                popItemAdapter.setListDatas(listOptions);
                                listViewPop.setSelection(iSelect2);
                            }
                        } else {
                            return;
                        }
                    } else {
                        return;
                    }
                }
                if (actCommitItem.getLevel_num() > 2) {
                    if (option.getNext_level() != null && iSelect3 >= 0 && iSelect3 < option.getNext_level().size()) {
                        listOptions = option.getNext_level();
                        option = option.getNext_level().get(iSelect3);
                        if (option != null) {
                            textSelect3.setVisibility(View.VISIBLE);
                            textSelect3.setText(option.getName());
                            if (actCommitItem.getLevel_num() == 3) {
                                textSelect1.setSelected(false);
                                textSelect2.setSelected(false);
                                textSelect3.setSelected(true);
                                popItemAdapter.setListDatas(listOptions);
                                listViewPop.setSelection(iSelect3);
                            }
                        } else {
                            return;
                        }
                    } else {
                        return;
                    }
                }
            } else {
                iSelect1 = -1;
                iSelect2 = -1;
                iSelect3 = -1;
                popItemAdapter.setListDatas(actCommitItem.getOptions());
            }
        }
    }

    private boolean isHaveValue(Object data) {
        try {
            ArrayList<Integer> list = (ArrayList<Integer>) data;
            if (list != null && list.size() > 0) {
                for (int i = 0; i < list.size(); i++) {
                    if (i == 0) {
                        iSelect1 = list.get(0);
                    } else if (i == 1) {
                        iSelect2 = list.get(1);
                    } else if (i == 2) {
                        iSelect3 = list.get(2);
                    }
                }
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (popItemAdapter.getItem(position) == null) {
                return;
            }
            ActCommitItemOption commitItemOption = (ActCommitItemOption) popItemAdapter.getItem(position);
            if (commitItemOption == null) {
                return;
            }

            /**
             * 通过actCommitItem.getLevel_num()判断
             *如果菜单只有一级，则直接返回；
             *如果共二级，点击第一级的时候则直接刷新二级菜单；点击二级菜单直接返回
             *如果共三级，点击第一级的时候则直接刷新二级菜单；点击二级菜单直接刷新三级菜单，点击三级直接返回
             * */

            if (textSelect1.isSelected() && actCommitItem.getLevel_num() > 1) {
                if (commitItemOption.getNext_level() == null || commitItemOption.getNext_level().size() <= 0) {
                    return;
                }
                //一级菜单
                if (iSelect1 != position) {
                    iSelect2 = -1;
                    iSelect3 = -1;
                    textSelect3.setVisibility(View.GONE);
                    textSelect2.setText(getContext().getString(R.string.pls_select));
                }
                iSelect1 = position;
                textSelect1.setText(commitItemOption.getName());
                //刷新二级菜单
                refreshUi(1, commitItemOption.getNext_level());
            } else if (textSelect2.isSelected() && actCommitItem.getLevel_num() > 2) {
                if (commitItemOption.getNext_level() == null || commitItemOption.getNext_level().size() <= 0) {
                    return;
                }
                //二级菜单
                if (iSelect2 != position) {
                    iSelect3 = -1;
                    textSelect3.setText(getContext().getString(R.string.pls_select));
                }
                iSelect2 = position;
                textSelect2.setText(commitItemOption.getName());
                //刷新三级菜单
                refreshUi(2, commitItemOption.getNext_level());
            } else {
                if (lastSelectListener != null) {
                    if (actCommitItem.getLevel_num() == 1) {
                        lastSelectListener.onLastSelectedItem(commitItemOption);
                        lastSelectListener.onLastSelectedIndex(commitItemOption.getId());
                    } else if (actCommitItem.getLevel_num() == 2) {
                        lastSelectListener.onLastSelectedItem(actCommitItem.getOptions().get(iSelect1),
                            commitItemOption);
                        lastSelectListener.onLastSelectedIndex(actCommitItem.getOptions().get(iSelect1).getId(),
                            commitItemOption.getId());
                    } else if (actCommitItem.getLevel_num() == 3) {
                        lastSelectListener.onLastSelectedItem(actCommitItem.getOptions().get(iSelect1),
                            actCommitItem.getOptions().get(iSelect1).getNext_level().get(iSelect2), commitItemOption);
                        lastSelectListener.onLastSelectedIndex(actCommitItem.getOptions().get(iSelect1).getId(),
                            actCommitItem.getOptions().get(iSelect1).getNext_level().get(iSelect2).getId(),
                            commitItemOption.getId());
                    }
                }
                dismiss();
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.popImageViewClose:
                dismiss();
                return;

            case R.id.textViewSelect1:
                if (!textSelect1.isSelected()) {
                    refreshUi(0, actCommitItem.getOptions());
                }
                break;

            case R.id.textViewSelect2:
                if (!textSelect2.isSelected()) {
                    refreshUi(1, actCommitItem.getOptions().get(iSelect1).getNext_level());
                }
                break;

            case R.id.textViewSelect3:
                if (!textSelect3.isSelected()) {
                    refreshUi(2,
                        actCommitItem.getOptions().get(iSelect1).getNext_level().get(iSelect2).getNext_level());
                }
                break;
        }
    }

    private void refreshUi(int index, ArrayList<ActCommitItemOption> listDatas) {
        if (index == 0 && actCommitItem.getLevel_num() > 0) {
            textSelect1.setVisibility(View.VISIBLE);
            textSelect1.setSelected(true);
            textSelect2.setSelected(false);
            textSelect3.setSelected(false);
        } else if (index == 1 && actCommitItem.getLevel_num() > 1) {
            textSelect2.setVisibility(View.VISIBLE);
            textSelect1.setSelected(false);
            textSelect2.setSelected(true);
            textSelect3.setSelected(false);
        } else if (index == 2 && actCommitItem.getLevel_num() > 2) {
            textSelect3.setVisibility(View.VISIBLE);
            textSelect1.setSelected(false);
            textSelect2.setSelected(false);
            textSelect3.setSelected(true);
        } else {
            return;
        }

        listViewPop.startAnimation(getAnimation());
        popItemAdapter.setListDatas(listDatas);
        popItemAdapter.notifyDataSetChanged();

        //滚动到已经选择的项目上
        if (index == 0) {
            listViewPop.setSelection(iSelect1 >= 0 ? iSelect1 : 0);
        } else if (index == 1) {
            listViewPop.setSelection(iSelect2 >= 0 ? iSelect2 : 0);
        } else if (index == 2) {
            listViewPop.setSelection(iSelect3 >= 0 ? iSelect3 : 0);
        }
    }

    private Animation getAnimation() {
        if (animation == null) {
            animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
            animation.setDuration(500);
        }
        return animation;
    }

    class PopItemAdapter extends BaseAdapter {
        private ArrayList<ActCommitItemOption> listDatas = new ArrayList<ActCommitItemOption>();

        public PopItemAdapter() {

        }

        public void setListDatas(ArrayList<ActCommitItemOption> listDatas) {
            this.listDatas = listDatas;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return listDatas == null ? 0 : listDatas.size();
        }

        @Override
        public Object getItem(int position) {
            if (listDatas == null || position < 0 || position > listDatas.size() - 1) {
                return null;
            }
            return listDatas.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.select_address_item, null);
                viewHolder.textName = (TextView) convertView.findViewById(R.id.textViewName);
                viewHolder.iamgeSelect = (ImageView) convertView.findViewById(R.id.imageViewSelected);
                convertView.setTag(R.layout.select_address_item, viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag(R.layout.select_address_item);
            }

            Object object = getItem(position);
            if (object == null) {
                return convertView;
            }
            ActCommitItemOption option = (ActCommitItemOption) object;
            if (option == null) {
                return convertView;
            }

            viewHolder.textName.setText(option.getName());
            if (textSelect1.isSelected() && iSelect1 == position) {
                viewHolder.iamgeSelect.setVisibility(View.VISIBLE);
            } else if (textSelect2.isSelected() && iSelect2 == position) {
                viewHolder.iamgeSelect.setVisibility(View.VISIBLE);
            } else if (textSelect3.isSelected() && iSelect3 == position) {
                viewHolder.iamgeSelect.setVisibility(View.VISIBLE);
            } else {
                viewHolder.iamgeSelect.setVisibility(View.INVISIBLE);
            }

            return convertView;
        }

        class ViewHolder {
            TextView textName;
            ImageView iamgeSelect;
        }
    }

    public interface OnLastSelectListener {
        public void onLastSelectedItem(ActCommitItemOption... commitItemOption);

        public void onLastSelectedIndex(int... index);
    }

    public void setOnLastSelectListener(OnLastSelectListener listener) {
        this.lastSelectListener = listener;
    }
}
