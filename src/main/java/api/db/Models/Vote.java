package api.db.Models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Vote {

    private Integer id;
    private String nickname;
    private Integer voice;
    private Integer threadid;

    public Vote() {}
    public Vote(int id, String nickname, int voice, int threadid) {
        this.id = id;
        this.nickname = nickname;
        this.voice = voice;
        this.threadid = threadid;
    }

    @JsonCreator
    public Vote(@JsonProperty("id") Integer id,
                @JsonProperty("nickname") String nickname,
                @JsonProperty("voice") Integer voice,
                @JsonProperty("threadid") Integer threadid) {
        this.id = id;
        this.nickname = nickname;
        this.voice = voice;
        this.threadid = threadid;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Integer getVoice() {
        return voice;
    }

    public void setVoice(Integer voice) {
        this.voice = voice;
    }

    public Integer getThreadid() {
        return threadid;
    }

    public void setThreadid(Integer threadid) {
        this.threadid = threadid;
    }
}
