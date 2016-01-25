package org.jenkinsci.plugins.cf.uaa.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchResults <T> {

    @JsonProperty("resources")
    private List<T> resources =  new ArrayList<T>();
    @JsonProperty("startIndex")
    private int startIndex;
    @JsonProperty("itemsPerPage")
    private int itemsPerPage;
    @JsonProperty("totalResults")
    private int totalResults;
    @JsonProperty("schemas")
    private List<String> schemas =  new ArrayList<String>();

    public List<String> getSchemas() {
        return schemas;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public int getItemsPerPage() {
        return itemsPerPage;
    }

    public int getTotalResults() {
        return totalResults;
    }

    public List<T> getResources() {
        return resources;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("SearchResults[schemas:");
        builder.append(getSchemas());
        builder.append("; count:");
        builder.append(getTotalResults());
        builder.append("; size:");
        builder.append(getResources().size());
        builder.append("; index:");
        builder.append(getStartIndex());
        builder.append("; resources:");
        builder.append(getResources());
        builder.append("; id:");
        builder.append(System.identityHashCode(this));
        builder.append(";]");
        return builder.toString();
    }

}
