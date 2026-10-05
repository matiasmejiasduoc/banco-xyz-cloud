package cl.duoc.bancoxyz.cuentas.carga;

import cl.duoc.bancoxyz.commons.legacy.DatoLegacyInvalidoException;
import cl.duoc.bancoxyz.commons.legacy.FilaCsv;
import cl.duoc.bancoxyz.commons.legacy.ValorLegacy;
import cl.duoc.bancoxyz.cuentas.dominio.Cuenta;
import cl.duoc.bancoxyz.cuentas.dominio.TipoCuenta;
import java.math.BigDecimal;
import java.util.Optional;

class ConsolidadorCuenta {

    private static final int EDAD_MINIMA = 18;
    private static final int EDAD_MAXIMA = 110;
    private static final String TITULAR_DESCONOCIDO = "Titular no identificado";

    private final Long id;
    private final Moda<String> titulares = new Moda<>();
    private final Moda<TipoCuenta> tipos = new Moda<>();
    private final Moda<Integer> edades = new Moda<>();
    private BigDecimal ultimoSaldo;
    private int registros;
    private int incompletos;

    ConsolidadorCuenta(Long id) {
        this.id = id;
    }

    void agregar(FilaCsv fila) {
        registros++;
        boolean completo = true;

        Optional<String> titular = ValorLegacy.texto(fila.valor("nombre")).filter(n -> !n.equalsIgnoreCase("unknown"));
        titular.ifPresent(titulares::agregar);
        completo &= titular.isPresent();

        Optional<TipoCuenta> tipo = TipoCuenta.desde(fila.valor("tipo"));
        tipo.ifPresent(tipos::agregar);
        completo &= tipo.isPresent();

        Optional<Integer> edad = edad(fila.valor("edad"));
        edad.ifPresent(edades::agregar);
        completo &= edad.isPresent();

        Optional<BigDecimal> saldo = saldo(fila.valor("saldo"));
        saldo.ifPresent(valor -> ultimoSaldo = valor);
        completo &= saldo.isPresent();

        if (!completo) {
            incompletos++;
        }
    }

    Optional<Cuenta> consolidar() {
        Optional<TipoCuenta> tipo = tipos.valor();
        if (tipo.isEmpty() || ultimoSaldo == null) {
            return Optional.empty();
        }
        String observaciones = "Consolidada desde " + registros + " registros legacy (" + incompletos
                + " incompletos)";
        return Optional.of(new Cuenta(id, titulares.valor().orElse(TITULAR_DESCONOCIDO),
                edades.valor().orElse(null), tipo.get(), ultimoSaldo, observaciones));
    }

    private Optional<Integer> edad(String valor) {
        try {
            long edad = ValorLegacy.entero(valor, "edad");
            return edad >= EDAD_MINIMA && edad <= EDAD_MAXIMA ? Optional.of((int) edad) : Optional.empty();
        } catch (DatoLegacyInvalidoException e) {
            return Optional.empty();
        }
    }

    private Optional<BigDecimal> saldo(String valor) {
        try {
            BigDecimal saldo = ValorLegacy.decimal(valor, "saldo");
            return saldo.signum() >= 0 ? Optional.of(saldo) : Optional.empty();
        } catch (DatoLegacyInvalidoException e) {
            return Optional.empty();
        }
    }
}
