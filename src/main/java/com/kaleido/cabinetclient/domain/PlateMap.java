package com.kaleido.cabinetclient.domain;
import com.kaleido.cabinetclient.domain.enumeration.Status;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;


/**
 * A PlateMap.
 */
public class PlateMap implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Status status;

    private ZonedDateTime lastModified;

    /**
     * The checksum is used when saving a new draft, as the last checksum has to be passed\nand match the most recent timestamp. Otherwise it is considered attempting to save a stale draft
     */
    private String checksum;

    /**
     * The name of the activity. Used for grouping on
     */
    private String activityName;

    /**
     * The data field is a gzip -> base64 encoded string of the plate map data
     */
    private String data;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Status getStatus() {
        return status;
    }

    public PlateMap status(Status status) {
        this.status = status;
        return this;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public ZonedDateTime getLastModified() {
        return lastModified;
    }

    public PlateMap lastModified(ZonedDateTime lastModified) {
        this.lastModified = lastModified;
        return this;
    }

    public void setLastModified(ZonedDateTime lastModified) {
        this.lastModified = lastModified;
    }

    public String getChecksum() {
        return checksum;
    }

    public PlateMap checksum(String checksum) {
        this.checksum = checksum;
        return this;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public String getActivityName() {
        return activityName;
    }

    public PlateMap activityName(String activityName) {
        this.activityName = activityName;
        return this;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public String getData() {
        return data;
    }

    public PlateMap data(String data) {
        this.data = data;
        return this;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PlateMap)) {
            return false;
        }
        return id != null && id.equals(((PlateMap) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "PlateMap{" +
                "id=" + getId() +
                ", status='" + getStatus() + "'" +
                ", lastModified='" + getLastModified() + "'" +
                ", checksum='" + getChecksum() + "'" +
                ", activityName='" + getActivityName() + "'" +
                ", data='" + getData() + "'" +
                "}";
    }

    public String prepareStringForChecksum() {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        return getStatus().toString() + getLastModified().format(formatter)+getActivityName()+getData();
    }
}
