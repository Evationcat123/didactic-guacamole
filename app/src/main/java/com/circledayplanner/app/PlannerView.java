package com.circledayplanner.app;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.ColorDrawable;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.Nullable;
import java.util.List;

public class PlannerView extends View {
    public interface Listener { void onEventTap(Event e); void onDateSwipe(int direction); void onEventMoved(Event e); }
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG); private final Paint text=new Paint(Paint.ANTI_ALIAS_FLAG);
    private List<Event> events=java.util.Collections.emptyList(); private Listener listener; private int movingIndex=-1;
    private float downX,downY,lastAngle; private boolean dragging; private long downTime;
    private int background=0x00000000, ring=0xFFB8AFBF, primary=0xFF6750A4, onSurface=0xFFE6E0E9;
    private boolean light=false; private boolean showCurrentTime=true;
    public PlannerView(Context c){super(c); setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
    public void setListener(Listener l){listener=l;}
    public void setEvents(List<Event> list){events=list; invalidate();}
    public void setThemeColors(boolean lightMode,int surface,int primaryColor,int textColor){light=lightMode;background=surface;primary=primaryColor;onSurface=textColor;invalidate();}
    public void setShowCurrentTime(boolean show){showCurrentTime=show;invalidate();}
    private float cx(){return getWidth()/2f;} private float cy(){return getHeight()/2f;} private float r(){return Math.min(getWidth(),getHeight())*0.41f;}
    private float angleForMinute(float m){ return -90f + m/4f; }
    private float normalized(float a){a%=360; if(a<0)a+=360; return a;}
    private float minuteForPoint(float x,float y){ double deg=Math.toDegrees(Math.atan2(y-cy(),x-cx())); float a=normalized((float)deg+90); return a*4f; }
    private boolean inCircle(float x,float y){float dx=x-cx(),dy=y-cy(); return dx*dx+dy*dy <= r()*r()*1.15f;}
    @Override protected void onDraw(Canvas c){super.onDraw(c); float centerX=cx(),centerY=cy(),R=r();
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(Math.max(2,getWidth()*0.008f));p.setColor(light?0xFFDDD7E0:0xFF49454F);p.setShadowLayer(10,0,4,0x33000000);c.drawCircle(centerX,centerY,R+9,p);p.clearShadowLayer();
        p.setStrokeWidth(Math.max(1,getWidth()*0.004f));p.setColor(ring); for(int h=0;h<24;h++){float a=(float)Math.toRadians(angleForMinute(h*60));float inner=R-10,outer=R+(h%6==0?18:10);c.drawLine(centerX+(float)Math.cos(a)*inner,centerY+(float)Math.sin(a)*inner,centerX+(float)Math.cos(a)*outer,centerY+(float)Math.sin(a)*outer,p);}
        text.setTypeface(Typeface.create("sans",Typeface.NORMAL));text.setTextAlign(Paint.Align.CENTER);text.setTextSize(Math.max(12,getWidth()*0.035f));text.setColor(onSurface); for(int h=0;h<24;h++){if(h%2==0){float a=(float)Math.toRadians(angleForMinute(h*60));float tr=R+30;c.drawText(String.format(java.util.Locale.getDefault(),"%02d",h),centerX+(float)Math.cos(a)*tr,centerY+(float)Math.sin(a)*tr+text.getTextSize()/3,p);}}
        p.setStyle(Paint.Style.FILL); for(Event e:events){float start=angleForMinute(e.startMinute),sweep=e.duration()/4f; RectF oval=new RectF(centerX-R,centerY-R,centerX+R,centerY+R);p.setColor(e.color);p.setAlpha(235);c.drawArc(oval,start,sweep,true,p);}
        // inner cutout for donut effect
        p.setAlpha(255);p.setColor(background==0? (light?0xFFF9F7FC:0xFF121212):background);c.drawCircle(centerX,centerY,R*0.62f,p);
        // Current time is shown only for today; other dates stay visually neutral.
        int now=new CalendarClock().minute();
        if(showCurrentTime){
            float a=(float)Math.toRadians(angleForMinute(now));
            p.setColor(primary);p.setStrokeWidth(Math.max(3,getWidth()*0.012f));p.setStrokeCap(Paint.Cap.ROUND);
            c.drawLine(centerX,centerY,centerX+(float)Math.cos(a)*(R-2),centerY+(float)Math.sin(a)*(R-2),p);
            p.setStrokeCap(Paint.Cap.BUTT);c.drawCircle(centerX,centerY,7,p);
        }
        text.setTypeface(Typeface.create("sans",Typeface.BOLD));text.setTextSize(Math.max(26,getWidth()*0.075f));text.setColor(onSurface);
        c.drawText(showCurrentTime?TimeUtils.now():"24 h",centerX,centerY-5,text);
        text.setTypeface(Typeface.create("sans",Typeface.NORMAL));text.setTextSize(Math.max(13,getWidth()*0.034f));
        String current=showCurrentTime?findCurrent(now):"Tagesübersicht";
        c.drawText(current,centerX,centerY+27,text);
    }
    private String findCurrent(int now){for(Event e:events)if(now>=e.startMinute&&now<e.endMinute)return e.title; return "Freier Zeitraum";}
    @Override public boolean onTouchEvent(MotionEvent e){
        if(e.getAction()==MotionEvent.ACTION_DOWN){downX=e.getX();downY=e.getY();lastAngle=minuteForPoint(downX,downY);downTime=System.currentTimeMillis();dragging=false;movingIndex=findIndex(downX,downY);return true;}
        if(e.getAction()==MotionEvent.ACTION_MOVE && movingIndex>=0 && inCircle(e.getX(),e.getY())){
            float nowAngle=minuteForPoint(e.getX(),e.getY()); float delta=nowAngle-lastAngle; if(delta>720)delta-=1440;if(delta<-720)delta+=1440; if(delta>180)delta-=360;if(delta<-180)delta+=360;
            Event ev=events.get(movingIndex); int d=Math.round(delta/15f)*15;
            if(d!=0){
                int duration=ev.duration();
                int newStart=TimeUtils.clampStartMinute(ev.startMinute+d);
                int newEnd=newStart+duration;
                if(newEnd>1440){newEnd=1440;newStart=Math.max(0,newEnd-duration);}
                ev.startMinute=newStart; ev.endMinute=newEnd;
                lastAngle=nowAngle; dragging=true; invalidate();
            } return true;}
        if(e.getAction()==MotionEvent.ACTION_UP){long dt=System.currentTimeMillis()-downTime;float dx=e.getX()-downX;if(!dragging && Math.abs(dx)>120 && dt<700 && movingIndex<0){if(listener!=null)listener.onDateSwipe(dx<0?1:-1);return true;}if(movingIndex>=0){Event ev=events.get(movingIndex);if(dragging){if(listener!=null)listener.onEventMoved(ev);}else if(listener!=null)listener.onEventTap(ev);}movingIndex=-1;return true;}
        return true;
    }
    private int findIndex(float x,float y){if(!inCircle(x,y))return -1;float minute=minuteForPoint(x,y);for(int i=0;i<events.size();i++){Event ev=events.get(i);if(minute>=ev.startMinute&&minute<ev.endMinute)return i;}return -1;}
    private static class CalendarClock{int minute(){java.util.Calendar c=java.util.Calendar.getInstance();return c.get(java.util.Calendar.HOUR_OF_DAY)*60+c.get(java.util.Calendar.MINUTE);}}
}
