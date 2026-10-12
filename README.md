# Verificación de un lote de transacciones con invariante de ciclo

**Entrega Previa 2 – Semana 7 · Herramientas de Lógica Computacional**
Politécnico Grancolombiano · Ingeniería de Software · Subgrupo B06

**Autores:** Carol Dayana Muñoz Reyes, Cristian Mauricio Ortiz Cano

---

## 1. Objetivo

Aplicar las proposiciones cuantificadas del cálculo de predicados a un caso de Ingeniería de Software: **demostrar la corrección de un ciclo de validación** que revisa que todas las transacciones de un lote tengan un monto mayor que cero.

El proyecto muestra que el código con ciclo (`validarConCiclo`) calcula exactamente lo que dice la especificación lógica (`cumpleEspecificacion`), usando un **invariante de ciclo** y el **Teorema 13 (Ruptura de rango)** de la Lectura fundamental 6. Además, el invariante se comprueba en ejecución mediante aserciones.

El desarrollo completo de la demostración está en el documento escrito del grupo. Este README explica el código y cómo se relaciona con esa demostración.

---

## 2. Modelo de dominio

| Clase | Responsabilidad |
|---|---|
| `Transaccion` | Guarda el monto (en pesos colombianos, tipo `long`) y expone el predicado `esValida()`, que corresponde a `V(x) ≡ monto(x) > 0`. |
| `Lote` | Agrupa transacciones y aplica la regla de capacidad máxima (10). Lanza `ExcesoTransaccionesException` si se intenta superar. |
| `ValidadorLote` | Contiene la especificación declarativa y la implementación con ciclo. |
| `PruebasValidador` | Compara ambas versiones en casos específicos y en una prueba aleatoria. |

### Decisiones de diseño

- **El monto es `long`, no `int` ni `float`.** Los pesos no usan decimales en la práctica y un `int` (≈ 2.147 millones) puede desbordarse con transferencias empresariales.
- **El constructor de `Transaccion` no rechaza montos ≤ 0.** Las transferencias llegan de fuera con cualquier monto, y `esValida()` es el predicado que las clasifica. Si el constructor las rechazara, nunca existirían transacciones inválidas y el ciclo no verificaría nada.
- **La capacidad máxima es una regla de `Lote`**, distinta de la propiedad lógica `∀`. Para la demostración actúa como precondición.
- **Lote vacío.** La lógica dice que un `∀` sobre un rango vacío es verdadero (verdad vacua), así que el validador devuelve `true` para un lote vacío. La regla de negocio "un lote vacío no se procesa" se mantiene **aparte**, en `Lote.estaVacio()`, y debe consultarla quien llama al validador.

---

## 3. Especificación formal

Variables: `L` es el lote (de tipo lista de transacciones) y `x` es una transacción.

```
V(x) ≡ monto(x) > 0

Especificación:   (∀x | x ∈ L : V(x))
```

Se lee: "para toda transacción `x` que pertenece al lote `L`, el monto de `x` es mayor que cero". El método `cumpleEspecificacion` es su traducción directa (`allMatch`).

Con el lote como arreglo `L[0..n-1]`, la misma propiedad se escribe con índices:

```
(∀k | 0 ≤ k < n : V(L[k]))
```

Aquí `k` es una **posición** (variable ligada) y `L[k]` es la transacción en esa posición.

---

## 4. Demostración de corrección (resumen)

**Invariante `I`** (se cumple antes y después de cada vuelta):

```
0 ≤ i ≤ n   ∧   ok ≡ (∀k | 0 ≤ k < i : V(L[k]))
```

Significa: "`ok` dice si las primeras `i` transacciones son todas válidas".

### 4.1 Inicio: `i = 0`, `ok = true`

```
    (∀k | 0 ≤ k < 0 : V(L[k]))
≡ ⟨El rango es vacío: 0 ≤ k < 0 ≡ false⟩
    (∀k | false : V(L[k]))
≡ ⟨Axioma de trueque para ∀⟩
    (∀k |: false → V(L[k]))
≡ ⟨false → P ≡ true⟩
    (∀k |: true)
≡ ⟨Teorema del término constante⟩
    true
```

Por eso `ok` empieza en `true`: aún no se ha encontrado ninguna transacción inválida.

### 4.2 Preservación: de `i` a `i + 1`

Si `I` vale al comenzar la vuelta, el nuevo `ok` debe ser `(∀k | 0 ≤ k < i+1 : V(L[k]))`:

```
    (∀k | 0 ≤ k < i+1 : V(L[k]))
≡ ⟨Aritmética entera: 0 ≤ k < i+1 ≡ (0 ≤ k < i) ∨ k = i⟩
    (∀k | (0 ≤ k < i) ∨ k = i : V(L[k]))
≡ ⟨Teorema 13 (Ruptura de rango)⟩
    (∀k | 0 ≤ k < i : V(L[k])) ∧ (∀k | k = i : V(L[k]))
≡ ⟨Hipótesis (I) para la 1.ª mitad; rango de un solo elemento para la 2.ª⟩
    ok ∧ V(L[i])
```

