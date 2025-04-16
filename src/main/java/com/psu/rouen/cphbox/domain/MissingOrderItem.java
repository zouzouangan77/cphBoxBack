package com.psu.rouen.cphbox.domain;

public class MissingOrderItem {
    private String book;
    private int missingQuantity;
    private String reason;

    // Getters et Setters
    public String getBook() {
        return book;
    }

    public void setBook(String book) {
        this.book = book;
    }

    public int getMissingQuantity() {
        return missingQuantity;
    }

    public void setMissingQuantity(int missingQuantity) {
        this.missingQuantity = missingQuantity;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        return "MissingItem{" +
            "book='" + book + '\'' +
            ", missingQuantity=" + missingQuantity +
            ", reason='" + reason + '\'' +
            '}';
    }
}
