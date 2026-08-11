package com.circledayplanner.app;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends AppCompatActivity implements PlannerView.Listener {
    private PlannerStore store;
    private PlannerView planner;
    private Calendar selected=Calendar.getInstance();
    private TextView dateText, progressText, nextText;
    private ActivityResultLauncher<String> notificationPermission;
    private final int[] palette={0xFF6750A4,0xFF00639A,0xFF2E7D5B,0xFFAA5D00,0xFFC53864,0xFF65558F,0xFF7A5962,0xFF3F6B8A};

    @Override protected void onCreate(Bundle b){
        super.onCreate(b);
        store=PlannerStore.get(this);
        applyStoredTheme();
        createChannel();
        notificationPermission=registerForActivityResult(new ActivityResultContracts.RequestPermission(), ok->{});
        buildUi();
        refresh();
    }

    @Override protected void onResume(){super.onResume(); if(store!=null)refresh();}

    private void createChannel(){
        NotificationManager nm=getSystemService(NotificationManager.class);
        if(nm!=null) nm.createNotificationChannel(new NotificationChannel(
                "planner","Terminerinnerungen",NotificationManager.IMPORTANCE_HIGH));
    }

    private int dp(float x){return (int)(x*getResources().getDisplayMetrics().density+.5f);}
    private int resolve(int attr){android.util.TypedValue v=new android.util.TypedValue();getTheme().resolveAttribute(attr,v,true);return v.data;}
    private TextView tv(String s,float size){
        TextView t=new TextView(this); t.setText(s); t.setTextSize(size);
        t.setTextColor(resolve(android.R.attr.textColorPrimary)); return t;
    }

    private void buildUi(){
        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16),dp(8),dp(16),dp(12));

        MaterialToolbar bar=new MaterialToolbar(this);
        bar.setTitle("Circle Day Planner");
        bar.setSubtitle("Dein Tag. Ein Kreis. Alles im Blick.");
        bar.setNavigationIcon(android.R.drawable.ic_menu_my_calendar);
        bar.setNavigationContentDescription("Kalender öffnen");
        bar.setNavigationOnClickListener(v->showCalendar());
        Menu menu=bar.getMenu();
        MenuItem settings=menu.add("Einstellungen");
        settings.setIcon(android.R.drawable.ic_menu_preferences);
        settings.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        settings.setOnMenuItemClickListener(x->{startActivity(new Intent(this,SettingsActivity.class));return true;});
        root.addView(bar,new LinearLayout.LayoutParams(-1,dp(66)));

        MaterialCardView summary=new MaterialCardView(this);
        summary.setRadius(dp(20)); summary.setCardElevation(0); summary.setUseCompatPadding(false);
        LinearLayout summaryBox=new LinearLayout(this);
        summaryBox.setOrientation(LinearLayout.VERTICAL);
        summaryBox.setPadding(dp(18),dp(14),dp(18),dp(14));
        dateText=tv("",20); dateText.setTypeface(null,1);
        progressText=tv("",13); progressText.setAlpha(.72f);
        nextText=tv("",13); nextText.setAlpha(.82f);
        summaryBox.addView(dateText);
        summaryBox.addView(progressText,new LinearLayout.LayoutParams(-1,dp(24)));
        summaryBox.addView(nextText,new LinearLayout.LayoutParams(-1,dp(24)));
        summary.addView(summaryBox);
        root.addView(summary,new LinearLayout.LayoutParams(-1,dp(92)));

        FrameLayout plannerFrame=new FrameLayout(this);
        plannerFrame.setPadding(0,dp(8),0,dp(2));
        planner=new PlannerView(this);
        planner.setListener(this);
        MaterialCardView plannerCard=new MaterialCardView(this);
        plannerCard.setRadius(dp(28)); plannerCard.setCardElevation(0);
        plannerCard.addView(planner,new FrameLayout.LayoutParams(-1,-1));
        plannerFrame.addView(plannerCard,new FrameLayout.LayoutParams(-1,-1));
        root.addView(plannerFrame,new LinearLayout.LayoutParams(-1,0,1));

        LinearLayout actions=new LinearLayout(this);
        actions.setGravity(Gravity.CENTER_VERTICAL);
        actions.setPadding(0,dp(8),0,0);

        MaterialButton calendar=new MaterialButton(this);
        calendar.setText("Kalender");
        calendar.setIconResource(android.R.drawable.ic_menu_my_calendar);
        calendar.setOnClickListener(v->showCalendar());
        actions.addView(calendar,new LinearLayout.LayoutParams(0,dp(52),1));

        Space gap=new Space(this);
        actions.addView(gap,new LinearLayout.LayoutParams(dp(10),1));

        MaterialButton add=new MaterialButton(this);
        add.setText("Termin");
        add.setIconResource(android.R.drawable.ic_input_add);
        add.setOnClickListener(v->showEditor(null));
        actions.addView(add,new LinearLayout.LayoutParams(0,dp(52),1));

        root.addView(actions);
        setContentView(root);
    }

    private boolean isToday(){
        Calendar now=Calendar.getInstance();
        return selected.get(Calendar.YEAR)==now.get(Calendar.YEAR)
                && selected.get(Calendar.DAY_OF_YEAR)==now.get(Calendar.DAY_OF_YEAR);
    }

    private void refresh(){
        String key=TimeUtils.dateKey(selected);
        dateText.setText(TimeUtils.header(selected));

        Calendar today=Calendar.getInstance();
        int mins;
        if(isToday()) mins=TimeUtils.nowMinute();
        else if(selected.before(today)) mins=1440;
        else mins=0;

        int percent=(int)Math.round(mins/14.4);
        progressText.setText(isToday()
                ? percent+" % des Tages vorbei  ·  "+TimeUtils.time(mins)+" von 24:00"
                : (mins==1440 ? "Tag abgeschlossen" : "Tag steht noch bevor"));

        Event next=null;
        for(Event e:store.forDate(key)){
            if(e.startMinute>=mins && (next==null||e.startMinute<next.startMinute)) next=e;
        }
        nextText.setText(next==null?"Keine weiteren Termine":"Als Nächstes  ·  "+TimeUtils.time(next.startMinute)+"  "+next.title);

        planner.setEvents(store.forDate(key));
        planner.setThemeColors(isLight(),getSurface(),accentColor(),resolve(android.R.attr.textColorPrimary));
        planner.setShowCurrentTime(isToday());
    }

    private boolean isLight(){
        int ui=getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
        return ui!=android.content.res.Configuration.UI_MODE_NIGHT_YES;
    }
    private int getSurface(){return isLight()?0xFFF9F7FC:0xFF121212;}
    private int accentColor(){
        String a=store.setting("accent","Violett");
        if("Blau".equals(a))return 0xFF00639A;
        if("Grün".equals(a))return 0xFF2E7D5B;
        if("Orange".equals(a))return 0xFFAA5D00;
        if("Pink".equals(a))return 0xFFC53864;
        return 0xFF6750A4;
    }
    private void applyStoredTheme(){
        String mode=store.setting("theme","Systemmodus");
        if("Hell".equals(mode))getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        else if("Dunkel".equals(mode))getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        else getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    @Override public void onEventTap(Event e){showEditor(e);}
    @Override public void onEventMoved(Event e){
        if(e.reminder) scheduleReminder(e); else cancelReminder(e);
        store.upsert(e);
        PlannerWidgetProvider.updateAll(this);
        refresh();
    }
    @Override public void onDateSwipe(int dir){selected.add(Calendar.DAY_OF_YEAR,dir);refresh();}

    private void showCalendar(){
        final BottomSheetDialog dialog=new BottomSheetDialog(this);
        LinearLayout box=new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(18),dp(16),dp(18),dp(24));

        TextView title=tv(new SimpleDateFormat("MMMM yyyy",Locale.getDefault()).format(selected.getTime()),21);
        title.setTypeface(null,1); box.addView(title);

        LinearLayout weekdays=new LinearLayout(this); weekdays.setWeightSum(7);
        String[] wd={"Mo","Di","Mi","Do","Fr","Sa","So"};
        for(String w:wd){TextView t=tv(w,12);t.setGravity(Gravity.CENTER);t.setAlpha(.65f);weekdays.addView(t,new LinearLayout.LayoutParams(0,dp(30),1));}
        box.addView(weekdays);

        Calendar first=(Calendar)selected.clone(); first.set(Calendar.DAY_OF_MONTH,1);
        int dayOffset=(first.get(Calendar.DAY_OF_WEEK)+5)%7;
        int max=first.getActualMaximum(Calendar.DAY_OF_MONTH);
        LinearLayout grid=new LinearLayout(this); grid.setOrientation(LinearLayout.VERTICAL);

        for(int i=0;i<42;i++){
            if(i%7==0){LinearLayout row=new LinearLayout(this);row.setWeightSum(7);grid.addView(row,new LinearLayout.LayoutParams(-1,dp(48)));}
            LinearLayout row=(LinearLayout)grid.getChildAt(grid.getChildCount()-1);
            int day=i-dayOffset+1;
            if(day<1||day>max){row.addView(new Space(this),new LinearLayout.LayoutParams(0,dp(44),1));continue;}

            final Calendar cell=(Calendar)first.clone(); cell.set(Calendar.DAY_OF_MONTH,day);
            String key=TimeUtils.dateKey(cell);
            MaterialButton b=new MaterialButton(this);
            b.setText(String.valueOf(day)); b.setTextSize(13); b.setInsetTop(0); b.setInsetBottom(0);
            b.setPadding(0,0,0,0);
            if(store.hasEvents(key)){b.setTextColor(Color.WHITE);b.setBackgroundColor(resolve(com.google.android.material.R.attr.colorPrimary));}
            b.setOnClickListener(v->{selected.setTime(cell.getTime());dialog.dismiss();refresh();});
            row.addView(b,new LinearLayout.LayoutParams(0,dp(44),1));
        }
        box.addView(grid);
        TextView hint=tv("Markierte Tage enthalten Termine.",12);hint.setAlpha(.62f);
        hint.setPadding(dp(4),dp(8),0,0); box.addView(hint);
        dialog.setContentView(box); dialog.show();
    }

    private TextInputLayout field(String hint,LinearLayout box,String value){
        TextInputLayout l=new TextInputLayout(this);
        l.setHint(hint);
        TextInputEditText e=new TextInputEditText(this); e.setText(value); e.setSingleLine(false);
        l.addView(e); box.addView(l,new LinearLayout.LayoutParams(-1,dp(68)));
        return l;
    }

    private void showEditor(@Nullable Event original){
        final Event e=original==null?new Event():original;
        if(original==null)e.date=TimeUtils.dateKey(selected);

        BottomSheetDialog dialog=new BottomSheetDialog(this);
        LinearLayout box=new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(22),dp(18),dp(22),dp(24));

        TextView title=tv(original==null?"Neuen Termin planen":"Termin bearbeiten",22);
        title.setTypeface(null,1); box.addView(title);

        TextView hint=tv("Zeiten lassen sich später auch direkt im Kreis verschieben.",12);
        hint.setAlpha(.62f); hint.setPadding(0,dp(4),0,dp(12)); box.addView(hint);

        TextInputLayout tl=field("Titel",box,e.title);
        TextInputLayout cat=field("Kategorie",box,e.category);
        TextInputLayout note=field("Notiz",box,e.note);

        LinearLayout times=new LinearLayout(this); times.setOrientation(LinearLayout.HORIZONTAL); times.setWeightSum(2);
        String[] timesArr=new String[97]; for(int i=0;i<96;i++)timesArr[i]=TimeUtils.time(i*15); timesArr[96]="24:00";
        ArrayAdapter<String> adapter=new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,timesArr);
        Spinner start=new Spinner(this); Spinner end=new Spinner(this);
        start.setAdapter(adapter); end.setAdapter(adapter);
        start.setSelection(Math.max(0,Math.min(95,e.startMinute/15)));
        end.setSelection(Math.max(1,Math.min(96,(e.endMinute+14)/15)));
        times.addView(start,new LinearLayout.LayoutParams(0,dp(58),1));
        times.addView(end,new LinearLayout.LayoutParams(0,dp(58),1));
        box.addView(times);

        Spinner colors=new Spinner(this);
        String[] names={"Violett","Blau","Grün","Orange","Pink","Indigo","Rose","Petrol"};
        colors.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,names));
        int ci=0;for(int i=0;i<palette.length;i++)if(palette[i]==e.color)ci=i;colors.setSelection(ci);
        box.addView(colors,new LinearLayout.LayoutParams(-1,dp(58)));

        CheckBox rem=new CheckBox(this); rem.setText("10 Minuten vorher erinnern"); rem.setChecked(e.reminder); box.addView(rem);

        MaterialButton save=new MaterialButton(this); save.setText("Termin speichern");
        save.setOnClickListener(v->{
            String titleValue=String.valueOf(((TextInputEditText)tl.getEditText()).getText()).trim();
            String categoryValue=String.valueOf(((TextInputEditText)cat.getEditText()).getText()).trim();
            String noteValue=String.valueOf(((TextInputEditText)note.getEditText()).getText()).trim();
            int startMinute=start.getSelectedItemPosition()*15;
            int endMinute=end.getSelectedItemPosition()*15;
            if(titleValue.isEmpty())titleValue="Termin";
            if(endMinute<=startMinute){
                ((TextInputEditText)tl.getEditText()).setError("Bitte eine Endzeit nach der Startzeit wählen.");
                return;
            }

            boolean oldReminder=e.reminder;
            if(oldReminder)cancelReminder(e);
            e.title=titleValue; e.category=categoryValue; e.note=noteValue;
            e.startMinute=startMinute; e.endMinute=endMinute;
            e.color=palette[colors.getSelectedItemPosition()];
            e.reminder=rem.isChecked();

            store.upsert(e);
            if(e.reminder) scheduleReminder(e);
            PlannerWidgetProvider.updateAll(this);
            refresh();
            dialog.dismiss();
        });
        box.addView(save,new LinearLayout.LayoutParams(-1,dp(54)));

        if(original!=null){
            MaterialButton del=new MaterialButton(this); del.setText("Termin löschen");
            del.setOnClickListener(v->new MaterialAlertDialogBuilder(this)
                    .setTitle("Termin löschen?")
                    .setMessage(e.title)
                    .setNegativeButton("Abbrechen",null)
                    .setPositiveButton("Löschen",(d,w)->{
                        store.delete(e.id); cancelReminder(e);
                        PlannerWidgetProvider.updateAll(this); refresh(); dialog.dismiss();
                    }).show());
            box.addView(del,new LinearLayout.LayoutParams(-1,dp(52)));
        }

        ScrollView scroll=new ScrollView(this);scroll.addView(box);
        dialog.setContentView(scroll);dialog.show();
    }

    private void scheduleReminder(Event e){
        if(!e.reminder||e.date==null||e.date.isEmpty())return;
        if(android.os.Build.VERSION.SDK_INT>=33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED){
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS);
        }

        Calendar c=Calendar.getInstance();
        String[] parts=e.date.split("-");
        try{
            c.set(Integer.parseInt(parts[0]),Integer.parseInt(parts[1])-1,Integer.parseInt(parts[2]),
                    e.startMinute/60,e.startMinute%60,0);
            c.set(Calendar.MILLISECOND,0);
            c.add(Calendar.MINUTE,-10);
        }catch(Exception ex){return;}

        if(c.before(Calendar.getInstance()))return;
        Intent in=new Intent(this,ReminderReceiver.class)
                .putExtra("title",e.title)
                .putExtra("text","In 10 Minuten beginnt "+e.title+".");
        PendingIntent pi=PendingIntent.getBroadcast(this,e.id.hashCode(),in,
                PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        android.app.AlarmManager am=getSystemService(android.app.AlarmManager.class);
        if(am!=null)am.setAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP,c.getTimeInMillis(),pi);
    }

    private void cancelReminder(Event e){
        Intent in=new Intent(this,ReminderReceiver.class);
        PendingIntent pi=PendingIntent.getBroadcast(this,e.id.hashCode(),in,
                PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        android.app.AlarmManager am=getSystemService(android.app.AlarmManager.class);
        if(am!=null)am.cancel(pi);
    }
}