- La **primera mitad** es lo que el invariante ya dice que vale `ok`.
- La **segunda mitad** solo menciona la posición `i`, así que equivale a `V(L[i])`.

Nota sobre la segunda mitad: `(∀k | k = i : V(L[k])) ≡ V(L[i])` se obtiene con el axioma de trueque, Leibniz (dentro del rango `k = i`, `k` se sustituye por `i`), la distribución de `∨` sobre `∀` (Teorema 9, porque `k` no es libre en `V(L[i])`) y `(∃k |: k = i) ≡ true`.

Por tanto: **`ok_nuevo ≡ ok ∧ V(L[i])`**, que en Java es `ok = ok && lista.get(i).esValida()`.

### 4.3 Final: `i = n`

El ciclo termina cuando `i < n` es falso. Junto con `i ≤ n` del invariante, se obtiene `i = n`, y `I` queda:

```
ok ≡ (∀k | 0 ≤ k < n : V(L[k]))
```

Es exactamente la especificación del lote completo. Por eso `return ok` es correcto.

---

## 5. Mapa código ↔ demostración

| Línea de código (`validarConCiclo`) | Paso de la demostración |
|---|---|
| `int i = 0; boolean ok = true;` | Inicio (4.1): rango vacío, trueque y verdad vacua |
| `assert invariante(lista, i, ok)` antes del ciclo | Comprobación de que `I` vale al inicio |
| `while (i < n)` | Condición de parada; con `0 ≤ i ≤ n` implica `i = n` al salir |
| `ok = ok && lista.get(i).esValida();` | Preservación (4.2): Teorema 13, `ok ∧ V(L[i])` |
| `i = i + 1;` | Cambio de rango de `0 ≤ k < i` a `0 ≤ k < i+1` |
| `assert invariante(lista, i, ok)` dentro del ciclo | Comprobación de que `I` se preserva en cada vuelta |
| `assert i == n;` y `return ok;` | Final (4.3): `I ∧ i = n` ⇒ `ok ≡ (∀k | 0 ≤ k < n : V(L[k]))` |

---

## 6. Estructura del proyecto

```
segunda_entrega/
├── Transaccion.java                 Modelo de la transacción y predicado V(x)
├── Lote.java                        Colección con capacidad máxima
├── ExcesoTransaccionesException.java  Excepción por superar la capacidad
├── ValidadorLote.java               Especificación y versión con ciclo + invariante
├── PruebasValidador.java            Casos específicos y prueba aleatoria
└── README.md
```

---

## 7. Cómo compilar y ejecutar

Desde la carpeta del proyecto:

```
javac -encoding UTF-8 *.java
java -ea PruebasValidador
```

> **Importante:** la opción `-ea` activa las aserciones. **Sin ella, los `assert` del invariante no se evalúan** y solo se estaría comparando el resultado final.

Los archivos `.class` generados son productos de compilación y no forman parte de la entrega.

---

## 8. Pruebas y evidencia

`PruebasValidador` ejecuta, para cada lote, `validarConCiclo` y `cumpleEspecificacion`, y comprueba que ambos coincidan con el resultado esperado (escrito a mano en cada caso).

| Caso | Por qué se prueba |
|---|---|
| Lote vacío → `true` | Verdad vacua (caso de inicio de la demostración) |
| Un elemento válido / inválido | Casos mínimos |
| Inválido al inicio, en el medio y al final | Que `ok`, una vez en `false`, nunca vuelva a `true` |
| Monto exactamente `0` y `0` en el medio | Caso borde de `> 0` contra `>= 0` |
| Todos válidos / todos negativos | Casos extremos |
| `[5000, -200, 300]` | Ejemplo de la tabla de traza de la demostración |
| Lote lleno (10 válidos) | Límite de capacidad |
| Transacción número 11 | La excepción de capacidad se lanza y el lote conserva 10 |
| 1000 lotes aleatorios (tamaño 0–10, montos −5 a 5, semilla fija) | Coincidencia general entre ciclo y especificación |

### Evidencia de ejecución

Salida con todas las pruebas correctas:

![Pruebas correctas](docs/pruebas_ok.png)

Comprobación de que el invariante detecta errores: al cambiar temporalmente `boolean ok = true;` por `false`, la aserción falla desde la primera llamada (`El invariante falla al inicio`):

![Fallo del invariante](docs/invariante_falla.png)

---

## 9. Decisiones y limitaciones

- El validador sigue la lógica: lote vacío → `true`. La regla de negocio de no procesar lotes vacíos se aplica fuera de él.
- Las aserciones sirven para verificar durante el desarrollo y las pruebas; no están pensadas para dejarse activas en producción.
- El modelo no contempla decimales (centavos), monedas distintas ni desbordamiento de `long`.
- La prueba aleatoria usa semilla fija (`42`) para que los resultados sean reproducibles.

---

## 10. Referencias

- Politécnico Grancolombiano. *Herramientas de Lógica Computacional*. Lectura fundamental 5 (predicados y cuantificadores) y Lectura fundamental 6 (teoremas de ∀ y ∃, Teorema 13: Ruptura de rango, Teorema 9, Teorema del término constante, metateorema de la generalización).
- Aula virtual del curso: https://poli.instructure.com/courses/101888