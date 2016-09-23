package com.devs.xmppclient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;

// http://www.tutorialsface.com/2015/08/building-your-own-android-chat-messenger-app-similar-to-whatsapp-using-xmpp-smack-4-1-api-from-scratch-part-2/
// http://www.tutorialsface.com/2015/08/building-your-own-android-chat-messenger-app-similar-to-whatsapp-using-xmpp-smack-4-1-api-from-scratch-part-1/
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    public static final String USERNAME = "deven";
    public static final String PASSWORD = "qwerty";

    private EditText edtUser;
    private EditText edtPassword;
    private EditText edtMsg;
    private EditText edtToUser;
    private Button btnLogin;
    private Button btnSend;

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent

            String msg = intent.getStringExtra("message");
            if (msg!=null) {
                final ChatMessage chatMessage = new Gson().fromJson(
                        msg, ChatMessage.class);
                Toast.makeText(MainActivity.this, chatMessage.msg, Toast.LENGTH_SHORT).show();
            }
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }


    private void init() {
        edtUser = (EditText) findViewById(R.id.txtUser);
        edtUser.setText(USERNAME);
        edtPassword = (EditText) findViewById(R.id.txtPwd);
        edtPassword.setText(PASSWORD);
        edtMsg = (EditText) findViewById(R.id.txtMsg);
        edtToUser = (EditText) findViewById(R.id.txtTo);
        btnLogin = (Button) findViewById(R.id.btnSignin);
        btnLogin.setOnClickListener(this);
        btnSend = (Button) findViewById(R.id.btnSend);
        btnSend.setOnClickListener(this);
    }

    public void sendTextMessage(String sender, String receiver) {

        String msgStr = edtMsg.getText().toString();
        if (!msgStr.equalsIgnoreCase("")) {
            final ChatMessage chatMessage = new ChatMessage(sender, receiver, msgStr);
            chatMessage.setMsgID();
            chatMessage.msg = msgStr;
            chatMessage.Date = Utils.getCurrentDate();
            chatMessage.Time = Utils.getCurrentTime();
            edtMsg.setText("");
            XMPPConnectorIService.xmppConnector.sendMessage(chatMessage);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.btnSignin:
                String uname = edtUser.getText().toString();
                String psw = edtPassword.getText().toString();
                if(!uname.equals("") && !psw.equals("")) {
                   // doBindService(uname, psw);
                    XMPPManager.getInstance(this).startXMPPAM(this, uname, psw);
                    LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                            new IntentFilter(XMPPConnector.ACTION_SEND));
                }
                else {
                    Toast.makeText(MainActivity.this, "Can't Empty", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.btnSend:
                String sender = edtUser.getText().toString();
                String receiver = edtToUser.getText().toString();
                if(!sender.equals("") && !receiver.equals("")) {
                    sendTextMessage(sender, receiver);
                }
                else {
                    Toast.makeText(MainActivity.this, "Require Sender and Receiver", Toast.LENGTH_SHORT).show();
                }

                break;
        }
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "===onDestroy");
        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }

}

