import java.util.Random;

/**
 * Pruebas del validador de lotes.
 * Compara la implementación con ciclo (validarConCiclo) contra la
 * especificación (cumpleEspecificacion), que es la traducción de
 * (∀x | x ∈ L : V(x)).
 *
 * Ejecutar con aserciones habilitadas: java -ea PruebasValidador
 */
public class PruebasValidador {

    private static final ValidadorLote VALIDADOR = new ValidadorLote();
    private static int totalPruebas = 0;
    private static int pruebasFallidas = 0;

    public static void main(String[] args) {
        System.out.println("=== Casos específicos ===");
        casosEspecificos();

        System.out.println();
        System.out.println("=== Regla de capacidad máxima ===");
        probarCapacidadMaxima();

        System.out.println();
        System.out.println("=== Prueba aleatoria (1000 lotes) ===");
        pruebaAleatoria(1000);

        System.out.println();
        System.out.println("Pruebas ejecutadas: " + totalPruebas
                + " | Fallidas: " + pruebasFallidas);
        if (pruebasFallidas > 0) {
            System.exit(1);
        }
    }

    // ---------- Casos específicos ----------

    private static void casosEspecificos() {
        // probarCaso(descripcion, resultadoEsperado, montos...)
        probarCaso("Lote vacío (verdad vacua)", true);
        probarCaso("Un elemento válido", true, 1000);
        probarCaso("Un elemento inválido", false, -500);
        probarCaso("Inválido al inicio", false, -1, 100, 200);
        probarCaso("Inválido en el medio", false, 100, -1, 200);
        probarCaso("Inválido al final", false, 100, 200, -1);
        probarCaso("Monto exactamente 0", false, 0);
        probarCaso("Un 0 en el medio", false, 100, 0, 200);
        probarCaso("Todos válidos", true, 5000, 100, 1);
        probarCaso("Todos negativos", false, -1, -2, -3);
        probarCaso("Ejemplo de la tabla de traza", false, 5000, -200, 300);
        probarCaso("Lote lleno, todos válidos", true,
                1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }

    private static void probarCaso(String descripcion, boolean esperado, long... montos) {
        Lote lote = crearLote(montos);

        boolean conCiclo = VALIDADOR.validarConCiclo(lote);
        boolean especificacion = VALIDADOR.cumpleEspecificacion(lote);

        boolean correcto = (conCiclo == esperado) && (especificacion == esperado);
        registrar(correcto);

        System.out.println((correcto ? "[OK]    " : "[FALLA] ") + descripcion
                + " -> ciclo=" + conCiclo
                + ", especificación=" + especificacion
                + ", esperado=" + esperado);
    }

    // ---------- Capacidad máxima ----------

    private static void probarCapacidadMaxima() {
        Lote lote = new Lote();
        boolean lanzoExcepcion = false;
        try {
            // El lote admite 10; la transacción número 11 debe rechazarse.
            for (int i = 1; i <= 11; i++) {
                lote.agregarTransaccion(new Transaccion(i));
            }
        } catch (ExcesoTransaccionesException e) {
            lanzoExcepcion = true;
        }
        boolean correcto = lanzoExcepcion && lote.getTamanoLote() == 10;
        registrar(correcto);
        System.out.println((correcto ? "[OK]    " : "[FALLA] ")
                + "La transacción 11 se rechaza y el lote conserva 10");
    }

    // ---------- Prueba aleatoria ----------

    private static void pruebaAleatoria(int cantidadLotes) {
        Random azar = new Random(42); // semilla fija: resultados reproducibles
        int discrepancias = 0;

        for (int n = 0; n < cantidadLotes; n++) {
            int tamano = azar.nextInt(11);           // 0 a 10 transacciones
            long[] montos = new long[tamano];
            for (int j = 0; j < tamano; j++) {
                montos[j] = azar.nextInt(11) - 5;    // montos entre -5 y 5
            }
            Lote lote = crearLote(montos);

            if (VALIDADOR.validarConCiclo(lote) != VALIDADOR.cumpleEspecificacion(lote)) {
                discrepancias++;
            }
        }

        boolean correcto = discrepancias == 0;
        registrar(correcto);
        System.out.println((correcto ? "[OK]    " : "[FALLA] ")
                + cantidadLotes + " lotes aleatorios, discrepancias: " + discrepancias);
    }

    // ---------- Utilidades ----------

    private static Lote crearLote(long... montos) {
        Lote lote = new Lote();
        for (long monto : montos) {
            lote.agregarTransaccion(new Transaccion(monto));
        }
        return lote;
    }

    private static void registrar(boolean correcto) {
        totalPruebas++;
        if (!correcto) {
            pruebasFallidas++;
        }
    }
}