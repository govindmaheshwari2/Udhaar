package com.govind.udhaar;

/**
 * Created by Govind on 15-Jun-18.
 */

public class Model {
    String Username;
    String Amount;


    public String getUsername() {
        return this.Username;
    }

    public String getAmount() {
        return this.Amount;
    }

    public Model(String username, String amount) {
        this.Username = username;
        this.Amount = amount;
    }


}
