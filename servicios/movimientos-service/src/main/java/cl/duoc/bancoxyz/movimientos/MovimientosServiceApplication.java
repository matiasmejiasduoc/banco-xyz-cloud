package cl.duoc.bancoxyz.movimientos;

import cl.duoc.bancoxyz.commons.error.ManejadorErroresApi;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(ManejadorErroresApi.class)
public class MovimientosServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MovimientosServiceApplication.class, args);
    }
}
