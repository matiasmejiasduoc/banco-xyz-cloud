package cl.duoc.bancoxyz.cuentas.dominio;

import cl.duoc.bancoxyz.commons.error.ReglaNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Entity
@Table(name = "cuenta")
public class Cuenta {

    @Id
    private Long id;

    @Column(nullable = false, length = 80)
    private String titular;

    private Integer edad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoCuenta tipo;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal saldo;

    @Column(length = 200)
    private String observaciones;

    @Column(nullable = false)
    private LocalDateTime actualizadaEn;

    protected Cuenta() {
    }

    public Cuenta(Long id, String titular, Integer edad, TipoCuenta tipo, BigDecimal saldo, String observaciones) {
        this.id = id;
        this.titular = titular;
        this.edad = edad;
        this.tipo = tipo;
        this.saldo = saldo.setScale(2, RoundingMode.HALF_UP);
        this.observaciones = observaciones;
        this.actualizadaEn = LocalDateTime.now();
    }

    public void debitar(BigDecimal monto) {
        if (!tipo.permiteRetiros()) {
            throw new ReglaNegocioException("RETIRO_NO_PERMITIDO",
                    "La cuenta " + id + " es de tipo " + tipo + " y no admite retiros");
        }
        if (saldo.compareTo(monto) < 0) {
            throw new ReglaNegocioException("SALDO_INSUFICIENTE",
                    "La cuenta " + id + " no tiene saldo suficiente para debitar " + monto);
        }
        saldo = saldo.subtract(monto);
        actualizadaEn = LocalDateTime.now();
    }

    public void acreditar(BigDecimal monto) {
        saldo = saldo.add(monto);
        actualizadaEn = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getTitular() {
        return titular;
    }

    public Integer getEdad() {
        return edad;
    }

    public TipoCuenta getTipo() {
        return tipo;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public LocalDateTime getActualizadaEn() {
        return actualizadaEn;
    }
}
