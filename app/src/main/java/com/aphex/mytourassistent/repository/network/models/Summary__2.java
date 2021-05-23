
package com.aphex.mytourassistent.repository.network.models;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class Summary__2 {

    @SerializedName("symbol_code")
    @Expose
    private String symbolCode;

    public String getSymbolCode() {
        return symbolCode;
    }

    public void setSymbolCode(String symbolCode) {
        this.symbolCode = symbolCode;
    }

}
