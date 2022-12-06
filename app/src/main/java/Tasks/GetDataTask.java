package Tasks;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.google.gson.Gson;

import Request.GetAllFamilyEventsRequest;
import Request.GetFamilyRequest;
import Result.GetAllFamilyEventsResult;
import Result.GetFamilyResult;
import Result.LoginResult;
import ServerClient.ServerProxy;

public class GetDataTask implements Runnable {

    private final Handler messageHandler;
    private final String serverHost;
    private final String serverPort;
    private GetFamilyRequest getFamilyRequest;
    private GetAllFamilyEventsRequest getEventsRequest;

    private GetFamilyResult getFamilyResult;
    private GetAllFamilyEventsResult getEventsResult;

    public GetDataTask(Handler messageHandler, String serverHost,
                       String serverPort, String authtoken) {
        this.messageHandler = messageHandler;
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.getFamilyRequest = new GetFamilyRequest(authtoken);
        this.getEventsRequest = new GetAllFamilyEventsRequest(authtoken);
    }

    @Override
    public void run() {
        ServerProxy serverProxy = new ServerProxy();
        serverProxy.main(serverHost, serverPort);

        getEventsResult = serverProxy.getEvents(getEventsRequest);
        getFamilyResult = serverProxy.getPeople(getFamilyRequest);

        sendMessage(getEventsResult, getFamilyResult);
    }

    private void sendMessage(GetAllFamilyEventsResult getEventsResult,
                             GetFamilyResult getFamilyResult) {

        Gson gson = new Gson();
        String eventsResultData = gson.toJson(getEventsResult);
        String familyResultData = gson.toJson(getFamilyResult);

        Message message = Message.obtain();

        Bundle messageBundle = new Bundle();
        messageBundle.putString("EventResultJsonDataKey", eventsResultData);
        messageBundle.putString("FamilyResultJsonDataKey", familyResultData);

        message.setData(messageBundle);
        messageHandler.sendMessage(message);
    }
}
