package com.zhqchen.ninegrid.sample;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.zhqchen.ninegrid.NineGridView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    NineGridView ngvTest;

    LinearLayout ll_test;

    private List<Integer> itemDatas;
    private NineGridView.NineGridAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ngvTest = (NineGridView) findViewById(R.id.ngv_test);
        ll_test = (LinearLayout) findViewById(R.id.ll_test);
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

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mAdapter = new MyGridAdapter(MainActivity.this, itemDatas);
                ngvTest.setAdapter(mAdapter);
            }
        }, 2000);
    }

    @Override
    protected void onDestroy() {
        ngvTest.recycle();
        super.onDestroy();
    }

    public class MyGridAdapter extends NineGridView.NineGridAdapter<Integer> {

        public MyGridAdapter(Context context, List<Integer> itemDatas) {
            super(context, itemDatas);
        }

        @Override
        protected View initViewHolder(View convertView, ViewGroup parent) {
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
