package com.baidu.speech.serviceapi;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.DatatypeConverter;

import org.json.JSONObject;

public class Sample {

    private  final String serverURL = "http://vop.baidu.com/server_api";
    private  String token = "";
    //private static final String testFileName = "1.wav";
    //put your own params here
    private  final String apiKey = "i6qwCCocxKwGgnAGEtGwMPr1";
    private  final String secretKey = "2365d35ae0783e8d8b5fad2966f47dc9";
    private  final String cuid = "wsf19960733";

   
public Sample()
{
        try {
            getToken();
        } catch (Exception ex) {
            Logger.getLogger(Sample.class.getName()).log(Level.SEVERE, null, ex);
        }
}
    private void getToken() throws Exception {
        String getTokenURL = "https://openapi.baidu.com/oauth/2.0/token?grant_type=client_credentials" + 
            "&client_id=" + apiKey + "&client_secret=" + secretKey;
        HttpURLConnection conn = (HttpURLConnection) new URL(getTokenURL).openConnection();
        token = new JSONObject(printResponse(conn)).getString("access_token");
    }

    public String method1(String testFileName) throws Exception {
        File pcmFile = new File(testFileName);
        HttpURLConnection conn = (HttpURLConnection) new URL(serverURL).openConnection();
        // construct params
        JSONObject params = new JSONObject();
        params.put("format", "wav");
        params.put("rate", 8000);
        params.put("channel", "1");
        params.put("token", token);
        params.put("cuid", cuid);
        params.put("len", pcmFile.length());
        params.put("speech", DatatypeConverter.printBase64Binary(loadFile(pcmFile)));

        // add request header
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");

        conn.setDoInput(true);
        conn.setDoOutput(true);

        // send request
        DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
        wr.writeBytes(params.toString());
        wr.flush();
        wr.close();

        String a = printResponse(conn);   
        int start = a.indexOf("[");
        int end = a.indexOf("]");
        if(start == -1 || end == -1)
        {
            return "解析错误";
        }
        else
        {
        return a.substring(start+2, end-1);
        }
    }

    /*public void method2() throws Exception {
        File pcmFile = new File(testFileName);
        HttpURLConnection conn = (HttpURLConnection) new URL(serverURL
                + "?cuid=" + cuid + "&token=" + token).openConnection();

        // add request header
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "audio/wav; rate=8000");

        conn.setDoInput(true);
        conn.setDoOutput(true);

        // send request
        DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
        wr.write(loadFile(pcmFile));
        wr.flush();
        wr.close();

        printResponse(conn);
    }*/

    private  String printResponse(HttpURLConnection conn) throws Exception {
        if (conn.getResponseCode() != 200) {
            // request error
            return "";
        }
        InputStream is = conn.getInputStream();
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));
        String line;
        StringBuffer response = new StringBuffer();
        while ((line = rd.readLine()) != null) {
            response.append(line);
            response.append('\r');
        }
        rd.close();
        System.out.println(new JSONObject(response.toString()).toString(4));
        return response.toString();
    }

    private  byte[] loadFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);

        long length = file.length();
        byte[] bytes = new byte[(int) length];

        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
                && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
            offset += numRead;
        }

        if (offset < bytes.length) {
            is.close();
            throw new IOException("Could not completely read file " + file.getName());
        }

        is.close();
        return bytes;
    }
}
