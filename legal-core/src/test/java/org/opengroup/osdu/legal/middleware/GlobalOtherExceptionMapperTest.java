package org.opengroup.osdu.legal.middleware;

import org.apache.http.HttpStatus;
import org.junit.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;


public class GlobalOtherExceptionMapperTest {

    private GlobalExceptionMapper globalExceptionMapper;
    private GlobalOtherExceptionMapper sut;


    @Test
    public void should_returnInternalServerError_when_GeneralExceptionIsCaptured() {
        globalExceptionMapper = mock(GlobalExceptionMapper.class);
        GlobalOtherExceptionMapper sut = new GlobalOtherExceptionMapper(globalExceptionMapper);

        Exception exception = mock(Exception .class);
        ResponseEntity<Object> responseEntity = ResponseEntity.internalServerError().body("internal error occurred");
        when(globalExceptionMapper.getErrorResponse(any())).thenReturn(responseEntity);

        ResponseEntity response = sut.handleGeneralException(exception);

        assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, response.getStatusCodeValue());
    }


}