package johnrobert.delinila.jesusreigns.objects;

import java.io.Serializable;

public class People implements Serializable {
    String displayName, uid, ministry, photoURL;

    public People() {}

    public People(String displayName, String uid, String ministry, String photoURL) {
        this.displayName = displayName;
        this.uid = uid;
        this.ministry = ministry;
        this.photoURL = photoURL;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getMinistry() {
        return ministry;
    }

    public void setMinistry(String ministry) {
        this.ministry = ministry;
    }

    public String getPhotoURL() {
        return photoURL;
    }

    public void setPhotoURL(String photoURL) {
        this.photoURL = photoURL;
    }
}
