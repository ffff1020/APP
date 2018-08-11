package com.example.hx.ihanc;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.gprinter.command.EscCommand;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import static com.example.hx.ihanc.DeviceConnFactoryManager.CONN_STATE_FAILED;

public class GPrinter {
    private ThreadPool threadPool;
    private boolean isBluetooth;
    private String info;
    private static final int HAS_CONNECTED = 0x97;
    private static final int ORDER_PRINT = 0x98;
    private boolean mHAS_CONNECTED;
    private boolean mORDER_PRINT;
    private int id;
    private static final String PRINT_LINE = "------------------------------------------------\n";

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
    public void print(String info){
        this.info=info;
        mORDER_PRINT=true;
        mHandler.obtainMessage(ORDER_PRINT).sendToTarget();
    }

    void sendReceiptWithResponse() {
        EscCommand esc = new EscCommand();
        esc.addInitializePrinter();
        esc.addPrintAndFeedLines((byte) 3);
        // 设置打印居中
        esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);
        // 设置为倍高倍宽
        esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF, EscCommand.ENABLE.ON, EscCommand.ENABLE.ON, EscCommand.ENABLE.OFF);
        // 打印文字
        byte[] command = new byte[]{29, 33, 17};
        esc.addUserCommand(command);
        esc.addText(Utils.mCompanyInfo.getName());
        esc.addPrintAndLineFeed();
        command = new byte[]{29, 33, 0};
        esc.addUserCommand(command);
        esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);
        esc.addText("\n客户："+Utils.printMemberName+"\n");
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = df.format(new Date());
        esc.addText("打印时间："+date+"\n");
        esc.addText(PRINT_LINE);
        /* 打印文字 */
        // 取消倍高倍宽
        esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF);
        // 设置打印左对齐
        esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);
        // 打印文字
        esc.addText(info);
        esc.addText("Print text\n");
        // 打印文字

        esc.addText("Welcome to use SMARNET printer!\n");


        /* 绝对位置 具体详细信息请查看GP58编程手册 */
        esc.addText("智汇");
        esc.addSetHorAndVerMotionUnits((byte) 7, (byte) 0);
        esc.addSetAbsolutePrintPosition((short) 6);
        esc.addText("网络");
        esc.addSetAbsolutePrintPosition((short) 10);
        esc.addText("设备");
        esc.addPrintAndLineFeed();

        /* 打印图片 */
        // 打印文字
        esc.addText("Print bitmap!\n");


        // 设置打印左对齐


        esc.addText(PRINT_LINE);
        esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);
        esc.addText("感谢您的惠顾，欢迎下次光临！\n");
        esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);
        esc.addText("联系电话："+Utils.mCompanyInfo.getTel()+"\n");
        for (int i=0;i<Utils.mCompanyInfo.getAddress().length;i++){
            esc.addText("地址"+(i+1)+":"+Utils.mCompanyInfo.getAddress()[i]+"\n");
        }
        esc.addPrintAndLineFeed();
        esc.addPrintAndLineFeed();
        esc.addPrintAndLineFeed();
        esc.addPrintAndLineFeed();

        //切纸
        command = new byte[]{29, 86, 1};
        esc.addUserCommand(command);

        // 加入查询打印机状态，打印完成后，此时会接收到GpCom.ACTION_DEVICE_STATUS广播
        esc.addQueryPrinterStatus();
        Vector<Byte> datas = esc.getCommand();
        // 发送数据
        DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].sendDataImmediately(datas);
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
            switch (msg.what) {
                case HAS_CONNECTED:
                    Log.d("GPrinter","HAS_CONNECTED:");
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
                default:
                    break;
            }
        }
    };
}
