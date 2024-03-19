package com.cuneyt.gelirgider.entities;

public class EarningSpendingModel {
    private String id; // Verinin benzersiz numarası.
    private String title; // Verinin adı
    private int amounth; // Miktar
    private String type; // Verinin türü. (Maaş, gelir, birikim, gider)
    private String sort; // Verinin sıralanması
    private String year; // Geçerli yıl
    private String month; // Geçerli ay
    private String payment; // Giderleri ödenip ödenmediği bilgisi
    private String earningNote; // Ay ile ilgili gelir notu
    private String spendingNote; // Ay ile ilgili gider notu
    private String savingNote;


    public EarningSpendingModel() {
    }

    public EarningSpendingModel(String id, String earningNote, String spendingNote, String savingNote) {
        this.id = id;
        this.earningNote = earningNote;
        this.spendingNote = spendingNote;
        this.savingNote = savingNote;
    }

    public EarningSpendingModel(String id, String title, int amounth, String type, String sort, String year, String month, String payment) {
        this.id = id;
        this.title = title;
        this.amounth = amounth;
        this.type = type;
        this.sort = sort;
        this.year = year;
        this.month = month;
        this.payment = payment;
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

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
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

    public String getPayment() {
        return payment;
    }

    public void setPayment(String payment) {
        this.payment = payment;
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

    public String getSavingNote() {
        return savingNote;
    }

    public void setSavingNote(String savingNote) {
        this.savingNote = savingNote;
    }
}
