package ServerClient;

import com.google.gson.Gson;

import java.io.*;
import java.net.*;

import Request.GetAllFamilyEventsRequest;
import Request.GetFamilyRequest;
import Request.LoginRequest;
import Request.RegisterRequest;
import Result.GetAllFamilyEventsResult;
import Result.GetFamilyResult;
import Result.LoginResult;
import Result.RegisterResult;

public class ServerProxy {
    private static String serverHost;
    private static String serverPort;

    public static void main(String host, String port) {
        serverHost = host;
        serverPort = port;
    }

    public static LoginResult login(LoginRequest request) {
        try {
            URL url = new URL("http://" + serverHost + ":" + serverPort + "/user/login");
            HttpURLConnection http = (HttpURLConnection) url.openConnection();

            http.setRequestMethod("POST");
            http.setDoOutput(true);

            http.addRequestProperty("Accept", "application/json");

            http.connect();

            OutputStream reqBody = http.getOutputStream();
            Gson gson = new Gson();

            String reqData = gson.toJson(request);
            writeString(reqData, reqBody);

            reqBody.close();
            if (http.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream respBody = http.getInputStream();
                String respData = readString(respBody);

                LoginResult result = gson.fromJson(respData, LoginResult.class);
                return result;
            }
            else {
                System.out.println("ERROR: " + http.getResponseMessage());

                InputStream respBody = http.getErrorStream();
                String respData = readString(respBody);

                LoginResult result = gson.fromJson(respData, LoginResult.class);
                return result;

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static RegisterResult register(RegisterRequest request) {
        try {
            URL url = new URL("http://" + serverHost + ":" + serverPort + "/user/register");
            HttpURLConnection http = (HttpURLConnection) url.openConnection();

            http.setRequestMethod("POST");
            http.setDoOutput(true);

            http.addRequestProperty("Accept", "application/json");

            http.connect();

            OutputStream reqBody = http.getOutputStream();
            Gson gson = new Gson();

            String reqData = gson.toJson(request);
            writeString(reqData, reqBody);

            reqBody.close();
            if (http.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream respBody = http.getInputStream();
                String respData = readString(respBody);

                RegisterResult result = gson.fromJson(respData, RegisterResult.class);
                return result;
            }
            else {
                System.out.println("ERROR: " + http.getResponseMessage());

                InputStream respBody = http.getErrorStream();
                String respData = readString(respBody);

                RegisterResult result = gson.fromJson(respData, RegisterResult.class);
                return result;

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

    public static GetAllFamilyEventsResult getEvents(GetAllFamilyEventsRequest request) {
        try {
            URL url = new URL("http://" + serverHost + ":" + serverPort + "/event");
            HttpURLConnection http = (HttpURLConnection) url.openConnection();

            http.setRequestMethod("GET");
            http.setDoOutput(false);

            http.addRequestProperty("Authorization", request.getAuthtoken());
            http.addRequestProperty("Accept", "application/json");

            http.connect();

            Gson gson = new Gson();
            if (http.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream respBody = http.getInputStream();
                String respData = readString(respBody);

                GetAllFamilyEventsResult result = gson.fromJson(respData,
                        GetAllFamilyEventsResult.class);
                return result;
            }
            else {
                System.out.println("ERROR: " + http.getResponseMessage());

                InputStream respBody = http.getErrorStream();
                String respData = readString(respBody);

                GetAllFamilyEventsResult result = gson.fromJson(respData,
                        GetAllFamilyEventsResult.class);
                return result;

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static GetFamilyResult getPeople(GetFamilyRequest request) {
        try {
            URL url = new URL("http://" + serverHost + ":" + serverPort + "/person");
            HttpURLConnection http = (HttpURLConnection) url.openConnection();

            http.setRequestMethod("GET");
            http.setDoOutput(false);

            http.addRequestProperty("Authorization", request.getAuthtoken());
            http.addRequestProperty("Accept", "application/json");

            http.connect();

            Gson gson = new Gson();
            if (http.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream respBody = http.getInputStream();
                String respData = readString(respBody);

                GetFamilyResult result = gson.fromJson(respData, GetFamilyResult.class);
                return result;
            }
            else {
                System.out.println("ERROR: " + http.getResponseMessage());

                InputStream respBody = http.getErrorStream();
                String respData = readString(respBody);

                GetFamilyResult result = gson.fromJson(respData, GetFamilyResult.class);
                return result;

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    private static String readString(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        InputStreamReader sr = new InputStreamReader(is);
        char[] buf = new char[1024];
        int len;
        while ((len = sr.read(buf)) > 0) {
            sb.append(buf, 0, len);
        }
        return sb.toString();
    }

    private static void writeString(String str, OutputStream os) throws IOException {
        OutputStreamWriter sw = new OutputStreamWriter(os);
        sw.write(str);
        sw.flush();
    }
}
