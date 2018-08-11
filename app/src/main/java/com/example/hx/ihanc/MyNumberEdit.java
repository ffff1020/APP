package com.example.hx.ihanc;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.function.DoubleUnaryOperator;

public class MyNumberEdit extends LinearLayout {
    private EditText textView1;
    private Double num1;
    private RelativeLayout add;
    private RelativeLayout minus;
    private double d=0.1;
    private TextView mTV;
    private DecimalFormat dFormat=new DecimalFormat("#.0");
    public MyNumberEdit(Context context, AttributeSet attrs){
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.my_number, this);
        add=(RelativeLayout)findViewById(R.id.iv_1);
        textView1=(EditText)findViewById(R.id.textView1);
        mTV=(TextView)findViewById(R.id.title);
        minus=(RelativeLayout)findViewById(R.id.iv_2);
        add.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                num1=Double.parseDouble(textView1.getText().toString());
                if(num1<999){
                    num1+=d;
                }
                textView1.setText(dFormat.format(num1));
            }
        });
        minus.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                num1=Double.parseDouble(textView1.getText().toString());
                if(num1>0){
                    num1-=d;
                }
                textView1.setText(dFormat.format(num1));
            }
        });
    }
    public void setD(double D){
        this.d=D;
    }
    public double getNum(){
        return Double.parseDouble(textView1.getText().toString());
    }
    public void setTitle(String title){
        mTV.setText(title);
    }
    public void setPrice(double price){
        textView1.setText(dFormat.format(price));
    }
    public void setPrice(String price){textView1.setText(price);}
}
