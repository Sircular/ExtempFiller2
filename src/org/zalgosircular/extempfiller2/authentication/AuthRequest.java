package org.zalgosircular.extempfiller2.authentication;

/**
 * Created by Logan Lembke on 7/28/2015.
 */
public class AuthRequest {
    private String[] fields;

    public AuthRequest(String[] fields) {
        this.fields = fields;
    }

    public String[] getAuthFields() {
        return fields;
    }
}
