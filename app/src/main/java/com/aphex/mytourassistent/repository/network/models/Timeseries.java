
package com.aphex.mytourassistent.repository.network.models;



import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class Timeseries {

    @SerializedName("time")
    @Expose
    private String time;
    @SerializedName("data")
    @Expose
    private Data data;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

}
