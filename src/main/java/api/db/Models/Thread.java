package api.db.Models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.sql.Timestamp;
import java.util.Date;

public class Thread {

    private String author;
    //private Timestamp created;
    private String forum;
    private int id;
    private String message;
    private String slug;
    private String title;
    private int votes;
    private int forumid;

    //TODO:: Пофиксить время!
//
//    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
//    private Date created;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
    private Timestamp created;

    public Thread() {}

    @JsonCreator
    public Thread (@JsonProperty("author") String author,
                   @JsonProperty("created") Timestamp created,
                   @JsonProperty("forum") String forum,
                   @JsonProperty("id") int id,
                   @JsonProperty("message") String message,
                   @JsonProperty("slug") String slug,
                   @JsonProperty("title") String title,
                   @JsonProperty("votes") int votes,
                   @JsonProperty("forumid") int forumid) {
        this.author = author;
        this.forum = forum;
        this.id = id;
        this.message = message;
        this.slug = slug;
        this.title = title;
        this.votes = votes;
        this.created = created;
        this.forumid = forumid;
//        if (created == null) {
//            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
//            this.created = timestamp.toInstant().toString();
//            this.created = null;
//        } else {
//            this.created = created;
//        }
    }


    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Timestamp getCreated() {
        return created;
    }

    public void setCreated(Timestamp created) {
        this.created = created;
    }

    public String getForum() {
        return forum;
    }

    public void setForum(String forum) {
        this.forum = forum;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getVotes() {
        return votes;
    }

    public void setVotes(int votes) {
        this.votes = votes;
    }

    public int getForumid() {
        return forumid;
    }

    public void setForumid(int forumid) {
        this.forumid = forumid;
    }
}
