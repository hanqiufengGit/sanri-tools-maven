package com.sanri.app.dtos;

public class NovelBook {
    private String title;
    private String updateTime;
    private String category;
    private String author;
    private String logo;
    private String link;
    private String describe;
    /**
     * 最新章节链接和标题
     */
    private String lastChapterTitle;
    private String lastChapterLink;

    public NovelBook(String title, String author, String link, String lastChapterTitle, String lastChapterLink) {
        this.title = title;
        this.author = author;
        this.link = link;
        this.lastChapterTitle = lastChapterTitle;
        this.lastChapterLink = lastChapterLink;
    }

    public NovelBook() {
    }

    public NovelBook(String title, String updateTime, String category, String author, String logo, String link, String describe, String lastChapterTitle, String lastChapterLink) {
        this.title = title;
        this.updateTime = updateTime;
        this.category = category;
        this.author = author;
        this.logo = logo;
        this.link = link;
        this.describe = describe;
        this.lastChapterTitle = lastChapterTitle;
        this.lastChapterLink = lastChapterLink;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    public String getLastChapterTitle() {
        return lastChapterTitle;
    }

    public void setLastChapterTitle(String lastChapterTitle) {
        this.lastChapterTitle = lastChapterTitle;
    }

    public String getLastChapterLink() {
        return lastChapterLink;
    }

    public void setLastChapterLink(String lastChapterLink) {
        this.lastChapterLink = lastChapterLink;
    }
}
