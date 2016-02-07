package org.zalgosircular.extempfiller2.authentication;

/**
 * Created by Logan Lembke on 7/28/2015.
 */
public class AuthRequest {
    private String[] fields;

    public AuthRequest(String[] fields) {
        this.fields = fields;
    }

    public String[] getRequestFields() {
        return fields;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("AuthRequest: ");
        for (String f : fields) {
            sb.append(f).append("; ");
        }
        return sb.toString();
    }
}
