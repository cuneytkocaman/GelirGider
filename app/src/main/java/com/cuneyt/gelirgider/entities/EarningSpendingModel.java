package com.cuneyt.gelirgider.entities;

public class EarningSpendingModel {
    private String id;
    private String title;
    private int amounth;
    private String type;
    private int number;
    private String year;
    private String month;
    private String earningNote;
    private String spendingNote;


    public EarningSpendingModel() {
    }

    public EarningSpendingModel(String id, String earningNote, String spendingNote) {
        this.id = id;
        this.earningNote = earningNote;
        this.spendingNote = spendingNote;
    }

    public EarningSpendingModel(String id, String title, int amounth, String type, int number, String year, String month) {
        this.id = id;
        this.title = title;
        this.amounth = amounth;
        this.type = type;
        this.number = number;
        this.year = year;
        this.month = month;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getAmounth() {
        return amounth;
    }

    public void setAmounth(int amounth) {
        this.amounth = amounth;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getEarningNote() {
        return earningNote;
    }

    public void setEarningNote(String earningNote) {
        this.earningNote = earningNote;
    }

    public String getSpendingNote() {
        return spendingNote;
    }

    public void setSpendingNote(String spendingNote) {
        this.spendingNote = spendingNote;
    }

}
