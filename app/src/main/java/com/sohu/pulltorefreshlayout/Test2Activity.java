package com.sohu.pulltorefreshlayout;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.ArrayAdapter;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.sohu.pulltorefreshlayout.view.RefreshableLayout;

/**
 * Created by shaoyinghui on 2016/10/22.
 */
public class Test2Activity extends AppCompatActivity implements RefreshableLayout.IOnHeaderRefreshListener, RefreshableLayout.IOnfootRefreshListener {
    private ScrollView scrollView;
    private RefreshableLayout mRefreshView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test2);
        scrollView = (ScrollView)findViewById(R.id.scrollView);
        mRefreshView= (RefreshableLayout) findViewById(R.id.id_pull1);
        mRefreshView.setHeaderRefreshListener(this);
        mRefreshView.setOnfootRefreshListener(this);
        headerView=new TextView(this);
        headerView.setTextColor(Color.BLACK);
        headerView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14.5f);
        headerView.setText("HEADER VIEW");
        headerView.setGravity(Gravity.CENTER);
        footView=new TextView(this);
        footView.setTextColor(Color.BLACK);
        footView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14.5f);
        footView.setText("FOOT VIEW");
        footView.setGravity(Gravity.CENTER);

    }
    private ArrayAdapter<String> mAdapter;
    private TextView headerView;
    private TextView footView;


    @Override
    public void onHeaderRefresh(final RefreshableLayout view) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(Test2Activity.this, "刷新完成", Toast.LENGTH_SHORT).show();
                view.refreshComplete();
            }
        },3000);
    }

    @Override
    public void onFootRefresh(final RefreshableLayout view) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(Test2Activity.this, "刷新完成", Toast.LENGTH_SHORT).show();
                view.refreshComplete();

            }
        },3000);
    }
}
