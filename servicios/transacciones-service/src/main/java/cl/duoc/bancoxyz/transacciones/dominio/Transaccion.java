package cl.duoc.bancoxyz.transacciones.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "transaccion", indexes = @Index(name = "idx_transaccion_fecha", columnList = "fecha"))
public class Transaccion {

    @Id
    private Long id;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal monto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoTransaccion tipo;

    @Column(length = 200)
    private String observaciones;

    protected Transaccion() {
    }

    public Transaccion(Long id, LocalDate fecha, BigDecimal monto, TipoTransaccion tipo, String observaciones) {
        this.id = id;
        this.fecha = fecha;
        this.monto = monto;
        this.tipo = tipo;
        this.observaciones = observaciones;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public TipoTransaccion getTipo() {
        return tipo;
    }

    public String getObservaciones() {
        return observaciones;
    }
}
