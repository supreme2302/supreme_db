package api.db.Models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Forum {
    private int id;
    private long posts;
    private String slug;
    private int threads;
    private String title;
    private String author;

    public Forum() {}

    @JsonCreator
    public Forum(@JsonProperty("id") int id,
                 @JsonProperty("posts") int posts,
                 @JsonProperty("slug") String slug,
                 @JsonProperty("threads") int threads,
                 @JsonProperty("title") String title,
                 @JsonProperty("author") String author) {
        this.id = id;
        this.posts = posts;
        this.slug = slug;
        this.threads = threads;
        this.title = title;
        this.author = author;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public int getThreads() {
        return threads;
    }

    public void setThreads(int threads) {
        this.threads = threads;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public long getPosts() {
        return posts;
    }

    public void setPosts(long posts) {
        this.posts = posts;
    }
}
