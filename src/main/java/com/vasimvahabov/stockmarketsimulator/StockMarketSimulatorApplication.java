package com.vasimvahabov.stockmarketsimulator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;


@SpringBootApplication
@ConfigurationPropertiesScan
public class StockMarketSimulatorApplication {

    static void main(String[] args) {
        // SpringApplication.run(StockMarketSimulatorApplication.class, args);
        var application = new SpringApplication(StockMarketSimulatorApplication.class);
        application.setLazyInitialization(false);
//        application.setAllowBeanDefinitionOverriding(true);
        application.run(args);
    }

}
