package org.opengroup.osdu.legal.jobs.models;

import java.util.Date;
import lombok.Data;

@Data
public class AboutToExpireLegalTag {
    private String tagName;
    private Date expirationDate;

    AboutToExpireLegalTag(){
    }
    public AboutToExpireLegalTag(String tagName, Date expirationDate) {
        this.tagName = tagName;
        this.expirationDate = expirationDate;
    }
}
