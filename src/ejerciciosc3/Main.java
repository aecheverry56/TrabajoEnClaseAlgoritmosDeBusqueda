package ejerciciosc3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.LongSupplier;

/** Punto de entrada: valida límites y mide los cinco algoritmos. */
public class Main {
    private static final int TAMANO_GRANDE = 200_000;
    private static final long TIEMPO_MINIMO_MUESTRA_NS = 30_000_000L;
    private static final int MAX_REPETICIONES = 1 << 24;
    private static volatile long sumidero;
    private static volatile int objetivoActivo;
    private static volatile List<Ejercicio1Lineal.Ticket> colaActiva;

    private static final class Medicion {
        final double microsegundosPorBusqueda;

        Medicion(double microsegundosPorBusqueda) {
            this.microsegundosPorBusqueda = microsegundosPorBusqueda;
        }
    }

    public static void main(String[] args) {
        verificarCasosLimite();
        medirAlgoritmos();
    }

    private static void verificarCasosLimite() {
        // Lineal: cola vacía, sin crítico y más de un crítico.
        check(!Ejercicio1Lineal.primerTicketCritico(
                Collections.<Ejercicio1Lineal.Ticket>emptyList()).isPresent(), "cola vacía");
        List<Ejercicio1Lineal.Ticket> cola = Arrays.asList(
                new Ejercicio1Lineal.Ticket("A", false),
                new Ejercicio1Lineal.Ticket("B", true),
                new Ejercicio1Lineal.Ticket("C", true));
        check("B".equals(Ejercicio1Lineal.primerTicketCritico(cola).get().codigo),
                "primer crítico");
        check(!Ejercicio1Lineal.primerTicketCritico(Arrays.asList(
                new Ejercicio1Lineal.Ticket("A", false))).isPresent(), "sin crítico");

        // Binaria: lista vacía, duplicados y extremos de inserción.
        check(Ejercicio2Binaria.lowerBound(Collections.<Integer>emptyList(), 5) == 0,
                "lowerBound vacío");
        List<Integer> duplicados = Arrays.asList(1, 2, 2, 4);
        check(Ejercicio2Binaria.lowerBound(duplicados, 2) == 1, "lowerBound duplicado");
        check(Ejercicio2Binaria.lowerBound(duplicados, -1) == 0, "lowerBound antes del inicio");
        check(Ejercicio2Binaria.lowerBound(duplicados, 9) == duplicados.size(),
                "lowerBound después del final");
        List<Integer> precios = new ArrayList<>(Arrays.asList(10, 20, 20, 40));
        Ejercicio2Binaria.insertarPrecio(precios, 20);
        check(precios.equals(Arrays.asList(10, 20, 20, 20, 40)), "insertar precio duplicado");

        // Saltos: lista vacía, extremos y elemento ausente.
        check(Ejercicio3Saltos.buscar(Collections.<Integer>emptyList(), 1).indice == -1,
                "saltos vacío");
        check(Ejercicio3Saltos.buscar(Arrays.asList(2, 4, 6, 8), 2).indice == 0,
                "saltos primero");
        check(Ejercicio3Saltos.buscar(Arrays.asList(2, 4, 6, 8), 8).indice == 3,
                "saltos último");
        check(Ejercicio3Saltos.buscar(Arrays.asList(2, 4, 6, 8), 5).indice == -1,
                "saltos ausente");

        // Interpolación: lista vacía, valores repetidos y rango constante.
        check(Ejercicio4Interpolacion.buscarPrimero(
                Collections.<Integer>emptyList(), 1) == -1, "interpolación vacío");
        check(Ejercicio4Interpolacion.buscarPrimero(Arrays.asList(1, 3, 3, 3, 5), 3) == 1,
                "interpolación duplicado");
        check(Ejercicio4Interpolacion.buscarPrimero(Arrays.asList(7, 7, 7), 7) == 0,
                "interpolación valores iguales");
        check(Ejercicio4Interpolacion.buscarPrimero(Arrays.asList(7, 7, 7), 8) == -1,
                "interpolación fuera de rango");

        // Exponencial: lista vacía, duplicados, extremo y ausente.
        check(Ejercicio5Exponencial.buscarPrimero(
                Collections.<Integer>emptyList(), 1) == -1, "exponencial vacío");
        check(Ejercicio5Exponencial.buscarPrimero(Arrays.asList(1, 3, 3, 3, 8), 3) == 1,
                "exponencial duplicado");
        check(Ejercicio5Exponencial.buscarPrimero(Arrays.asList(1, 3, 5, 8), 8) == 3,
                "exponencial último");
        check(Ejercicio5Exponencial.buscarPrimero(Arrays.asList(1, 3, 5, 8), 9) == -1,
                "exponencial ausente");

        System.out.println("Casos limite: correctos.");
    }

    private static void medirAlgoritmos() {
        int n = TAMANO_GRANDE;
        List<Integer> datos = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            datos.add(i); // IDs uniformes y ordenados para la medición.
        }

        String[] casos = {"primero", "medio", "ultimo", "ausente < min", "ausente > max"};
        int[] objetivos = {0, n / 2, n - 1, -1, n};
        int[] posicionCritica = {0, n / 2, n - 1, -1, -1};

