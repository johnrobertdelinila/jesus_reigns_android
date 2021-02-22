package johnrobert.delinila.jesusreigns.receivers;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import johnrobert.delinila.jesusreigns.MainActivity;
import johnrobert.delinila.jesusreigns.R;


public class NotificationUtil {

    private static String TAG = NotificationUtil.class.getSimpleName();
    private Context mContext;
    public static boolean read = true;

    public NotificationUtil(Context mContext) {
        this.mContext = mContext;
    }

    private static long getTimeMilliSec(String timeStamp) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = format.parse(timeStamp);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void showNotificationMessage(String timeStamp, final String user_image, final String title, final String message, Intent intent, String imageUrl, String notification_type) {

        // Check for empty push message
        if (TextUtils.isEmpty(message))
            return;

        // Notification icon
        final int icon = R.mipmap.ic_launcher;

        int requestID = (int) System.currentTimeMillis();

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        final PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        mContext,
                        requestID,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT //PendingIntent.FLAG_CANCEL_CURRENT
                );

        try {
            if (!TextUtils.isEmpty(imageUrl)) {

                Bitmap bitmap = getBitmapFromURL(imageUrl);

                if (bitmap != null) {
                    showBigNotification(timeStamp, user_image, bitmap, icon, title, message, resultPendingIntent,notification_type);
                } else {
                    showSmallNotification(timeStamp, user_image, icon, title, message, resultPendingIntent,notification_type);
                }

            } else {

                showSmallNotification(timeStamp,user_image, icon, title, message, resultPendingIntent,notification_type);
            }
        } catch (Exception e) {
            Log.e("showNotificationMessage", e.getMessage() == null ? "" : e.getMessage());
        }
    }

    public Intent getIntent(String click_action) {

        Intent resultIntent;

        switch (click_action) {
            case "TARGATLINEUPSONGS":
                resultIntent = new Intent(mContext, MainActivity.class);
                break;
            /*case "com.amsavarthan.hify.TARGETNOTIFICATIONREPLY":
                resultIntent = new Intent(mContext, NotificationReplyActivity.class);
                break;
            case "com.amsavarthan.hify.TARGETNOTIFICATION_IMAGE":
                resultIntent = new Intent(mContext, NotificationImage.class);
                break;
            case "com.amsavarthan.hify.TARGETNOTIFICATIONREPLY_IMAGE":
                resultIntent = new Intent(mContext, NotificationImageReply.class);
                break;
            case "com.amsavarthan.hify.TARGET_FRIENDREQUEST":
                resultIntent = new Intent(mContext, FriendProfile.class);
                break;
            case "com.amsavarthan.hify.TARGET_ACCEPTED":
            case "REPORT USER":
                resultIntent = new Intent(mContext, FriendProfile.class);
                break;
            case "com.amsavarthan.hify.TARGET_LIKE":
            case "REPORT POST":
                resultIntent = new Intent(mContext, MainActivity.class).putExtra("openFragment","forLike");
                break;
            case "com.amsavarthan.hify.TARGET_COMMENT":
                resultIntent = new Intent(mContext, MainActivity.class).putExtra("openFragment","forComment");
                break;
            case "com.amsavarthan.hify.TARGET_FORUM":
                resultIntent = new Intent(mContext, AnswersActivity.class);
                break; //TARGET_COMMENT
            case "SHOW COMMUNITY":
                resultIntent = new Intent(mContext, ApproveActivity.class);
                break;*/
            default:
                resultIntent = new Intent(mContext, MainActivity.class);
                break;
        }
        return resultIntent;

    }

    private void showSmallNotification(String timeStamp, String user_image, int icon, String title, String message,
                                       PendingIntent resultPendingIntent, String notification_type) {

        int id;
        NotificationCompat.Builder mBuilder;
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(mContext);

        switch (notification_type){

            case "like":
                id=100;
                mBuilder = new NotificationCompat.Builder(mContext, "like_channel");
                break;
            case "comment":
                id=200;
                mBuilder = new NotificationCompat.Builder(mContext, "comments_channel");
                break;
            case "forum":
                id=(int)System.currentTimeMillis();
                mBuilder = new NotificationCompat.Builder(mContext, "forum_channel");
                break;
            case "Message":
                id=(int)System.currentTimeMillis();
                mBuilder = new NotificationCompat.Builder(mContext, "flash_message");
                break;
            default:
                id=(int)System.currentTimeMillis();
                mBuilder = new NotificationCompat.Builder(mContext, "hify_other_channel");
        }

        createNotificationChannel(notification_type, notification_type, "like_channel");

        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.setBigContentTitle(title);
        bigTextStyle.bigText(message);

        Notification notification;

        notification = mBuilder
                .setContentTitle(title)
                .setSmallIcon(icon)
                .setContentText(message)
                .setStyle(bigTextStyle)
                .setContentIntent(resultPendingIntent)
                .setPriority(Notification.PRIORITY_MAX)
                .setAutoCancel(true)
                .setColorized(true)
                .setWhen(getTimeMilliSec(timeStamp))
                .setShowWhen(true)
                .setSound(Uri.parse("android.resource://"+mContext.getPackageName()+"/"+R.raw.jrm_sound))
                .setColor(Color.parseColor("#2591FC"))
                .setLargeIcon(getCircularBitmap(getBitmapFromURL(user_image)))
                .setTicker(title)
                .build();

        read=false;

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            notificationManagerCompat.notify(id, notification);
        }

        Log.d("FCM_LOGIC", "MERON");

    }

    private void createNotificationChannel(String name, String description, String CHANNEL_ID) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other Notification behaviors after this
            NotificationManager notificationManager = mContext.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void showBigNotification(String timeStamp, String user_image, Bitmap bitmap, int icon, String title, String message,
                                     PendingIntent resultPendingIntent, String notification_type) {

        int id;
        NotificationCompat.Builder mBuilder;
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(mContext);

        switch (notification_type){

            case "like":
                id=100;
                mBuilder = new NotificationCompat.Builder(mContext, "like_channel");
                break;
            case "comment":
                id=200;
                mBuilder = new NotificationCompat.Builder(mContext, "comments_channel");
                break;
            case "forum":
                id=(int)System.currentTimeMillis();
                mBuilder = new NotificationCompat.Builder(mContext, "forum_channel");
                break;
            case "Message":
                id=(int)System.currentTimeMillis();
                mBuilder = new NotificationCompat.Builder(mContext, "flash_message");
                break;
            default:
                id=(int)System.currentTimeMillis();
                mBuilder = new NotificationCompat.Builder(mContext, "hify_other_channel");
        }

        Notification notification;
        NotificationCompat.BigPictureStyle bigPictureStyle = new NotificationCompat.BigPictureStyle();
        bigPictureStyle.setBigContentTitle(title);
        bigPictureStyle.bigPicture(bitmap);

        notification = mBuilder
                .setContentTitle(title)
                .setContentIntent(resultPendingIntent)
                .setColorized(true)
                .setSmallIcon(icon)
                .setStyle(bigPictureStyle) //bigPictureStyle
                .setAutoCancel(true)
                .setTicker(title)
                .setContentText(message)
                .setShowWhen(true)
                .setSound(Uri.parse("android.resource://" + mContext.getPackageName() + "/" + R.raw.jrm_sound))
                .setWhen(getTimeMilliSec(timeStamp))
                .setColor(Color.parseColor("#2591FC"))
                .setLargeIcon(getCircularBitmap(getBitmapFromURL(user_image)))
                .build();

        read=false;


        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            notificationManagerCompat.notify(id, notification);
        }

    }

    private Bitmap getBitmapFromURL(String strURL) {
        try {
            URL url = new URL(strURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Bitmap getCircularBitmap(Bitmap bitmap){
        final Bitmap output=Bitmap.createBitmap(bitmap.getWidth(),bitmap.getHeight(),Bitmap.Config.ARGB_8888);
        final Canvas canvas=new Canvas(output);
        final int color=Color.RED;
        final Paint paint=new Paint();
        final Rect rect=new Rect(0,0,bitmap.getWidth(),bitmap.getHeight());
        final RectF rectF=new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0,0,0,0);
        paint.setColor(color);
        canvas.drawOval(rectF,paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap,rect,rect,paint);
        bitmap.recycle();
        return output;

    }

}