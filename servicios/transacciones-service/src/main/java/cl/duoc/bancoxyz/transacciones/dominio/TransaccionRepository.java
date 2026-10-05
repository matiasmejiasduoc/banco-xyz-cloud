package cl.duoc.bancoxyz.transacciones.dominio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TransaccionRepository extends JpaRepository<Transaccion, Long>,
        JpaSpecificationExecutor<Transaccion> {
}
