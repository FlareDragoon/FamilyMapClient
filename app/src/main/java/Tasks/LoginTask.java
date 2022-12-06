package Tasks;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.google.gson.Gson;

import Request.LoginRequest;
import Result.LoginResult;
import ServerClient.ServerProxy;

public class LoginTask implements Runnable {

    private final Handler messageHandler;
    private final String serverHost;
    private final String serverPort;
    private final LoginRequest request;

    private LoginResult result;

    public LoginTask(Handler messageHandler, String serverHost,
                     String serverPort,LoginRequest request) {
        this.messageHandler = messageHandler;
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.request = request;
    }

    @Override
    public void run() {
        ServerProxy serverProxy = new ServerProxy();
        serverProxy.main(serverHost, serverPort);

        result = serverProxy.login(request);
        if (result != null) {
            sendMessage(result);
        }
    }

    private void sendMessage(LoginResult result) {

        Gson gson = new Gson();
        String resultData = gson.toJson(result);

        Message message = Message.obtain();

        Bundle messageBundle = new Bundle();
        messageBundle.putString("LoginResultJsonDataKey", resultData);

        message.setData(messageBundle);
        messageHandler.sendMessage(message);
    }


}
