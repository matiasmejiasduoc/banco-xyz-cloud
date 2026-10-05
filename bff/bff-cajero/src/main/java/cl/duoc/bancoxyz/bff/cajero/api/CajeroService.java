package cl.duoc.bancoxyz.bff.cajero.api;

import cl.duoc.bancoxyz.bff.cajero.cliente.CuentaOrigen;
import cl.duoc.bancoxyz.bff.cajero.cliente.CuentasCliente;
import cl.duoc.bancoxyz.bff.cajero.cliente.OperacionOrigen;
import cl.duoc.bancoxyz.bff.cajero.config.CajeroProperties;
import cl.duoc.bancoxyz.bff.cajero.evento.PublicacionFallidaException;
import cl.duoc.bancoxyz.bff.cajero.evento.PublicadorRetiros;
import cl.duoc.bancoxyz.commons.evento.RetiroRealizado;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class CajeroService {

    private static final Logger log = LoggerFactory.getLogger(CajeroService.class);
    private static final String CANAL = "CAJERO";

    private final CuentasCliente cuentas;
    private final PublicadorRetiros publicador;
    private final CajeroProperties.Retiro reglas;

    public CajeroService(CuentasCliente cuentas, PublicadorRetiros publicador, CajeroProperties propiedades) {
        this.cuentas = cuentas;
        this.publicador = publicador;
        this.reglas = propiedades.retiro();
    }

    public SaldoCajero saldo(Long cuentaId) {
        CuentaOrigen cuenta = cuentas.obtener(cuentaId);
        if (!cuenta.habilitadaEnCajero()) {
            throw new OperacionCajeroException(HttpStatus.UNPROCESSABLE_CONTENT, "CUENTA_NO_HABILITADA",
                    "La cuenta no esta habilitada para operar en cajero");
        }
        return new SaldoCajero(enmascarar(cuentaId), cuenta.saldo(), LocalDateTime.now());
    }

    public ComprobanteRetiro retirar(Long cuentaId, BigDecimal monto, String terminal, String claveIdempotencia) {
        validarMonto(monto);
        String referencia = terminal + "-" + claveIdempotencia;

        OperacionOrigen debito = cuentas.debitar(cuentaId, monto, referencia);
        try {
            publicador.publicar(new RetiroRealizado(referencia, cuentaId, debito.monto(), terminal, CANAL,
                    debito.fecha()));
        } catch (PublicacionFallidaException e) {
            log.error("No se pudo publicar el retiro {}, se revierte el debito", referencia, e);
            revertir(cuentaId, referencia);
            throw new OperacionCajeroException(HttpStatus.SERVICE_UNAVAILABLE, "OPERACION_REVERTIDA",
                    "No fue posible completar el retiro, no se realizo ningun cargo");
        }

        log.info("Retiro {} de {} en cuenta {} desde {}", referencia, monto, cuentaId, terminal);
        return new ComprobanteRetiro(referencia, enmascarar(cuentaId), debito.monto(), debito.saldoActual(),
                debito.fecha());
    }

    private void validarMonto(BigDecimal monto) {
        boolean fueraDeRango = monto.compareTo(reglas.montoMinimo()) < 0
                || monto.compareTo(reglas.montoMaximo()) > 0;
        boolean noEsMultiplo = monto.remainder(reglas.multiplo()).signum() != 0;
        if (fueraDeRango || noEsMultiplo) {
            throw new OperacionCajeroException(HttpStatus.UNPROCESSABLE_CONTENT, "MONTO_INVALIDO",
                    "El monto debe estar entre " + reglas.montoMinimo().toPlainString() + " y "
                            + reglas.montoMaximo().toPlainString() + " y ser multiplo de "
                            + reglas.multiplo().toPlainString());
        }
    }

    private void revertir(Long cuentaId, String referencia) {
        try {
            cuentas.revertirDebito(cuentaId, referencia);
        } catch (RuntimeException e) {
            log.error("Fallo la reversa de {}, requiere revision manual", referencia, e);
        }
    }

    private String enmascarar(Long cuentaId) {
        String numero = String.valueOf(cuentaId);
        String visible = numero.length() <= 2 ? numero : numero.substring(numero.length() - 2);
        return "****" + visible;
    }
}
