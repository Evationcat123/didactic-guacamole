package com.circledayplanner.app;

import android.os.Bundle;
import android.content.res.Configuration;
import android.graphics.Color;
import android.view.View;
import android.view.Gravity;
import android.widget.*;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.materialswitch.MaterialSwitch;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class SettingsActivity extends AppCompatActivity {
    private PlannerStore store; private LinearLayout root;
    private int dp(float x){return (int)(x*getResources().getDisplayMetrics().density+.5f);}
    private TextView text(String s,float size){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(0xFF1D1B20);return t;}
    @Override protected void onCreate(Bundle b){super.onCreate(b);store=PlannerStore.get(this);build();}
    private void build(){ScrollView scroll=new ScrollView(this);root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(16),dp(10),dp(16),dp(24));MaterialToolbar bar=new MaterialToolbar(this);bar.setTitle("Einstellungen");bar.setNavigationIcon(android.R.drawable.ic_media_previous);bar.setNavigationOnClickListener(v->finish());root.addView(bar,new LinearLayout.LayoutParams(-1,dp(60)));TextView intro=text("Passe Circle Day Planner an deinen Alltag an.",15);root.addView(intro,new LinearLayout.LayoutParams(-1,dp(50)));addSection("Darstellung");
        addChoice("Theme",new String[]{"Systemmodus","Hell","Dunkel"},store.setting("theme","Systemmodus"),v->{
            String mode=v.toString(); store.setSetting("theme",mode); applyMode(mode);
        });
        addChoice("Kreis-Stil",new String[]{"Premium","Minimal","High Contrast"},store.setting("ring","Premium"),v->{store.setSetting("ring",v.toString());});
        addChoice("Akzentfarbe",new String[]{"Violett","Blau","Grün","Orange","Pink"},store.setting("accent","Violett"),v->{store.setSetting("accent",v.toString());});
        addSwitch("Animationen",true,"animations");addSwitch("Erinnerungen",true,"notifications");
        addSection("Zeit & Kalender");addChoice("Zeitformat",new String[]{"24 Stunden","12 Stunden"},store.setting("timeformat","24 Stunden"),v->store.setSetting("timeformat",v.toString()));addChoice("Wochenstart",new String[]{"Montag","Sonntag"},store.setting("weekstart","Montag"),v->store.setSetting("weekstart",v.toString()));addChoice("Standarddauer",new String[]{"15 Minuten","30 Minuten","45 Minuten","60 Minuten"},store.setting("defaultDuration","30 Minuten"),v->store.setSetting("defaultDuration",v.toString()));
        addSection("Kreisdiagramm");addChoice("Kreisgröße",new String[]{"Kompakt","Standard","Groß"},store.setting("circleSize","Standard"),v->store.setSetting("circleSize",v.toString()));addChoice("Segmentstil",new String[]{"Voll","Donut","Soft"},store.setting("segmentStyle","Donut"),v->store.setSetting("segmentStyle",v.toString()));addChoice("Zeigerstil",new String[]{"Nadel","Punkt","Linie"},store.setting("handStyle","Nadel"),v->store.setSetting("handStyle",v.toString()));addChoice("Stundenmarkierungen",new String[]{"Alle Stunden","2-Stunden-Raster","Nur 6/12/18/24"},store.setting("ticks","Alle Stunden"),v->store.setSetting("ticks",v.toString()));addSwitch("Große Zeit im Mittelpunkt",true,"bigTime");
        addSection("Daten");MaterialButton info=new MaterialButton(this);info.setText("Lokale Speicherung · keine Cloud erforderlich");info.setEnabled(false);root.addView(info,new LinearLayout.LayoutParams(-1,dp(52)));scroll.addView(root);setContentView(scroll);applyMode(store.setting("theme","Systemmodus"));}
    private void addSection(String s){TextView t=text(s,17);t.setTypeface(null,1);t.setPadding(dp(5),dp(18),dp(5),dp(8));root.addView(t,new LinearLayout.LayoutParams(-1,dp(52)));}
    private void addSwitch(String title,boolean def,String key){MaterialSwitch sw=new MaterialSwitch(this);sw.setText(title);sw.setTextSize(16);sw.setChecked(Boolean.parseBoolean(store.setting(key,String.valueOf(def))));sw.setPadding(dp(6),dp(8),dp(6),dp(8));sw.setOnCheckedChangeListener((b,c)->store.setSetting(key,String.valueOf(c)));root.addView(sw,new LinearLayout.LayoutParams(-1,dp(60)));}
    private void addChoice(String title,String[] values,String current,java.util.function.Consumer<String> save){LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(6),dp(2),dp(6),dp(2));TextView t=text(title,16);row.addView(t,new LinearLayout.LayoutParams(0,dp(64),1));Spinner sp=new Spinner(this);sp.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,values));for(int i=0;i<values.length;i++)if(values[i].equals(current))sp.setSelection(i);sp.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){public void onNothingSelected(android.widget.AdapterView<?> p){}public void onItemSelected(android.widget.AdapterView<?> p,View v,int pos,long id){save.accept(values[pos]);}});row.addView(sp,new LinearLayout.LayoutParams(dp(170),dp(64)));root.addView(row,new LinearLayout.LayoutParams(-1,dp(68)));}
    private void applyMode(String mode){if(mode.equals("Hell")){getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);}else if(mode.equals("Dunkel")){getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);}else{getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);}}
}
