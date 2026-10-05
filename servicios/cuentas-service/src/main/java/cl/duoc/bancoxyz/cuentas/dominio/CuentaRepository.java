package cl.duoc.bancoxyz.cuentas.dominio;

import cl.duoc.bancoxyz.cuentas.api.ResumenTipoCuenta;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface CuentaRepository extends JpaRepository<Cuenta, Long> {

    Page<Cuenta> findByTipo(TipoCuenta tipo, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Cuenta c where c.id = :id")
    Optional<Cuenta> buscarParaActualizar(Long id);

    @Query("""
            select new cl.duoc.bancoxyz.cuentas.api.ResumenTipoCuenta(c.tipo, count(c), sum(c.saldo))
            from Cuenta c group by c.tipo order by c.tipo""")
    List<ResumenTipoCuenta> resumenPorTipo();
}
