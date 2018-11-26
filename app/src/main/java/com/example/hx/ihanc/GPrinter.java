package com.example.hx.ihanc;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.gprinter.command.EscCommand;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Vector;

import static android.text.TextUtils.substring;
import static com.example.hx.ihanc.DeviceConnFactoryManager.CONN_STATE_FAILED;

public class GPrinter {
    private ThreadPool threadPool;
    private boolean isBluetooth;
    private Bitmap info=null;
    private static final int HAS_CONNECTED = 0x97;
    private static final int ORDER_PRINT = 0x98;
    private static final int ORDER_PRINT_SALE = 0x99;
    private boolean mHAS_CONNECTED;
    private boolean mORDER_PRINT;
    private int id;
    private JSONArray creditJson=null;
    private ArrayList<SaleDetail> mSaleDetails=null;
    private int paid_sum;
    private int credit_sum;

    public static final String PRINT_LINE = "------------------------------------------------";
    public static final int PRINT_TOTAL_LENGTH = 48 * 3;
    public static final short PRINT_POSITION_0 = 6 * 3;
    public static final short PRINT_POSITION_1 = 20 * 3;
    public static final short PRINT_POSITION_2 = 26 * 3;
    public static final short PRINT_POSITION_3 = 36 * 3;

    public static final short PRINT_POSITION_4 = 16 * 3;
    public static final short PRINT_POSITION_5 = 24 * 3;
    public static final short PRINT_POSITION_6 = 38 * 3;

    public static final int MAX_GOODS_NAME_LENGTH = 22 * 3;

    public static final short PRINT_UNIT = 43;
    public static final String RECEIPT_TYPE_CREDIT="credit";
    public static final String RECEIPT_TYPE_SALE="sale";
    public String receiptType=RECEIPT_TYPE_CREDIT;

