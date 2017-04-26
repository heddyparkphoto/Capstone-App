package com.is.was.be.wannareddit;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by hyeryungpark on 4/5/17.
 */

public class MainPost implements Parcelable {

    // about vars
    String postId;
    String postSubreddit;
    String author;

    // stats
    long createdUtcTime;
    int numComments;

    // main content
    String postTitleLarge;

    // vars for more info
    String thumburl;
    String userUrl;

    // Flag for use
    private int media;          // 1 if video can be played, -1 if not

    public MainPost(){
        super();
    }

    protected MainPost(Parcel in) {
        // about post
        postId = in.readString();
        postSubreddit = in.readString();
        author = in.readString();

        // stats
        createdUtcTime = in.readLong();
        numComments = in.readInt();

        // main content
        postTitleLarge = in.readString();

        // for more info
        thumburl = in.readString();
        userUrl = in.readString();
        media = in.readInt();
    }

    public static final Creator<MainPost> CREATOR = new Creator<MainPost>() {
        @Override
        public MainPost createFromParcel(Parcel in) {
            return new MainPost(in);
        }

        @Override
        public MainPost[] newArray(int size) {
            return new MainPost[size];
        }
    };

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getPostTitleLarge() {
        return postTitleLarge;
    }

    public void setPostTitleLarge(String postTitleLarge) {
        this.postTitleLarge = postTitleLarge;
    }

    public String getThumburl() {
        return thumburl;
    }

    public void setThumburl(String thumburl) {
        this.thumburl = thumburl;
    }

    public String getPostSubreddit() {
        return postSubreddit;
    }

    public void setPostSubreddit(String postSubreddit) {
        this.postSubreddit = postSubreddit;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public long getCreatedUtcTime() {
        return createdUtcTime;
    }

    public void setCreatedUtcTime(long createdUtcTime) {
        this.createdUtcTime = createdUtcTime;
    }

    public int getNumComments() {
        return numComments;
    }

    public void setNumComments(int numComments) {
        this.numComments = numComments;
    }

    public String getUserUrl() {
        return userUrl;
    }

    public void setUserUrl(String userUrl) {
        this.userUrl = userUrl;
    }

    public int getMedia() {
        return media;
    }

    public void setMedia(int media) {
        this.media = media;
    }

    @Override
    public String toString(){
        return postTitleLarge;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(postId);
        parcel.writeString(postSubreddit);
        parcel.writeString(author);

        parcel.writeLong(createdUtcTime);
        parcel.writeInt(numComments);

        parcel.writeString(postTitleLarge);

        parcel.writeString(thumburl);
        parcel.writeString(userUrl);

        parcel.writeInt(media);
    }
}
