/* Licensed Materials - Property of IBM              */
/* (c) Copyright IBM Corp. 2020. All Rights Reserved.*/

package org.opengroup.osdu.legal.ibm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan("org.opengroup.osdu")
@SpringBootApplication
public class LegalApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(LegalApplication.class, args);
	}
}
