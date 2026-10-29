package ejerciciosc3;

import java.util.List;
import java.util.Objects;

/** Ejercicio 3: búsqueda por saltos sobre una lista de códigos ordenada. */
public class Ejercicio3Saltos {
    static final class Resultado {
        final int indice;
        final int comparaciones;

        Resultado(int indice, int comparaciones) {
            this.indice = indice;
            this.comparaciones = comparaciones;
        }

        @Override
        public String toString() {
            return "índice=" + indice + ", comparaciones=" + comparaciones;
        }
    }

    /** O(√n) comparaciones y O(1) espacio adicional. */
    static Resultado buscar(List<Integer> codigos, int buscado) {
        Objects.requireNonNull(codigos);
        int n = codigos.size();
        if (n == 0) return new Resultado(-1, 0);

        int tamBloque = Math.max(1, (int) Math.sqrt(n));
        int inicio = 0;
        long limite = tamBloque;
        int comparaciones = 0;

        while (inicio < n) {
            int fin = (int) Math.min(limite, n) - 1;
            int cmpLimite = Integer.compare(codigos.get(fin), buscado);
            comparaciones++;

            if (cmpLimite >= 0) {
                for (int i = inicio; i <= fin; i++) {
                    int cmp = Integer.compare(codigos.get(i), buscado);
                    comparaciones++;
                    if (cmp == 0) return new Resultado(i, comparaciones);
                    if (cmp > 0) return new Resultado(-1, comparaciones);
                }
                return new Resultado(-1, comparaciones);
            }

            inicio = (int) limite;
            limite += tamBloque;
        }
        return new Resultado(-1, comparaciones);
    }

}
