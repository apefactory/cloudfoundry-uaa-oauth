package org.jenkinsci.plugins.cf.uaa.models;


import com.fasterxml.jackson.annotation.JsonProperty;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CfResource<T> {

    @JsonProperty("metadata")
    public CfMetadata metaData;

    @JsonProperty("entity")
    public T entity;

    public CfMetadata getMetaData() {
        return metaData;
    }

    public void setMetaData(CfMetadata metaData) {
        this.metaData = metaData;
    }

    public T getEntity() {
        return entity;
    }

    public void setEntity(T entity) {
        this.entity = entity;
    }

}