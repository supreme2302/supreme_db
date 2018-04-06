package api.db.Models;

public class PostFull {
    private User author;
    private Post post;
    private Thread thread;
    private Forum forum;

    public PostFull() {}
    public PostFull(User author, Post post, Thread thread, Forum forum) {
        this.author = author;
        this.post = post;
        this.thread = thread;
        this.forum = forum;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public Thread getThread() {
        return thread;
    }

    public void setThread(Thread thread) {
        this.thread = thread;
    }

    public Forum getForum() {
        return forum;
    }

    public void setForum(Forum forum) {
        this.forum = forum;
    }
}
