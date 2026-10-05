package cl.duoc.bancoxyz.movimientos.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "movimiento", indexes = @Index(name = "idx_movimiento_cuenta_fecha", columnList = "cuentaId, fecha"))
public class Movimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long cuentaId;

    @Column(nullable = false)
    private LocalDate fecha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoMovimiento tipo;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal monto;

    @Column(nullable = false, length = 120)
    private String descripcion;

    @Column(nullable = false, length = 30)
    private String canal;

    @Column(unique = true, length = 80)
    private String referencia;

    @Column(length = 200)
    private String observaciones;

    @Column(nullable = false)
    private LocalDateTime registradoEn;

    protected Movimiento() {
    }

    public Movimiento(Long cuentaId, LocalDate fecha, TipoMovimiento tipo, BigDecimal monto, String descripcion,
            String canal, String referencia, String observaciones) {
        this.cuentaId = cuentaId;
        this.fecha = fecha;
        this.tipo = tipo;
        this.monto = monto;
        this.descripcion = descripcion;
        this.canal = canal;
        this.referencia = referencia;
        this.observaciones = observaciones;
        this.registradoEn = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getCuentaId() {
        return cuentaId;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public TipoMovimiento getTipo() {
        return tipo;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getCanal() {
        return canal;
    }

    public String getReferencia() {
        return referencia;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public LocalDateTime getRegistradoEn() {
        return registradoEn;
    }
}
