package cl.duoc.bancoxyz.cuentas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cl.duoc.bancoxyz.commons.error.ConflictoException;
import cl.duoc.bancoxyz.commons.error.ReglaNegocioException;
import cl.duoc.bancoxyz.cuentas.api.CuentaService;
import cl.duoc.bancoxyz.cuentas.api.OperacionRespuesta;
import cl.duoc.bancoxyz.cuentas.api.OperacionSolicitud;
import cl.duoc.bancoxyz.cuentas.dominio.Cuenta;
import cl.duoc.bancoxyz.cuentas.dominio.CuentaRepository;
import cl.duoc.bancoxyz.cuentas.dominio.TipoCuenta;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:cuentas-test;DB_CLOSE_DELAY=-1")
class CuentaServiceTest {

    private static final long AHORRO = 9001L;
    private static final long HIPOTECA = 9002L;

    @Autowired
    private CuentaService servicio;

    @Autowired
    private CuentaRepository cuentas;

    @BeforeEach
    void preparar() {
        cuentas.save(new Cuenta(AHORRO, "Titular prueba", 30, TipoCuenta.AHORRO, new BigDecimal("5000"), null));
        cuentas.save(new Cuenta(HIPOTECA, "Titular prueba", 30, TipoCuenta.HIPOTECA, new BigDecimal("5000"), null));
    }

    @Test
    void cargaLasCuentasConsolidadasDelArchivoLegacy() {
        assertThat(cuentas.count()).isGreaterThan(2);
        assertThat(servicio.estadisticas().porTipo()).hasSize(3);
    }

    @Test
    void debitaYDescuentaDelSaldo() {
        OperacionRespuesta respuesta = servicio.debitar(AHORRO, solicitud("2000", "t-debito"));

        assertThat(respuesta.saldoAnterior()).isEqualByComparingTo("5000");
        assertThat(respuesta.saldoActual()).isEqualByComparingTo("3000");
    }

    @Test
    void repetirLaMismaReferenciaNoDebitaDosVeces() {
        servicio.debitar(AHORRO, solicitud("1000", "t-idempotente"));
        servicio.debitar(AHORRO, solicitud("1000", "t-idempotente"));

        assertThat(servicio.obtener(AHORRO).saldo()).isEqualByComparingTo("4000");
    }

    @Test
    void rechazaReferenciaReutilizadaConOtroMonto() {
        servicio.debitar(AHORRO, solicitud("1000", "t-conflicto"));

        assertThatThrownBy(() -> servicio.debitar(AHORRO, solicitud("500", "t-conflicto")))
                .isInstanceOf(ConflictoException.class);
    }

    @Test
    void laReversaDevuelveElSaldoYEsIdempotente() {
        servicio.debitar(AHORRO, solicitud("1500", "t-reversa"));

        servicio.revertirDebito(AHORRO, "t-reversa");
        servicio.revertirDebito(AHORRO, "t-reversa");

        assertThat(servicio.obtener(AHORRO).saldo()).isEqualByComparingTo("5000");
    }

    @Test
    void unDebitoRevertidoNoSePuedeReintentarConLaMismaReferencia() {
        servicio.debitar(AHORRO, solicitud("1000", "t-revertido"));
        servicio.revertirDebito(AHORRO, "t-revertido");

        assertThatThrownBy(() -> servicio.debitar(AHORRO, solicitud("1000", "t-revertido")))
                .isInstanceOf(ConflictoException.class)
                .hasFieldOrPropertyWithValue("codigo", "OPERACION_REVERTIDA");
        assertThat(servicio.obtener(AHORRO).saldo()).isEqualByComparingTo("5000");
    }

    @Test
    void rechazaSaldoInsuficiente() {
        assertThatThrownBy(() -> servicio.debitar(AHORRO, solicitud("999999", "t-sin-saldo")))
                .isInstanceOf(ReglaNegocioException.class)
                .hasFieldOrPropertyWithValue("codigo", "SALDO_INSUFICIENTE");
    }

    @Test
    void rechazaRetirosEnCuentasQueNoSonDeAhorro() {
        assertThatThrownBy(() -> servicio.debitar(HIPOTECA, solicitud("100", "t-hipoteca")))
                .isInstanceOf(ReglaNegocioException.class)
                .hasFieldOrPropertyWithValue("codigo", "RETIRO_NO_PERMITIDO");
    }

    private OperacionSolicitud solicitud(String monto, String referencia) {
        return new OperacionSolicitud(new BigDecimal(monto), referencia, "PRUEBA");
    }
}
