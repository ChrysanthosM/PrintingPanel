package org.masouras;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PrintingPanelRun {
    public static void main(String[] args) {
        SpringApplication.run(PrintingPanelRun.class, args);
    }
}
