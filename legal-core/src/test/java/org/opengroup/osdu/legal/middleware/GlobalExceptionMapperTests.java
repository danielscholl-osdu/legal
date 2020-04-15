package org.opengroup.osdu.legal.middleware;

import static org.junit.Assert.assertEquals;

import javassist.NotFoundException;

import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.springframework.http.ResponseEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

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
		assertEquals("\"any message\"", response.getBody());
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
	public void should_useGenericValuesInResponse_When_ExceptionIsHandledByGlobalExceptionMapper() {

		Exception exception = new Exception("any message");

		ResponseEntity<Object> response = sut.handleGeneralException(exception);
		assertEquals(500, response.getStatusCodeValue());
		//assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType().toString());
		//assertEquals("An unknown error has occurred.", response.getBody().getMessage());
	}


}