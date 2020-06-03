/**
 * Author: Michael Canche
 * Dartmouth College, Spring 2020, Professor Campbell
 */
package edu.dartmouth.cs65.searchandrescue.code.structures;

import java.util.Date;

//Stores content for a chat message
public class ChatMessage {
    private String user;
    private String text;
    private String userID;
    private long time;

    public ChatMessage() { }

    public ChatMessage(String user, String messageText, String messageUserId) {
        this.user = user;
        this.text = messageText;
        time = new Date().getTime();
        this.userID = messageUserId;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

}