package org.opengroup.osdu.legal.middleware;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.common.model.http.AppException;

@ExtendWith(MockitoExtension.class)
class GlobalErrorHandlerTest {

    @Mock
    HttpServletRequest request;

    @Test
    void givenMessageWhenExceptionIsThrownThenMessageSanitiseTriggered() {
        // given
        String originalMessage = "original${}Message";
        String expectedMessage = "originalMessage";
        AppException appException = new AppException(200, "reason", originalMessage);
        appException.getError().setMessage(originalMessage);
        when(request.getAttribute("jakarta.servlet.error.exception")).thenReturn(appException);
        // when
        GlobalErrorHandler globalErrorHandler = new GlobalErrorHandler();
        globalErrorHandler.handleError(request, null);
        // then
        assertEquals(expectedMessage, appException.getError().getMessage());
    }

    @Test
    void givenReasonWhenExceptionIsThrownThenReasonSanitiseTriggered() {
        // given
        String originalReason = "original${}Reason";
        String expectedReason = "originalReason";
        AppException appException = new AppException(200, originalReason, "message");
        appException.getError().setReason(originalReason);
        when(request.getAttribute("jakarta.servlet.error.exception")).thenReturn(appException);
        // when
        GlobalErrorHandler globalErrorHandler = new GlobalErrorHandler();
        globalErrorHandler.handleError(request, null);
        // then
        assertEquals(expectedReason, appException.getError().getReason());
    }

}
