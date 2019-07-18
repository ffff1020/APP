package com.example.hx.ihanc;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.sdk.android.push.CloudPushService;
import com.alibaba.sdk.android.push.CommonCallback;
import com.alibaba.sdk.android.push.noonesdk.PushServiceFactory;
import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import cz.msebera.android.httpclient.Header;
import static android.Manifest.permission.READ_CONTACTS;
import static com.example.hx.ihanc.ActivityCollector.removeActivity;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;
    public static final int REQUEST_ENABLE=1;
    public  static SharedPreferences.Editor  mPrefEditor ;
    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;
    public static BluetoothAdapter mBluetoothAdapter;
    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private Button  mCodeButtonView;
    private Timer timer = null;
    private EditText mCode;
    private int getCodeCount=0;
    private double mVersionCode=0;
    private ProgressDialog dialog;
    private String url="";
    private String DOWNLOAD_NAME = "ihanc";
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE };
    public SharedPreferences sp;
    @Override
    protected void onStart(){
        super.onStart();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ActivityCollector.addActivity(this);
        blueSerialSetting();
        mProgressView = findViewById(R.id.login_progress);
        mLoginFormView = findViewById(R.id.login_form);
        mPrefEditor = getSharedPreferences("pref_ihanc", MODE_PRIVATE).edit();
        sp=getSharedPreferences("pref_ihanc",MODE_PRIVATE);
        showProgress(true);
       // Toast.makeText(LoginActivity.this,"onCreate",Toast.LENGTH_LONG).show();
        verifyStoragePermissions(LoginActivity.this);
        if(sp.getString("name","").length()>0&&sp.getString("token","").length()>0){
            IhancHttpClient.setAuth(sp.getString("token",""));
            IhancHttpClient.get("/index/setting/info", null, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String res=new String(responseBody);
                        try {
                            JSONObject mObject = new JSONObject(res);
                            String[] address = mObject.getString("cadd").split("\\+");
                            Utils.mCompanyInfo = new CompanyInfo(
                                    mObject.getString("cname"),
                                    mObject.getString("ctel"),
                                    address
                            );
                            showProgress(false);
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            LoginActivity.this.finish();
                            IhancHttpClient.get("/index/setting/getAuth", null, new AsyncHttpResponseHandler() {
                                @Override
                                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                    JsonObject JSres = new JsonParser().parse(new String(responseBody)).getAsJsonObject();
                                    Utils.role=JSres.get("role").getAsBoolean();
                                    if(!Utils.role){
                                        Utils.auth=new HashMap<String, Boolean>();
                                        JsonObject auth=JSres.get("role_auth").getAsJsonObject();
                                        Set<String> keys = auth.keySet();
                                        Iterator<String> key_Iterator= keys.iterator();
                                        while (key_Iterator.hasNext()){
                                            String key=key_Iterator.next();
                                            Utils.auth.put(key,auth.get(key).getAsBoolean());
                                        }
                                    }
                                    Log.d("login",Utils.auth.toString());
                                }

                                @Override
                                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                                }
                            });
                        } catch (JSONException e) {
                            Log.d("JSONException", e.toString());
                            showProgress(false);
                            mLoginFormView.setVisibility(View.VISIBLE);
                        }

                }
                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                   // Log.d("Login",new String(responseBody));
                    showProgress(false);
                    Toast.makeText(LoginActivity.this,"不能连接到服务器，请检查网络！",Toast.LENGTH_LONG).show();
                    mLoginFormView.setVisibility(View.VISIBLE);
                    mPrefEditor.putString("token","").commit();
                }
            });


        }else
            {
                showProgress(false);
                mLoginFormView.setVisibility(View.VISIBLE);
            }
        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mCodeButtonView=(Button)findViewById(R.id.codeSendBtn);
        mCode=(EditText)findViewById(R.id.code);
        mEmailView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(!b&&getCodeCount==0){
                    if(timer==null)timer=new Timer();
                    sendCode();
                    getCodeCount++;
                    }
            }
        });

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mCodeButtonView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                sendCode();
            }
        });



    }

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
        if(requestCode==REQUEST_EXTERNAL_STORAGE){
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkVersion();
            } else {
                System.exit(0);
                return;
            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        final String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        String code= mCode.getText().toString();
        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (TextUtils.isEmpty(code)) {
            mCode.setError(getString(R.string.error_field_required));
            focusView = mCode;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            hideKeyboard();
            showProgress(true);
            String input=email+":"+password+":"+code;
            IhancHttpClient.setAuth(Base64.encodeToString(input.getBytes(),0));
            IhancHttpClient.get("/index/index/appLogin",null,new AsyncHttpResponseHandler(){
                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    mCode.requestFocus();
                    showProgress(false);
                    stopTimer();
                    System.out.print(error.getMessage());
                    Log.d("login fail",statusCode+new String(responseBody));
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String res = new String(responseBody);
                    if(res.indexOf("error")>-1){
                        String error=new String("密码或验证码错误！");
                        Toast.makeText(LoginActivity.this,error,Toast.LENGTH_LONG).show();
                    }else {
                        JsonObject JSres = new JsonParser().parse(res).getAsJsonObject();
                        String userName=JSres.get("name").toString();
                        String token=JSres.get("token").toString();
                        mPrefEditor.putString("token",token);
                        mPrefEditor.putString("name",userName);
                        mPrefEditor.commit();
                        Utils.role=JSres.get("role").getAsBoolean();
                        if(!Utils.role){
                            Utils.auth=new HashMap<String, Boolean>();
                            JsonObject auth=JSres.get("role_auth").getAsJsonObject();
                            Set<String> keys = auth.keySet();
                            Iterator<String> key_Iterator= keys.iterator();
                            while (key_Iterator.hasNext()){
                                String key=key_Iterator.next();
                                Utils.auth.put(key,auth.get(key).getAsBoolean());
                            }
                        }
                        Log.d("login",Utils.auth.toString());
                        IhancHttpClient.setAuth(token);
                        IhancHttpClient.get("/index/setting/info", null, new AsyncHttpResponseHandler() {
                                    @Override
                                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                        String res = new String(responseBody);
                                        try {
                                            JSONObject mObject = new JSONObject(res);
                                            String[] address = mObject.getString("cadd").split("\\+");
                                            Utils.mCompanyInfo = new CompanyInfo(
                                                    mObject.getString("cname"),
                                                    mObject.getString("ctel"),
                                                    address
                                            );
                                        } catch (JSONException e) {
                                            Log.d("JSONException", e.toString());
                                        }
                                    }
                                    @Override
                                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                                    }
                                });
                        stopTimer();
                        Intent intent=new Intent(LoginActivity.this,MainActivity.class);
                        startActivity(intent);
                        //ActivityCollector.removeActivity(LoginActivity.this);
                        LoginActivity.this.finish();
                    }
                    mCode.requestFocus();
                    showProgress(false);
                }
            });

          /*  try {
                RSAPublicKey publicKey=RSAEncrypt.loadPublicKeyByStr(pem);
                byte[] cipherData=RSAEncrypt.encrypt(publicKey,input.getBytes());
                String cipher=Base64.encode(cipherData);
                Log.d("RSA",cipher);
            } catch (Exception ex) {
                Log.d("RSA", ex.toString());
            }*/

            //mAuthTask = new UserLoginTask(email, password,code);
            //mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        Pattern p = Pattern.compile("^((18[0-9])|(13[0-9])|(19[^4,\\D])|(15[^4,\\D])|(18[0-9]))\\d{8}$");
       // String tel = "^((1[0-9]))\\d{9}$";
       // p = Pattern.compile(tel);
        Matcher m = p.matcher(email);
        System.out.println(m.matches()+"---");
        return m.matches();
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

           // mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                   // mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
           // mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;
        private final String mCode;
        private  String input;
        UserLoginTask(String email, String password,String code) {
            mEmail = email;
            mPassword = password;
            mCode=code;
            input=mEmail+":"+mPassword+":"+mCode;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            try {

            } catch (Exception ex) {
                Log.d("login", ex.toString());
                return false;
            }

           /* try {
                // Simulate network access.
                Thread.sleep(2000);
                //RSAPublicKey publicKey=RSAEncrypt.loadPublicKeyByStr(pem);
                //byte[] cipherData=RSAEncrypt.encrypt(publicKey,input.getBytes());
               // String cipher=Base64.encode(cipherData);

            } catch (InterruptedException e) {
                return false;
            }*/

          /*  for (String credential : DUMMY_CREDENTIALS) {
                String[] pieces = credential.split(":");
                if (pieces[0].equals(mEmail)) {
                    // Account exists, return true if the password matches.
                    return pieces[1].equals(mPassword);
                }
            }*/

            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    private void updateTime(final Handler handler){
        timer.schedule(new TimerTask() {
            int i=90;
            @Override
            public void run() {
                Message message=new Message();
                message.what=i;
                handler.sendMessage(message);
                Log.d("timer",i+"");
                i--;
            }
        },0,1000);
    };
    private void stopTimer(){
        if(timer!=null){
            timer.cancel();
            timer=null;
        }
        mCodeButtonView.setText("发送验证码");
        mCodeButtonView.setClickable(true);
    }

    private void sendCode() {
        String email = mEmailView.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
        } else {
            RequestParams params = new RequestParams();
            params.put("data", email);
            System.out.print(IhancHttpClient.getAbsoluteUrl("/index/index/getCode"));
            IhancHttpClient.post("/index/index/getCode", params, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                    // called when response HTTP status is "200 OK"
                    String res = new String(response);
                    if (res.indexOf("error")>-1) {
                        mEmailView.setError("输入的手机号码不正确！");
                    } else {
                        mCodeButtonView.setClickable(false);
                        JsonObject JSres = new JsonParser().parse(res).getAsJsonObject();
                        if (JSres.get("result").getAsJsonObject().get("Message").toString().indexOf("OK")>-1) {
                            if(timer!=null) updateTime(handler);
                        }
                    }
                    Log.d("Login on success", res);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                    // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                   // Log.d("Login on error", new String(errorResponse));
                    System.out.println(e.getMessage());
                }
            });    //测试用不发送验证码*/
        }
    }
    //检测设置蓝牙
    public  void blueSerialSetting(){
        mBluetoothAdapter= BluetoothAdapter.getDefaultAdapter();
        if(!mBluetoothAdapter.isEnabled()){
            Intent intent=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent,REQUEST_ENABLE);
        }
    }
    //是否已经打开蓝牙
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_ENABLE){
            if(resultCode!=RESULT_OK){
                ActivityCollector.finishAll();
            }
        }
    }

    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 0:
                    mCodeButtonView.setText("发送验证码");
                    mCodeButtonView.setClickable(true);
                    timer.cancel();
                    timer=null;
                    break;
                default:
                    mCodeButtonView.setText(msg.what+"秒后重发");
                    break;
            }
        };
    };
    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(timer!=null){
            timer.cancel();
            timer=null;
        }
        removeActivity(this);
    }
    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive() && this.getCurrentFocus() != null) {
            if (this.getCurrentFocus().getWindowToken() != null) {
                imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    public void checkVersion(){
        //Toast.makeText(LoginActivity.this,"checkVersion",Toast.LENGTH_LONG).show();
        final Context context=LoginActivity.this;
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            mVersionCode = Double.parseDouble(info.versionName);
            Log.d("checkVersion","haha"+mVersionCode);
           // Toast.makeText(LoginActivity.this,"checkVersion"+mVersionCode,Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
           // Toast.makeText(LoginActivity.this,"checkVersion E",Toast.LENGTH_LONG).show();
            return;
        }

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(IhancHttpClient.HOST_URL+"version.html", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String res=new String(responseBody);
                Log.d("updateApp",res+"");
                try {
                    JSONObject mObject=new JSONObject(res);
                    if(mObject.getDouble("version")>mVersionCode){
                        Log.d("updateApp",mVersionCode+"");
                        url=IhancHttpClient.HOST_URL+mObject.getString("url");
                        showProcessDialog();
                    }
                }catch (JSONException e){e.printStackTrace();}

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(context,"不能连接到服务器，请检查网络！",Toast.LENGTH_LONG).show();
            }
        });
    }
    public  void verifyStoragePermissions(Activity activity) {
        //   Toast.makeText(LoginActivity.this,"verifyStoragePermissions",Toast.LENGTH_LONG).show();
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
         //   Toast.makeText(LoginActivity.this,"verifyStoragePermissions",Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE);
            Log.d("checkVersion","verifyStorage false");
        }else{
            checkVersion();
        }
    }

    public void showProcessDialog(){
        final Context mContext=LoginActivity.this;
        dialog = new ProgressDialog(mContext);
        dialog.setIcon(R.mipmap.ihanc);
        dialog.setTitle("iHanc--瀚盛水产销售行业专用系统");
        dialog.setMessage("APP下载进度：");
        dialog.setCancelable(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setMax(100);
        //dialog.incrementProgressBy(10);
        dialog.setIndeterminate(false);
        final DownloadTask downloadTask = new DownloadTask(
                mContext);
        downloadTask.execute(url);

    }

   class DownloadTask extends AsyncTask<String, Integer, String>{
       private Context context;
       public DownloadTask(Context context) {
           this.context = context;
       }
       protected String doInBackground(String... sUrl) {
           InputStream input = null;
           OutputStream output = null;
           HttpURLConnection connection = null;
           File file = null;
           try {
               URL url = new URL(sUrl[0]);
               connection = (HttpURLConnection) url.openConnection();
               connection.connect();
               // expect HTTP 200 OK, so we don't mistakenly save error
               // report
               // instead of the file
               if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                   return "Server returned HTTP "
                           + connection.getResponseCode() + " "
                           + connection.getResponseMessage();
               }
               // this will be useful to display download percentage
               // might be -1: server did not report the length
               int fileLength = connection.getContentLength();
               if (Environment.getExternalStorageState().equals(
                       Environment.MEDIA_MOUNTED)) {
                   file = new File(Environment.getExternalStorageDirectory(),
                           DOWNLOAD_NAME+".apk");

                   if (!file.exists()) {
                       // 判断父文件夹是否存在
                       if (!file.getParentFile().exists()) {
                           file.getParentFile().mkdirs();
                       }
                   }else{
                       int i=0;
                       while (file.exists()) {
                           i++;
                           file = new File(Environment.getExternalStorageDirectory(),
                                   DOWNLOAD_NAME + i + ".apk");
                       }
                       DOWNLOAD_NAME+=i--;
                   }
               } else {
                   Toast.makeText(context, "sd卡未挂载",
                           Toast.LENGTH_LONG).show();
               }
               input = connection.getInputStream();
               output = new FileOutputStream(file);
               byte data[] = new byte[4096];
               long total = 0;
               int count;
               while ((count = input.read(data)) != -1) {
                   // allow canceling with back button
                   if (isCancelled()) {
                       input.close();
                       return null;
                   }
                   total += count;
                   // publishing the progress....
                   if (fileLength > 0) // only if total length is known
                       publishProgress((int) (total * 100 / fileLength));
                   output.write(data, 0, count);

               }
           } catch (Exception e) {
               Log.d("update",e.toString());
               return e.toString();

           } finally {
               try {
                   if (output != null)
                       output.close();
                   if (input != null)
                       input.close();
               } catch (IOException ignored) {
               }
               if (connection != null)
                   connection.disconnect();
           }
           return null;
       }
       @Override
       protected void onPreExecute() {
           super.onPreExecute();
           // take CPU lock to prevent CPU from going off if the user
           // presses the power button during download
           dialog.show();
       }
       @Override
       protected void onProgressUpdate(Integer... progress) {
           super.onProgressUpdate(progress);
           // if we get here, length is known, now set indeterminate to false
           dialog.setIndeterminate(false);
           dialog.setMax(100);
           dialog.setProgress(progress[0]);
       }
       @Override
       protected void onPostExecute(String result) {
           dialog.dismiss();
           Toast.makeText(context,"下载完成！",Toast.LENGTH_LONG).show();
           Uri uri =null;
           Intent intent = new Intent(Intent.ACTION_VIEW);
           intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
           if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                File file = new File(Environment
                        .getExternalStorageDirectory(), DOWNLOAD_NAME+".apk");
                //Log.d("update")
                uri = FileProvider.getUriForFile(context,"com.example.hx.ihanc.fileProvider",file);
                Log.d("update",uri.getEncodedPath()+file.exists());
                //Toast.makeText(context,uri.toString(),Toast.LENGTH_LONG).show();
               // mCodeButtonView.setText(uri.toString());
               intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
               intent.setDataAndType(uri, "application/vnd.android.package-archive");
               startActivity(intent);
           }else {
                uri = Uri.fromFile(new File(Environment
                       .getExternalStorageDirectory(), DOWNLOAD_NAME+".apk"));
               //mCodeButtonView.setText(uri.toString());
               intent.setDataAndType(uri, "application/vnd.android.package-archive");
               intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
               startActivity(intent);
           }


       }
   }

}

