package cl.duoc.bancoxyz.cuentas;

import cl.duoc.bancoxyz.commons.error.ManejadorErroresApi;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@ConfigurationPropertiesScan
@Import(ManejadorErroresApi.class)
public class CuentasServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CuentasServiceApplication.class, args);
    }
}
