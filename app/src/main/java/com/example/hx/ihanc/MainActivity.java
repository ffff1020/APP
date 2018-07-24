package com.example.hx.ihanc;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.gprinter.command.EscCommand;
import com.gprinter.command.LabelCommand;

import java.util.Vector;

import static android.hardware.usb.UsbManager.ACTION_USB_DEVICE_DETACHED;
import static com.example.hx.ihanc.Constant.ACTION_USB_PERMISSION;
import static com.example.hx.ihanc.DeviceConnFactoryManager.ACTION_QUERY_PRINTER_STATE;
import static com.example.hx.ihanc.DeviceConnFactoryManager.CONN_STATE_FAILED;

public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;
    public SharedPreferences sp;
    private Button mButton;
    private Button mPrintButton;
    private Button mGPrint;
    private ThreadPool threadPool;
    private GPrinter mGPrinter;
    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
                    Intent intent=new Intent(MainActivity.this,SettingsActivity.class);
                    startActivity(intent);
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }
    };
    @Override
    protected void onStart(){
        super.onStart();
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(ACTION_USB_DEVICE_DETACHED);
        filter.addAction(ACTION_QUERY_PRINTER_STATE);
        filter.addAction(DeviceConnFactoryManager.ACTION_CONN_STATE);
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityCollector.addActivity(this);
        initView();
    }
    @Override
    protected void onStop() {
        super.onStop();
        //if(mGPrinter!=null){unregisterReceiver(mGPrinter.receiver);}
        unregisterReceiver(receiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DeviceConnFactoryManager.closeAllPort();
        if (threadPool != null) {
            threadPool.stopThreadPool();
        }
    }

    public void initView(){
        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        sp=PreferenceManager.getDefaultSharedPreferences(this);
        mButton=(Button)findViewById(R.id.initPrinter);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initPrinter();
            }
        });
        mPrintButton=(Button)findViewById(R.id.print);
        mPrintButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                print();
            }
        });
        mPrintButton=(Button)findViewById(R.id.GPrint);
        mPrintButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
                filter.addAction(ACTION_USB_DEVICE_DETACHED);
                filter.addAction(ACTION_QUERY_PRINTER_STATE);
                filter.addAction(DeviceConnFactoryManager.ACTION_CONN_STATE);
                String macAddress=sp.getString(getString(R.string.bluetooth_printer_address),"");
                mGPrinter=new GPrinter(macAddress,true);
                registerReceiver(mGPrinter.receiver, filter);
                mGPrinter.print("韩川");
            }
        });
    }

    public void onBackPressed() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);// "android.intent.action.MAIN"
        intent.addCategory(Intent.CATEGORY_HOME); //"android.intent.category.HOME"
        startActivity(intent);
    }

    public void initPrinter(){
        if(sp.getBoolean("bluetooth_printer",false)){
            String macAddress=sp.getString(getString(R.string.bluetooth_printer_address),"");
            if(macAddress.equals("")){
                Toast.makeText(MainActivity.this,"请配置您的打印机！",Toast.LENGTH_LONG);
            }else{
                IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
                filter.addAction(ACTION_USB_DEVICE_DETACHED);
                filter.addAction(ACTION_QUERY_PRINTER_STATE);
                filter.addAction(DeviceConnFactoryManager.ACTION_CONN_STATE);
                mGPrinter=new GPrinter(macAddress,true);
                registerReceiver(mGPrinter.receiver, filter);
                mGPrinter.print("韩川");
            }
        }else{
            String IP=sp.getString(getString(R.string.printer_IP),"").trim();
            IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
            filter.addAction(ACTION_USB_DEVICE_DETACHED);
            filter.addAction(ACTION_QUERY_PRINTER_STATE);
            filter.addAction(DeviceConnFactoryManager.ACTION_CONN_STATE);
            mGPrinter=new GPrinter(IP,false);
            registerReceiver(mGPrinter.receiver, filter);
            mGPrinter.print("SAMPLE");
        }
   }
   public void print(){
       if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[0] == null ||
               !DeviceConnFactoryManager.getDeviceConnFactoryManagers()[0].getConnState()) {
           Utils.toast(this, "not connected");
           return;
       }else {
           threadPool = ThreadPool.getInstantiation();
           threadPool.addTask(new Runnable() {
               @Override
               public void run() {
                   //if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[0].getCurrentPrinterCommand() == PrinterCommand.ESC) {
                   sendReceiptWithResponse();
                   //System.out.print(DeviceConnFactoryManager.getDeviceConnFactoryManagers()[0].getMacAddress());
                   // }
               }
           });
       }
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
        esc.addText("Sample\n");
        esc.addPrintAndLineFeed();

        /* 打印文字 */
        // 取消倍高倍宽
        esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF);
        // 设置打印左对齐
        esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);
        // 打印文字
        esc.addText("Print text\n");
        // 打印文字
        esc.addText("Welcome to use SMARNET printer!\n");

        /* 打印繁体中文 需要打印机支持繁体字库 */
        String message = "佳博智匯票據打印機\n";
        esc.addText(message, "GB2312");
        esc.addPrintAndLineFeed();

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
        /*
         * QRCode命令打印 此命令只在支持QRCode命令打印的机型才能使用。 在不支持二维码指令打印的机型上，则需要发送二维条码图片
         */
        // 打印文字
        esc.addText("Print QRcode\n");

        // 设置打印左对齐
        esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);
        //打印文字
        esc.addText("Completed!\r\n");

        // 加入查询打印机状态，打印完成后，此时会接收到GpCom.ACTION_DEVICE_STATUS广播
        esc.addQueryPrinterStatus();
        Vector<Byte> datas = esc.getCommand();
        // 发送数据
        DeviceConnFactoryManager.getDeviceConnFactoryManagers()[0].sendDataImmediately(datas);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
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
                            Utils.toast(MainActivity.this, "CONN_STATE_DISCONNECT");
                            break;
                        case DeviceConnFactoryManager.CONN_STATE_CONNECTING:
                            Utils.toast(MainActivity.this, "CONN_STATE_CONNECTING");
                            break;
                        case DeviceConnFactoryManager.CONN_STATE_CONNECTED:
                            Utils.toast(MainActivity.this, "CONN_STATE_CONNECTED");
                            break;
                        case CONN_STATE_FAILED:
                            Utils.toast(MainActivity.this, "CONN_STATE_FAILED");
                            break;
                        default:
                            break;
                    }
                    break;
                default:
                    break;
            }
        }
    };

}
