package com.devs.xmppclient;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by ${Deven} on ${4/1/16}.
 */
public class XMPPManager {

    private static final String TAG = "XMPPManager";
    private static final int INTERVAL = 60; // in seconds

    private static SQLiteDatabase database;
    private final static String DATA_BASE_NAME = "XMPP_DB", TABLE_NAME = "XMPP_Chat";
    private static Context context;

    private static XMPPManager xmppManager;

    private XMPPManager(){
       initDB();
    }

    public static XMPPManager getInstance(Context mContext){
        XMPPManager.context = mContext;
        if(xmppManager == null)
            synchronized (XMPPManager.class){
                xmppManager = new XMPPManager();
            }
        return xmppManager;
    }

    private void initDB() {
        try {
            if (database == null) {
                database = context.openOrCreateDatabase(DATA_BASE_NAME,
                        SQLiteDatabase.CREATE_IF_NECESSARY, null);
                database.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(id INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + "user_name varchar,from_name varchar,message varchar,type varchar,date_time varchar,status varchar,sendstatus varchar)");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addEntry(String user_name, String from_name, String message,
                         String type, String date_time, String status) {
        ContentValues values = new ContentValues();
        values.put("user_name", user_name.toLowerCase());
        values.put("from_name", from_name.toLowerCase());
        values.put("message", message);
        values.put("type", type);
        values.put("date_time", date_time);
        values.put("status", status);
        values.put("sendstatus", "1");
        try {
            database.insert(TABLE_NAME, null, values);
            // Toast.makeText(getApplicationContext(),
            // "inserted",Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addEntry(String user_name, String from_name, String message,
                         String type, String date_time, String status, String sendstatus) {
        ContentValues values = new ContentValues();
        values.put("user_name", user_name.toLowerCase());
        values.put("from_name", from_name.toLowerCase());
        values.put("message", message);
        values.put("type", type);
        values.put("date_time", date_time);
        values.put("status", status);
        values.put("sendstatus", "0");
        try {
            database.insert(TABLE_NAME, null, values);
            // Toast.makeText(getApplicationContext(),
            // "inserted",Toast.LENGTH_LONG).show();
            // Toast.makeText(context, "success", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Exception", Toast.LENGTH_LONG).show();
        }
    }

    public int getCount(String userName) {
        int count = 0;
        try {
            Cursor cursor = database.rawQuery(
                    "SELECT * FROM " + TABLE_NAME + " where user_name='" + userName.toLowerCase()
                            + "' and type='2' and status='0' COLLATE NOCASE",
                    null);
            count = cursor.getCount();
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }

    public int getCount(String userName, String to) {
        int count = 0;
        try {
            Cursor cursor = database.rawQuery(
                    "SELECT * FROM " + TABLE_NAME + " where user_name='" + userName.toLowerCase()
                            + "' and from_name='" + to.toLowerCase()
                            + "' and type='2' and status='0' COLLATE NOCASE",
                    null);
            count = cursor.getCount();
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }

    public Cursor getUser(String userName) {
        Cursor cursor = null;
        try {
            cursor = database.rawQuery(
                    "SELECT * FROM " + TABLE_NAME + " where user_name='" + userName.toLowerCase()
                            + "' COLLATE NOCASE", null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cursor;
    }

    public Cursor getUserChat(String userName, String to) {
        Cursor cursor = null;
        try {
            cursor = database.rawQuery(
                    "SELECT * FROM " + TABLE_NAME + " where user_name='" + userName.toLowerCase()
                            + "' and from_name='" + to.toLowerCase() + "' COLLATE NOCASE",
                    null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cursor;
    }

    public int delete(String userName) {
        int count = 0;
        try {
            count = database.delete(TABLE_NAME,
                    "user_name= ? and status='1'", new String[]{userName.toLowerCase()});
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }

    public int update(String userName, String to) {
        int count = 0;
        try {
            ContentValues values = new ContentValues();
            values.put("status", "1");
//            count = database.update(TABLE_NAME, values, "user_name=?",
//                    new String[]{userName.toLowerCase()});
            count = database.update(TABLE_NAME, values,
                    "user_name=? and from_name=?",
                    new String[]{userName.toLowerCase(), to.toLowerCase()});
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }

    @SuppressLint("DefaultLocale")
//    public void sendRemainingText() {
//        Cursor cursor = null;
//        try {
//            if (BaseActivity.xmppConnection != null) {
//                appSession = new AppSession(context);
//                cursor = database.rawQuery(
//                        "SELECT * FROM " + TABLE_NAME + " where user_name='"
//                                + appSession.getUser().getChatName()
//                                .toLowerCase()
//                                + "'and sendstatus='0' COLLATE NOCASE", null);
//                // SELECT * FROM desitio_chat where user_name='minaxi' and
//                // sendstatus='0' COLLATE NOCASE
//
//                if (cursor.getCount() > 0) {
//                    cursor.moveToFirst();
//                    while (!cursor.isAfterLast()) {
//                        // to user
//                        // text
//                        String toName = cursor.getString(2)
//                                + "@"
//                                + context.getResources().getString(
//                                R.string.service);
//                        Message msg = new Message(toName, Message.Type.chat);
//                        msg.setBody(cursor.getString(3));
//                        BaseActivity.xmppConnection.sendPacket(msg);
//                        updateSendStatus(cursor.getString(0));
//                        // Toast.makeText(context,
//                        // "send to "+cursor.getString(2),
//                        // Toast.LENGTH_LONG).show();
//                        cursor.moveToNext();
//                    }
//                }
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    private int updateSendStatus(String id) {
        int count = 0;
        try {
            ContentValues values = new ContentValues();
            values.put("sendstatus", "1");
            // Log.i(getClass().getName(), "updatesendStatus id : " + id);
            count = database.update(TABLE_NAME, values,
                    "id=? and sendstatus=?", new String[]{id, "0"});
            // Log.i(getClass().getName(), "updatesendStatus count : " + count);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }

    public void startXMPPAM(Context ctxt, String user, String psw) {
        AlarmManager mgr=
                (AlarmManager)ctxt.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(ctxt, XMPPConnectorIService.class);
        i.putExtra("username",user);
        i.putExtra("password",psw);
        i.setData((Uri.parse("XMPP_12")));
        PendingIntent pi= PendingIntent.getService(ctxt, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
        mgr.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis(), INTERVAL*1000, pi);
        Log.i(TAG, "=====scheduleAlarms");
    }

    public void stopXMPPAM(Context ctxt) {
        AlarmManager mgr=
                (AlarmManager)ctxt.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(ctxt, XMPPConnectorIService.class);
        i.setData((Uri.parse("XMPP_12")));
        PendingIntent pi= PendingIntent.getService(ctxt, 0, i, 0);
        mgr.cancel(pi);
        Log.i(TAG, "=====stopAlarms");
    }

}
