package com.nbucedog.sxsocket;


import android.os.Looper;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import android.view.MenuItem;

import com.tencent.smtt.sdk.CookieManager;
import com.tencent.smtt.sdk.CookieSyncManager;

public class setting extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        //加入返回按钮
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setCustomView(R.layout.title_setting);
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        final String filePathName_WAN = getApplicationContext().getFilesDir().getAbsolutePath()+"/wanipadr.cfg";
        final String filePathName_router = getApplicationContext().getFilesDir().getAbsolutePath()+"/lanipadr.cfg";
        //转为数组，后面会用
        String filenameArr[] = new String[2];
        filenameArr[0] = filePathName_WAN;
        filenameArr[1] = filePathName_router;

        String ip_WAN = readFile(filePathName_WAN);
        String ip_LAN = readFile(filePathName_router);
        final EditText ETipWAN = (EditText)findViewById(R.id.et_ipWAN);//寻找WANip地址编辑栏
        final EditText ETiprouter = (EditText)findViewById(R.id.et_router);//寻找LANip地址编辑栏
        ETipWAN.setText(ip_WAN);
        ETiprouter.setText(ip_LAN);

        final Button btn_clr = (Button)findViewById(R.id.btn_clr_setting);
        btn_clr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //btn_clr.setClickable(false);
                Toast.makeText(setting.this,"清理中...",Toast.LENGTH_SHORT).show();
                new Thread(){
                    @Override
                    public void run(){
                        CheckBox ckCache = (CheckBox)findViewById(R.id.ckbox_cache);
                        CheckBox ckCook = (CheckBox)findViewById(R.id.ckbox_cook);
                        if(ckCache.isChecked()){
                            //清空Caches
                            getCacheDir().delete();
                        }
                        if(ckCook.isChecked()){
                            //清空所有Cookies
                            CookieSyncManager.createInstance(setting.this);  //Create a singleton CookieSyncManager within a context
                            CookieManager cookieManager = CookieManager.getInstance(); // the singleton CookieManager instance
                            cookieManager.removeAllCookie();// Removes all cookies.
                            CookieSyncManager.getInstance().sync(); // forces sync manager to sync now
                        }
                        Looper.prepare();
                        Toast.makeText(setting.this,"清理完成",Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                }.start();
            }
        });

        final Button btn_WANpre = (Button)findViewById(R.id.btn_WANpre_setting);
        btn_WANpre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean b = writeFile(filePathName_WAN,ETipWAN.getText().toString());
                if(!b){
                    Toast.makeText(setting.this,"保存失败",Toast.LENGTH_SHORT).show();
                }
            }
        });
        final Button btn_LANpre = (Button)findViewById(R.id.btn_LANpre_setting);
        btn_LANpre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean b = writeFile(filePathName_router,ETiprouter.getText().toString());
                if(!b){
                    Toast.makeText(setting.this,"保存失败",Toast.LENGTH_SHORT).show();
                }
            }
        });

        //大保存按钮
        final Button btn_preserve = (Button)findViewById(R.id.btn_preserve);
        btn_preserve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText ETipWAN = (EditText)findViewById(R.id.et_ipWAN);//寻找ip地址编辑栏
                String ipaddress = ETipWAN.getText().toString();
                ipaddress = ipaddress.trim();//去掉首尾空格
                EditText ETrouter = (EditText)findViewById(R.id.et_router);//寻找ip地址编辑栏
                String ripaddress = ETrouter.getText().toString();
                ripaddress = ripaddress.trim();//去掉首尾空格
                //ipaddress.replaceAll(" ","");//去掉所有空格
                String filenameArr[] = new String[2];
                filenameArr[0] = filePathName_WAN;
                filenameArr[1] = filePathName_router;
                String dataArr[] = new String[2];
                dataArr[0] = ipaddress;
                dataArr[1] = ripaddress;
                //将IP写入缓存
                for (int i=0;i<2;i++){
                    boolean writebool = writeFile(filenameArr[i],dataArr[i]);
                    if(!writebool){
                        int j=i+1;
                        Toast.makeText(setting.this,"第"+j+"栏"+"保存失败",Toast.LENGTH_SHORT).show();
                        break;
                    }
                    setting.this.finish();
                }
            }
        });

        //大取消按钮
        final Button btn_cancel = (Button)findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setting.this.finish();
            }
        });

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setting.this.finish(); // back button
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected String readFile(String filename){
        String filedata="";
        Log.d("DEMOLOG", filename);
        /*读取文件缓存值*/
        File file = new File(filename);
        if(file.exists()){
            Log.d("DEMOLOG", "file exists");
            try {
                InputStream in = new FileInputStream(filename);
                Log.d("DEMOLOG", "onCreate:I am In");
                InputStreamReader reader = new InputStreamReader(in);
                BufferedReader bufrd = new BufferedReader(reader);
                try {
                    filedata = bufrd.readLine();
                    bufrd.close();
                    reader.close();
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return filedata;
    }
    protected boolean writeFile(String filename,String data){
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
            byte[] b_data = data.getBytes();
            os.write(b_data);
            os.close();
            Toast.makeText(setting.this,"保存成功",Toast.LENGTH_SHORT).show();
            //setting.this.finish();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
