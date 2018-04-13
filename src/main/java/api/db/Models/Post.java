package api.db.Models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.sql.Timestamp;

public class Post {
    private Long id;
    private String author;
    private String forum;
    private String message;

    //@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
    private String created;
    private Boolean isEdited;
    private Long parent;
    private Long thread;
    private Object[] path;

    public Post() {}

    @JsonCreator
    public Post(@JsonProperty("id") Long id,
                @JsonProperty("author") String author,
                @JsonProperty("forum") String forum,
                @JsonProperty("message") String message,
                @JsonProperty("created") Timestamp created,
                @JsonProperty("isEdited") Boolean isEdited,
                @JsonProperty("parent") Long parent,
                @JsonProperty("thread") Long thread,
                @JsonProperty("path") Object[] path) {
        this.id = id;
        this.author = author;
        this.forum = forum;
        this.message = message;
        this.isEdited = isEdited;
        this.path = path;
        if (parent == null) {
            this.parent = (long)0;
        }
        else {
            this.parent = parent;
        }

        this.thread = thread;

        if (created == null) {
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            this.created = timestamp.toInstant().toString();
        } else {
            this.created = created.toInstant().toString();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getForum() {
        return forum;
    }

    public void setForum(String forum) {
        this.forum = forum;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }


    public Long getParent() {
        return parent;
    }

    public void setParent(Long parent) {
        this.parent = parent;
    }

    public Long getThread() {
        return thread;
    }

    public void setThread(Long thread) {
        this.thread = thread;
    }

    public Object[] getPath() {
        return path;
    }

    public void setPath(Object[] path) {
        this.path = path;
    }

    public Boolean getIsEdited() {
        return isEdited;
    }

    public void setIsEdited(Boolean isEdited) {
        this.isEdited = isEdited;
    }
}
