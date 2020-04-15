package org.opengroup.osdu.legal.api;

import javax.annotation.security.PermitAll;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/_ah")
public class HealthCheckApi {

	@PermitAll
	@GetMapping("/liveness_check")
	public ResponseEntity<String> livenessCheck() {
		return new ResponseEntity<String>("Legal service is alive", HttpStatus.OK);
	}

	@PermitAll
	@GetMapping("/readiness_check")
	public ResponseEntity<String> readinessCheck() {
		return new ResponseEntity<String>("Legal service is ready", HttpStatus.OK);
	}
}
