package ejerciciosc3;

import java.util.List;
import java.util.Objects;

/** Ejercicio 2: lowerBound e inserción de precios ordenados con duplicados. */
public class Ejercicio2Binaria {
    /** Devuelve el primer índice con valor >= precio. O(log n) tiempo, O(1) espacio. */
    static int lowerBound(List<Integer> precios, int precio) {
        Objects.requireNonNull(precios);
        int izquierda = 0;
        int derecha = precios.size(); // extremo exclusivo
        while (izquierda < derecha) {
            int medio = izquierda + (derecha - izquierda) / 2;
            if (precios.get(medio) < precio) {
                izquierda = medio + 1;
            } else {
                derecha = medio;
            }
        }
        return izquierda;
    }

    /** Inserta antes de los precios iguales. En ArrayList, desplazar cuesta O(n). */
    static void insertarPrecio(List<Integer> precios, int precio) {
        precios.add(lowerBound(precios, precio), precio);
    }

}
