package com.zhy.tree.bean;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.R;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * http://blog.csdn.net/lmj623565791/article/details/40212367
 *
 * @param <T>
 * @author zhy
 */
public abstract class TreeListViewAdapter<T> extends BaseAdapter
{

    protected Context mContext;
    /**
     * 存储所有可见的Node
     */
    protected List<Node> mNodes;
    protected LayoutInflater mInflater;
    /**
     * 存储所有的Node
     */
    protected List<Node> mAllNodes;

    /**
     * 点击的回调接口
     */
    private OnTreeNodeClickListener onTreeNodeClickListener;

    private int checkedPosition = ListView.INVALID_POSITION;
    private Resources mRes;
    private int choosedNodeId;

    public interface OnTreeNodeClickListener
    {
        void onClick(Node node, int position, boolean bShow);
    }

    public void setOnTreeNodeClickListener(OnTreeNodeClickListener onTreeNodeClickListener)
    {
        this.onTreeNodeClickListener = onTreeNodeClickListener;
    }

    /**
     * @param mTree
     * @param context
     * @param datas
     * @param defaultExpandLevel 默认展开几级树
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    public TreeListViewAdapter(ListView mTree, Context context, List<T> datas, int defaultExpandLevel)
    {
        mContext = context;
        /**
         * 对所有的Node进行排序
         */
        try
        {
            mAllNodes = TreeHelper.getSortedNodes(datas, defaultExpandLevel);
        }
        catch (Exception e)
        {
            Log.d(TreeListViewAdapter.class.getCanonicalName(), e.getMessage());
        }
        /**
         * 过滤出可见的Node
         */
        mNodes = TreeHelper.filterVisibleNode(mAllNodes);
        mInflater = LayoutInflater.from(context);

        mRes = context.getResources();

