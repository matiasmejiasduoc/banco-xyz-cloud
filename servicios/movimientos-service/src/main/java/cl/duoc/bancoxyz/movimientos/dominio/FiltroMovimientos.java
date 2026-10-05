package cl.duoc.bancoxyz.movimientos.dominio;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public record FiltroMovimientos(Long cuentaId, TipoMovimiento tipo, LocalDate desde, LocalDate hasta) {

    public Specification<Movimiento> comoEspecificacion() {
        return (root, query, cb) -> {
            List<Predicate> condiciones = new ArrayList<>();
            if (cuentaId != null) {
                condiciones.add(cb.equal(root.get("cuentaId"), cuentaId));
            }
            if (tipo != null) {
                condiciones.add(cb.equal(root.get("tipo"), tipo));
            }
            if (desde != null) {
                condiciones.add(cb.greaterThanOrEqualTo(root.get("fecha"), desde));
            }
            if (hasta != null) {
                condiciones.add(cb.lessThanOrEqualTo(root.get("fecha"), hasta));
            }
            return cb.and(condiciones.toArray(Predicate[]::new));
        };
    }
}
