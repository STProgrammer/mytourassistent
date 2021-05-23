
package com.aphex.mytourassistent.repository.network.models;




import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class Data {

    @SerializedName("instant")
    @Expose
    private Instant instant;
    @SerializedName("next_12_hours")
    @Expose
    private Next12Hours next12Hours;
    @SerializedName("next_1_hours")
    @Expose
    private Next1Hours next1Hours;
    @SerializedName("next_6_hours")
    @Expose
    private Next6Hours next6Hours;

    public Instant getInstant() {
        return instant;
    }

    public void setInstant(Instant instant) {
        this.instant = instant;
    }

    public Next12Hours getNext12Hours() {
        return next12Hours;
    }

    public void setNext12Hours(Next12Hours next12Hours) {
        this.next12Hours = next12Hours;
    }

    public Next1Hours getNext1Hours() {
        return next1Hours;
    }

    public void setNext1Hours(Next1Hours next1Hours) {
        this.next1Hours = next1Hours;
    }

    public Next6Hours getNext6Hours() {
        return next6Hours;
    }

    public void setNext6Hours(Next6Hours next6Hours) {
        this.next6Hours = next6Hours;
    }

}
