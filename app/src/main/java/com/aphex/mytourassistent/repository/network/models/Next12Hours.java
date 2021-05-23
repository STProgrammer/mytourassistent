
package com.aphex.mytourassistent.repository.network.models;



import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class Next12Hours {

    @SerializedName("summary")
    @Expose
    private Summary summary;

    public Summary getSummary() {
        return summary;
    }

    public void setSummary(Summary summary) {
        this.summary = summary;
    }

}