    public  GPrinter(String Address,boolean isBluetooth){
        DeviceConnFactoryManager.closeAllPort();
        mHAS_CONNECTED=false;
        mORDER_PRINT=false;
        this.isBluetooth=isBluetooth;
        if(isBluetooth){
            id=0;
            new DeviceConnFactoryManager.Build()
                    .setId(id)
                    .setConnMethod(DeviceConnFactoryManager.CONN_METHOD.BLUETOOTH)
                    .setMacAddress(Address)
                    .build();
            threadPool = ThreadPool.getInstantiation();
            threadPool.addTask(new Runnable() {
                @Override
                public void run() {
                    DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].openPort();
                }
            });
        }else{
            id=1;
            new DeviceConnFactoryManager.Build()
                    .setConnMethod(DeviceConnFactoryManager.CONN_METHOD.WIFI)
                    .setIp(Address)
                    .setId(id)
                    .setPort(9100)
                    .build();
            threadPool = ThreadPool.getInstantiation();
            threadPool.addTask(new Runnable() {
                @Override
                public void run() {
                    DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].openPort();
                }
            });
        }
    }
    public void print(Bitmap info){
        this.info=info;
        mORDER_PRINT=true;
        mHandler.obtainMessage(ORDER_PRINT).sendToTarget();
    }
    public void print(JSONArray credit){
        this.creditJson=credit;
        mORDER_PRINT=true;
        mHandler.obtainMessage(ORDER_PRINT).sendToTarget();
    }

    public void print(ArrayList<SaleDetail> mSaleDetails,int paid_sum,int credit_sum){
        Log.d("GPrinter","ORDER_PRINT_SALE:");
        this.mSaleDetails=mSaleDetails;
        this.paid_sum=paid_sum;
        this.credit_sum=credit_sum;
        mORDER_PRINT=true;
        mHandler.obtainMessage(ORDER_PRINT_SALE).sendToTarget();
    }

    void sendReceiptWithResponse() {
        EscCommand esc = new EscCommand();
        esc.addInitializePrinter();

        byte[] command = new byte[]{29, 33, 17};
        esc.addUserCommand(command);
        if(info!=null){
           // Log.d("BITMAP","GPRINT");
            esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);
            esc.addOriginRastBitImage(info,576,0);
            //return;
        }else{
            command = new byte[]{29, 33, 17};
            esc.addUserCommand(command);
            esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);
            esc.addText(Utils.mCompanyInfo.getName()+"\n");
            esc.addPrintAndLineFeed();
            command = new byte[]{29, 33, 0};
            esc.addUserCommand(command);
            if(receiptType.equals(RECEIPT_TYPE_CREDIT))
               esc.addText("对账单\n");
            else esc.addText("销售单\n");
            esc.addPrintAndLineFeed();
            esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);
            esc.addText("\n客户："+Utils.printMemberName.getMember_name()+"\n");
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String date = df.format(new Date());
            esc.addText("打印时间："+date+"\n");
            esc.addText(PRINT_LINE);
            // 商品头信息
            esc.addSetHorAndVerMotionUnits((byte) PRINT_UNIT, (byte) 0);
            esc.addText("日期");
            esc.addSetAbsolutePrintPosition(PRINT_POSITION_0);
            esc.addText("商品名");

            esc.addSetAbsolutePrintPosition(PRINT_POSITION_2);
            esc.addText("单价");

            esc.addSetAbsolutePrintPosition(PRINT_POSITION_1);
            esc.addText("数量");

            esc.addSetAbsolutePrintPosition(PRINT_POSITION_3);
            esc.addText("金额");
            esc.addPrintAndLineFeed();
            esc.addPrintAndLineFeed();
            int totalSum=0;
            double totalNum=0;
            DecimalFormat formatter=new DecimalFormat(",###,####.0");
            //NumberFormat formatter = NumberFormat.getNumberInstance(Locale.CHINA);
            // 商品信息
            if (creditJson!= null && creditJson.length() > 0) {
                int preSale_id=0;
               for (int i = 0; i < creditJson.length(); i++) {
                   try{
                       JSONObject mDetail = creditJson.getJSONObject(i);
                       esc.addSetHorAndVerMotionUnits((byte) PRINT_UNIT, (byte) 0);
                       String info="";
                       String type=mDetail.getString("type");
                       switch (type) {
                           case "S":
                               if(preSale_id==0||preSale_id!=mDetail.getInt("sale_id")) {
                                   preSale_id=mDetail.getInt("sale_id");
                                   info=mDetail.getString("time").substring(5,10)+"  ";
                               }else info="";
                               esc.addText(info);
                               info = mDetail.getString("goods_name");
                               String[] goodsNames = new String[]{};
                               Log.d("goodsName", info.length() + "");
                               if (info.getBytes().length > 18) {
                                   goodsNames = getStrList(info, 6);
                               }
                               esc.addSetAbsolutePrintPosition(PRINT_POSITION_0);
                               if (goodsNames.length > 0) {
                                   esc.addText(goodsNames[0]);
                               } else {
                                   esc.addText(info);
                               }
                               // 单价
                               int length = mDetail.getString("price").length() - 1;
                               info = "￥" + mDetail.getString("price").substring(0, length);
                               //  int priceLength =info.getBytes().length;
                               //short pricePosition = (short) (PRINT_POSITION_1 + 12 - priceLength * 3);
                               esc.addSetAbsolutePrintPosition(PRINT_POSITION_2);
                               esc.addText(info);      // 单价还未获取

                               // 数量
                               length = mDetail.getString("number").length() - 1;
                               info = mDetail.getString("number").substring(0, length) + mDetail.getString("unit_name");
                               int numLength = info.getBytes().length;
                               short numPosition = (short) (PRINT_POSITION_1 + 14 - numLength * 3);
                               esc.addSetAbsolutePrintPosition(numPosition);
                               esc.addText(info);
                               totalNum += mDetail.getDouble("number");

                               // 金额
                               if(mDetail.getInt("sum")>0)
                                  info = "￥" + mDetail.getInt("sum");
                               else
                                   info = "￥-" + mDetail.getInt("sum")*-1;
                               int amountLength = info.getBytes().length;
                               short amountPosition = (short) (PRINT_POSITION_3 + 11 - amountLength * 3);
                               amountPosition = PRINT_POSITION_3;
                               esc.addSetAbsolutePrintPosition(amountPosition);
                               esc.addText(info);
                               totalSum += mDetail.getInt("sum");
                               esc.addPrintAndLineFeed();
                               if (goodsNames == null || goodsNames.length == 0) {
                                   esc.addPrintAndLineFeed();
                               } else if (goodsNames != null && goodsNames.length > 1) {
                                   esc.addSetAbsolutePrintPosition(PRINT_POSITION_0);
                                   for (int j = 1; j < goodsNames.length; j++) {
                                       esc.addText("" + goodsNames[j]);
                                       esc.addPrintAndLineFeed();
                                   }
                               }
                               break;
                           case "Init":
                               esc.addSetAbsolutePrintPosition(PRINT_POSITION_0);
                               esc.addText("初期录入应收款");
                               esc.addSetAbsolutePrintPosition(PRINT_POSITION_3);
                               info = "￥" + mDetail.getString("ttl")+"\n\n";
                               totalSum += mDetail.getInt("ttl");
                               esc.addText(info);
                               esc.addPrintAndLineFeed();
                               break;
                           case "P":
                               esc.addSetAbsolutePrintPosition(PRINT_POSITION_0);
                               esc.addText("客户付款");
                               esc.addSetAbsolutePrintPosition(PRINT_POSITION_3);
                               info = "￥" + mDetail.getString("ttl")+"\n\n";
                               totalSum += mDetail.getInt("ttl");
                               esc.addText(info);
                               esc.addPrintAndLineFeed();
                               break;
                           case "income":
                               esc.addSetAbsolutePrintPosition(PRINT_POSITION_0);
                               String summary=mDetail.getString("summary");
                               String[] sum=summary.split("-");
                               esc.addText(sum[sum.length-1]);
                               esc.addSetAbsolutePrintPosition(PRINT_POSITION_3);
                               if(mDetail.getInt("ttl")>=0)
                               info = "￥" + mDetail.getString("ttl")+"\n";
                               else
                                   info = "￥-" + mDetail.getInt("ttl")*-1+"\n";
                               totalSum += mDetail.getInt("ttl");
                               esc.addText(info);
                               esc.addPrintAndLineFeed();
                               break;
                           case "cost":
                               esc.addSetAbsolutePrintPosition(PRINT_POSITION_0);
                               summary=mDetail.getString("summary");
                               sum=summary.split("-");
                               esc.addText(sum[sum.length-1]);
                               esc.addSetAbsolutePrintPosition(PRINT_POSITION_3);
                               if(mDetail.getInt("ttl")>0)
                                   info = "￥" + mDetail.getString("ttl")+"\n";
                               else
                                   info = "￥-" + mDetail.getInt("ttl")*-1+"\n";
                               totalSum += mDetail.getInt("ttl");
                               esc.addText(info);
                               esc.addPrintAndLineFeed();
                               break;
                           case "CF":
                               esc.addSetAbsolutePrintPosition(PRINT_POSITION_0);
                               esc.addText("结转余额");
                               esc.addSetAbsolutePrintPosition(PRINT_POSITION_3);
                               info = "￥" + mDetail.getString("ttl")+"\n";
                               totalSum += mDetail.getInt("ttl");
                               esc.addText(info);
                               esc.addPrintAndLineFeed();
                               break;
                           case "B":
                               if(preSale_id==0||preSale_id!=mDetail.getInt("sale_id")) {
                                   preSale_id=mDetail.getInt("sale_id");
                                   info=mDetail.getString("time").substring(5,10)+"  ";
                               }else info="";
                               esc.addText(info);
                               info = mDetail.getString("goods_name");
                               goodsNames = new String[]{};
                               Log.d("goodsName", info.length() + "");
                               if (info.length() > 6) {
                                   goodsNames = getStrList(info, 6);
                               }
                               esc.addSetAbsolutePrintPosition(PRINT_POSITION_0);
                               if (goodsNames.length > 0) {
                                   esc.addText(goodsNames[0]);
                               } else {
                                   esc.addText(info);
                               }
                               // 单价
                               length = mDetail.getString("price").length() - 1;
                               info = "￥" + mDetail.getString("price").substring(0, length);
                               //  int priceLength =info.getBytes().length;
                               //short pricePosition = (short) (PRINT_POSITION_1 + 12 - priceLength * 3);
                               esc.addSetAbsolutePrintPosition(PRINT_POSITION_2);
                               esc.addText(info);      // 单价还未获取

                               // 数量
                               length = mDetail.getString("number").length() - 1;
                               info = mDetail.getString("number").substring(0, length) + mDetail.getString("unit_name");
                               numLength = info.getBytes().length;
                               numPosition = (short) (PRINT_POSITION_1 + 14 - numLength * 3);
                               esc.addSetAbsolutePrintPosition(numPosition);
                               esc.addText(info);
                               totalNum += mDetail.getDouble("number");

                               // 金额
                               info = "￥" +formatter.format( mDetail.getString("sum"));
                               amountLength = info.getBytes().length;
                               amountPosition = (short) (PRINT_POSITION_3 + 11 - amountLength * 3);
                               amountPosition = PRINT_POSITION_3;
                               esc.addSetAbsolutePrintPosition(amountPosition);
                               esc.addText(info);
                               totalSum += mDetail.getInt("sum");
                               esc.addPrintAndLineFeed();
                               if (goodsNames == null || goodsNames.length == 0) {
                                   esc.addPrintAndLineFeed();
                               } else if (goodsNames != null && goodsNames.length > 1) {
                                   esc.addSetAbsolutePrintPosition(PRINT_POSITION_0);
                                   for (int j = 1; j < goodsNames.length; j++) {
                                       esc.addText("" + goodsNames[j]);
                                       esc.addPrintAndLineFeed();
                                   }
                               }
                               break;
                           default:
                               break;
                       }
                   }catch (JSONException e){e.printStackTrace();}

                }
                esc.addText(PRINT_LINE);
            }

            // 总计信息
            esc.addSelectJustification(EscCommand.JUSTIFICATION.RIGHT);// 设置打印居右
            String num="合计数量："+totalNum;
            esc.addText(num.substring(0,num.length())+"\n");
            if(totalSum>=0)
            esc.addText("合计金额：￥"+formatter.format(totalSum)+"\n");
            else{
                esc.addText("合计金额：￥-"+formatter.format(totalSum*-1)+"\n");
            }
            esc.addText("现累计欠款：￥"+formatter.format(credit_sum)+"\n");
            esc.addText(PRINT_LINE);
            //公司信息
            esc.addPrintAndLineFeed();
            esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);
            esc.addText("\n感谢您的惠顾，欢迎下次光临!\n\n");
            esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);
            esc.addText("联系电话："+Utils.mCompanyInfo.getTel()+"\n\n");
            if(Utils.mCompanyInfo.getAddress().length>1){
                for (int i=0;i<Utils.mCompanyInfo.getAddress().length;i++){
                    esc.addText("地址："+(i+1)+Utils.mCompanyInfo.getAddress()[i]+"\n");
                }
            }else{
                esc.addText("地址："+Utils.mCompanyInfo.getAddress()[0]+"\n\n");
            }
        }

        esc.addPrintAndLineFeed();
        esc.addPrintAndLineFeed();
        esc.addPrintAndFeedLines((byte) 5);
       // Log.d("Gprint","kaishi");
        //切纸
        command = new byte[]{29, 86, 1};
        esc.addUserCommand(command);

        // 加入查询打印机状态，打印完成后，此时会接收到GpCom.ACTION_DEVICE_STATUS广播
        esc.addQueryPrinterStatus();
        Vector<Byte> datas = esc.getCommand();
        // 发送数据
        DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].sendDataImmediately(datas);
    }
    public static String[] getStrList(String inputString, int length) {
        String[] list=new String[2];
        list[0]=inputString.substring(0, length-1);
        list[1]=inputString.substring(length-1);
       /* int time=inputString.getBytes().length/(length*2);
        if(inputString.getBytes().length% (length*2 )>0) time++;
        String[] list=new String[time];
        for (int index = 0; index < time-1; index++) {
            String childStr = substring(inputString, index * length,
                    (index + 1) * length);
            list[index]=childStr;
            Log.d("getStringLIST",childStr);
        }
        time--;
        list[time]=substring(inputString,time*length,inputString.length()); */
        return list;
    }
    public void finishPrint(){
        //App.getContext().unregisterReceiver(receiver);
        DeviceConnFactoryManager.closeAllPort();
    }

    public BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                //Usb连接断开、蓝牙连接断开广播
                case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                    break;
                case DeviceConnFactoryManager.ACTION_CONN_STATE:
                    int state = intent.getIntExtra(DeviceConnFactoryManager.STATE, -1);
                    // int deviceId = intent.getIntExtra(DeviceConnFactoryManager.DEVICE_ID, -1);
                    switch (state) {
                        case DeviceConnFactoryManager.CONN_STATE_DISCONNECT:
                            Utils.toast(App.getContext(), "打印机已断开");
                            break;
                        case DeviceConnFactoryManager.CONN_STATE_CONNECTING:
                            Utils.toast(App.getContext(), "打印机连接中");
                            break;
                        case DeviceConnFactoryManager.CONN_STATE_CONNECTED:
                            Utils.toast(App.getContext(), "打印机已连接");
                            Log.d("GPrinter","receiver");
                            mHAS_CONNECTED=true;
                            mHandler.obtainMessage(HAS_CONNECTED).sendToTarget();
                            break;
                        case CONN_STATE_FAILED:
                            Utils.toast(App.getContext(), "打印机连接失败");
                            break;
                        default:
                            break;
                    }
                    break;
                    case DeviceConnFactoryManager.ACTION_QUERY_PRINTER_STATE:
                        finishPrint();
                        break;
                default:
                    break;
            }
        }
    };
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.d("GPrinter","hander"+msg.what);
            switch (msg.what) {
                case HAS_CONNECTED:
                    Log.d("GPrinter","HAS_CONNECTED:");
                    if(mHAS_CONNECTED && mORDER_PRINT){
                        threadPool = ThreadPool.getInstantiation();
                        threadPool.addTask(new Runnable() {
                            @Override
                            public void run() {
                                if(mSaleDetails!=null&&mSaleDetails.size()>0)
                                    sendReceiptWithResponseSale();
                                else sendReceiptWithResponse();
                             mORDER_PRINT=false;
                             //finishPrint();
                            }
                        });
                    }
                    break;
                case ORDER_PRINT:
                    Log.d("GPrinter","ORDER_PRINT:");
                    if(mHAS_CONNECTED && mORDER_PRINT){
                        threadPool = ThreadPool.getInstantiation();
                        threadPool.addTask(new Runnable() {
                            @Override
                            public void run() {
                                sendReceiptWithResponse();
                                mORDER_PRINT=false;
                                //finishPrint();
                            }
                        });
                    }
                    break;
                case ORDER_PRINT_SALE:
                    Log.d("GPrinter","ORDER_PRINT_SALE:");
                    if(mHAS_CONNECTED && mORDER_PRINT){
                        threadPool = ThreadPool.getInstantiation();
                        threadPool.addTask(new Runnable() {
                            @Override
                            public void run() {
                                sendReceiptWithResponseSale();
                                mORDER_PRINT=false;
                                //finishPrint();
                            }
                        });
                    }
                    break;
                default:
                    break;
            }
        }
    };

    void sendReceiptWithResponseSale() {

        EscCommand esc = new EscCommand();
        esc.addInitializePrinter();

        byte[] command = new byte[]{29, 33, 17};
        esc.addUserCommand(command);
        if(info!=null){
            // Log.d("BITMAP","GPRINT");
            esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);
            esc.addOriginRastBitImage(info,576,0);
            //return;
        }else{
            command = new byte[]{29, 33, 17};
            esc.addUserCommand(command);
            esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);
            esc.addText(Utils.mCompanyInfo.getName()+"\n");
            esc.addPrintAndLineFeed();
            command = new byte[]{29, 33, 0};
            esc.addUserCommand(command);
            esc.addText("销售单\n");
            esc.addPrintAndLineFeed();
            esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);
            esc.addText("\n客户："+Utils.printMemberName.getMember_name()+"\n");
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String date = df.format(new Date());
            esc.addText("打印时间："+date+"\n");
            esc.addText(PRINT_LINE);
            // 商品头信息
            esc.addSetHorAndVerMotionUnits((byte) PRINT_UNIT, (byte) 0);
            esc.addText("商品名");

            esc.addSetAbsolutePrintPosition(PRINT_POSITION_4);
            esc.addText("数量");

            esc.addSetAbsolutePrintPosition(PRINT_POSITION_5);
            esc.addText("单价");

            esc.addSetAbsolutePrintPosition(PRINT_POSITION_6);
            esc.addText("金额");
            esc.addPrintAndLineFeed();
            esc.addPrintAndLineFeed();
            int totalSum=0;
            double totalNum=0;
            DecimalFormat f=new DecimalFormat(",###,####.0");
            // 商品信息
            if (mSaleDetails!= null && mSaleDetails.size() > 0) {
                for (int i = 0; i < mSaleDetails.size(); i++) {
                    SaleDetail detail=mSaleDetails.get(i);
                    String info = detail.getGoods_name();
                    String[] goodsNames = new String[]{};
                    if (info.length() > 6) {
                        goodsNames = getStrList(info, 6);
                    }
                    esc.addSetHorAndVerMotionUnits((byte) PRINT_UNIT, (byte) 0);
                    if (goodsNames.length > 0) {
                        esc.addText(goodsNames[0]);
                    } else {
                        esc.addText(info);
                    }
                    // 数量
                    info = detail.getNumber();
                    esc.addSetAbsolutePrintPosition(PRINT_POSITION_4);
                    esc.addText(info);

                    // 单价
                    info =  detail.getPrice();
                    esc.addSetAbsolutePrintPosition(PRINT_POSITION_5);
                    esc.addText(info);      // 单价还未获取


                    // 金额
                    info = "￥" + f.format(detail.getSum());
                    esc.addSetAbsolutePrintPosition(PRINT_POSITION_6);
                    esc.addText(info);
                    totalSum += detail.getSum();
                    esc.addPrintAndLineFeed();
                    if (goodsNames == null || goodsNames.length == 0) {
                        esc.addPrintAndLineFeed();
                    } else if (goodsNames != null && goodsNames.length > 1) {
                        esc.addSetAbsolutePrintPosition(PRINT_POSITION_0);
                        for (int j = 1; j < goodsNames.length; j++) {
                            esc.addText("" + goodsNames[j]);
                            esc.addPrintAndLineFeed();
                        }
                    }
                }

                esc.addText(PRINT_LINE);
            }
            NumberFormat formatter = NumberFormat.getNumberInstance(Locale.CHINA);

            // 总计信息
            esc.addSelectJustification(EscCommand.JUSTIFICATION.RIGHT);// 设置打印居右
            esc.addText("合计金额：￥"+f.format(totalSum)+"\n");
            if(paid_sum>=0)
            esc.addText("实收金额：￥"+f.format(paid_sum)+"\n");
            else{
                esc.addText("实收金额：￥-"+f.format(paid_sum*-1)+"\n");
            }
            int ttl_credit=credit_sum+totalSum-paid_sum;
            if(ttl_credit>0)
            esc.addText("累计欠款：￥"+f.format(ttl_credit)+"\n");
            if(ttl_credit<0)
                esc.addText("累计欠款：￥-"+f.format(ttl_credit*-1)+"\n");
            esc.addText(PRINT_LINE);
            //公司信息
            esc.addPrintAndLineFeed();
            esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);
            esc.addText("\n感谢您的惠顾，欢迎下次光临!\n\n");
            esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);
            esc.addText("联系电话："+Utils.mCompanyInfo.getTel()+"\n\n");
            if(Utils.mCompanyInfo.getAddress().length>1){
                for (int i=0;i<Utils.mCompanyInfo.getAddress().length;i++){
                    esc.addText("地址："+(i+1)+Utils.mCompanyInfo.getAddress()[i]+"\n");
                }
            }else{
                esc.addText("地址："+Utils.mCompanyInfo.getAddress()[0]+"\n\n");
            }
        }

        esc.addPrintAndLineFeed();
        esc.addPrintAndFeedLines((byte) 5);
        // Log.d("Gprint","kaishi");
        //切纸
        command = new byte[]{29, 86, 1};
        esc.addUserCommand(command);

        // 加入查询打印机状态，打印完成后，此时会接收到GpCom.ACTION_DEVICE_STATUS广播
        esc.addQueryPrinterStatus();
        Vector<Byte> datas = esc.getCommand();
        // 发送数据
        DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].sendDataImmediately(datas);
    }

    public void setCredit_sum(int sum){
        this.credit_sum=sum;
    }
}
