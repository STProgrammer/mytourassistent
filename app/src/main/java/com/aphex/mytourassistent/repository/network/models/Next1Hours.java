
package com.aphex.mytourassistent.repository.network.models;



import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class Next1Hours {

    @SerializedName("summary")
    @Expose
    private Summary__1 summary;
    @SerializedName("details")
    @Expose
    private Details__1 details;

    public Summary__1 getSummary() {
        return summary;
    }

    public void setSummary(Summary__1 summary) {
        this.summary = summary;
    }

    public Details__1 getDetails() {
        return details;
    }

    public void setDetails(Details__1 details) {
        this.details = details;
    }

}
