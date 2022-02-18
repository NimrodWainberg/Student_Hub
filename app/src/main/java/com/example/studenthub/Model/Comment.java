package com.example.studenthub.Model;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Comment {

    private String comment;
    private String publisher;
    private String commentId;

    public Comment(String comment, String publisher,String commentId) {
        this.comment = comment;
        this.publisher = publisher;
        this.commentId = commentId;
    }

    public Comment() {
        // Empty default constructor
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getPublisher() {
        return publisher;
    }
    public String getCommentId()
    {
        return commentId;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }
}
