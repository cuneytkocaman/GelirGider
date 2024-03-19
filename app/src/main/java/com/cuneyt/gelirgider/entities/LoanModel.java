package com.cuneyt.gelirgider.entities;

public class LoanModel {

    private String id;
    private String person;
    private String loan;
    private int amounth;
    private String loanType;
    private String updateDate;
    private String sort;
    private String note;

    public LoanModel() {
    }

    public LoanModel(String id, String person) {
        this.id = id;
        this.person = person;
    }

    public LoanModel(String id, String updateDate, String note) {
        this.id = id;
        this.updateDate = updateDate;
        this.note = note;
    }

    public LoanModel(String id, String person, String loan, int amounth, String loanType, String updateDate, String sort) {
        this.id = id;
        this.person = person;
        this.loan = loan;
        this.amounth = amounth;
        this.loanType = loanType;
        this.updateDate = updateDate;
        this.sort = sort;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPerson() {
        return person;
    }

    public void setPerson(String person) {
        this.person = person;
    }

    public String getLoan() {
        return loan;
    }

    public void setLoan(String loan) {
        this.loan = loan;
    }

    public int getAmounth() {
        return amounth;
    }

    public void setAmounth(int amounth) {
        this.amounth = amounth;
    }

    public String getLoanType() {
        return loanType;
    }

    public void setLoanType(String loanType) {
        this.loanType = loanType;
    }

    public String getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(String updateDate) {
        this.updateDate = updateDate;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
