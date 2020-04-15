package org.opengroup.osdu.legal.util;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class HtmlEncodeAdapterTests {

    HtmlEncodeAdapter sut = new HtmlEncodeAdapter();

    @Test
    public void should_convertGivenString_To_aHtmlEncodedStringGoogleDate(){
       String result = sut.unmarshal("<>");
        assertEquals("&lt;&gt;", result);
    }

    @Test
    public void should_convertNull_To_aEmptyString(){
        String result = sut.unmarshal(null);
        assertEquals("", result);
    }

    @Test
    public void should_convertDate_To_aDateString(){
        String result = sut.unmarshal("hello");
        assertEquals("hello", result);
    }

}
