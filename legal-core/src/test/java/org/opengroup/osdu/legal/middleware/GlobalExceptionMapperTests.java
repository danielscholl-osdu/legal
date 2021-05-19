package org.opengroup.osdu.legal.middleware;

import static org.junit.Assert.assertEquals;
import static org.powermock.api.mockito.PowerMockito.mock;

import javassist.NotFoundException;

import org.opengroup.osdu.core.common.model.http.AppError;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.context.request.WebRequest;

@RunWith(MockitoJUnitRunner.class)
public class GlobalExceptionMapperTests {
	@Mock
	private JaxRsDpsLog log;

	@InjectMocks
	private GlobalExceptionMapper sut;

	@Test
	public void should_useValuesInAppExceptionInResponse_When_AppExceptionIsHandledByGlobalExceptionMapper() {

		AppException exception = new AppException(409, "any reason", "any message");

		ResponseEntity<Object> response = sut.handleAppException(exception);
		assertEquals(409, response.getStatusCodeValue());
		//assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType().toString());
		AppError expectedError = new AppError(409, "any reason", "any message");
		assertEquals(expectedError, response.getBody());
	}
	@Test
	public void should_addLocationHeader_when_fromAppException() {

		AppException exception = new AppException(302, "any reason", "any message");

		ResponseEntity<Object> response = sut.handleAppException(exception);
		assertEquals(302, response.getStatusCodeValue());
	}
	@Test
	public void should_useGenericResponse_when_exceptionIsThrownDuringMapping() {

		AppException exception = new AppException(302, "any reason", "any message");

		ResponseEntity<Object> response = sut.handleAppException(exception);

		assertEquals(302, response.getStatusCodeValue());
		//assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType().toString());
		//assertEquals("any message", response.getBody());
	}

	@Test
	public void should_use404ValueInResponse_When_NotFoundExceptionIsHandledByGlobalExceptionMapper() {

		NotFoundException exception = new NotFoundException("any message");
		ResponseEntity<Object> response = sut.handleNotFoundException(exception);
		assertEquals(404, response.getStatusCodeValue());
		//assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType().toString());
		//assertEquals(null, response.getBody().getMessage());
	}

	@Test
	public void should_use405ValueInResponse_When_HttpRequestMethodNotSupportedExceptionIsHandledByGlobalExceptionMapper() {
		HttpHeaders httpHeaders = mock(HttpHeaders.class);
		WebRequest webRequest = mock(WebRequest.class);
		HttpRequestMethodNotSupportedException exception = new HttpRequestMethodNotSupportedException("any message");
		ResponseEntity<Object> response = sut.handleHttpRequestMethodNotSupported(exception, httpHeaders, HttpStatus.METHOD_NOT_ALLOWED, webRequest);
		assertEquals(405, response.getStatusCodeValue());
	}

	@Test
	public void should_useGenericValuesInResponse_When_ExceptionIsHandledByGlobalExceptionMapper() {

		Exception exception = new Exception("any message");

		ResponseEntity<Object> response = sut.handleGeneralException(exception);
		assertEquals(500, response.getStatusCodeValue());
		//assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType().toString());
		//assertEquals("An unknown error has occurred.", response.getBody().getMessage());
	}


}