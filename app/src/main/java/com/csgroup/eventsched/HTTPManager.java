package com.csgroup.eventsched;

import android.net.Uri;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;

/**
 * Created by pc on 8/16/2015.
 */
public class HTTPManager {

    // VirtualBox > File > Properties > Network > VirtualBox Host-Only Network Adapter > Modify > Here you will see the IP address to use
    private final String BASE_URL = "http://192.168.56.1/eventsched/v1/";
    private final String METHOD_POST = "POST";
    private final String METHOD_GET = "GET";

    private final String LOG_TAG = HTTPManager.class.getSimpleName();

    public String post(String route, HashMap<String, String> header,
                       HashMap<String, String> payload) {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // contains the raw JSON response string
        String resultJsonStr = null;

        try {

            // construct URL
            Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                    .appendPath(route)
                    .build();
            URL finalUrl = new URL(builtUri.toString());
            Log.v(LOG_TAG, "Built URI: " + builtUri.toString());

            // open connection
            urlConnection = (HttpURLConnection) finalUrl.openConnection();
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(15000);
            urlConnection.setRequestMethod(METHOD_POST);
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);

            // set request properties
            if (header != null) {


                for (String key : header.keySet()) {
                    urlConnection.setRequestProperty(key, header.get(key));
                }
            }

            Log.v(LOG_TAG, "Request Properties: "
                    + urlConnection.getRequestProperties().toString());


            // set payload
            OutputStream os = urlConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));

            String payloadStr = getQuery(payload);
            Log.v(LOG_TAG, "Payload String: " + payloadStr);

            writer.write(payloadStr);
            writer.flush();
            writer.close();
            os.close();

            // connect
            urlConnection.connect();
            int responseCode = urlConnection.getResponseCode();

            // read the input stream into a string
            InputStream inputStream;

            // if response code is OK or CREATED, open the input stream
            if (responseCode == 200 || responseCode == 201) {
                // throws exception if response code != 200 or 201
                inputStream = urlConnection.getInputStream();
            }

            // otherwise open the error stream
            else {
                inputStream = urlConnection.getErrorStream();
            }

            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // nothing to do
                return null;
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;

            // reading lines into buffer
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty, no point in parsing
                return null;
            }

            resultJsonStr = buffer.toString();
            Log.v(LOG_TAG, "Response JSON String: " + resultJsonStr);


        } catch (IOException e) {
            Log.e(LOG_TAG, "Error: " + e.getMessage(), e);
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Error", e);
                }
            }
        }

        return resultJsonStr;
    }

    public String get(String route, HashMap<String, String> header,
                      HashMap<String, String> queryParamsMap) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // contains the raw JSON response string
        String resultJsonStr = null;

        try {

            // construct URL with query string
            Uri.Builder uriBuilder =  Uri.parse(BASE_URL).buildUpon()
                    .appendPath(route);

            if (queryParamsMap != null) {
                for (String key : queryParamsMap.keySet()) {
                    uriBuilder.appendQueryParameter(key, queryParamsMap.get(key));
                }
            }
            Uri builtUri = uriBuilder.build();

            URL finalUrl = new URL(builtUri.toString());
            Log.v(LOG_TAG, "Built URI: " + builtUri.toString());

            // open connection
            urlConnection = (HttpURLConnection) finalUrl.openConnection();
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(15000);
            urlConnection.setRequestMethod(METHOD_GET);
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(false);

            // set request properties
            if (header != null) {
                for (String key : header.keySet()) {
                    urlConnection.setRequestProperty(key, header.get(key));
                }
            }

            Log.v(LOG_TAG, "Request Properties: "
                    + urlConnection.getRequestProperties().toString());


            // connect
            urlConnection.connect();
            int responseCode = urlConnection.getResponseCode();

            // read the input stream into a string
            InputStream inputStream;

            // if response code is ok, open the input stream
            if (responseCode == 200) {
                // throws exception if response code != 200
                inputStream = urlConnection.getInputStream();
            }

            // otherwise open the error stream
            else {
                inputStream = urlConnection.getErrorStream();
            }

            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // nothing to do
                return null;
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;

            // reading lines into buffer
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty, no point in parsing
                return null;
            }

            resultJsonStr = buffer.toString();
            Log.v(LOG_TAG, "Response JSON String: " + resultJsonStr);


        } catch (IOException e) {
            Log.e(LOG_TAG, "Error: " + e.getMessage(), e);
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Error", e);
                }
            }
        }

        return resultJsonStr;
    }


    private String getQuery(HashMap<String, String> params) throws UnsupportedEncodingException
    {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (String key : params.keySet())
        {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(params.get(key), "UTF-8"));
        }

        return result.toString();
    }
}
