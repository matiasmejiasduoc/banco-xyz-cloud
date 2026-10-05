package cl.duoc.bancoxyz.cuentas.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "operacion_cuenta")
public class OperacionCuenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String referencia;

    @Column(nullable = false)
    private Long cuentaId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoOperacion tipo;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal monto;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal saldoAnterior;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal saldoPosterior;

    @Column(nullable = false, length = 30)
    private String canal;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(nullable = false)
    private boolean revertida;

    protected OperacionCuenta() {
    }

    public OperacionCuenta(String referencia, Long cuentaId, TipoOperacion tipo, BigDecimal monto,
            BigDecimal saldoAnterior, BigDecimal saldoPosterior, String canal) {
        this.referencia = referencia;
        this.cuentaId = cuentaId;
        this.tipo = tipo;
        this.monto = monto;
        this.saldoAnterior = saldoAnterior;
        this.saldoPosterior = saldoPosterior;
        this.canal = canal;
        this.fecha = LocalDateTime.now();
    }

    public boolean coincideCon(Long cuentaId, TipoOperacion tipo, BigDecimal monto) {
        return this.cuentaId.equals(cuentaId) && this.tipo == tipo && this.monto.compareTo(monto) == 0;
    }

    public void marcarRevertida() {
        revertida = true;
    }

    public boolean isRevertida() {
        return revertida;
    }

    public Long getId() {
        return id;
    }

    public String getReferencia() {
        return referencia;
    }

    public Long getCuentaId() {
        return cuentaId;
    }

    public TipoOperacion getTipo() {
        return tipo;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public BigDecimal getSaldoAnterior() {
        return saldoAnterior;
    }

    public BigDecimal getSaldoPosterior() {
        return saldoPosterior;
    }

    public String getCanal() {
        return canal;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }
}
