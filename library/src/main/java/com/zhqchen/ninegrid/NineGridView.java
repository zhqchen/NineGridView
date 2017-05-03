package com.zhqchen.ninegrid;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;

import java.util.LinkedList;
import java.util.List;

/**
 * 九宫格View, 主要用来展示图片，也可用于自定义的itemView
 * Created by zhqchen on 2017-01-05.
 */
public class NineGridView extends LinearLayout {
    private final int MAX_ITEM_COUNT = 9;
    private final int COLUMN_DEFAULT = 3;
    private final int SPACING_H_DEFAULT = 10;
    private final int SPACING_V_DEFAULT = 10;

    private int mItemWidth;//每个item的宽度
    private int maxItems;//item的最大数量
    private int mColumns;//列数
    private int hSpacing;//水平间距
    private int vSpacing;//垂直间距

    private DataSetObserver observer;
    private NineGridAdapter mAdapter;//由上次使用者传入，可自定义item

    public NineGridView(Context context) {
        this(context, null);
    }

    public NineGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews(context, attrs);
    }

    private void initViews(Context context, AttributeSet attrs) {
        if(isInEditMode()) {
            return;
        }
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.NineGridIV);
        maxItems = array.getInt(R.styleable.NineGridIV_maxItems, MAX_ITEM_COUNT);
        mColumns = array.getInt(R.styleable.NineGridIV_numColumns, COLUMN_DEFAULT);
        vSpacing = array.getDimensionPixelSize(R.styleable.NineGridIV_verticalSpacing, SPACING_V_DEFAULT);
        hSpacing = array.getDimensionPixelSize(R.styleable.NineGridIV_horizontalSpacing, SPACING_H_DEFAULT);
        array.recycle();
        setGravity(Gravity.CENTER_VERTICAL);
        setOrientation(LinearLayout.VERTICAL);
        setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        initItemWidth();

        //在mAdapter的notifyDataSetChanged后，观察者也会运行onChanged
        observer = new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                updateContentViews();
            }
        };
    }

    private void initItemWidth() {
        final ViewTreeObserver viewTreeObserver = getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (mItemWidth <= 0) {//计算item的宽度
                    mItemWidth = (getMeasuredWidth() - (mColumns - 1) * hSpacing - getPaddingLeft() - getPaddingRight()) / mColumns;
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
                updateContentViews();
            }
        });
    }

    public void setAdapter(NineGridAdapter adapter) {
        if(adapter == null) {
            return;
        }
        if(this.mAdapter == adapter) {
            return;
        }
        if(this.mAdapter != null) {
            this.mAdapter.unregisterDataSetObserver(observer);
        }
        this.mAdapter = adapter;
        this.mAdapter.registerDataSetObserver(observer);//注册观察者
        updateContentViews();
    }

    public NineGridAdapter getAdapter() {
        return this.mAdapter;
    }

    public void setMaxItemCount(int itemCount) {
        this.maxItems = itemCount;
    }

    public void setColumns(int columns) {
        this.mColumns = columns;
    }

    public void setVerticalSpacing(int spacing) {
        this.vSpacing = spacing;
    }

    public void setHorizontalSpacing(int spacing) {
        this.hSpacing = spacing;
    }

    public void recycle() {
        if(mAdapter != null) {
            mAdapter.unregisterDataSetObserver(observer);
            mAdapter = null;
        }
        removeAllViews();
    }

    private void updateContentViews() {
        if(mColumns <= 0 || maxItems <= 0) {
            throw new IllegalStateException("mColumns or maxItems can not <= 0");//配置异常
        }
        if(mAdapter == null || mItemWidth <= 0) {
            return;
        }
        int count = mAdapter.getCount() > maxItems ? maxItems : mAdapter.getCount();//限制最大展示数量
        int realRows = count / mColumns + (count % mColumns == 0 ? 0 : 1);
        LinkedList<LinearLayout> rowLayouts = new LinkedList<>();//行的父布局
        LinkedList<View> itemLayouts = new LinkedList<>();//item的父布局
        for(int i = 0; i < getChildCount(); i++) {
            LinearLayout rowLayout = (LinearLayout) getChildAt(i);
            rowLayouts.add(rowLayout);
            for(int j = 0; j < rowLayout.getChildCount(); j++) {
                itemLayouts.add(rowLayout.getChildAt(j));
            }
            rowLayout.removeAllViews();//清除一行里的itemViews
        }
        removeAllViews();//清除所有行父布局

        int row = -1;
        LinearLayout rowTempLayout = null;
        ViewGroup.LayoutParams params;
        LayoutParams itemParams;
        for(int position = 0; position < count; position++) {
            View itemView = mAdapter.initViewHolder(itemLayouts.poll(), rowLayouts.poll());//创建或复用itemView
            params = itemView.getLayoutParams();
            if(params  != null && params instanceof LayoutParams) {
                itemParams = (LayoutParams) params;
                itemParams.width = mItemWidth;
                itemParams.height = mItemWidth;
            } else {
                itemParams = new LayoutParams(mItemWidth, mItemWidth);
            }
            itemView.setLayoutParams(itemParams);//重新设置item的LayoutParam

            if(position % mColumns == 0) {//每一行的起点
                row++;
                itemParams.leftMargin = 0;
                rowTempLayout = generateRowLayout(rowLayouts.poll());
                addView(rowTempLayout);
            } else {
                if(rowTempLayout == null) {
                    rowTempLayout = generateRowLayout(rowLayouts.poll());//实际不会走到这里
                }
                itemParams.leftMargin = hSpacing;
            }
            itemParams.topMargin = row == 0 ? 0 : vSpacing / 2;//第一行不设置topMargin
            itemParams.bottomMargin = row == realRows - 1 ? 0 : vSpacing /2;//最后一行不设置bottomMargin
            rowTempLayout.addView(itemView);
            mAdapter.bindData(itemView, position);//在设置好params之后，绑定数据
        }
        rowLayouts.clear();
        itemLayouts.clear();
    }

    private LinearLayout generateRowLayout(LinearLayout linearLayout) {
        if(linearLayout == null) {
            linearLayout = new LinearLayout(getContext());
        }
        linearLayout.setGravity(Gravity.CENTER_VERTICAL);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        return linearLayout;
    }

    /**
     * 测试NineGridImageView的适配器
     * Created by pa_zhiqiang on 2017-01-05.
     */
    public static abstract class NineGridAdapter<T> extends BaseAdapter {

        protected Context context;
        protected List<T> itemDatas;

        public NineGridAdapter(Context context, List<T> itemDatas) {
            this.context = context;
            this.itemDatas = itemDatas;
        }

        @Override
        public int getCount() {
            return itemDatas.size();
        }

        @Override
        public T getItem(int position) {
            return itemDatas.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        //子类不用重写这个方法，由initViewHolder代替
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            return initViewHolder(convertView, parent);
        }

        /**
         * 创建/复用itemView
         * @param convertView
         * @return
         */
        protected abstract View initViewHolder(View convertView, ViewGroup parent);

        /**
         * 绑定item的数据
         * @param convertView
         * @param position
         */
        protected abstract void bindData(View convertView, int position);

    }

}