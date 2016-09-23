package com.devs.xmppclient;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.gson.Gson;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.sasl.SASLErrorException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.search.ReportedData;
import org.jivesoftware.smackx.search.UserSearchManager;
import org.jivesoftware.smackx.xdata.Form;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * Created by ${Deven} on ${4/1/16}.
 */
public class XMPPConnector {

    private static final String TAG = "XMPPConnector";
    private static final String SERVER_ADDRESS = "example.com";//"nimbuzz.com";
    private static final boolean DEBUGGABLE = true;
    public static final String ACTION_SEND = "com.it.action_send";

    private static String loginUser = "";
    private static String passwordUser;
    //public static boolean isToasted = true;
    private static XMPPTCPConnection connection;
    private static XMPPConnector instance = null;
    public static boolean connected = false;
    public boolean loggedin = false;

    private boolean chatCreated = false;
    private org.jivesoftware.smack.chat.Chat Mychat;
    private Gson gson;
    private static Context context;
    private MYManagerListener myManagerListener;
    private MYChatMessageListener myChatMessageListener;

    public static XMPPConnector getInstance(Context con, String user, String pass) {
        context =  con;
        // Create new instance if user detail changed
        if (instance == null || !loginUser.equals(user)) {
            // To make old user logout
            if(connection != null) connection.disconnect();
            instance = new XMPPConnector(user, pass);
        }
        return instance;
    }

    private XMPPConnector(String user,
                          String password) {
        loginUser = user;
        passwordUser = password;
        init();
    }

    private void init() {
        gson = new Gson();
        myChatMessageListener = new MYChatMessageListener();
        myManagerListener = new MYManagerListener();
        initialiseConnection();
    }

    private void initialiseConnection() {
        showLOG(" ===initialiseConnection....");
        XMPPTCPConnectionConfiguration.Builder config = XMPPTCPConnectionConfiguration
                .builder();
        config.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
        config.setServiceName(SERVER_ADDRESS);
        config.setHost(SERVER_ADDRESS);
        config.setPort(5222);
        config.setDebuggerEnabled(DEBUGGABLE);
        XMPPTCPConnection.setUseStreamManagementResumptiodDefault(true);
        XMPPTCPConnection.setUseStreamManagementDefault(true);
        connection = new XMPPTCPConnection(config.build());
        MYConnectionListener connectionListener = new MYConnectionListener();
        connection.addConnectionListener(connectionListener);
        connection.setPacketReplyTimeout(10000);
    }

