package com.nbucedog.sxsocket;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URLEncoder;

public class Logout implements Runnable{
    private int port;
    private String host;
    private Socket socket;

    String LogoutURL;
    String uuid;
    String Data;
    private String userip = "";

    public boolean ready = false;
    public int ResponseCode = 0;

    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    public Logout(String host, int port,String LogoutURL,String uuid,String userip) {
        socket = new Socket();
        this.host = host;
        this.port = port;
        this.LogoutURL = LogoutURL;
        this.uuid = uuid;
        this.userip = userip;
    }
    public void run(){
        try {
            sendPost();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void close(){
        try {
            socket.close();
            Log.d("DEMOLOG", "logout close success");
        } catch (IOException e) {
            Log.d("DEMOLOG", "logout close fail");
            e.printStackTrace();
        }
    }

    public void sendPost() throws IOException
    {
        String path = LogoutURL;
        Log.d("DEMOLOG", path);

        String muldata = URLEncoder.encode("uuid", "utf-8")+"="+URLEncoder.encode(uuid, "utf-8")
                +"&"+
                URLEncoder.encode("userip", "utf-8")+"="+URLEncoder.encode(userip, "utf-8");
        SocketAddress dest = new InetSocketAddress(this.host, this.port);
        Log.d("DEMOLOG", "sendPost: connect1");
        socket.connect(dest);
        Log.d("DEMOLOG", "sendPost: connect2");
        OutputStreamWriter streamWriter = new OutputStreamWriter(socket.getOutputStream(), "utf-8");
        bufferedWriter = new BufferedWriter(streamWriter);
        BufferedInputStream streamReader = new BufferedInputStream(socket.getInputStream());
        bufferedReader = new BufferedReader(new InputStreamReader(streamReader, "utf-8"));

        bufferedWriter.write("POST " + path + " HTTP/1.1\r\n");
        bufferedWriter.write("Content-Type: application/x-www-form-urlencoded\r\n");
        bufferedWriter.write("User-Agent: China Telecom Client\r\n");
        bufferedWriter.write("Host: " + this.host + ":" + this.port + "\r\n");
        bufferedWriter.write("Content-Length: " + muldata.length() + "\r\n");
        bufferedWriter.write("Connection: Keep-Alive\r\n");
        bufferedWriter.write("\r\n");
        bufferedWriter.write(muldata);
        bufferedWriter.flush();

        String line = null;
        int datalen = -1;
        while((line = bufferedReader.readLine())!= null)
        {
            Log.d("DEMOLOG",line);
            if(line.equals("")){
                break;
            }
            int indexp = line.indexOf("Content-Length");
            if(indexp != -1){
                datalen = getContentLen(line);
                Log.d("DEMOLOG", "Content-Length:"+datalen);
            }
        }
        if(datalen > 0){
            Log.d("DEMOLOG", "I am Data1");
            char[] dataget = new char[datalen];
            for(int i=0;i<datalen;i++){
                int a = bufferedReader.read();
                dataget[i] = (char)a;
            }
            String datagetstr = String.valueOf(dataget);
            Log.d("DEMOLOG", datagetstr);
            ResponseCode = getResponseCode(datagetstr);
            Data = getData(datagetstr);
        }
        ready = true;
        bufferedReader.close();
        bufferedWriter.close();
        socket.close();
    }
    int getContentLen(String line){
        int indexp;
        String output;
        int len;
        if((indexp=line.indexOf(": ")) != -1){
            output = line.substring(indexp+2);
        }
        else if((indexp=line.indexOf(":")) != -1){
            output = line.substring(indexp+1);
        }
        else{
            System.out.println("getContentLen Error!");
            output = "0";
        }
        System.out.println(output);
        len = Integer.parseInt(output);
        System.out.println(len);
        return len;
    }
    String getData(String datagetstr){
        String output="";
        int indexStart,indexEnd;
        indexStart = datagetstr.indexOf("<Data>");
        indexEnd = datagetstr.indexOf("</Data>");
        if(indexStart != -1){
            output = datagetstr.substring(indexStart+6,indexEnd);
        }
        Log.d("DOMOLOG", output);
        return output;
    }
    int getResponseCode(String datagetstr){
        int output=0;
        int indexStart,indexEnd;
        indexStart = datagetstr.indexOf("<ResponseCode>");
        indexEnd = datagetstr.indexOf("</ResponseCode>");
        if(indexStart != -1){
            String outputstr = datagetstr.substring(indexStart+14,indexEnd);
            Log.d("DEMOLOG", outputstr);
            output = Integer.parseInt(outputstr);
        }
        return output;
    }

}