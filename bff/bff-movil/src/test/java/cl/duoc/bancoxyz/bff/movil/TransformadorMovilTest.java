package cl.duoc.bancoxyz.bff.movil;

import static org.assertj.core.api.Assertions.assertThat;

import cl.duoc.bancoxyz.bff.movil.api.MovimientoMovil;
import cl.duoc.bancoxyz.bff.movil.api.MovimientosMovil;
import cl.duoc.bancoxyz.bff.movil.api.TransformadorMovil;
import cl.duoc.bancoxyz.bff.movil.cliente.MovimientoOrigen;
import cl.duoc.bancoxyz.bff.movil.config.MovilProperties;
import cl.duoc.bancoxyz.commons.api.Pagina;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class TransformadorMovilTest {

    private final TransformadorMovil transformador = new TransformadorMovil(new MovilProperties(5, 10, 12));

    @Test
    void losEgresosSeInformanConSignoNegativo() {
        MovimientoMovil retiro = transformador.movimiento(origen("RETIRO", "1500.00", "Retiro parcial"));
        MovimientoMovil deposito = transformador.movimiento(origen("DEPOSITO", "3000.00", "Ingreso mensual"));

        assertThat(retiro.monto()).isEqualByComparingTo("-1500");
        assertThat(deposito.monto()).isEqualByComparingTo("3000");
        assertThat(deposito.monto().toString()).isEqualTo("3000");
    }

    @Test
    void usaElTipoCuandoNoHayDescripcionYRecortaTextosLargos() {
        assertThat(transformador.movimiento(origen("COMPRA", "10", "Sin descripcion")).detalle()).isEqualTo("Compra");
        assertThat(transformador.movimiento(origen("COMPRA", "10", "Ingreso navideño extra")).detalle())
                .hasSize(12);
    }

    @Test
    void indicaLaSiguientePaginaSoloSiExiste() {
        List<MovimientoOrigen> contenido = List.of(origen("PAGO", "10", "Pago"));

        MovimientosMovil conMas = transformador.movimientos(Pagina.de(contenido, 0, 1, 3));
        MovimientosMovil ultima = transformador.movimientos(Pagina.de(contenido, 2, 1, 3));

        assertThat(conMas.siguiente()).isEqualTo(1);
        assertThat(ultima.siguiente()).isNull();
    }

    private MovimientoOrigen origen(String tipo, String monto, String descripcion) {
        return new MovimientoOrigen(LocalDate.of(2024, 5, 10), tipo, new BigDecimal(monto), descripcion);
    }
}
