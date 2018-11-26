package com.example.hx.ihanc;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.function.DoubleUnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyNumberEdit extends LinearLayout {
    private EditText textView1;
    private Double num1=0.0;
    private RelativeLayout add;
    private RelativeLayout minus;
    private double d=0.1;
    private TextView mTV;
    private DecimalFormat dFormat=new DecimalFormat("0.0");
    private TextChangedListener listner=null;
    public boolean check=false;
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
                num1=0.0;
                String number=textView1.getText().toString();
                if(!number.equals("")) num1=Double.parseDouble(number);
                if(num1<999){
                    num1+=d;
                }
                textView1.setText(dFormat.format(num1));
                listner.TextChanged();
            }
        });
        minus.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                num1=0.0;
                String number=textView1.getText().toString();
                if(!number.equals("")) num1=Double.parseDouble(number);
                if(num1>0){
                    num1-=d;
                }
                textView1.setText(dFormat.format(num1));
                listner.TextChanged();
            }
        });
        textView1.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b) {
                    textView1.setText(null);
                    check=true;
                } else{
                    Log.d("saleDialog",textView1.getText().toString());
                    if(TextUtils.isEmpty(textView1.getText())) {
                        setNum(num1);
                    }
                    check=false;
                }
            }
        });

        textView1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
               //Log.d("saleDialogAfterText",textView1.getText().toString());
                if((!TextUtils.isEmpty(textView1.getText()))&& check){
                    listner.TextChanged();
                   // check=false;
                }


            }
        });
    }
    public void setD(double D){
        this.d=D;
    }
    public double getNum(){
        double number=0.0;
        String str=textView1.getText().toString().trim();
        Pattern pattern = Pattern.compile("[^0-9]+(.[0-9]{1})?$");
        Matcher matcher = pattern.matcher((CharSequence) str);
        str=matcher.replaceAll("").trim();
        Log.d("fragment","str:"+str+"len:"+str.length());
        if(str.length()>0)number = Double.parseDouble(str);
        return number;
    }
    public void setNum(double num){
        textView1.setText(dFormat.format(num));
        num1=num;
    }
    public void setTitle(String title){
        mTV.setText(title);
    }
    public void setPrice(double price){
        textView1.setText(dFormat.format(price));
    }
    public void setPrice(String price){textView1.setText(price);}
    public void addTextChangedListener(TextChangedListener listner){
        this.listner=listner;
    }
    public  interface  TextChangedListener{
          void TextChanged(); //事件处理接口
    }
}
