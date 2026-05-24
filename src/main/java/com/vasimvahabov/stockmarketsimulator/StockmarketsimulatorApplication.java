package com.vasimvahabov.stockmarketsimulator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class StockmarketsimulatorApplication {

    static void main(String[] args) {
        // SpringApplication.run(StockmarketsimulatorApplication.class, args);
        var application = new SpringApplication(StockmarketsimulatorApplication.class);
        application.setLazyInitialization(false);
//        application.setAllowBeanDefinitionOverriding(true);
        application.run(args);
    }

}
