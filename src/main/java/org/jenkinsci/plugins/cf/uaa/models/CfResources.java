package org.jenkinsci.plugins.cf.uaa.models;


import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CfResources<T> {

    @JsonProperty("total_results")
    private int totalResults;

    @JsonProperty("total_pages")
    private int totalPages;

    @JsonProperty("prev_url")
    private String prevUrl;

    @JsonProperty("next_url")
    private String nextUrl;

    @JsonProperty("resources")
    private List<CfResource<T>> resources;


    public int getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(int totalResults) {
        this.totalResults = totalResults;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public String getPrevUrl() {
        return prevUrl;
    }

    public void setPrevUrl(String prevUrl) {
        this.prevUrl = prevUrl;
    }

    public String getNextUrl() {
        return nextUrl;
    }

    public void setNextUrl(String nextUrl) {
        this.nextUrl = nextUrl;
    }

    public List<CfResource<T>> getResources() {
        return resources;
    }

    public void setResources(List<CfResource<T>> resources) {
        this.resources = resources;
    }

}
