package cl.duoc.bancoxyz.cuentas.carga;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

class Moda<T> {

    private final Map<T, Integer> frecuencias = new LinkedHashMap<>();

    void agregar(T valor) {
        frecuencias.merge(valor, 1, Integer::sum);
    }

    Optional<T> valor() {
        T elegido = null;
        int maximo = 0;
        for (Map.Entry<T, Integer> entrada : frecuencias.entrySet()) {
            if (entrada.getValue() > maximo) {
                elegido = entrada.getKey();
                maximo = entrada.getValue();
            }
        }
        return Optional.ofNullable(elegido);
    }
}
