package johnrobert.delinila.jesusreigns.objects;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class LineUp {

    private Map<String, Map<String, String>> events;
    private Map<String, List<String>> songs;
    private List<String> invited_uids;
    private String uid;
    private @ServerTimestamp Date date_created;
    private String timestamp;

    public LineUp(){}

    public LineUp(Map<String, Map<String, String>> events, Map<String, List<String>> songs, List<String> invited_uids, String uid, String timestamp) {
        this.events = events;
        this.songs = songs;
        this.invited_uids = invited_uids;
        this.uid = uid;
        this.timestamp = timestamp;
    }

    public Date getDate_created() {
        return date_created;
    }

    public void setDate_created(Date date_created) {
        this.date_created = date_created;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, Map<String, String>> getEvents() {
        return events;
    }

    public void setEvents(Map<String, Map<String, String>> events) {
        this.events = events;
    }

    public Map<String, List<String>> getSongs() {
        return songs;
    }

    public void setSongs(Map<String, List<String>> songs) {
        this.songs = songs;
    }

    public List<String> getInvited_uids() {
        return invited_uids;
    }

    public void setInvited_uids(List<String> invited_uids) {
        this.invited_uids = invited_uids;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Date getDate() {
        return date_created;
    }

    public void setDate(Date date) {
        this.date_created = date;
    }
}
