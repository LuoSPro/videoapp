package com.ls.videoapp.model;

import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import java.io.Serializable;

/**
 * 帖子
 */
public class Feed extends BaseObservable implements Serializable {

    public static final int TYPE_IMAGE = 1;
    public static final int TYPE_VIDEO = 2;
    /**
     * id : 428
     * itemId : 1578976510452
     * itemType : 2
     * createTime : 1578977844500
     * duration : 8
     * feeds_text : 2020ä»–æ¥äº†ï¼Œå°±åœ¨çœ¼å‰äº†
     * authorId : 1578919786
     * activityIcon : null
     * activityText : 2020æ–°å¹´å¿«ä¹
     * width : 960
     * height : 540
     * url : https://pipijoke.oss-cn-hangzhou.aliyuncs.com/New%20Year%20-%2029212-video.mp4
     * cover : https://pipijoke.oss-cn-hangzhou.aliyuncs.com/2020%E5%B0%81%E9%9D%A2%E5%9B%BE.png
     */

    private int id;
    private long itemId;
    private int itemType;
    private long createTime;
    private int duration;
    private String feeds_text;
    private int authorId;
    private String activityIcon;
    private String activityText;
    private int width;
    private int height;
    private String url;
    private String cover;

    private User author;
    private Comment topComment;
    private Ugc ugc;

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null || !(obj instanceof Feed))
            return false;
        Feed newFeed = (Feed) obj;

        return id == newFeed.id &&
                itemId == newFeed.itemId &&
                itemType == newFeed.itemType &&
                createTime == newFeed.createTime &&
                duration == newFeed.duration &&
                TextUtils.equals(feeds_text, newFeed.feeds_text) &&
                authorId == newFeed.authorId &&
                TextUtils.equals(activityIcon, newFeed.activityIcon) &&
                TextUtils.equals(activityText, newFeed.activityText) &&
                width == newFeed.width &&
                height == newFeed.height &&
                TextUtils.equals(url, newFeed.url) &&
                TextUtils.equals(cover, newFeed.cover) &&
                (author != null && author.equals(newFeed.author)) &&
                (topComment != null && topComment.equals(newFeed.topComment)) &&
                (ugc != null && ugc.equals(newFeed.ugc));


    }

    public void setActivityIcon(String activityIcon) {
        this.activityIcon = activityIcon;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public Comment getTopComment() {
        return topComment;
    }

    public void setTopComment(Comment topComment) {
        this.topComment = topComment;
    }

    @Bindable
    public Ugc getUgc() {
        if (ugc == null){
            ugc = new Ugc();
        }
        return ugc;
    }

    public void setUgc(Ugc ugc) {
        this.ugc = ugc;
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

    public int getItemType() {
        return itemType;
    }

    public void setItemType(int itemType) {
        this.itemType = itemType;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getFeeds_text() {
        return feeds_text;
    }

    public void setFeeds_text(String feeds_text) {
        this.feeds_text = feeds_text;
    }

    public int getAuthorId() {
        return authorId;
    }

    public void setAuthorId(int authorId) {
        this.authorId = authorId;
    }

    public String getActivityText() {
        return activityText;
    }

    public void setActivityText(String activityText) {
        this.activityText = activityText;
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }
}
