package cl.duoc.bancoxyz.commons.legacy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class LectorCsvLegacy {

    private static final String SEPARADOR = ",";

    private LectorCsvLegacy() {
    }

    public static List<FilaCsv> leer(InputStream entrada) {
        try (BufferedReader lector = new BufferedReader(new InputStreamReader(entrada, StandardCharsets.UTF_8))) {
            String cabecera = lector.readLine();
            if (cabecera == null) {
                return List.of();
            }
            String[] columnas = cabecera.replace("﻿", "").split(SEPARADOR, -1);
            List<FilaCsv> filas = new ArrayList<>();
            String linea;
            int numero = 1;
            while ((linea = lector.readLine()) != null) {
                numero++;
                if (linea.isBlank()) {
                    continue;
                }
                filas.add(new FilaCsv(numero, linea, mapear(columnas, linea.split(SEPARADOR, -1))));
            }
            return filas;
        } catch (IOException e) {
            throw new UncheckedIOException("No fue posible leer el archivo legacy", e);
        }
    }

    private static Map<String, String> mapear(String[] columnas, String[] partes) {
        Map<String, String> valores = new HashMap<>();
        for (int i = 0; i < columnas.length; i++) {
            valores.put(columnas[i].trim(), i < partes.length ? partes[i] : null);
        }
        return valores;
    }
}
