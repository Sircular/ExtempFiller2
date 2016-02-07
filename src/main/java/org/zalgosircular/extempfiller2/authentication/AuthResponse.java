package org.zalgosircular.extempfiller2.authentication;

import java.util.HashMap;

/**
 * Created by Logan Lembke on 7/28/2015.
 */
public class AuthResponse {
    private final String[] requestFields;
    private final String[] responses;

    public AuthResponse(String[] requestFields, String[] responses) {
        this.requestFields = requestFields;
        this.responses = responses;
    }

    public String[] getRequestFields() {
        return requestFields;
    }

    public String[] getResponses() {
        return responses;
    }

    public HashMap<String, String> getResponseMap() {
        final HashMap<String, String> map = new HashMap<String, String>(requestFields.length);
        for (int i = 0; i < requestFields.length; i++) {
            map.put(requestFields[i], responses[i]);
        }
        return map;
    }
}
