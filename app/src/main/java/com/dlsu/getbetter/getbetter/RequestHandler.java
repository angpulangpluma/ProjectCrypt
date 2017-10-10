package com.dlsu.getbetter.getbetter;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mikedayupay on 26/02/2016.
 * GetBetter 2016
 */
public class RequestHandler {

    private int bytesAvailable;
    private int totalProgress;

    public String sendPostRequest(String requestURL, HashMap<String, String> postDataParams) {
        URL url;

        StringBuilder sb = new StringBuilder();

        try {
            url = new URL(requestURL);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(30000);
            conn.setConnectTimeout(30000);
            conn.setRequestMethod("POST");
//            conn.setRequestProperty("Content-type", "application/json");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(getPostDataString(postDataParams));

            writer.flush();
            writer.close();
            os.close();
            int responseCode = conn.getResponseCode();

            if(responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                sb = new StringBuilder();
                String response;
                while ((response = br.readLine()) != null) {
                    sb.append(response).append("\n");
                }
            } else {
                Log.d("connection", "Response is " + conn.getResponseCode() + " " + conn.getResponseMessage());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d("sb to string", sb.toString());

        return sb.toString();

    }

    public String sendFileRequest(String requestURL, String filePath) {

        URL url;
        StringBuilder sb = new StringBuilder();
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1024 * 1024;

        File sourceFile = new File(filePath);
        if (!sourceFile.isFile()) {
            Log.e("File", "Source File Does not exist");
            return null;
        }

        try {
            FileInputStream fileInputStream = new FileInputStream(sourceFile);
            url = new URL(requestURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("ENCTYPE", "multipart/form-data");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            conn.setRequestProperty("myFile", filePath);
            DataOutputStream dos = new DataOutputStream(conn.getOutputStream());

            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"myFile\";filename=\"" + filePath + "\"" + lineEnd);
            dos.writeBytes(lineEnd);

            bytesAvailable = fileInputStream.available();
            Log.i("bytes", "Initial .available : " + bytesAvailable);

            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            totalProgress = 0;

            while (bytesRead > 0) {
                dos.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }

            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            int responseCode = conn.getResponseCode();

            fileInputStream.close();
            dos.flush();
            dos.close();

            if(responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                sb = new StringBuilder();
                String response;
                while ((response = br.readLine()) != null) {
                    sb.append(response);
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }


        return sb.toString();
    }

    public String getPostRequest(String requestURL) {

        URL url;

        StringBuilder sb = new StringBuilder();

        try {
            url = new URL(requestURL);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(30000);
            conn.setConnectTimeout(30000);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-type", "application/json");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            int responseCode = conn.getResponseCode();

            if(responseCode == HttpURLConnection.HTTP_OK) {

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"), 8);

                sb = new StringBuilder();

                String line;

                while((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            }else {
                Log.d("connection", "response: " + conn.getResponseCode() + " " + conn.getResponseMessage());
            }

        }catch (Exception e) {
            e.printStackTrace();
        }

        return sb.toString();
    }

    public void getAudioFile(String path) {

        int count;

        try {
            URL url1 = new URL("http://www.stephaniequinn.com/Music/Commercial%20DEMO%20-%2015.mp3");
            URLConnection conn = url1.openConnection();


            InputStream in = conn.getInputStream();

            OutputStream out = new FileOutputStream(new File(path));

            byte[] buffer = new byte[4096];

            int len;

            while((len = in.read(buffer)) != 1) {
                out.write(buffer, 0, len);
            }

            out.flush();
            out.close();

//            HttpURLConnection conn = (HttpURLConnection) url1.openConnection();
//
//            conn.setReadTimeout(30000);
//            conn.setConnectTimeout(30000);
//            conn.setRequestMethod("GET");
////            conn.setRequestProperty("Connection", "Keep-Alive");
////            conn.setRequestProperty("ENCTYPE", "multipart/form-data");
//            conn.setDoInput(true);
////            conn.connect();

//            int responseCode = conn.getResponseCode();
//            Log.d("response", responseCode + "");


//            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//            InputStream inputStream = new BufferedInputStream(conn.getInputStream(), 8192);

//            FileOutputStream fos = new FileOutputStream(new File(path));
//            byte data[] = new byte[1024];

//            int data;
//            while((data = br.read()) != -1) {
//                fos.write(data);
//            }
//
//            while((count = inputStream.read(data)) != -1) {
//                fos.write(data, 0, count);
//                count = inputStream.read(data);
//            }
//
////            br.close();
//            inputStream.close();
//            fos.flush();
//            fos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException{
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (Map.Entry<String, String> entry : params.entrySet()) {
            if(first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }
        return result.toString();

    }

    public int getBytesAvailable() {
        return bytesAvailable;
    }

    public int getTotalProgress() {
        return totalProgress;
    }
}
