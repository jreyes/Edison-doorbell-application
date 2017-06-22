package org.vaporwarecorp.doorbell;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan
@SpringBootApplication
public class DoorbellApplication {
// --------------------------- main() method ---------------------------

    public static void main(String[] args) {
        SpringApplication.run(DoorbellApplication.class, args);
    }
}
