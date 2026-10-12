import java.util.List;

public class ValidadorLote {
    /**
     * (∀x | x ∈ L : V(x))
     * @param lote
     * @return boolean
     */
    public boolean cumpleEspecificacion(Lote lote) {
        return lote.getLoteTransaccions().stream().allMatch(Transaccion::esValida);
    }

    /**
     * Implementación con ciclo.
     *
     * Invariante I: 0 ≤ i ≤ n  ∧  ok ≡ (∀k | 0 ≤ k < i : V(L[k]))
     *
     * @param lote lote a validar
     * @return true si todas las transacciones del lote son válidas
     */
    public boolean validarConCiclo(Lote lote) {
        List<Transaccion> lista = lote.getLoteTransaccions(); // una sola copia
        int n = lista.size();
        int i = 0;
        boolean ok = true; // rango vacío => ∀ vacuo => true

        // INICIO: I se cumple antes de la primera vuelta
        assert invariante(lista, i, ok) : "El invariante falla al inicio";

        while (i < n) {
            // Ruptura de rango (Teorema 13): nuevo ok ≡ ok ∧ V(L[i])
            ok = ok && lista.get(i).esValida();
            i = i + 1;

            // PRESERVACIÓN: I se sigue cumpliendo con el nuevo i
            assert invariante(lista, i, ok) : "El invariante falla con i = " + i;
        }

        // FINAL: I ∧ i = n  ⇒  ok ≡ (∀k | 0 ≤ k < n : V(L[k]))
        assert i == n : "Al salir del ciclo se esperaba i == n";
        return ok;
    }

    /**
     * Comprueba el invariante usando la versión declarativa sobre los
     * primeros i elementos.
     */
    private boolean invariante(List<Transaccion> lista, int i, boolean ok) {
        return 0 <= i && i <= lista.size()
                && ok == lista.subList(0, i).stream().allMatch(Transaccion::esValida);
    }
}
