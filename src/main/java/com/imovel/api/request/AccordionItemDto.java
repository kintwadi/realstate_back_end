package com.imovel.api.request;

public class AccordionItemDto {
    private String title;
    private String details;

    // Constructors, Getters, and Setters
    public AccordionItemDto() {}
    public AccordionItemDto(String title, String details) {
        this.title = title;
        this.details = details;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}