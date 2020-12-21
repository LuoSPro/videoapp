package com.ls.videoapp.model;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import java.io.Serializable;

public class Comment implements Serializable {

    /**
     * id : 1092
     * itemId : 1578921365712
     * commentId : 1578922158910003
     * userId : 1578919786
     * commentType : 1
     * createTime : 1578922158913
     * commentCount : 0
     * likeCount : 1002
     * commentText : 2020å¹´æ–°å¹´å¿«ä¹é¸­~
     * imageUrl : null
     * videoUrl : null
     * width : 0
     * height : 0
     * hasLiked : false
     * author : {"id":1250,"userId":1578919786,"name":"ã\u20ac\u0081è\u201c\u2026å\u201c\u2013â\u2022°ä¼Šäººä¸ºè°\u0081ç¬\u2018","avatar":"http://qzapp.qlogo.cn/qzapp/101794421/FE41683AD4ECF91B7736CA9DB8104A5C/100","description":"è¿™æ˜¯ä¸\u20acå\u008fªç¥žç§˜çš\u201ejetpack","likeCount":3,"topCommentCount":0,"followCount":0,"followerCount":2,"qqOpenId":"FE41683AD4ECF91B7736CA9DB8104A5C","expires_time":1586695789903,"score":0,"historyCount":222,"commentCount":9,"favoriteCount":0,"feedCount":0,"hasFollow":false}
     * ugc : {"likeCount":102,"shareCount":10,"commentCount":10,"hasFavorite":false,"hasLiked":false,"hasdiss":false,"hasDissed":false}
     */

    private int id;
    private long itemId;
    private long commentId;
    private int userId;
    private int commentType;
    private long createTime;
    private int commentCount;
    private int likeCount;
    private String commentText;
    private Object imageUrl;
    private Object videoUrl;
    private int width;
    private int height;
    private boolean hasLiked;
    private User author;
    private Ugc ugc;

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null || !(obj instanceof Feed))
            return false;
        Comment newComment = (Comment) obj;
        return id == newComment.id &&
                itemId == newComment.itemId &&
                commentId == newComment.commentId &&
                userId == newComment.userId &&
                commentType == newComment.commentType &&
                createTime == newComment.createTime &&
                commentCount == newComment.commentCount &&
                likeCount == newComment.likeCount &&
                TextUtils.equals(commentText, newComment.commentText) &&
                imageUrl == newComment.imageUrl &&
                videoUrl == newComment.videoUrl &&
                width == newComment.width &&
                height == newComment.height &&
                hasLiked == newComment.hasLiked &&
                author == newComment.author;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getItemId() {
        return itemId;
    }

    public void setItemId(long itemId) {
        this.itemId = itemId;
    }

    public long getCommentId() {
        return commentId;
    }

    public void setCommentId(long commentId) {
        this.commentId = commentId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getCommentType() {
        return commentType;
    }

    public void setCommentType(int commentType) {
        this.commentType = commentType;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public Object getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(Object imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Object getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(Object videoUrl) {
        this.videoUrl = videoUrl;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public boolean isHasLiked() {
        return hasLiked;
    }

    public void setHasLiked(boolean hasLiked) {
        this.hasLiked = hasLiked;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public Ugc getUgc() {
        return ugc;
    }

    public void setUgc(Ugc ugc) {
        this.ugc = ugc;
    }
}
