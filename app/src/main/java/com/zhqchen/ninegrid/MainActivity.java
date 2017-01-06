package com.zhqchen.ninegrid;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    NineGridImageView ngvTest;

    private List<Integer> itemDatas;
    private NineGridImageView.NineGridAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ngvTest = (NineGridImageView) findViewById(R.id.ngv_test);
        initViews();
    }

    private void initViews() {
        itemDatas = new ArrayList<>(Arrays.asList(
                R.mipmap.app_btn_camera_normal,
                R.mipmap.app_btn_camera_normal,
                R.mipmap.app_btn_camera_normal,
                R.mipmap.app_btn_camera_normal,
                R.mipmap.app_btn_camera_normal,
                R.mipmap.app_btn_camera_normal,
                R.mipmap.app_btn_camera_normal,
                R.mipmap.app_btn_camera_normal,
                R.mipmap.app_btn_camera_normal
        ));
        mAdapter = new MyGridAdapter(this, itemDatas);
        ngvTest.setAdapter(mAdapter);
    }

    @Override
    protected void onDestroy() {
        ngvTest.recycle();
        super.onDestroy();
    }

    public class MyGridAdapter extends NineGridImageView.NineGridAdapter<Integer> {

        public MyGridAdapter(Context context, List<Integer> itemDatas) {
            super(context, itemDatas);
        }

        @Override
        protected View initViewHolder(View convertView) {
            if(convertView == null) {
                convertView = new ImageView(context);//自定义Layout
            }
            return convertView;
        }

        @Override
        protected void bindData(View convertView, int position) {
            ((ImageView)convertView).setImageResource(itemDatas.get(position));
        }

    }
}