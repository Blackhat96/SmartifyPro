package app.smartifyPro;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

public class CallReceiver extends BroadcastReceiver {
    private static boolean ring = false;
    private static boolean callReceived = false;
    private MediaPlayer player;

    private void sendNotification(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, 0);
        Bitmap icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.smartify);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.notif)
                        .setContentText("Stop ringing")
                        .setContentTitle("Smartify")
                        .setLargeIcon(icon)
                        .setAutoCancel(true)
                        .setColor(context.getResources().getColor(R.color.black))
                        .setContentIntent(pIntent);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mBuilder.setDefaults(Notification.DEFAULT_VIBRATE);
            mBuilder.setLights(Color.YELLOW, 1000, 300);

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(0, mBuilder.build());

        } else {
            mBuilder = mBuilder.setContentTitle("Smartify")
                    .setContentText("Stop Ringing")
                    .setSmallIcon(R.drawable.notif);
        }

        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, mBuilder.build());
    }


    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(Context mContext, Intent intent) {

        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        if (state == null) {
            return;
        }

        if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
            ring = true;
        }
        if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
            callReceived = true;
        }

        if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
            if (ring && !callReceived) {
                final SharedPreferences settings = mContext.getSharedPreferences("Settings", Context.MODE_PRIVATE);
                final SharedPreferences number = mContext.getSharedPreferences("Numbers", Context.MODE_PRIVATE);

                Boolean wifi = settings.getBoolean("wifi", false);
                Boolean ring = settings.getBoolean("ring", false);
                Boolean profile = settings.getBoolean("profile", false);

                Set<String> numbers = number.getStringSet("num", new LinkedHashSet<String>());
                Iterator itr = numbers.iterator();

                String numb = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                while (itr.hasNext())
                    if (PhoneNumberUtils.compare(itr.next().toString(), numb)) {
                        {
                            if (wifi) {
                                WifiManager wifiManager = (WifiManager) mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                                if (wifiManager.isWifiEnabled()) {
                                    wifiManager.setWifiEnabled(false);
                                } else {
                                    wifiManager.setWifiEnabled(true);
                                }
                            }

                            if (ring || profile) {
                                AudioManager am;
                                am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
                                switch (am.getRingerMode()) {
                                    case AudioManager.RINGER_MODE_SILENT:
                                    case AudioManager.RINGER_MODE_VIBRATE:

                                        int maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_RING);
                                        am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                                        am.setStreamVolume(AudioManager.STREAM_RING, maxVolume, AudioManager.FLAG_PLAY_SOUND);
                                        am.setStreamVolume(AudioManager.STREAM_MUSIC, am.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
                                        if (ring) {
                                            player = MediaPlayer.create(mContext, Settings.System.DEFAULT_RINGTONE_URI);
                                            if (player == null)
                                                player = MediaPlayer.create(mContext, R.raw.ringtone);
                                            player.start();
                                            player.setLooping(true);
                                            sendNotification(mContext);
                                        }
                                        break;
                                    case AudioManager.RINGER_MODE_NORMAL:
                                        break;
                                }
                            }
                            callReceived = false;
                        }
                        return;
                    }
            }
        }
    }
}