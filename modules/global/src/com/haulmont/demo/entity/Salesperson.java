package com.haulmont.demo.entity;

import com.haulmont.chile.core.annotations.MetaProperty;
import com.haulmont.chile.core.annotations.NamePattern;
import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.cuba.core.entity.annotation.Listeners;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@NamePattern("%s|name")
@Table(name = "LEAFLETDEMO_SALESPERSON")
@Entity(name = "leafletdemo$Salesperson")
@Listeners("leafletdemo_SalespersonListener")
public class Salesperson extends StandardEntity {
    @NotNull
    @Column(name = "FIRST_NAME", nullable = false)
    protected String name;

    @Column(name = "PHONE", nullable = false)
    protected String phone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PHOTO_ID")
    protected FileDescriptor photo;

    @NotNull
    @Column(name = "LATITUDE", nullable = false)
    @MetaProperty(datatype = GeoCoordinateDatatype.NAME)
    protected Double latitude;

    @NotNull
    @Column(name = "LONGITUDE", nullable = false)
    @MetaProperty(datatype = GeoCoordinateDatatype.NAME)
    protected Double longitude;

    public FileDescriptor getPhoto() {
        return photo;
    }

    public void setPhoto(FileDescriptor photo) {
        this.photo = photo;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}