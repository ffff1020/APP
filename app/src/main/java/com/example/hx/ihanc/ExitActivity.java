package com.example.hx.ihanc;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class ExitActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exit);
        Button exit=(Button)findViewById(R.id.exitButton);
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ExitActivity.this);
                builder.setTitle("真的要退出么？");
                builder.setPositiveButton("退出", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        IhancHttpClient.setAuth("");
                        LoginActivity.mPrefEditor.putString("name","");
                        LoginActivity.mPrefEditor.putString("token","");
                        LoginActivity.mPrefEditor.commit();
                        ActivityCollector.finishAll();
                       // System.exit(0);
                        //Toast.makeText(ExitActivity.this, "positive: " + which, Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                         dialog.dismiss();
                        //Toast.makeText(ExitActivity.this, "negative: " + which, Toast.LENGTH_SHORT).show();
                    }
                });
                builder.create();
                builder.show();
            }
        });
    }
}
