package com.circledayplanner.app;

import android.app.*;
import android.content.*;
import androidx.core.app.NotificationCompat;
import java.util.concurrent.atomic.AtomicInteger;

public class ReminderReceiver extends BroadcastReceiver {
    private static final AtomicInteger NEXT=new AtomicInteger(4102);
    @Override public void onReceive(Context context, Intent intent){String title=intent.getStringExtra("title");String body=intent.getStringExtra("text");NotificationManager nm=context.getSystemService(NotificationManager.class);Notification n=new NotificationCompat.Builder(context,"planner").setSmallIcon(com.circledayplanner.app.R.drawable.ic_stat_planner).setContentTitle(title==null?"Circle Day Planner":title).setContentText(body==null?"Ein Termin beginnt bald.":body).setAutoCancel(true).setPriority(NotificationCompat.PRIORITY_HIGH).build();nm.notify(NEXT.getAndIncrement(),n);}
}
