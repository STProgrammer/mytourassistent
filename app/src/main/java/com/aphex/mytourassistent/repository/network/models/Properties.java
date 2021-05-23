
package com.aphex.mytourassistent.repository.network.models;

import java.util.List;


 import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class Properties {

    @SerializedName("meta")
    @Expose
    private Meta meta;
    @SerializedName("timeseries")
    @Expose
    private List<Timeseries> timeseries = null;

    public Meta getMeta() {
        return meta;
    }

    public void setMeta(Meta meta) {
        this.meta = meta;
    }

    public List<Timeseries> getTimeseries() {
        return timeseries;
    }

    public void setTimeseries(List<Timeseries> timeseries) {
        this.timeseries = timeseries;
    }

}
