package ejerciciosc3;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Ejercicio 1: buscar el primer ticket crítico sin cambiar el orden. */
public class Ejercicio1Lineal {
    static final class Ticket {
        final String codigo;
        final boolean critico;

        Ticket(String codigo, boolean critico) {
            this.codigo = Objects.requireNonNull(codigo);
            this.critico = critico;
        }
    }

    /** O(n) tiempo y O(1) espacio adicional. */
    static Optional<Ticket> primerTicketCritico(List<Ticket> cola) {
        Objects.requireNonNull(cola);
        for (Ticket ticket : cola) {
            if (ticket.critico) {
                return Optional.of(ticket);
            }
        }
        return Optional.empty();
    }

}
