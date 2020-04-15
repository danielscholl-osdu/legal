package org.opengroup.osdu.legal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan({"org.opengroup.osdu"})
@SpringBootApplication
public class LegalApplication extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(LegalApplication.class);
	}

	public static void main(String[] args) {
		SpringApplication.run(LegalApplication.class, args);
	}

}