        /**
         * 设置节点点击时，可以展开以及关闭；并且将ItemClick事件继续往外公布
         */
        mTree.setOnItemClickListener(new OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                // expandOrCollapse(position);

                if (onTreeNodeClickListener != null)
                {
                    onTreeNodeClickListener.onClick(mNodes.get(position), position, false);
                }
            }

        });

    }

    public void setData(List<T> datas, int defaultExpandLevel, int nodeId)
    {
        choosedNodeId = nodeId;
        /**
         * 对所有的Node进行排序
         */
        mAllNodes = getSortedNodes(datas, defaultExpandLevel);

        /**
         * 过滤出可见的Node
         */
        mNodes = TreeHelper.filterVisibleNode(mAllNodes);

        notifyDataSetChanged();
    }

    private <T> List<Node> getSortedNodes(List<T> datas, int defaultExpandLevel)
    {
        List<Node> result = new ArrayList<Node>();
        // 将用户数据转化为List<Node>
        List<Node> nodes = convetData2Node(datas);
        // 拿到根节点
        List<Node> rootNodes = getRootNodes(nodes);
        // 排序以及设置Node间关系
        for (Node node : rootNodes)
        {
            addNode(result, node, defaultExpandLevel, 1);
        }
        return result;
    }

    private <T> List<Node> convetData2Node(List<T> datas)
    {
        List<Node> nodes = new ArrayList<Node>();
        Node node = null;

        for (T t : datas)
        {
            int id = -1;
            int pId = -1;
            String label = null;
            String showlabel = null;
            Boolean haschild = null;
            Class<? extends Object> clazz = t.getClass();
            Field[] declaredFields = clazz.getDeclaredFields();
            try
            {
                for (Field f : declaredFields)
                {
                    if (f.getAnnotation(TreeNodeId.class) != null)
                    {
                        f.setAccessible(true);
                        id = f.getInt(t);
                    }
                    if (f.getAnnotation(TreeNodePid.class) != null)
                    {
                        f.setAccessible(true);
                        pId = f.getInt(t);
                    }
                    if (f.getAnnotation(TreeNodeLabel.class) != null)
                    {
                        f.setAccessible(true);
                        label = (String) f.get(t);
                    }
                    if (f.getAnnotation(TreeNodeShowLabel.class) != null)
                    {
                        f.setAccessible(true);
                        showlabel = (String) f.get(t);
                    }
                    if (f.getAnnotation(TreeNodeHaschild.class) != null)
                    {
                        f.setAccessible(true);
                        haschild = (boolean) f.get(t);
                    }
                    if (id != -1 && pId != -1 && label != null && showlabel != null && haschild != null)
                    {
                        break;
                    }
                }
            }
            catch (Exception e)
            {
                Log.e(TreeListViewAdapter.class.getCanonicalName(), e.getMessage());
            }
            node = new Node(id, pId, label, showlabel, haschild);
            nodes.add(node);
        }

        /**
         * 设置Node间，父子关系;让每两个节点都比较一次，即可设置其中的关系
         */
        for (int i = 0; i < nodes.size(); i++)
        {
            Node n = nodes.get(i);
            for (int j = i + 1; j < nodes.size(); j++)
            {
                Node m = nodes.get(j);
                if (m.getpId() == n.getId())
                {
                    n.getChildren().add(m);
                    m.setParent(n);
                }
                else if (m.getId() == n.getpId())
                {
                    m.getChildren().add(n);
                    n.setParent(m);
                }
            }
        }

        // 设置图片
        for (Node n : nodes)
        {
            setNodeIcon(n);
        }
        return nodes;
    }

    private List<Node> getRootNodes(List<Node> nodes)
    {
        List<Node> root = new ArrayList<Node>();
        for (Node node : nodes)
        {
            if (node.isRoot())
            {
                root.add(node);
            }
        }
        return root;
    }

    /**
     * 把一个节点上的所有的内容都挂上去
     */
    private void addNode(List<Node> nodes, Node node, int defaultExpandLeval, int currentLevel)
    {
        nodes.add(node);
        if (defaultExpandLeval >= currentLevel)
        {
            for (Node mNode : mAllNodes)
            {
                if (mNode.getId() == node.getId())
                {
                    if (mNode.getId() == choosedNodeId)
                    {
                        node.setExpand(true);
                    }
                    else
                    {
                        node.setExpand(mNode.isExpand());
                    }
                }
            }
        }

        if (node.isLeaf())
        {
            return;
        }
        for (int i = 0; i < node.getChildren().size(); i++)
        {
            addNode(nodes, node.getChildren().get(i), defaultExpandLeval, currentLevel + 1);
        }
    }

    /**
     * 设置节点的图标
     *
     * @param node
     */
    private void setNodeIcon(Node node)
    {
        if (node.getChildren().size() > 0 && node.isExpand())
        {
            node.setIcon(R.drawable.tree_ex);
        }
        else if (node.getChildren().size() > 0 && !node.isExpand())
        {
            node.setIcon(R.drawable.tree_ec);
        }
    }

    public void setNodeChecked(int nodeId)
    {
        mAllNodes = getAllUnCheckedNodes(mAllNodes);
        mAllNodes = setCheckedNode(mAllNodes, nodeId);
        notifyDataSetChanged();// 刷新视图
    }

    public static List<Node> getAllUnCheckedNodes(List<Node> nodes)
    {
        List<Node> result = new ArrayList<Node>();

        for (Node node : nodes)
        {
            node.setChecked(false);
            result.add(node);
        }
        return result;
    }

    public static List<Node> setCheckedNode(List<Node> nodes, int nodeId)
    {
        List<Node> result = new ArrayList<Node>();

        for (Node node : nodes)
        {
            if (nodeId == node.getId())
            {
                node.setChecked(true);
            }
            result.add(node);
        }
        return result;
    }

    /**
     * 相应ListView的点击事件 展开或关闭某节点
     *
     * @param position
     */
    public void expandOrCollapse(int position)
    {
        Node n = mNodes.get(position);
        if (n != null)// 排除传入参数错误异常
        {
            if (!n.isLeaf())
            {
                n.setExpand(!n.isExpand());
                mNodes = TreeHelper.filterVisibleNode(mAllNodes);
                notifyDataSetChanged();// 刷新视图
            }
            else
            {
                //isLeaf为true,表示下级数据还没拿到，所以走正常的点击账号流程
                if (onTreeNodeClickListener != null)
                {
                    onTreeNodeClickListener.onClick(n, position, true);
                }
            }
        }
    }

    /**
     *
     */
    public int getItemSelectedPosition(int nodeId)
    {
        if (mNodes != null)
        {
            for (int i = 0; i < mNodes.size(); i++)
            {
                Node n = mNodes.get(i);
                if (n != null && n.getId() == nodeId)
                {
                    return i;
                }
            }
        }
        return -1;
    }

    @Override
    public int getCount()
    {
        return mNodes.size();
    }

    @Override
    public Object getItem(int position)
    {
        return mNodes.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent)
    {
        Node node = mNodes.get(position);

        ViewHolder viewHolder = null;
        if (convertView == null)
        {
            convertView = mInflater.inflate(R.layout.list_treeview_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.icon = (ImageView) convertView.findViewById(R.id.id_treenode_icon);
            viewHolder.label = (TextView) convertView.findViewById(R.id.id_treenode_label);
            viewHolder.tvNumber = (TextView) convertView.findViewById(R.id.tvNumber);
            convertView.setTag(viewHolder);

        }
        else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (node.isHaschild())
        {
            viewHolder.icon.setVisibility(View.VISIBLE);
            if (node.getIcon() != -1)
            {
                viewHolder.icon.setImageResource(node.getIcon());
            }

        }
        else
        {
            viewHolder.icon.setImageResource(R.drawable.ic_arrow_node);
            //viewHolder.icon.setVisibility(View.INVISIBLE);

        }

        viewHolder.label.setText(node.getShowName());
        //        viewHolder.tvNumber.setText(String.valueOf(node.getChildrenSize()));
        viewHolder.icon.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                expandOrCollapse(position);
            }
        });

        // 设置内边距
        float scale = AllOnlineApp.mApp.getResources().getDisplayMetrics().density;
        int padding = (int) (node.getLevel() * 16 * scale + 0.5);
        convertView.setPadding(padding, 0, 0, 0);
        if (node.isChecked())
        {
            convertView.setBackgroundColor(mRes.getColor(R.color.card_selected));
        }
        else
        {
            convertView.setBackgroundColor(mRes.getColor(R.color.white));
        }
        return convertView;
    }

    private final class ViewHolder
    {
        ImageView icon;
        TextView label;
        TextView tvNumber;
    }

    public abstract View getConvertView(Node node, int position, View convertView, ViewGroup parent);

}
