package cl.duoc.bancoxyz.transacciones;

import cl.duoc.bancoxyz.commons.error.ManejadorErroresApi;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(ManejadorErroresApi.class)
public class TransaccionesServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TransaccionesServiceApplication.class, args);
    }
}
