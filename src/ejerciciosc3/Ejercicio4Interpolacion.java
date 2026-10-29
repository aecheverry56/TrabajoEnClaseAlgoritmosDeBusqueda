package ejerciciosc3;

import java.util.List;
import java.util.Objects;

/** Ejercicio 4: búsqueda por interpolación de IDs uniformemente distribuidos. */
public class Ejercicio4Interpolacion {
    /**
     * Devuelve el primer índice del ID repetido o -1 si no aparece.
     * Esperado O(log log n) con distribución uniforme; peor caso O(n).
     * Espacio adicional O(1).
     */
    static int buscarPrimero(List<Integer> ids, int buscado) {
        Objects.requireNonNull(ids);
        int izquierda = 0;
        int derecha = ids.size() - 1;

        while (izquierda <= derecha
                && buscado >= ids.get(izquierda)
                && buscado <= ids.get(derecha)) {
            int valorIzquierdo = ids.get(izquierda);
            int valorDerecho = ids.get(derecha);

            if (valorIzquierdo == valorDerecho) {
                return valorIzquierdo == buscado ? izquierda : -1;
            }

            long numerador = ((long) buscado - valorIzquierdo) * (derecha - izquierda);
            long denominador = (long) valorDerecho - valorIzquierdo;
            int posicion = izquierda + (int) (numerador / denominador);
            posicion = Math.max(izquierda, Math.min(posicion, derecha));

            int valor = ids.get(posicion);
            if (valor == buscado) {
                while (posicion > 0 && ids.get(posicion - 1) == buscado) posicion--;
                return posicion;
            }
            if (valor < buscado) izquierda = posicion + 1;
            else derecha = posicion - 1;
        }
        return -1;
    }

}
