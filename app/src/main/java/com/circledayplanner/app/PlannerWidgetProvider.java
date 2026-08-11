package com.circledayplanner.app;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.*;
import android.widget.RemoteViews;
import java.util.*;

public class PlannerWidgetProvider extends AppWidgetProvider {
    @Override public void onUpdate(Context c,AppWidgetManager m,int[] ids){for(int id:ids)update(c,m,id);}
    private static void update(Context c,AppWidgetManager m,int id){PlannerStore s=PlannerStore.get(c);Calendar cal=Calendar.getInstance();String date=TimeUtils.dateKey(cal);int now=TimeUtils.nowMinute();int pct=Math.round(now/14.4f);RemoteViews v=new RemoteViews(c.getPackageName(),R.layout.widget_planner);v.setTextViewText(R.id.widgetTitle,"Circle Day Planner · "+TimeUtils.shortDate(cal));v.setTextViewText(R.id.widgetProgress,pct+" % des Tages vorbei");Event next=null;for(Event e:s.forDate(date))if(e.startMinute>now&&(next==null||e.startMinute<next.startMinute))next=e;v.setTextViewText(R.id.widgetNext,next==null?"Keine weiteren Termine":"Als Nächstes · "+TimeUtils.time(next.startMinute)+"  "+next.title);Intent in=new Intent(c,MainActivity.class);v.setOnClickPendingIntent(R.id.widgetTitle,PendingIntent.getActivity(c,0,in,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE));m.updateAppWidget(id,v);}
    public static void updateAll(Context c){AppWidgetManager m=AppWidgetManager.getInstance(c);int[] ids=m.getAppWidgetIds(new ComponentName(c,PlannerWidgetProvider.class));for(int id:ids)update(c,m,id);}
}
