package cl.duoc.bancoxyz.cuentas.api;

import cl.duoc.bancoxyz.commons.api.Pagina;
import cl.duoc.bancoxyz.commons.error.ConflictoException;
import cl.duoc.bancoxyz.commons.error.RecursoNoEncontradoException;
import cl.duoc.bancoxyz.cuentas.config.TasasInteres;
import cl.duoc.bancoxyz.cuentas.dominio.Cuenta;
import cl.duoc.bancoxyz.cuentas.dominio.CuentaRepository;
import cl.duoc.bancoxyz.cuentas.dominio.OperacionCuenta;
import cl.duoc.bancoxyz.cuentas.dominio.OperacionCuentaRepository;
import cl.duoc.bancoxyz.cuentas.dominio.TipoCuenta;
import cl.duoc.bancoxyz.cuentas.dominio.TipoOperacion;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CuentaService {

    private static final String SUFIJO_REVERSA = "-REV";

    private final CuentaRepository cuentas;
    private final OperacionCuentaRepository operaciones;
    private final TasasInteres tasas;

    public CuentaService(CuentaRepository cuentas, OperacionCuentaRepository operaciones, TasasInteres tasas) {
        this.cuentas = cuentas;
        this.operaciones = operaciones;
        this.tasas = tasas;
    }

    public Pagina<CuentaRespuesta> listar(TipoCuenta tipo, int pagina, int tamano) {
        PageRequest solicitud = PageRequest.of(pagina, tamano, Sort.by("id"));
        Page<Cuenta> resultado = tipo == null ? cuentas.findAll(solicitud) : cuentas.findByTipo(tipo, solicitud);
        return Pagina.de(resultado.map(this::aRespuesta).getContent(), pagina, tamano,
                resultado.getTotalElements());
    }

    public CuentaRespuesta obtener(Long id) {
        return cuentas.findById(id).map(this::aRespuesta)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cuenta", id));
    }

    public EstadisticasCuentas estadisticas() {
        List<ResumenTipoCuenta> porTipo = cuentas.resumenPorTipo();
        long total = porTipo.stream().mapToLong(ResumenTipoCuenta::cantidad).sum();
        BigDecimal saldoTotal = porTipo.stream().map(ResumenTipoCuenta::saldoTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new EstadisticasCuentas(total, saldoTotal, porTipo);
    }

    @Transactional
    public OperacionRespuesta debitar(Long id, OperacionSolicitud solicitud) {
        return aplicar(id, solicitud, TipoOperacion.DEBITO);
    }

    @Transactional
    public OperacionRespuesta acreditar(Long id, OperacionSolicitud solicitud) {
        return aplicar(id, solicitud, TipoOperacion.CREDITO);
    }

    @Transactional
    public OperacionRespuesta revertirDebito(Long id, String referencia) {
        Cuenta cuenta = cuentas.buscarParaActualizar(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cuenta", id));
        OperacionCuenta debito = operaciones.findByReferencia(referencia)
                .filter(operacion -> operacion.getCuentaId().equals(id))
                .filter(operacion -> operacion.getTipo() == TipoOperacion.DEBITO)
                .orElseThrow(() -> new RecursoNoEncontradoException("Debito", referencia));

        String referenciaReversa = referencia + SUFIJO_REVERSA;
        if (debito.isRevertida()) {
            return operaciones.findByReferencia(referenciaReversa).map(OperacionRespuesta::de)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Reversa", referenciaReversa));
        }

        BigDecimal saldoAnterior = cuenta.getSaldo();
        cuenta.acreditar(debito.getMonto());
        debito.marcarRevertida();
        OperacionCuenta reversa = operaciones.save(new OperacionCuenta(referenciaReversa, id, TipoOperacion.CREDITO,
                debito.getMonto(), saldoAnterior, cuenta.getSaldo(), debito.getCanal()));
        return OperacionRespuesta.de(reversa);
    }

    private OperacionRespuesta aplicar(Long id, OperacionSolicitud solicitud, TipoOperacion tipo) {
        Cuenta cuenta = cuentas.buscarParaActualizar(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cuenta", id));
        BigDecimal monto = solicitud.monto().setScale(2, RoundingMode.HALF_UP);

        var existente = operaciones.findByReferencia(solicitud.referencia());
        if (existente.isPresent()) {
            if (!existente.get().coincideCon(id, tipo, monto)) {
                throw new ConflictoException("REFERENCIA_DUPLICADA",
                        "La referencia " + solicitud.referencia() + " ya fue usada en otra operacion");
            }
            if (existente.get().isRevertida()) {
                throw new ConflictoException("OPERACION_REVERTIDA",
                        "La operacion " + solicitud.referencia() + " fue revertida y no puede reutilizarse");
            }
            return OperacionRespuesta.de(existente.get());
        }

        BigDecimal saldoAnterior = cuenta.getSaldo();
        if (tipo == TipoOperacion.DEBITO) {
            cuenta.debitar(monto);
        } else {
            cuenta.acreditar(monto);
        }
        OperacionCuenta operacion = operaciones.save(new OperacionCuenta(solicitud.referencia(), id, tipo, monto,
                saldoAnterior, cuenta.getSaldo(), solicitud.canal()));
        return OperacionRespuesta.de(operacion);
    }

    private CuentaRespuesta aRespuesta(Cuenta cuenta) {
        BigDecimal tasa = tasas.de(cuenta.getTipo());
        BigDecimal interes = cuenta.getSaldo().multiply(tasa).setScale(2, RoundingMode.HALF_UP);
        return new CuentaRespuesta(cuenta.getId(), cuenta.getTitular(), cuenta.getEdad(), cuenta.getTipo(),
                cuenta.getSaldo(), tasa, interes, cuenta.getObservaciones(), cuenta.getActualizadaEn());
    }
}
