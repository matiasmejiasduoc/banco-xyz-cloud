package cl.duoc.bancoxyz.cuentas.config;

import cl.duoc.bancoxyz.cuentas.dominio.TipoCuenta;
import java.math.BigDecimal;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("banco.tasas")
public record TasasInteres(BigDecimal ahorro, BigDecimal prestamo, BigDecimal hipoteca) {

    public BigDecimal de(TipoCuenta tipo) {
        return switch (tipo) {
            case AHORRO -> ahorro;
            case PRESTAMO -> prestamo;
            case HIPOTECA -> hipoteca;
        };
    }
}