    public void disconnect() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if( connection != null )
                connection.disconnect();
                showLOG("===Disconnected");
            }
        }).start();
    }

    public void connectNLogin(String callerClass) {
        if ( !connection.isConnected()) {
            try {
                synchronized (this) {
                    showLOG(" ===Connecting.... from: "+callerClass);
                    connection.connect();
                    connected = true;
                }
            }
            catch (IOException | SmackException | XMPPException e) {
                e.printStackTrace();
                Log.e(TAG, "(" + callerClass + ")IOException: " + e.getMessage());
            }
        }
        else {
            showLOG("====Already connected");
            // Check if already logged in
            if (!connection.isAuthenticated()) {
                login();
            }
            else {
                showLOG( "====Already Logged in");
            }
        }
    }

    private void login() {
        try {
            showLOG("===Trying to login...");
            connection.login(loginUser, passwordUser);
            showLOG( "===Logged-in "+loginUser);
        }
        catch (SASLErrorException e){
            createUser();
            Log.e(TAG, "SASLErrorException: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createUser(){
        // Registering the user
        showLOG("===Creating new user...");
        try {
            AccountManager accountManager = AccountManager.getInstance(connection);
            accountManager.sensitiveOperationOverInsecureConnection(true);
            if(accountManager.supportsAccountCreation()) {
                accountManager.createAccount(loginUser, passwordUser);
                showLOG("===Created new user "+loginUser);
                connection.disconnect();
                connectNLogin(TAG);
            }
            else {
                showLOG( "===Not support user creation");
            }
        } catch (SmackException | XMPPException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private class MYManagerListener implements ChatManagerListener {
        @Override
        public void chatCreated(final org.jivesoftware.smack.chat.Chat chat,
                                final boolean createdLocally) {
            if (!createdLocally)
                chat.addMessageListener(myChatMessageListener);
        }
    }

    public void sendMessage(ChatMessage chatMessage) {
        String body = gson.toJson(chatMessage);
        if (!chatCreated) {
            Mychat = ChatManager.getInstanceFor(connection).createChat(
                    chatMessage.receiver + "@"
                            + SERVER_ADDRESS,
                    myChatMessageListener);
            chatCreated = true;
        }
        final Message message = new Message();
        message.setBody(body);
        message.setStanzaId(chatMessage.msgid);
        message.setType(Message.Type.chat);

        try {
            if (connection.isAuthenticated()) {
                Mychat.sendMessage(message);
            } else {
                login();
            }
        } catch (SmackException.NotConnectedException e) {
            Log.e(TAG, "xmpp.SendMessage() msg Not sent!-Not Connected!");

        } catch (Exception e) {
            Log.e(TAG,
                    "msg No" + e.getMessage());
        }
    }

    public class MYConnectionListener implements ConnectionListener {
        @Override
        public void connected(final XMPPConnection connection) {
            showLOG("===Connected!");
            connected = true;
            // Check if already logged in
            if (!connection.isAuthenticated()) {
                login();
            }
            else {
                showLOG("====Already Logged in");
            }
        }

        @Override
        public void connectionClosed() {
            showLOG("===ConnectionCLosed!");
            connected = false;
            chatCreated = false;
            loggedin = false;
        }

        @Override
        public void connectionClosedOnError(Exception arg0) {
            showLOG("===ConnectionClosedOn Error!");
            connected = false;

            chatCreated = false;
            loggedin = false;
        }

        @Override
        public void reconnectingIn(int arg0) {
            showLOG("===Reconnectingin " + arg0);
            loggedin = false;
        }

        @Override
        public void reconnectionFailed(Exception arg0) {
            showLOG("==ReconnectionFailed!");
            connected = false;
            chatCreated = false;
            loggedin = false;
        }

        @Override
        public void reconnectionSuccessful() {
            showLOG("==ReconnectionSuccessful");
            connected = true;
            chatCreated = false;
            loggedin = false;
        }

        @Override
        public void authenticated(XMPPConnection arg0, boolean arg1) {
            showLOG("xmpp:Authenticated!");
            loggedin = true;

            ChatManager.getInstanceFor(connection).addChatListener(
                    myManagerListener);

            chatCreated = false;
        }
    }

    private class MYChatMessageListener implements ChatMessageListener {

        public MYChatMessageListener() {
        }

        @Override
        public void processMessage(final org.jivesoftware.smack.chat.Chat chat,
                                   final Message message) {
            showLOG("==Message received: " +message);

            if (message.getType() == Message.Type.chat
                    && message.getBody() != null) {

                Intent intent = new Intent(ACTION_SEND);
                // You can also include some extra data.
                intent.putExtra("message", message.getBody());
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

//                final ChatMessage chatMessage = gson.fromJson(
//                        message.getBody(), ChatMessage.class);

            }
        }

    }

    private void showLOG(String msg) {
        Log.i(TAG, msg);
    }

    private Boolean checkIfUserExists(String user) {
        UserSearchManager search = new UserSearchManager(connection);
        Form searchForm = null;
        try {
            searchForm = search
                    .getSearchForm("search." + connection.getServiceName());
            Form answerForm = searchForm.createAnswerForm();
            answerForm.setAnswer("Username", true);
            answerForm.setAnswer("search", user);
            ReportedData data = search
                    .getSearchResults(answerForm, "search." + connection.getServiceName());
            if (data.getRows() != null) {
                List<ReportedData.Row> rows = data.getRows();
                Iterator<ReportedData.Row> it = rows.iterator();

                if (it.hasNext()) return true;
                else return false;
            }
        } catch (SmackException.NoResponseException | XMPPException.XMPPErrorException
                | SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
        return false;
    }

// Warning ==  NOT TESTED YET
//    public void getAllUsers(){
//        try {
//            connection.connect();
//        } catch (SmackException | IOException | XMPPException e) {
//            e.printStackTrace();
//        }
//
//        if(!connection.isConnected()) {
//            Log.i(TAG,"=======not connected ");
//            return;
//        }
//
//        UserSearchManager usm = new UserSearchManager(connection);
//
//        Form searchForm = null;
//        try {
//            searchForm = usm.getSearchForm("search." + SERVER_ADDRESS);
//        } catch (SmackException.NoResponseException | XMPPException.XMPPErrorException | SmackException.NotConnectedException e) {
//            e.printStackTrace();
//        }
//        //Here i am searching for the service name
//
//        Form answerForm = searchForm.createAnswerForm();
//        UserSearch userSearch = new UserSearch();
//        answerForm.setAnswer("Username", true);
//        answerForm.setAnswer("search", "*");
//        ReportedData data = null;
//        try {
//            data = userSearch.sendSearchForm(connection, answerForm, "search." + SERVER_ADDRESS);
//        } catch (SmackException.NoResponseException | XMPPException.XMPPErrorException | SmackException.NotConnectedException e) {
//            e.printStackTrace();
//        }
//        if (data.getRows() != null) {
//            System.out.println("not null");
//            Iterator<ReportedData.Row> it = (Iterator<ReportedData.Row>) data.getRows();
//            //arryAllUsers.clear();
//            while (it.hasNext()) {
//                //System.out.println("row");
//                ReportedData.Row row = it.next();
//                Iterator iterator = (Iterator) row.getValues("jid");
//                if (iterator.hasNext()) {
//                    String jid = iterator.next().toString(); //here i get all the user's of the open fire..
//                    Log.i(TAG,"=======jid "+jid);
//                }
//                // Log.i("Iteartor values......"," "+value);
//            }
//        }
//    }
}
