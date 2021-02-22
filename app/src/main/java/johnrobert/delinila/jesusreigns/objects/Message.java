package johnrobert.delinila.jesusreigns.objects;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Message {
    private @ServerTimestamp Date timestamp;
    private String message, uid, msgId, imgUrl;

    public Message() {}

    public Message(Date timestamp, String message, String uid, String msgId, String imgUrl) {
        this.timestamp = timestamp;
        this.message = message;
        this.uid = uid;
        this.msgId = msgId;
        this.imgUrl = imgUrl;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }
}
