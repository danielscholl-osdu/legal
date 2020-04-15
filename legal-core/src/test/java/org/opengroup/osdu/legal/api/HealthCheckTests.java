package org.opengroup.osdu.legal.api;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;

public class HealthCheckTests {

	private HealthCheckApi sut;

	@Before
	public void setup() {
		this.sut = new HealthCheckApi();
	}

	@Test
	public void should_returnHttp200_when_checkLiveness() {
		assertEquals(HttpStatus.OK, this.sut.livenessCheck().getStatusCode());
	}

	@Test
	public void should_returnHttp200_when_checkReadiness() {
		assertEquals(HttpStatus.OK, this.sut.readinessCheck().getStatusCode());
	}
}
