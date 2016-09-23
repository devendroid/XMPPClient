package com.devs.xmppclient;

import java.util.Random;

/**
 * Created by ${Deven} on ${4/1/16}.
 */
public class ChatMessage {
    public String msg, sender, receiver, senderName;
    public String Date, Time;
    public String msgid;
    public boolean isMine = false;// Did I send the message.

    public ChatMessage(String Sender, String Receiver, String messageString) {
        msg = messageString;
        sender = Sender;
        receiver = Receiver;
        senderName = sender;
    }

    public void setMsgID() {

        msgid = String.format("%2d-%02d", new Random().nextInt(100), new Random().nextInt(100));;
    }
}
