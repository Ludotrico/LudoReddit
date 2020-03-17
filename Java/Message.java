package com.example.ludoreddit;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.text.SimpleDateFormat;


public class Message {

    private String contents;
    private String name;
    private String time;
    private int upvotes;

    private String ID;

    boolean reply;
    int replyCount;
    int parentIndex;
    int index;



    public Message(String contents, String name, String key, boolean b, int num, int pIndex, int i ) {
        this.contents = contents;
        this.name = name;
        ID = key;
        reply = b;
        replyCount = num;

        parentIndex = pIndex;
        index = i;


        SimpleDateFormat sdf = new SimpleDateFormat("(dd/MM/yyyy) HH:mm");
        String time = sdf.format(new Date());

        upvotes = 0;
    }

    public Message() {

    }


    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public int getUpvotes() {
        return upvotes;
    }

    public void setUpvotes(int upvotes) {
        this.upvotes = upvotes;
    }

    public void incrementUpvote() {
        upvotes++;
    }

    public String getID() {
        return ID;
    }


    public void setID(String ID) {
        this.ID = ID;
    }


    public void setReply(boolean reply) {
        this.reply = reply;
    }
    public boolean getReply() {
        return reply; }

    public int getReplyCount() {
        return replyCount;
    }

    public void setReplyCount(int replyCount) {
        this.replyCount = replyCount;
    }



    public int getParentIndex() {
        return parentIndex;
    }

    public void incrementParentIndex() {
        parentIndex++;
    }

    public void decrementParentIndex() {
        parentIndex--;
    }

    public void setParentIndex(int parentIndex) {
        this.parentIndex = parentIndex;
    }


    public void decrementReplyCount() { replyCount--; }

    public void incrementReplyCount() { replyCount++; }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void incrementIndex() {
        index++;
    }

    public void decrementIndex() {
        index--;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTime() {
        return time;
    }
}

