package cl.duoc.bancoxyz.bff.web.api;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class Agregador {

    private static final Logger log = LoggerFactory.getLogger(Agregador.class);

    private final ExecutorService ejecutor;

    public Agregador(ExecutorService ejecutorAgregacion) {
        this.ejecutor = ejecutorAgregacion;
    }

    public <T> CompletableFuture<T> lanzar(Supplier<T> consulta) {
        return CompletableFuture.supplyAsync(consulta, ejecutor);
    }

    public <T> T esperar(CompletableFuture<T> futuro) {
        try {
            return futuro.join();
        } catch (CompletionException e) {
            if (e.getCause() instanceof RuntimeException causa) {
                throw causa;
            }
            throw e;
        }
    }

    public <T> T esperarOpcional(CompletableFuture<T> futuro, String seccion, List<String> avisos) {
        try {
            return futuro.join();
        } catch (CompletionException e) {
            log.warn("No fue posible obtener {}: {}", seccion, e.getCause().getMessage());
            avisos.add("La seccion " + seccion + " no esta disponible en este momento");
            return null;
        }
    }
}
