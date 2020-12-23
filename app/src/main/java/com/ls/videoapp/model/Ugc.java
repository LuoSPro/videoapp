package com.ls.videoapp.model;

import androidx.annotation.Nullable;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.library.baseAdapters.BR;

import java.io.Serializable;

/**
 * 帖子的点赞、分享、评论数量
 */
public class Ugc extends BaseObservable implements Serializable {
        /**
         * likeCount : 102
         * shareCount : 10
         * commentCount : 10
         * hasFavorite : false
         * hasLiked : false
         * hasdiss : false
         * hasDissed : false
         */

        private int likeCount;
        private int shareCount;
        private int commentCount;
        private boolean hasFavorite;
        private boolean hasLiked;
        private boolean hasdiss;
        private boolean hasDissed;

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null || !(obj instanceof Feed))
            return false;
        Ugc newUgc = (Ugc) obj;
        return likeCount == newUgc.likeCount&&
               shareCount == newUgc.shareCount&&
               commentCount == newUgc.commentCount&&
               hasFavorite == newUgc.hasFavorite&&
               hasLiked == newUgc.hasLiked&&
               hasdiss == newUgc.hasdiss;

    }

    public int getLikeCount() {
            return likeCount;
        }

        public void setLikeCount(int likeCount) {
            this.likeCount = likeCount;
        }

        public int getShareCount() {
            return shareCount;
        }

        public void setShareCount(int shareCount) {
            this.shareCount = shareCount;
        }

        public int getCommentCount() {
            return commentCount;
        }

        public void setCommentCount(int commentCount) {
            this.commentCount = commentCount;
        }

        public boolean isHasFavorite() {
            return hasFavorite;
        }

        public void setHasFavorite(boolean hasFavorite) {
            this.hasFavorite = hasFavorite;
        }

        @Bindable
        public boolean isHasLiked() {
            return hasLiked;
        }

        public void setHasLiked(boolean hasLiked) {
            if (this.hasLiked == hasLiked)
                return;
            if (this.hasLiked){
                likeCount = likeCount + 1;
                //取消 踩  的效果
                setHasdiss(false);
            }else{
                likeCount = likeCount - 1;
            }
            this.hasLiked = hasLiked;
            //告诉外面，这里有数据改变了，应该重新绑定数据
            notifyPropertyChanged(BR._all);
        }

        @Bindable
        public boolean isHasdiss() {
            return hasdiss;
        }

        /**
         * 点赞和踩是互斥的
         * @param hasdiss
         */
        public void setHasdiss(boolean hasdiss) {
            if (this.hasdiss == hasdiss)
                return;
            if (hasdiss){
                //取消  赞  的效果
                setHasLiked(false);
            }
            this.hasdiss = hasdiss;
        }

        public boolean isHasDissed() {
            return hasDissed;
        }

        public void setHasDissed(boolean hasDissed) {
            this.hasDissed = hasDissed;
        }
    }