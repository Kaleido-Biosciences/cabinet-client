package com.kaleido.cabinetclient.domain;

import javax.validation.constraints.*;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import com.kaleido.cabinetclient.domain.enumeration.Status;

/**
 * A CabinetPlateMap.
 */
public class CabinetPlateMap implements Serializable {

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

    /**
     * The number of plates that are in the CabinetPlateMap
     */
    private Integer numPlates;

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

    public CabinetPlateMap status(Status status) {
        this.status = status;
        return this;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public ZonedDateTime getLastModified() {
        return lastModified;
    }

    public CabinetPlateMap lastModified(ZonedDateTime lastModified) {
        this.lastModified = lastModified;
        return this;
    }

    public void setLastModified(ZonedDateTime lastModified) {
        this.lastModified = lastModified;
    }

    public String getChecksum() {
        return checksum;
    }

    public CabinetPlateMap checksum(String checksum) {
        this.checksum = checksum;
        return this;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public String getActivityName() {
        return activityName;
    }

    public CabinetPlateMap activityName(String activityName) {
        this.activityName = activityName;
        return this;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public String getData() {
        return data;
    }

    public CabinetPlateMap data(String data) {
        this.data = data;
        return this;
    }

    public void setData(String data) {
        this.data = data;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    public Integer getNumPlates() {
        return numPlates;
    }

    public CabinetPlateMap numPlates(Integer numPlates) {
        this.numPlates = numPlates;
        return this;
    }

    public void setNumPlates(Integer numPlates) {
        this.numPlates = numPlates;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CabinetPlateMap)) {
            return false;
        }
        return id != null && id.equals(((CabinetPlateMap) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "CabinetPlateMap{" +
                "id=" + getId() +
                ", status='" + getStatus() + "'" +
                ", lastModified='" + getLastModified() + "'" +
                ", checksum='" + getChecksum() + "'" +
                ", numPlates='" + getNumPlates() + "'" +
                ", activityName='" + getActivityName() + "'" +
                ", data='" + getData() + "'" +
                "}";
    }

    public String prepareStringForChecksum() {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        return getStatus().toString()+getLastModified().format(formatter)+getActivityName()+getData();
    }
}
