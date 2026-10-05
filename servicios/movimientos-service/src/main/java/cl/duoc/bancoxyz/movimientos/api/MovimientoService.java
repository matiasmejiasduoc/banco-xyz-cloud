package cl.duoc.bancoxyz.movimientos.api;

import cl.duoc.bancoxyz.commons.api.Pagina;
import cl.duoc.bancoxyz.commons.error.ConflictoException;
import cl.duoc.bancoxyz.movimientos.dominio.FiltroMovimientos;
import cl.duoc.bancoxyz.movimientos.dominio.Movimiento;
import cl.duoc.bancoxyz.movimientos.dominio.MovimientoRepository;
import cl.duoc.bancoxyz.movimientos.dominio.TipoMovimiento;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MovimientoService {

    private static final Sort MAS_RECIENTES = Sort.by(Sort.Order.desc("fecha"), Sort.Order.desc("id"));

    private final MovimientoRepository repositorio;

    public MovimientoService(MovimientoRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Transactional(readOnly = true)
    public Pagina<MovimientoRespuesta> listar(FiltroMovimientos filtro, int pagina, int tamano) {
        Page<Movimiento> resultado = repositorio.findAll(filtro.comoEspecificacion(),
                PageRequest.of(pagina, tamano, MAS_RECIENTES));
        return Pagina.de(resultado.map(MovimientoRespuesta::de).getContent(), pagina, tamano,
                resultado.getTotalElements());
    }

    @Transactional(readOnly = true)
    public ResumenMovimientos resumir(Long cuentaId, LocalDate desde, LocalDate hasta) {
        List<Movimiento> movimientos = repositorio.findAll(
                new FiltroMovimientos(cuentaId, null, desde, hasta).comoEspecificacion());

        Map<TipoMovimiento, TotalPorTipo> totales = new EnumMap<>(TipoMovimiento.class);
        BigDecimal ingresos = BigDecimal.ZERO;
        BigDecimal egresos = BigDecimal.ZERO;
        for (Movimiento movimiento : movimientos) {
            totales.merge(movimiento.getTipo(), new TotalPorTipo(movimiento.getTipo(), 1, movimiento.getMonto()),
                    (a, b) -> new TotalPorTipo(a.tipo(), a.cantidad() + 1, a.total().add(b.total())));
            if (movimiento.getTipo().esIngreso()) {
                ingresos = ingresos.add(movimiento.getMonto());
            } else {
                egresos = egresos.add(movimiento.getMonto());
            }
        }

        return new ResumenMovimientos(cuentaId, desde, hasta, movimientos.size(), escala(ingresos), escala(egresos),
                escala(ingresos.subtract(egresos)), List.copyOf(totales.values()),
                fechaExtrema(movimientos, Comparator.naturalOrder()),
                fechaExtrema(movimientos, Comparator.reverseOrder()));
    }

    public MovimientoRespuesta registrar(NuevoMovimiento nuevo) {
        BigDecimal monto = escala(nuevo.monto());
        if (nuevo.referencia() == null) {
            return MovimientoRespuesta.de(repositorio.save(aEntidad(nuevo, monto)));
        }
        Optional<Movimiento> existente = repositorio.findByReferencia(nuevo.referencia());
        if (existente.isPresent()) {
            return idempotente(existente.get(), nuevo, monto);
        }
        try {
            return MovimientoRespuesta.de(repositorio.saveAndFlush(aEntidad(nuevo, monto)));
        } catch (DataIntegrityViolationException e) {
            Movimiento concurrente = repositorio.findByReferencia(nuevo.referencia()).orElseThrow(() -> e);
            return idempotente(concurrente, nuevo, monto);
        }
    }

    private MovimientoRespuesta idempotente(Movimiento previo, NuevoMovimiento nuevo, BigDecimal monto) {
        boolean mismo = previo.getCuentaId().equals(nuevo.cuentaId()) && previo.getTipo() == nuevo.tipo()
                && previo.getMonto().compareTo(monto) == 0;
        if (!mismo) {
            throw new ConflictoException("REFERENCIA_DUPLICADA",
                    "La referencia " + nuevo.referencia() + " ya fue usada en otro movimiento");
        }
        return MovimientoRespuesta.de(previo);
    }

    private Movimiento aEntidad(NuevoMovimiento nuevo, BigDecimal monto) {
        return new Movimiento(nuevo.cuentaId(), LocalDate.now(), nuevo.tipo(), monto, nuevo.descripcion(),
                nuevo.canal(), nuevo.referencia(), null);
    }

    private LocalDate fechaExtrema(List<Movimiento> movimientos, Comparator<LocalDate> orden) {
        return movimientos.stream().map(Movimiento::getFecha).min(orden).orElse(null);
    }

    private BigDecimal escala(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }
}
