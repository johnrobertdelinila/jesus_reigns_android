package johnrobert.delinila.jesusreigns.objects;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;
import java.util.Map;

public class Notification {

    private String collection, doc_id, title, content, id;
    private Map<String, String> user_metadata;
    private Object data;
    private @ServerTimestamp Date timestamp;
    private Boolean isResolved = false, isSeen = false;

    public Notification() {}

    public Notification(String collection, String doc_id, String title, String content, String id, Map<String, String> user_metadata, Object data, Date timestamp, Boolean isResolved, Boolean isSeen) {
        this.collection = collection;
        this.doc_id = doc_id;
        this.title = title;
        this.content = content;
        this.id = id;
        this.user_metadata = user_metadata;
        this.data = data;
        this.timestamp = timestamp;
        this.isResolved = isResolved;
        this.isSeen = isSeen;
    }

    public Boolean getSeen() {
        return isSeen;
    }

    public void setSeen(Boolean seen) {
        isSeen = seen;
    }

    public Boolean getResolved() {
        return isResolved;
    }

    public void setResolved(Boolean resolved) {
        isResolved = resolved;
    }

    public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    public String getDoc_id() {
        return doc_id;
    }

    public void setDoc_id(String doc_id) {
        this.doc_id = doc_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, String> getUser_metadata() {
        return user_metadata;
    }

    public void setUser_metadata(Map<String, String> user_metadata) {
        this.user_metadata = user_metadata;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
