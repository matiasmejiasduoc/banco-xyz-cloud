package cl.duoc.bancoxyz.bff.movil;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class BffMovilApplication {

    public static void main(String[] args) {
        SpringApplication.run(BffMovilApplication.class, args);
    }
}
