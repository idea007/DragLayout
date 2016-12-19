package com.example.idea.draglayout;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.example.idea.draglayout.view.DragLayout;

public class HomeActivity extends AppCompatActivity {

    private DragLayout bql_basequestionlayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_home);
        initView();
    }

    private void initView() {
        bql_basequestionlayout = (DragLayout) findViewById(R.id.bql_basequestionlayout);

        View viewTop = View.inflate(HomeActivity.this, R.layout.view_test, null);
        View viewBottom = View.inflate(HomeActivity.this, R.layout.view_test_other, null);

        bql_basequestionlayout.setTopLayout(viewTop);
        bql_basequestionlayout.setBottomLayout(viewBottom);

    }
}
