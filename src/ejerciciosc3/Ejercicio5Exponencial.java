package ejerciciosc3;

import java.util.List;
import java.util.Objects;

/** Ejercicio 5: búsqueda exponencial de eventos en una lista ordenada. */
public class Ejercicio5Exponencial {
    /**
     * Devuelve la primera coincidencia en el prefijo cargado al iniciar la llamada.
     * Si la carga sigue activa, repite la búsqueda cuando se agreguen datos o
     * espera a que termine para concluir que un evento no existe.
     * Tiempo O(log p), peor caso O(log n); espacio adicional O(1).
     */
    static int buscarPrimero(List<Integer> eventos, int buscado) {
        Objects.requireNonNull(eventos);
        int n = eventos.size();
        if (n == 0 || eventos.get(0) > buscado) return -1;
        if (eventos.get(0) == buscado) return 0;

        long limite = 1;
        while (limite < n && eventos.get((int) limite) < buscado) {
            limite *= 2;
        }

        int izquierda = (int) (limite / 2);
        int derechaExclusiva = (int) Math.min(limite + 1, (long) n);
        int posicion = lowerBoundEnRango(eventos, buscado, izquierda, derechaExclusiva);
        return posicion < n && eventos.get(posicion) == buscado ? posicion : -1;
    }

    private static int lowerBoundEnRango(List<Integer> lista, int buscado,
                                         int izquierda, int derechaExclusiva) {
        int lo = izquierda;
        int hi = derechaExclusiva;
        while (lo < hi) {
            int medio = lo + (hi - lo) / 2;
            if (lista.get(medio) < buscado) lo = medio + 1;
            else hi = medio;
        }
        return lo;
    }

}
