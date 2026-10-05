package cl.duoc.bancoxyz.transacciones.api;

import cl.duoc.bancoxyz.commons.api.Pagina;
import cl.duoc.bancoxyz.commons.error.RecursoNoEncontradoException;
import cl.duoc.bancoxyz.transacciones.dominio.FiltroTransacciones;
import cl.duoc.bancoxyz.transacciones.dominio.Transaccion;
import cl.duoc.bancoxyz.transacciones.dominio.TransaccionRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TransaccionService {

    private final TransaccionRepository repositorio;

    public TransaccionService(TransaccionRepository repositorio) {
        this.repositorio = repositorio;
    }

    public Pagina<TransaccionRespuesta> listar(FiltroTransacciones filtro, int pagina, int tamano) {
        Page<Transaccion> resultado = repositorio.findAll(filtro.comoEspecificacion(),
                PageRequest.of(pagina, tamano, Sort.by(Sort.Order.desc("fecha"), Sort.Order.asc("id"))));
        return Pagina.de(resultado.map(TransaccionRespuesta::de).getContent(), pagina, tamano,
                resultado.getTotalElements());
    }

    public TransaccionRespuesta obtener(Long id) {
        return repositorio.findById(id).map(TransaccionRespuesta::de)
                .orElseThrow(() -> new RecursoNoEncontradoException("Transaccion", id));
    }

    public List<ResumenDiario> resumenDiario(LocalDate desde, LocalDate hasta) {
        List<Transaccion> transacciones = repositorio.findAll(
                new FiltroTransacciones(null, desde, hasta).comoEspecificacion());

        Map<LocalDate, ResumenDiario> porFecha = new TreeMap<>();
        for (Transaccion transaccion : transacciones) {
            porFecha.compute(transaccion.getFecha(), (fecha, actual) -> acumular(
                    actual == null ? ResumenDiario.vacio(fecha) : actual, transaccion));
        }
        return List.copyOf(porFecha.values());
    }

    private ResumenDiario acumular(ResumenDiario resumen, Transaccion transaccion) {
        return switch (transaccion.getTipo()) {
            case CREDITO -> new ResumenDiario(resumen.fecha(), resumen.cantidad() + 1,
                    resumen.totalCreditos().add(transaccion.getMonto()), resumen.totalDebitos(),
                    resumen.noClasificadas());
            case DEBITO -> new ResumenDiario(resumen.fecha(), resumen.cantidad() + 1, resumen.totalCreditos(),
                    resumen.totalDebitos().add(transaccion.getMonto()), resumen.noClasificadas());
            case NO_CLASIFICADA -> new ResumenDiario(resumen.fecha(), resumen.cantidad() + 1,
                    resumen.totalCreditos(), resumen.totalDebitos(), resumen.noClasificadas() + 1);
        };
    }
}
