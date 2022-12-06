package Tasks;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.google.gson.Gson;

import Request.LoginRequest;
import Request.RegisterRequest;
import Result.LoginResult;
import Result.RegisterResult;
import ServerClient.ServerProxy;

public class RegisterTask implements Runnable {

    private final Handler messageHandler;
    private final String serverHost;
    private final String serverPort;
    private final RegisterRequest request;

    private RegisterResult result;

    public RegisterTask(Handler messageHandler, String serverHost,
                        String serverPort, RegisterRequest request) {
        this.messageHandler = messageHandler;
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.request = request;
    }

    @Override
    public void run() {
        ServerProxy serverProxy = new ServerProxy();
        serverProxy.main(serverHost, serverPort);

        result = serverProxy.register(request);
        sendMessage(result);
    }

    private void sendMessage(RegisterResult result) {
        Gson gson = new Gson();
        String resultData = gson.toJson(result);

        Message message = Message.obtain();

        Bundle messageBundle = new Bundle();
        messageBundle.putString("RegisterResultJsonDataKey", resultData);

        message.setData(messageBundle);
        messageHandler.sendMessage(message);
    }
}
