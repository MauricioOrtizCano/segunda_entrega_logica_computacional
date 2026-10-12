public class Transaccion {
    private final long monto;

    public Transaccion(long monto) {
        this.monto = monto;
    }

    // Obtiene el monto de la transacción
    public long getMonto() {
        return monto;
    }

    /**
     * V(x) ≡ monto(x) > 0
     * @return boolean
     */
    public boolean esValida() {
        return monto > 0;
    }
}
