package com.nbucedog.sxsocket;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.tencent.smtt.sdk.QbSdk;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //初始化X5内核
        QbSdk.initX5Environment(this, new QbSdk.PreInitCallback() {
            @Override
            public void onCoreInitFinished() {
                //X5内核初始化完成回调接口，此接口回调并表示已经加载了X5，有可能特殊情况下X5内核加载失败，切换到系统内核。
            }
            @Override
            public void onViewInitFinished(boolean b) {
                //初始化完成，true启动X5，false切换到系统内核
                Log.e("DEMOLOG", "加载是否成功"+b);
                if(b){
                    Toast.makeText(MainActivity.this,"X5浏览器加载成功",Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(MainActivity.this,"X5加载失败，已切换至系统浏览器",Toast.LENGTH_SHORT).show();
                }
            }
        });

        setContentView(R.layout.activity_main);
        final EditText ETusername = (EditText)findViewById(R.id.username);
        final EditText ETpassword = (EditText)findViewById(R.id.password);
        final EditText ETip = (EditText)findViewById(R.id.ipadress);

        final Handler myHandler = new Handler(){
            @Override
            public void handleMessage(Message msg){
                super.handleMessage(msg);
                Bundle bundle = msg.getData();
                String data = bundle.getString("data");
                Log.d("DEMOLOG", "handleMessage: 1");
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("提示");
                builder.setPositiveButton("确定",null);
                Log.d("DEMOLOG", data);
                switch(msg.what){
                    case 0x01:
                        builder.setMessage(data);
                        builder.show();
                        Log.d("DEMOLOG", "handleMessage: 2");
                        break;
                    default:
                        break;
                }
            }
        };

        //读取用户名
        final String filename = getApplicationContext().getFilesDir().getAbsolutePath()+"/userdat.cfg";
        Log.d("DEMOLOG", filename);
        File file = new File(filename);
        if(file.exists()){
            Log.d("DEMOLOG", "file exists");
            try {
                InputStream in = new FileInputStream(filename);
                Log.d("DEMOLOG", "onCreate:I am In");
                InputStreamReader reader = new InputStreamReader(in);
                BufferedReader bufrd = new BufferedReader(reader);
                try {
                    String username = bufrd.readLine();
                    ETusername.setText(username);
                    String password = bufrd.readLine();
                    ETpassword.setText(password);
                    String ipaddress = bufrd.readLine();
                    ETip.setText(ipaddress);
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        //读取IP网段
        final String filename_ip = getApplicationContext().getFilesDir().getAbsolutePath()+"/wanipadr.cfg";
        Log.d("DEMOLOG", filename_ip);
        File file_ip = new File(filename_ip);
        if(file_ip.exists()){
            Log.d("DEMOLOG", "file_ip exists");
            try {
                InputStream in_ip = new FileInputStream(filename_ip);
                Log.d("DEMOLOG", "onCreate:IP I am In");
                InputStreamReader reader = new InputStreamReader(in_ip);
                BufferedReader bufrd = new BufferedReader(reader);
                try {
                    String ip = bufrd.readLine();
                    //EditText ETipadr = (EditText)findViewById(R.id.ipadress);//寻找ip地址编辑栏
                    if(ip==null){
                        Log.d("DEMOLOG", "我是空白");
                    }
                    else{
                        Log.d("DEMOLOG", "我不是空白");
                        ETip.setText(ip);
                    }
                    in_ip.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        final Button btn_login = (Button)findViewById(R.id.btn_login);
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String username = ETusername.getText().toString();username = username.trim();
                String password = ETpassword.getText().toString();password = password.trim();
                String ipaddress = ETip.getText().toString();ipaddress = ipaddress.replace(" ","");

                String currentdir = getApplicationContext().getFilesDir().getAbsolutePath();
                final SXZBsocket td = new SXZBsocket("115.239.134.163",8080,ipaddress,username,password,currentdir);
                new Thread(td).start();
                final ProgressDialog dialog = new ProgressDialog(MainActivity.this);
                dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                dialog.setProgressNumberFormat("");
                dialog.setProgressPercentFormat(null);
                dialog.setCancelable(false);
                dialog.setCanceledOnTouchOutside(false);
                dialog.setTitle("登录服务器");
                dialog.setMax(5000);
                dialog.setMessage("连接中...");
                dialog.show();
                Loading ld = new Loading(dialog,td);
                new Thread(ld).start();

                //写入数据
                File file = new File(filename);
                if(!file.exists()){
                    try {
                        file.createNewFile();
                        Log.d("DEMOLOG", "File creat success");
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.d("DEMOLOG", e.toString());
                    }
                }
                try {
                    FileOutputStream os = new FileOutputStream(file);
                    byte[] b_username = (username+"\n").getBytes();
                    byte[] b_password = (password+"\n").getBytes();
                    byte[] b_ipaddress = ipaddress.getBytes();
                    os.write(b_username);
                    os.write(b_password);
                    os.write(b_ipaddress);
                    os.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String result;
                        boolean timeout=false;
                        long startMili = System.currentTimeMillis();
                        while (!td.ready){
                            int Maxtime = 5000;
                            long time = System.currentTimeMillis()-startMili;
                            if(time>Maxtime){
                                timeout = true;
                                break;
                            }
                        }
                        if(td.ResponseCode==200){
                            result = "登录成功";
                        }
                        else if(timeout){
                            result = "连接超时！请检查网络...";
                        }
                        else {
                            result = "登录失败！错误编号："+td.ResponseCode;
                        }
                        Log.d("DEMOLOG", result);
                        Bundle bundle = new Bundle();
                        bundle.putString("data",result);
                        Message message = new Message();
                        message.what = 0x01;
                        message.setData(bundle);
                        myHandler.sendMessage(message);
                    }
                }).start();
            }
        });
        final Button btn_logout = (Button)findViewById(R.id.btn_logout);
        btn_logout.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                String currentdir = getApplicationContext().getFilesDir().getAbsolutePath();
                String ipaddress = ETip.getText().toString();
                ipaddress.replace(" ","");
                if(ipaddress.equals("")){
                    ipaddress = getIpFromFile(currentdir+"/ip.cfg");
                    ipaddress.replace(" ","");
                }
                Log.d("DEMOLOG", "ipaddress:"+ipaddress);
                String[] input = getUuidFromFile(currentdir+"/uuid.cfg");
                String logoutURL = input[0];
                String uuid = input[1];
                final Logout td = new Logout("115.239.134.163",8080,logoutURL,uuid,ipaddress);
                new Thread(td).start();
                final ProgressDialog dialog = new ProgressDialog(MainActivity.this);
                dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                dialog.setProgressNumberFormat("");
                dialog.setProgressPercentFormat(null);
                dialog.setCancelable(false);
                dialog.setCanceledOnTouchOutside(false);
                dialog.setTitle("注销连接");
                dialog.setMax(6000);
                dialog.setMessage("注销中...");
                dialog.show();
                LoadingOut ld = new LoadingOut(dialog,td);
                new Thread(ld).start();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String result;
                        boolean timeout=false;
                        long startMili = System.currentTimeMillis();
                        while (!td.ready){
                            int Maxtime = 6000;
                            long time = System.currentTimeMillis()-startMili;
                            if(time>Maxtime){
                                timeout = true;
                                break;
                            }
                        }
                        if(td.ResponseCode==150){
                            result = "注销成功";
                        }
                        else if(timeout){
                            result = "连接超时！请检查网络...";
                        }
                        else {
                            result = "注销失败！错误编号："+td.ResponseCode;
                        }
                        Log.d("DEMOLOG", result);
                        Bundle bundle = new Bundle();
                        bundle.putString("data",result);
                        Message message = new Message();
                        message.what = 0x01;
                        message.setData(bundle);
                        myHandler.sendMessage(message);
                    }
                }).start();
            }
        });
        //
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main,menu);
        return true;
    }

    @Override //定义菜单响应事件
    public boolean onOptionsItemSelected(MenuItem item){
        Intent mainIntent;
        switch (item.getItemId()){
            case R.id.router_item:
                mainIntent = new Intent(MainActivity.this, RouterActivity.class);
                MainActivity.this.startActivity(mainIntent);
                break;
            case R.id.setting_item:
                mainIntent = new Intent(MainActivity.this, setting.class);
                MainActivity.this.startActivity(mainIntent);
                break;
            case R.id.help_item:
                mainIntent = new Intent(MainActivity.this, HelpActivity.class);
                MainActivity.this.startActivity(mainIntent);
                break;
            case R.id.about_item:
                mainIntent = new Intent(MainActivity.this, AboutActivity.class);
                MainActivity.this.startActivity(mainIntent);
                break;
            case R.id.exit_item:
                MainActivity.this.finish();
                System.exit(0);
                break;
            default:
                break;
        }
        return true;
    }
    private String getIpFromFile(String ifilename){
        File ifile = new File(ifilename);
        String ip = "";
        if(ifile.exists()){
            Log.d("DEMOLOG", "ifile exists");
            try {
                InputStream in = new FileInputStream(ifilename);
                InputStreamReader reader = new InputStreamReader(in);
                BufferedReader bufrd = new BufferedReader(reader);
                try {
                    ip = bufrd.readLine();
                    Log.d("DEMOLOG", ip);
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return ip;
    }
    private String[] getUuidFromFile(String ufilename){
        File ufile = new File(ufilename);
        String[] output = new String[2];
        output[0] = "";
        output[1] = "";
        if(ufile.exists()){
            Log.d("DEMOLOG", "ufile exists");
            try {
                InputStream in = new FileInputStream(ufilename);
                InputStreamReader reader = new InputStreamReader(in);
                BufferedReader bufrd = new BufferedReader(reader);
                try {
                    output[0] = bufrd.readLine();//logoutURL
                    Log.d("DEMOLOG", "logoffURL:"+output[0]);
                    output[1] = bufrd.readLine();//uuid
                    Log.d("DEMOLOG", "uuid:"+output[1]);
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return output;
    }
}
