import java.util.List;
import java.util.ArrayList;

public class Lote {
    private List<Transaccion> loteTransacciones;
    private static final int MAX_TRANSACCIONES = 10;

    public Lote() {
        this.loteTransacciones = new ArrayList<>();
    }
    
    /**
     * Obtiene la lista de transacciones del lote
     * @return List
     */
    public List<Transaccion> getLoteTransaccions() {
        return new ArrayList<>(loteTransacciones);
    }

    /**
     * Obtiene el tamaño del lote
     * @return int
     */
    public int getTamanoLote() {
        return loteTransacciones.size();
    }

    /**
     * Agrega una transacción al lote 
     * @param transaccion
     */
    public void agregarTransaccion(Transaccion transaccion) {
        // Si excede el número máximo de transacciones manda una excepción
        if (getTamanoLote() >= MAX_TRANSACCIONES) throw new ExcesoTransaccionesException("Se ha excedido el número máximo de transacciones en el Lote");
        this.loteTransacciones.add(transaccion);
    }

    /**
     * Para cumplir con la regla de que un lote vacio no se procesa. No es parte de la validación lógica
     * @return boolean
     */
    public boolean estaVacio() {
        return getTamanoLote() == 0;
    }
}
