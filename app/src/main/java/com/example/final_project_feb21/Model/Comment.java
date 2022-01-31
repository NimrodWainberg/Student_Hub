package com.example.final_project_feb21.Model;

public class Comment {

    private String comment;
    private String publisher;
    private String commentid;

    public Comment(String comment, String publisher,String commentid) {
        this.comment = comment;
        this.publisher = publisher;
        this.commentid = commentid;
    }

    public Comment() {
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
    public String getCommentid()
    {
        return commentid;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }
}
