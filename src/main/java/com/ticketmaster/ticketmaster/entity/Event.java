package com.ticketmaster.ticketmaster.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "events")
public class Event {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String description;
    private String category;
    private String location;
    private Double latitude;
    private Double longitude;
    private LocalDateTime eventDate;
    private Double minPrice;
    private Double maxPrice;

    public Long getId(){return id;}
    public void setId(Long id){this.id=id;}
    public String getTitle(){return title;}
    public void setTitle(String t){this.title=t;}
    public String getDescription(){return description;}
    public void setDescription(String d){this.description=d;}
    public String getCategory(){return category;}
    public void setCategory(String c){this.category=c;}
    public String getLocation(){return location;}
    public void setLocation(String l){this.location=l;}
    public Double getLatitude(){return latitude;}
    public void setLatitude(Double v){this.latitude=v;}
    public Double getLongitude(){return longitude;}
    public void setLongitude(Double v){this.longitude=v;}
    public LocalDateTime getEventDate(){return eventDate;}
    public void setEventDate(LocalDateTime d){this.eventDate=d;}
    public Double getMinPrice(){return minPrice;}
    public void setMinPrice(Double p){this.minPrice=p;}
    public Double getMaxPrice(){return maxPrice;}
    public void setMaxPrice(Double p){this.maxPrice=p;}
}
