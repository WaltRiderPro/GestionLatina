package com.Gestion.PolleriaLatina;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PolleriaLatinaApplication {

  public static void main(String[] args) {
    SpringApplication.run(PolleriaLatinaApplication.class, args);
  }

}