        System.out.println();
        System.out.println("Medicion con " + n + " datos ordenados.");
        System.out.println("Mediana de 3 muestras en microsegundos por busqueda (minimo 30 ms por muestra).");
        System.out.println("Caso             | Lineal cola | lowerBound | Saltos (cmp) | Interpolacion | Exponencial");
        System.out.println("-----------------+-------------+------------+--------------+---------------+------------");

        for (int c = 0; c < casos.length; c++) {
            final int objetivo = objetivos[c];
            final List<Ejercicio1Lineal.Ticket> cola = crearCola(n, posicionCritica[c]);
            int indiceEsperado = objetivo >= 0 && objetivo < n ? objetivo : -1;
            boolean hayCritico = posicionCritica[c] >= 0;

            verificarResultado(datos, objetivo, indiceEsperado);
            check(Ejercicio1Lineal.primerTicketCritico(cola).isPresent() == hayCritico,
                    "cola grande " + casos[c]);
            Ejercicio3Saltos.Resultado resultadoSalto = Ejercicio3Saltos.buscar(datos, objetivo);

            // Lecturas volátiles por llamada evitan que la JVM sustituya el
            // algoritmo por el resultado constante de una búsqueda repetida.
            objetivoActivo = objetivo;
            colaActiva = cola;

            Medicion lineal = medir(() ->
                    Ejercicio1Lineal.primerTicketCritico(colaActiva).isPresent() ? 1 : 0);
            Medicion binaria = medir(() -> Ejercicio2Binaria.lowerBound(datos, objetivoActivo));
            Medicion saltos = medir(() -> {
                Ejercicio3Saltos.Resultado r = Ejercicio3Saltos.buscar(datos, objetivoActivo);
                return ((long) r.indice + 1) * 1_000_003L + r.comparaciones;
            });
            Medicion interpolacion = medir(() ->
                    Ejercicio4Interpolacion.buscarPrimero(datos, objetivoActivo));
            Medicion exponencial = medir(() ->
                    Ejercicio5Exponencial.buscarPrimero(datos, objetivoActivo));

            System.out.printf(Locale.ROOT,
                    "%-16s | %9.3f us | %8.3f us | %8.3f us (%d) | %9.3f us | %8.3f us%n",
                    casos[c], lineal.microsegundosPorBusqueda,
                    binaria.microsegundosPorBusqueda, saltos.microsegundosPorBusqueda,
                    resultadoSalto.comparaciones, interpolacion.microsegundosPorBusqueda,
                    exponencial.microsegundosPorBusqueda);
        }
        System.out.println();
        System.out.println("Saltos muestra tiempo y cantidad de comparaciones entre parentesis.");
        System.out.println("Los tiempos pueden variar segun el equipo y la carga del sistema.");
    }

    private static List<Ejercicio1Lineal.Ticket> crearCola(int n, int indiceCritico) {
        List<Ejercicio1Lineal.Ticket> cola = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            cola.add(new Ejercicio1Lineal.Ticket("ticket", i == indiceCritico));
        }
        return cola;
    }

    private static void verificarResultado(List<Integer> datos, int objetivo, int indiceEsperado) {
        int lowerBoundEsperado = objetivo < 0 ? 0 : Math.min(objetivo, datos.size());
        check(Ejercicio2Binaria.lowerBound(datos, objetivo) == lowerBoundEsperado,
                "lowerBound en datos grandes");
        check(Ejercicio3Saltos.buscar(datos, objetivo).indice == indiceEsperado,
                "saltos en datos grandes");
        check(Ejercicio4Interpolacion.buscarPrimero(datos, objetivo) == indiceEsperado,
                "interpolación en datos grandes");
        check(Ejercicio5Exponencial.buscarPrimero(datos, objetivo) == indiceEsperado,
                "exponencial en datos grandes");
    }

    private static Medicion medir(LongSupplier algoritmo) {
        long calentamiento = 0;
        for (int i = 0; i < 100; i++) {
            calentamiento += algoritmo.getAsLong();
        }
        sumidero = calentamiento;

        double[] muestras = new double[3];
        for (int i = 0; i < muestras.length; i++) {
            muestras[i] = medirUnaMuestra(algoritmo);
        }
        Arrays.sort(muestras);
        return new Medicion(muestras[1]);
    }

    private static double medirUnaMuestra(LongSupplier algoritmo) {
        int repeticiones = 1;
        long duracion;
        long resultado = 0;
        while (true) {
            resultado = 0;
            long inicio = System.nanoTime();
            for (int i = 0; i < repeticiones; i++) {
                resultado += algoritmo.getAsLong();
            }
            duracion = System.nanoTime() - inicio;

            if (duracion >= TIEMPO_MINIMO_MUESTRA_NS || repeticiones >= MAX_REPETICIONES) {
                break;
            }
            repeticiones = Math.min(repeticiones * 2, MAX_REPETICIONES);
        }

        sumidero = resultado;
        return (double) duracion / repeticiones / 1_000.0;
    }

    private static void check(boolean condicion, String caso) {
        if (!condicion) {
            throw new AssertionError("Fallo el caso: " + caso);
        }
    }
}
