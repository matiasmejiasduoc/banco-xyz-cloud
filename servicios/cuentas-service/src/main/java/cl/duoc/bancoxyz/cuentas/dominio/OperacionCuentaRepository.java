package cl.duoc.bancoxyz.cuentas.dominio;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OperacionCuentaRepository extends JpaRepository<OperacionCuenta, Long> {

    Optional<OperacionCuenta> findByReferencia(String referencia);
}
