package com.nbucedog.sxsocket;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URLEncoder;

public class SXZBsocket implements Runnable{
	private int port;
	private String host;
	private Socket socket;

	private String userip = "";
	private String username = "";
	private String password = "";
	private String currentdir;

	public boolean ready = false;
    public int ResponseCode = 0;
	public String LogoffURL = "";

	private BufferedReader bufferedReader;
	private BufferedWriter bufferedWriter;
	public SXZBsocket(String host, int port,String userip,String username,String password,String currentdir) {
		socket = new Socket();
		this.host = host;
		this.port = port;
		this.userip = userip;
		this.username = username;
		this.password = password;
		this.currentdir = currentdir;
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
            Log.d("DEMOLOG", "SZBsocket close success");
        } catch (IOException e) {
			e.printStackTrace();
            Log.d("DEMOLOG", "SZBsocket close fail");
		}
	}
	public void sendGet() throws IOException
	{
		String path = "/zhigang/getDemo.php";
		SocketAddress dest = new InetSocketAddress(this.host, this.port);
		socket.connect(dest);
		OutputStreamWriter streamWriter = new OutputStreamWriter(socket.getOutputStream());
		bufferedWriter = new BufferedWriter(streamWriter);
		
		bufferedWriter.write("GET " + path + " HTTP/1.1\r\n");
		bufferedWriter.write("Host: " + this.host + "\r\n");
		bufferedWriter.write("\r\n");
		bufferedWriter.flush();
		
		BufferedInputStream streamReader = new BufferedInputStream(socket.getInputStream());
		bufferedReader = new BufferedReader(new InputStreamReader(streamReader, "utf-8"));
		String line = null;
		while((line = bufferedReader.readLine())!= null)
		{
			System.out.println(line);
		}
		System.out.println("while id end");
		socket.close();
		bufferedReader.close();
		bufferedWriter.close();
	}
	
	public void sendPost() throws IOException
	{
		String path = "/showlogin.do";

		String data = URLEncoder.encode("wlanuserip", "utf-8") + "=" + URLEncoder.encode(userip, "utf-8");
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
		bufferedWriter.write("Content-Length: " + data.length() + "\r\n");
		//bufferedWriter.write("Expect: 100-continue\r\n");
		bufferedWriter.write("Connection: Keep-Alive\r\n");
		bufferedWriter.write("\r\n");
		bufferedWriter.write(data);
		bufferedWriter.flush();

		String line = null;
		int datalen = -1;
        String uuid = "";
        String loginURL_path = "";
		while((line = bufferedReader.readLine())!= null)
		{
			if(line.equals("")){
				break;
			}
			int indexp = line.indexOf("Content-Length");
			if(indexp != -1){
				datalen = getContentLen(line);
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
            loginURL_path = getLoginURL(datagetstr);
            uuid = getUuid(datagetstr);
		}


		String muldata = URLEncoder.encode("uuid", "utf-8")+"="+URLEncoder.encode(uuid, "utf-8")
		+"&"+
		URLEncoder.encode("userip", "utf-8")+"="+URLEncoder.encode(userip, "utf-8")
		+"&"+
		URLEncoder.encode("username", "utf-8")+"="+URLEncoder.encode(username, "utf-8")
		+"&"+
		URLEncoder.encode("password", "utf-8")+"="+URLEncoder.encode(password, "utf-8")
		+"&"+
		URLEncoder.encode("ratingtype", "utf-8")+"="+URLEncoder.encode("1", "utf-8");

		bufferedWriter.write("POST " + loginURL_path + " HTTP/1.1\r\n");
		bufferedWriter.write("Content-Type: application/x-www-form-urlencoded\r\n");
		bufferedWriter.write("User-Agent: China Telecom Client\r\n");
		bufferedWriter.write("Host: " + this.host + ":" + this.port + "\r\n");
		bufferedWriter.write("Content-Length: " + muldata.length() + "\r\n");
		bufferedWriter.write("Connection: Keep-Alive\r\n");
		bufferedWriter.write("\r\n");
		bufferedWriter.write(muldata);
		bufferedWriter.flush();

        datalen = -1;
        while((line = bufferedReader.readLine())!= null)
        {
            if(line.equals("")){
                break;
            }
            int indexp = line.indexOf("Content-Length");
            if(indexp != -1){
                datalen = getContentLen(line);
            }
        }
        if(datalen > 0){
            Log.d("DEMOLOG", "I am Data2");
            char[] dataget = new char[datalen];
            for(int i=0;i<datalen;i++){
                int a = bufferedReader.read();
                dataget[i] = (char)a;
            }
            String datagetstr = String.valueOf(dataget);
            ResponseCode = getResponseCode(datagetstr);
			LogoffURL = getLogoffURL(datagetstr);
			uuid = getUuid(datagetstr);
			if(ResponseCode==200){
				writeip(userip);
				writeUuid(LogoffURL,uuid);
			}
            Log.d("DEMOLOG", datagetstr);
		}
		ready = true;

		bufferedReader.close();
		bufferedWriter.close();
		socket.close();
	}

	//multi Function
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
	String getLoginURL(String datagetstr){
		String output;
		int indexStart,indexEnd;
		indexStart = datagetstr.indexOf("<LoginURL>");
		indexEnd = datagetstr.indexOf("</LoginURL>");
		if(indexStart != -1){
			output = datagetstr.substring(indexStart+10,indexEnd);
			int indexp;
			if((indexp=output.indexOf("://")) != -1){
				output = output.substring(indexp+3);
			}
			if((indexp=output.indexOf("/")) != -1){
				output = output.substring(indexp);
			}
		}
		else{
			output = "";
		}
		Log.d("DEMOLOG", output);
		return output;
	}
	String getLogoffURL(String datagetstr){
		String output;
		int indexStart,indexEnd;
		indexStart = datagetstr.indexOf("<LogoffURL>");
		indexEnd = datagetstr.indexOf("</LogoffURL>");
		if(indexStart != -1){
			output = datagetstr.substring(indexStart+11,indexEnd);
			int indexp;
			if((indexp=output.indexOf("://")) != -1){
				output = output.substring(indexp+3);
			}
			if((indexp=output.indexOf("/")) != -1){
				output = output.substring(indexp);
			}
		}
		else{
			output = "";
		}
		Log.d("DEMOLOG", output);
		return output;
	}
	String getUuid(String datagetstr){
		String output="";
		int indexStart,indexEnd;
		indexStart = datagetstr.indexOf("<Uuid>");
		indexEnd = datagetstr.indexOf("</Uuid>");
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
    void writeUuid(String logoffURL,String uuid){
        if(logoffURL.equals(null)||logoffURL.equals("")){
            logoffURL = "nothing";
            Log.d("DEMOLOG", "writeUuid: logoffURL nothing");
        }
        if(uuid.equals(null)||uuid.equals("")){
            uuid = "nothing";
            Log.d("DEMOLOG", "writeUuid: uuid nothing");
        }
		String ufilename = currentdir + "/uuid.cfg";
		File file = new File(ufilename);
		if(!file.exists()){
			try {
				file.createNewFile();
				Log.d("DEMOLOG", "uFile creat success");
			} catch (IOException e) {
				e.printStackTrace();
				Log.d("DEMOLOG", e.toString());
			}
		}
		try {
			FileOutputStream os = new FileOutputStream(file);
			byte[] b_logoffURL = (logoffURL+"\n").getBytes();
			byte[] b_uuid = uuid.getBytes();

			os.write(b_logoffURL);
			os.write(b_uuid);
			os.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	void writeip(String ip){
		if(ip.equals("")||ip.equals(null)){
			ip = "nothing";
		}
		String ifilename = currentdir + "/ip.cfg";
		File file = new File(ifilename);
		if(!file.exists()){
			try {
				file.createNewFile();
				Log.d("DEMOLOG", "iFile creat success");
			} catch (IOException e) {
				e.printStackTrace();
				Log.d("DEMOLOG", e.toString());
			}
		}
		try {
			FileOutputStream os = new FileOutputStream(file);
			byte[] b_ip = ip.getBytes();
			os.write(b_ip);
			os.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}