package cl.duoc.bancoxyz.commons.api;

import java.util.List;
import java.util.function.Function;

public record Pagina<T>(List<T> contenido, int pagina, int tamano, long totalElementos, int totalPaginas) {

    public static <T> Pagina<T> de(List<T> contenido, int pagina, int tamano, long totalElementos) {
        int totalPaginas = tamano == 0 ? 0 : (int) Math.ceil((double) totalElementos / tamano);
        return new Pagina<>(contenido, pagina, tamano, totalElementos, totalPaginas);
    }

    public <R> Pagina<R> map(Function<T, R> transformacion) {
        return new Pagina<>(contenido.stream().map(transformacion).toList(), pagina, tamano, totalElementos,
                totalPaginas);
    }

    public boolean haySiguiente() {
        return pagina + 1 < totalPaginas;
    }
}
