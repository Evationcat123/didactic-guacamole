package com.circledayplanner.app;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class PlannerStore {
    private static final String PREF = "circle_planner";
    private static final String KEY_EVENTS = "events";
    private static final String KEY_SETTINGS = "settings";
    private static PlannerStore instance;
    private final SharedPreferences prefs;
    private final ArrayList<Event> events = new ArrayList<>();

    private PlannerStore(Context c) { prefs=c.getSharedPreferences(PREF, Context.MODE_PRIVATE); load(); }
    public static synchronized PlannerStore get(Context c) { if(instance==null) instance=new PlannerStore(c.getApplicationContext()); return instance; }
    private void load() {
        events.clear();
        try { JSONArray a=new JSONArray(prefs.getString(KEY_EVENTS,"[]")); for(int i=0;i<a.length();i++) events.add(Event.fromJson(a.getJSONObject(i))); } catch(Exception ignored) {}
    }
    private void persist() {
        JSONArray a=new JSONArray(); for(Event e:events) try { a.put(e.toJson()); } catch(Exception ignored) {}
        prefs.edit().putString(KEY_EVENTS,a.toString()).apply();
    }
    public synchronized List<Event> forDate(String date) {
        ArrayList<Event> out=new ArrayList<>(); for(Event e:events) if(date.equals(e.date)) out.add(e);
        Collections.sort(out, Comparator.comparingInt(x->x.startMinute)); return out;
    }
    public synchronized List<Event> all() { return new ArrayList<>(events); }
    public synchronized void upsert(Event e) { for(int i=0;i<events.size();i++) if(events.get(i).id.equals(e.id)){events.set(i,e);persist();return;} events.add(e);persist(); }
    public synchronized void delete(String id) { events.removeIf(e->e.id.equals(id)); persist(); }
    public synchronized boolean hasEvents(String date) { for(Event e:events) if(date.equals(e.date)) return true; return false; }
    public synchronized void setSetting(String key,String val){prefs.edit().putString(KEY_SETTINGS+key,val).apply();}
    public synchronized String setting(String key,String def){return prefs.getString(KEY_SETTINGS+key,def);}
}
