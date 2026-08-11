package com.circledayplanner.app;

import org.json.JSONException;
import org.json.JSONObject;

public class Event {
    public String id = java.util.UUID.randomUUID().toString();
    public String date;
    public String title = "Neuer Termin";
    public int startMinute = 9 * 60;
    public int endMinute = 10 * 60;
    public String category = "Sonstiges";
    public int color = 0xFF6750A4;
    public String icon = "event";
    public String note = "";
    public boolean reminder = false;
    public String reminderText = "";

    public JSONObject toJson() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("id", id); o.put("date", date); o.put("title", title);
        o.put("start", startMinute); o.put("end", endMinute);
        o.put("category", category); o.put("color", color); o.put("icon", icon);
        o.put("note", note); o.put("reminder", reminder); o.put("reminderText", reminderText);
        return o;
    }
    public static Event fromJson(JSONObject o) throws JSONException {
        Event e = new Event();
        e.id=o.optString("id", e.id); e.date=o.optString("date", ""); e.title=o.optString("title", e.title);
        e.startMinute = Math.max(0, Math.min(1439, o.optInt("start", e.startMinute)));
        e.endMinute = Math.max(e.startMinute + 1, Math.min(1440, o.optInt("end", e.endMinute)));
        e.category=o.optString("category", e.category); e.color=o.optInt("color", e.color);
        e.icon=o.optString("icon", e.icon); e.note=o.optString("note", e.note);
        e.reminder=o.optBoolean("reminder", false); e.reminderText=o.optString("reminderText", "");
        return e;
    }
    public int duration() { return Math.max(1, endMinute - startMinute); }
}
