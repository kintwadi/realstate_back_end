package com.imovel.api.model.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Lob;

@Embeddable
public class AccordionItem {

    @Column(name = "accordion_item_title")
    private String title;

    @Lob
    @Column(name = "accordion_item_details", columnDefinition = "TEXT")
    private String details;

    public AccordionItem() {}

    public AccordionItem(String title, String details) {
        this.title = title;
        this.details = details;
    }

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    @Override
    public String toString() {
        return "AccordionItem{" +
                "title='" + title + '\'' +
                ", details='" + (details != null ? details.substring(0, Math.min(details.length(), 20)) + "..." : "null") + '\'' +
                '}';
    }
}