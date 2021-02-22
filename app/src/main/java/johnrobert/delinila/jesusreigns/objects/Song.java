package johnrobert.delinila.jesusreigns.objects;

public class Song {
    private String title, artist, speed, note, personal_note, link, uid, displayName, id;

    public Song() {}

    public Song(String title, String artist, String speed, String note, String personal_note, String link, String uid, String displayName) {
        this.title = title;
        this.artist = artist;
        this.speed = speed;
        this.note = note;
        this.personal_note = personal_note;
        this.link = link;
        this.uid = uid;
        this.displayName = displayName;
    }

    public String getUid() {
        return uid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getPersonal_note() {
        return personal_note;
    }

    public void setPersonal_note(String personal_note) {
        this.personal_note = personal_note;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
