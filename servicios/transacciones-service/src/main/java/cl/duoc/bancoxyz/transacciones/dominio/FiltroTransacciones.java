package cl.duoc.bancoxyz.transacciones.dominio;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public record FiltroTransacciones(TipoTransaccion tipo, LocalDate desde, LocalDate hasta) {

    public Specification<Transaccion> comoEspecificacion() {
        return (root, query, cb) -> {
            List<Predicate> condiciones = new ArrayList<>();
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
