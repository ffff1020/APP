package com.example.hx.ihanc;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class BitmapUtil {
    private final static int WIDTH = 576;
    private final static float SMALL_TEXT = 30;
    private final static float LARGE_TEXT = 40;
    private final static int START_RIGHT = WIDTH;
    private final static int START_LEFT = 0;
    private final static int START_CENTER = WIDTH / 2;

    /**
     * 特殊需求：
     */
    public final static int IS_LARGE = 10;
    public final static int IS_SMALL = 11;
    public final static int IS_RIGHT = 100;
    public final static int IS_LEFT = 101;
    public final static int IS_CENTER = 102;


    private static float x = START_LEFT, y;
    private final static int COL0=12;
    private final static int COL1=16;

    private final static String SPLIT=" ";
    private final static int COL5=79;
    public static final String PRINT_LINE = "----------------------------------------------------";
    /**
     * 生成图片
     */
    public static Bitmap StringListtoBitmap(Context context, ArrayList<StringBitmapParameter> AllString) {
        if (AllString.size() <= 0) return null;
        ArrayList<StringBitmapParameter> mBreakString = new ArrayList<>();

        Paint paint = new Paint();
        paint.setAntiAlias(false);
        paint.setTextSize(SMALL_TEXT);

        Typeface typeface = Typeface.createFromAsset(context.getAssets(), "fonts/msyh.ttf");
        Typeface font = Typeface.create(typeface, Typeface.NORMAL);
        paint.setTypeface(font);

        for (StringBitmapParameter mParameter : AllString) {
            int ALineLength = paint.breakText(mParameter.getText(), true, WIDTH, null);//检测一行多少字
            int lenght = mParameter.getText().length();
            if (ALineLength < lenght) {

                int num = lenght / ALineLength;
                String ALineString = new String();
                String RemainString = new String();

                for (int j = 0; j < num; j++) {
                    ALineString = mParameter.getText().substring(j * ALineLength, (j + 1) * ALineLength);
                    mBreakString.add(new StringBitmapParameter(ALineString, mParameter.getIsRightOrLeft(), mParameter.getIsSmallOrLarge()));
                }

                RemainString = mParameter.getText().substring(num * ALineLength, mParameter.getText().length());
                mBreakString.add(new StringBitmapParameter(RemainString, mParameter.getIsRightOrLeft(), mParameter.getIsSmallOrLarge()));
            } else {
                mBreakString.add(mParameter);
            }
        }


        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        int FontHeight = (int) Math.abs(fontMetrics.leading) + (int) Math.abs(fontMetrics.ascent) + (int) Math.abs(fontMetrics.descent)+4;
        y = (int) Math.abs(fontMetrics.leading) + (int) Math.abs(fontMetrics.ascent);

        int bNum = 0;
        for (StringBitmapParameter mParameter : mBreakString) {
            String bStr = mParameter.getText();
            if (bStr.isEmpty() | bStr.contains("\n") | mParameter.getIsSmallOrLarge() == IS_LARGE)
                bNum++;
        }
        Bitmap bitmap = Bitmap.createBitmap(WIDTH, FontHeight * (mBreakString.size() + bNum), Bitmap.Config.RGB_565);

        for (int i = 0; i < bitmap.getWidth(); i++) {
            for (int j = 0; j < bitmap.getHeight(); j++) {
                bitmap.setPixel(i, j, Color.WHITE);
            }
        }

        Canvas canvas = new Canvas(bitmap);

        for (StringBitmapParameter mParameter : mBreakString) {

            String str = mParameter.getText();

            if (mParameter.getIsSmallOrLarge() == IS_SMALL) {
                paint.setTextSize(SMALL_TEXT);

            } else if (mParameter.getIsSmallOrLarge() == IS_LARGE) {
                paint.setTextSize(LARGE_TEXT);
            }

            if (mParameter.getIsRightOrLeft() == IS_RIGHT) {
                x = WIDTH - paint.measureText(str);
            } else if (mParameter.getIsRightOrLeft() == IS_LEFT) {
                x = START_LEFT;
            } else if (mParameter.getIsRightOrLeft() == IS_CENTER) {
                x = (WIDTH - paint.measureText(str)) / 2.0f;
            }

            if (str.isEmpty() | str.contains("\n") | mParameter.getIsSmallOrLarge() == IS_LARGE) {
                canvas.drawText(str, x, y + FontHeight / 2, paint);
                y = y + FontHeight;
            } else {
                canvas.drawText(str, x, y, paint);
            }
            y = y + FontHeight;
        }
        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();
        return bitmap;
    }

    /**
     * 合并图片
     */
    public static Bitmap addBitmapInHead(Bitmap first, Bitmap second) {
        int width = Math.max(first.getWidth(), second.getWidth());
        int startWidth = (width - first.getWidth()) / 2;
        int height = first.getHeight() + second.getHeight();
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

        for (int i = 0; i < result.getWidth(); i++) {
            for (int j = 0; j < result.getHeight(); j++) {
                result.setPixel(i, j, Color.WHITE);
            }
        }
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(first, startWidth, 0, null);
        canvas.drawBitmap(second, 0, first.getHeight(), null);
        return result;
    }

    /***
     * 使用两个方法的原因是：
     * logo标志需要居中显示，如果直接使用同一个方法是可以显示的，但是不会居中
     */
    public static Bitmap addBitmapInFoot(Bitmap bitmap, Bitmap image) {
        int width = Math.max(bitmap.getWidth(), image.getWidth());
        int startWidth = (width - image.getWidth()) / 2;
        int height = bitmap.getHeight() + image.getHeight();
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

        for (int i = 0; i < result.getWidth(); i++) {
            for (int j = 0; j < result.getHeight(); j++) {
                result.setPixel(i, j, Color.WHITE);
            }
        }
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(bitmap, 0, 0, null);
        canvas.drawBitmap(image, startWidth, bitmap.getHeight(), null);
        return result;
    }
    public static Bitmap StringListtoBitmap(Context context, JSONArray AllString) {
        if (AllString.length() <= 0) return null;
        ArrayList<StringBitmapParameter> mBreakString = new ArrayList<>();

        Paint paint = new Paint();
        paint.setAntiAlias(false);
        paint.setTextSize(SMALL_TEXT);

        Typeface typeface = Typeface.createFromAsset(context.getAssets(), "fonts/msyh.ttf");
        Typeface font = Typeface.create(typeface, Typeface.NORMAL);
        paint.setTypeface(font);
        String title="  日期   商品名称       数量        单价         金额";

        mBreakString.add(new StringBitmapParameter(title));
        int totalSum=0;
        double totalNum=0;
        for (int i=0;i<AllString.length();i++) {
            String detail=SPLIT;
            try {
                JSONObject mDetail = AllString.getJSONObject(i);
                detail+=mDetail.getString("time").substring(5,10)+SPLIT+SPLIT;
                switch (mDetail.getString("type")) {
                    case "S":
                    String goods = mDetail.getString("goods_name");
                    Log.d("bitmap time", detail.getBytes().length + "goods_length" + goods.getBytes().length);
                    String[] goodsNames = new String[]{};
                    if (goods.getBytes().length > 10) {
                        goodsNames = GPrinter.getStrList(goods, 5);
                        detail += goodsNames[0];
                    } else {
                        detail += goods;
                        for (int j = 0; j < COL1 - goods.getBytes().length; j++) {
                            detail += SPLIT;
                        }
                    }
                    // Log.d("bitmap goods",detail.getBytes().length+"");
                    int len = mDetail.getString("number").length() - 1;
                    String number = mDetail.getString("number").substring(0, len) + mDetail.getString("unit_name");
                    detail += number;
                    for (int j = 0; j < COL0 - number.getBytes().length; j++) {
                        detail += SPLIT;
                    }
                    totalNum += mDetail.getDouble("number");
                    // Log.d("bitmap number",detail.getBytes().length+"");
                    len = mDetail.getString("price").length() - 1;
                    String price = "￥" + mDetail.getString("price").substring(0, len);
                    detail += price;

                    for (int j = 0; j < COL0 - price.getBytes().length; j++) {
                        detail += SPLIT;
                    }
                    //  Log.d("bitmap price",detail.getBytes().length+"");
                    String sum = "￥" + mDetail.getString("sum");
                    for (int j = 0; j < 56 - sum.getBytes().length - detail.getBytes().length; j++) {
                        detail += SPLIT;
                    }
                    detail += sum;
                    totalSum += mDetail.getInt("sum");
                    // Log.d("bitmap sum",detail.getBytes().length+"");
                    mBreakString.add(new StringBitmapParameter(detail));
                    if (goodsNames != null && goodsNames.length > 1) {
                        for (int j = 1; j < goodsNames.length; j++) {
                            mBreakString.add(new StringBitmapParameter("             " + goodsNames[j]));
                        }
                    }
                    break;
                    case "Init":
                        String info="初期录入应收款";
                        detail+=info;
                        sum="￥" + mDetail.getString("ttl");
                        for (int j = 0; j < COL5 - sum.getBytes().length - detail.getBytes().length; j++) {
                            detail += SPLIT;
                        }
                        detail += sum;
                        totalSum += mDetail.getInt("ttl");
                        // Log.d("bitmap sum","int");
                        mBreakString.add(new StringBitmapParameter(detail));
                        break;
                    case "P":
                        info="客户付款";
                        detail+=info;
                        sum="￥" + mDetail.getString("ttl");
                        for (int j = 0; j < COL5 - sum.getBytes().length - detail.getBytes().length; j++) {
                            detail += SPLIT;
                        }
                        detail += sum;
                        totalSum += mDetail.getInt("ttl");
                        // Log.d("bitmap sum",detail.getBytes().length+"");
                        mBreakString.add(new StringBitmapParameter(detail));
                        break;
                    case "income":
                        info="收入条目";
                        detail+=info;
                        sum="￥" + mDetail.getString("ttl");
                        for (int j = 0; j < COL5 - sum.getBytes().length - detail.getBytes().length; j++) {
                            detail += SPLIT;
                        }
                        detail += sum;
                        totalSum += mDetail.getInt("ttl");
                        // Log.d("bitmap sum",detail.getBytes().length+"");
                        mBreakString.add(new StringBitmapParameter(detail));
                        break;
                    case "cost":
                        info="支出条目";
                        detail+=info;
                        sum="￥" + mDetail.getString("ttl");
                        for (int j = 0; j < COL5 - sum.getBytes().length - detail.getBytes().length; j++) {
                            detail += SPLIT;
                        }
                        detail += sum;
                        totalSum += mDetail.getInt("ttl");
                        // Log.d("bitmap sum",detail.getBytes().length+"");
                        mBreakString.add(new StringBitmapParameter(detail));
                        break;
                    case "CF":
                        info="结转余额";
                        detail+=info;
                        sum="￥" + mDetail.getString("ttl");
                        for (int j = 0; j < COL5 - sum.getBytes().length - detail.getBytes().length; j++) {
                            detail += SPLIT;
                        }
                        detail += sum;
                        totalSum += mDetail.getInt("ttl");
                        // Log.d("bitmap sum",detail.getBytes().length+"");
                        mBreakString.add(new StringBitmapParameter(detail));
                        break;
                    case "B":
                        goods = mDetail.getString("goods_name");
                        Log.d("bitmap time", detail.getBytes().length + "goods_length" + goods.getBytes().length);
                        goodsNames = new String[]{};
                        if (goods.getBytes().length > 10) {
                            goodsNames = GPrinter.getStrList(goods, 5);
                            detail += goodsNames[0];
                        } else {
                            detail += goods;
                            for (int j = 0; j < COL1 - goods.getBytes().length; j++) {
                                detail += SPLIT;
                            }
                        }
                        // Log.d("bitmap goods",detail.getBytes().length+"");
                        len = mDetail.getString("number").length() - 1;
                        number = mDetail.getString("number").substring(0, len) + mDetail.getString("unit_name");
                        detail += number;
                        for (int j = 0; j < COL0 - number.getBytes().length; j++) {
                            detail += SPLIT;
                        }
                        totalNum += mDetail.getDouble("number");
                        // Log.d("bitmap number",detail.getBytes().length+"");
                        len = mDetail.getString("price").length() - 1;
                        price = "￥" + mDetail.getString("price").substring(0, len);
                        detail += price;

                        for (int j = 0; j < COL0 - price.getBytes().length; j++) {
                            detail += SPLIT;
                        }
                        //  Log.d("bitmap price",detail.getBytes().length+"");
                        sum = "￥" + mDetail.getString("sum");
                        for (int j = 0; j < 56 - sum.getBytes().length - detail.getBytes().length; j++) {
                            detail += SPLIT;
                        }
                        detail += sum;
                        totalSum += mDetail.getInt("sum");
                        // Log.d("bitmap sum",detail.getBytes().length+"");
                        mBreakString.add(new StringBitmapParameter(detail));
                        if (goodsNames != null && goodsNames.length > 1) {
                            for (int j = 1; j < goodsNames.length; j++) {
                                mBreakString.add(new StringBitmapParameter("             " + goodsNames[j]));
                            }
                        }
                        break;
                        default:
                            break;
                }
            }catch (JSONException e){ Log.d("JSON bitmapUitl",e.toString());}
        }
        mBreakString.add(new StringBitmapParameter(PRINT_LINE));
        String num="合计数量："+totalNum;
        mBreakString.add(new StringBitmapParameter(num.substring(0,num.length()),IS_RIGHT));
        mBreakString.add(new StringBitmapParameter("合计欠款：￥"+totalSum,IS_RIGHT));

        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        int FontHeight = (int) Math.abs(fontMetrics.leading) + (int) Math.abs(fontMetrics.ascent) + (int) Math.abs(fontMetrics.descent);
        FontHeight+=12;
        y = (int) Math.abs(fontMetrics.leading) + (int) Math.abs(fontMetrics.ascent);

        int bNum = 0;
      /*  for (StringBitmapParameter mParameter : mBreakString) {
            String bStr = mParameter.getText();
            if (bStr.isEmpty() | bStr.contains("\n") | mParameter.getIsSmallOrLarge() == IS_LARGE)
                bNum++;
        }*/
        Bitmap bitmap = Bitmap.createBitmap(WIDTH, FontHeight * (mBreakString.size() + bNum), Bitmap.Config.RGB_565);

        for (int i = 0; i < bitmap.getWidth(); i++) {
            for (int j = 0; j < bitmap.getHeight(); j++) {
                bitmap.setPixel(i, j, Color.WHITE);
            }
        }

        Canvas canvas = new Canvas(bitmap);

        for (StringBitmapParameter mParameter : mBreakString) {

            String str = mParameter.getText();

            //if (mParameter.getIsSmallOrLarge() == IS_SMALL) {
                paint.setTextSize(SMALL_TEXT);

           // } else if (mParameter.getIsSmallOrLarge() == IS_LARGE) {
            //    paint.setTextSize(LARGE_TEXT);
           // }

            if (mParameter.getIsRightOrLeft() == IS_RIGHT) {
                x = WIDTH - paint.measureText(str);
            } else if (mParameter.getIsRightOrLeft() == IS_LEFT) {
                x = START_LEFT;
            } else if (mParameter.getIsRightOrLeft() == IS_CENTER) {
                x = (WIDTH - paint.measureText(str)) / 2.0f;
            }

            if (str.isEmpty() | str.contains("\n") | mParameter.getIsSmallOrLarge() == IS_LARGE) {
                canvas.drawText(str, x, y + FontHeight / 2, paint);
                y = y + FontHeight;
            } else {
                canvas.drawText(str, x, y, paint);
            }
            y = y + FontHeight;
        }
        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();
        return bitmap;
    }

    public static Bitmap StringListtoBitmap(Context context, List<SaleDetail> AllString,int paid_sum,int credit) {
        if (AllString.size() <= 0) return null;
        ArrayList<StringBitmapParameter> mBreakString = new ArrayList<>();

        Paint paint = new Paint();
        paint.setAntiAlias(false);
        paint.setTextSize(SMALL_TEXT);

        Typeface typeface = Typeface.createFromAsset(context.getAssets(), "fonts/msyh.ttf");
        Typeface font = Typeface.create(typeface, Typeface.NORMAL);
        paint.setTypeface(font);
        String title="  商品名称         数量         单价             金额";

        mBreakString.add(new StringBitmapParameter(title));
        int totalSum=0;
        double totalNum=0;
        for (int i=0;i<AllString.size();i++) {
            String detail = SPLIT;
            SaleDetail mDetail = AllString.get(i);
            String goods = mDetail.getGoods_name();
            String[] goodsNames = new String[]{};

            if (goods.getBytes().length > 10) {
                goodsNames = GPrinter.getStrList(goods, 5);
                detail += goodsNames[0]+"    ";
            } else {
                detail += goods;
                for (int j = 0; j < 6+COL1 - goods.getBytes().length; j++) {
                    detail += SPLIT;
                }
            }

            int len = mDetail.getNumber().length() - 1;
            String number = mDetail.getNumber();
            detail += number;
            for (int j = 0; j < COL1 - number.getBytes().length; j++) {
                detail += SPLIT;
            }

            len = mDetail.getPrice().length() - 1;
            String price = mDetail.getPrice();
            detail += price;
            for (int j = 0; j < COL1 - price.getBytes().length; j++) {
                detail += SPLIT;
            }

            String sum = "￥" + mDetail.getSum();
            for (int j = 0; j < 56 - sum.getBytes().length - detail.getBytes().length; j++) {
                detail += SPLIT;
            }
            detail += sum;
            totalSum += mDetail.getSum();
            mBreakString.add(new StringBitmapParameter(detail));

            if (goodsNames != null && goodsNames.length > 1) {
                for (int j = 1; j < goodsNames.length; j++) {
                    mBreakString.add(new StringBitmapParameter(goodsNames[j]));
                }
            }
        }
        mBreakString.add(new StringBitmapParameter(PRINT_LINE));
        mBreakString.add(new StringBitmapParameter("合计应付：￥"+totalSum,IS_RIGHT));
        mBreakString.add(new StringBitmapParameter("实收金额：￥"+paid_sum,IS_RIGHT));
        int ttl=credit+totalSum-paid_sum;
        if(ttl!=0){
            mBreakString.add(new StringBitmapParameter("截止打印时间，累计欠款：￥"+ttl,IS_RIGHT));
        }
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        int FontHeight = (int) Math.abs(fontMetrics.leading) + (int) Math.abs(fontMetrics.ascent) + (int) Math.abs(fontMetrics.descent);
        FontHeight+=12;
        y = (int) Math.abs(fontMetrics.leading) + (int) Math.abs(fontMetrics.ascent);

        int bNum = 0;

        Bitmap bitmap = Bitmap.createBitmap(WIDTH, FontHeight * (mBreakString.size() + bNum), Bitmap.Config.RGB_565);

        for (int i = 0; i < bitmap.getWidth(); i++) {
            for (int j = 0; j < bitmap.getHeight(); j++) {
                bitmap.setPixel(i, j, Color.WHITE);
            }
        }

        Canvas canvas = new Canvas(bitmap);

        for (StringBitmapParameter mParameter : mBreakString) {

            String str = mParameter.getText();

            //if (mParameter.getIsSmallOrLarge() == IS_SMALL) {
            paint.setTextSize(SMALL_TEXT);

            // } else if (mParameter.getIsSmallOrLarge() == IS_LARGE) {
            //    paint.setTextSize(LARGE_TEXT);
            // }

            if (mParameter.getIsRightOrLeft() == IS_RIGHT) {
                x = WIDTH - paint.measureText(str);
            } else if (mParameter.getIsRightOrLeft() == IS_LEFT) {
                x = START_LEFT;
            } else if (mParameter.getIsRightOrLeft() == IS_CENTER) {
                x = (WIDTH - paint.measureText(str)) / 2.0f;
            }

            if (str.isEmpty() | str.contains("\n") | mParameter.getIsSmallOrLarge() == IS_LARGE) {
                canvas.drawText(str, x, y + FontHeight / 2, paint);
                y = y + FontHeight;
            } else {
                canvas.drawText(str, x, y, paint);
            }
            y = y + FontHeight;
        }
        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();
        return bitmap;
    }
}
