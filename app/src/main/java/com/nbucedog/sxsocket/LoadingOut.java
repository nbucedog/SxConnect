package com.nbucedog.sxsocket;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.util.Log;

/**
 * Created by cthLlxl on 2017/6/1.
 */

public class LoadingOut implements Runnable{
    private ProgressDialog progressDialog;
    private Logout td;
    boolean timeout = false;

    public LoadingOut(ProgressDialog progressDialog,Logout td){
        this.progressDialog = progressDialog;
        this.td = td;
    }
    public void run(){
        long startMili = System.currentTimeMillis();
        int Maxtime = 6000;
        while (!td.ready){
            long time = System.currentTimeMillis()-startMili;
            int progress = (int)(time*1.6);
            if(progress>Maxtime-380){
                progress = Maxtime-380;
            }
            progressDialog.setProgress(progress);
            if(time>Maxtime){
                timeout = true;
                td.close();
                break;
            }
        }

        progressDialog.setProgress(Maxtime);
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        progressDialog.dismiss();
        progressDialog.cancel();
    }
}
