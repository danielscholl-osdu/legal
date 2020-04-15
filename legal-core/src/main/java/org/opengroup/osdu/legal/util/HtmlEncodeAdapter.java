package org.opengroup.osdu.legal.util;

import org.apache.commons.lang.StringEscapeUtils;
import com.google.common.base.Strings;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class HtmlEncodeAdapter extends XmlAdapter<String, String> {

    @Override
    public String unmarshal(String rawString) {
        if(Strings.isNullOrEmpty(rawString))
            return "";

        return StringEscapeUtils.escapeHtml(rawString);
    }

    @Override
    public String marshal(String encodedString) {
        return encodedString;
    }
}
