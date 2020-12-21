package com.ls.videoapp.model;

import androidx.annotation.Nullable;

/**
 * 帖子的点赞、分享、评论数量
 */
public class Ugc {
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

        public boolean isHasLiked() {
            return hasLiked;
        }

        public void setHasLiked(boolean hasLiked) {
            this.hasLiked = hasLiked;
        }

        public boolean isHasdiss() {
            return hasdiss;
        }

        public void setHasdiss(boolean hasdiss) {
            this.hasdiss = hasdiss;
        }

        public boolean isHasDissed() {
            return hasDissed;
        }

        public void setHasDissed(boolean hasDissed) {
            this.hasDissed = hasDissed;
        }
    }