//Jhon Erik Peña Caro
//Entrega2_GrupoB01_Equipo13

/**
 * =============================================================
 * MEMOANIMALIA ENGINE
 * -------------------------------------------------------------
 * Esta clase implementa toda la lógica del juego MemoAnimalia 
 * aun sin su interfaz grafica.
 *
 * FUNCIONALIDADES PRINCIPALES:
 *  - Manejo del tablero y sus cartas.
 *  - Control de intentos.
 *  - Conteo de aciertos.
 *  - Comparación de cartas en cada turno.
 *  - Gestión del límite de intentos.
 *  - Eventos notificables hacia una futura interfaz gráfica.
 *
 * Este archivo es suficientemente completo para ser usado por 
 * cualquier interfaz (JavaFX, Swing o consola), ya que expone
 * solo la lógica y el estado del juego.
 * =============================================================
 */

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class MemoAnimaliaEngine {

    /** Número de filas por defecto del tablero */
    public static final int DEFAULT_ROWS = 3;
    /** Número de columnas por defecto del tablero */
    public static final int DEFAULT_COLS = 3;
    /** Límite de intentos permitido antes de reiniciar el juego */
    public static final int DEFAULT_ATTEMPT_LIMIT = 12;

    /**
     * =============================================================
     * Enumeración FlipResult
     * -------------------------------------------------------------
     * Representa los resultados posibles al intentar voltear una
     * carta durante el turno del jugador.
     * =============================================================
     */
    public enum FlipResult { 
        OK_REVEALED,     // Carta revelada correctamente (primer flip)
        MATCH,           // Pareja encontrada
        NO_MATCH,        // No hacen pareja
        ALREADY_REVEALED,// La carta ya estaba revelada o emparejada
        INVALID_INDEX    // Índice fuera del rango permitido
    }

    /**
     * =============================================================
     * Clase interna Card
     * -------------------------------------------------------------
     * Representa una carta individual en el juego. Cada carta 
     * tiene un ID (generalmente el nombre del animal) y estados
     * booleanos para saber si está revelada y si ya fue emparejada.
     * =============================================================
     */
    public static class Card {

        /** Identificador del animal o imagen asociada a la carta */
        private final String id;
        /** Indica si la carta está revelada en el turno actual */
        private boolean revealed = false;
        /** Indica si la carta ya fue emparejada correctamente */
        private boolean matched = false;

        /**
         * Constructor de Carta
         * @param id Identificador de la carta
         */
        public Card(String id) { 
            this.id = id; 
        }

        public String getId() { return id; }
        public boolean isRevealed() { return revealed; }
        public void setRevealed(boolean r) { revealed = r; }
        public boolean isMatched() { return matched; }
        public void setMatched(boolean m) { matched = m; }

        @Override 
        public String toString() { 
            return "Card(" + id + ",rev=" + revealed + ",mat=" + matched + ")"; 
        }
    }

    /**
     * =============================================================
     * Interfaz GameListener
     * -------------------------------------------------------------
     * Notifica eventos clave del juego hacia una interfaz gráfica
     * (JavaFX). El motor del juego no depende visualmente de nada,
     * pero informa sobre los cambios en tiempo real.
     * =============================================================
     */
    public interface GameListener {
        void onCardRevealed(int index, Card card);
        void onCardsHidden(int index1, int index2);
        void onCardsMatched(int index1, int index2);
        void onAttemptChanged(int attempts);
        void onMatchCountChanged(int matchesFound);
        void onGameWon(int attempts);
        void onGameOverAttemptLimit(int attemptLimit);
        void onGameReset();
    }

    /** Cantidad de filas del tablero */
    private final int rows;
    /** Cantidad de columnas del tablero */
    private final int cols;
    /** Tamaño total del tablero (rows * cols) */
    private final int size;
    /** Límite máximo de intentos permitidos */
    private final int attemptLimit;

    /** Lista que contiene todas las cartas del tablero */
    private final List<Card> board = new ArrayList<>();

    /** Listener para notificar eventos hacia la interfaz */
    private GameListener listener;

    /** Número de intentos realizados hasta el momento */
    private int attempts = 0;
    /** Cantidad de parejas encontradas */
    private int matchesFound = 0;

    /** Índice de la primera carta seleccionada en un turno */
    private Integer firstSelectedIndex = null;

    /**
     * Constructor principal del motor del juego.
     */
    public MemoAnimaliaEngine(int rows, int cols, List<String> imageIds, int attemptLimit) {
        this.rows = rows;
        this.cols = cols;
        this.size = rows * cols;
        this.attemptLimit = attemptLimit;
        initBoard(imageIds);
    }

    /** Constructor usando valores por defecto */
    public MemoAnimaliaEngine(List<String> imageIds) {
        this(DEFAULT_ROWS, DEFAULT_COLS, imageIds, DEFAULT_ATTEMPT_LIMIT);
    }

    /** Constructor completo por defecto */
    public MemoAnimaliaEngine() {
        this(DEFAULT_ROWS, DEFAULT_COLS, defaultImageIds(), DEFAULT_ATTEMPT_LIMIT);
    }

    /** Asigna un listener que recibirá las notificaciones del juego */
    public void setListener(GameListener l) { this.listener = l; }

    /**
     * Lista de animales predefinidos.
     */
    private static List<String> defaultImageIds() {
        return Arrays.asList("Perro","Gato","Elefante","Tigre","Mono","Pajaro","Vaca","Caballo","Conejo");
    }

    /**
     * =============================================================
     * Inicialización del tablero
     * -------------------------------------------------------------
     * Crea las cartas en forma de parejas, mezcla sus posiciones 
     * y reinicia contadores del juego.
     * =============================================================
     */
    private void initBoard(List<String> imageIds) {

        board.clear();
        int pairs = size / 2;

        List<String> ids = new ArrayList<>(imageIds != null ? imageIds : defaultImageIds());
        while (ids.size() < pairs) ids.addAll(ids);

        for (int i = 0; i < pairs; ++i) {
            String id = ids.get(i);
            board.add(new Card(id));
            board.add(new Card(id));
        }

        if (size % 2 == 1) {
            String extraId = (ids.size() > pairs) ? ids.get(pairs) : "Extra";
            board.add(new Card(extraId));
        }

        Collections.shuffle(board, ThreadLocalRandom.current());

        attempts = 0;
        matchesFound = 0;
        firstSelectedIndex = null;

        if (listener != null) listener.onGameReset();
    }

    // -------------------------------------------------------------
    // GETTERS DEL MODELO
    // -------------------------------------------------------------
    public int getRows() { return rows; }
    public int getCols() { return cols; }
    public int getSize() { return size; }
    public int getAttempts() { return attempts; }
    public int getMatchesFound() { return matchesFound; }
    public int getAttemptLimit() { return attemptLimit; }
    public Card getCard(int index) { return board.get(index); }

    public List<Card> getBoardSnapshot() {
        return Collections.unmodifiableList(board);
    }

    /**
     * =============================================================
     * flipCard()
     * -------------------------------------------------------------
     * Controla el proceso de voltear cartas:
     *  - Primer click → revela carta
     *  - Segundo click → compara ambas cartas
     *  - Si coinciden → suma acierto
     *  - Si no coinciden → se ocultarán después
     * =============================================================
     */
    public FlipResult flipCard(int index) {

        if (index < 0 || index >= size) return FlipResult.INVALID_INDEX;
        Card c = board.get(index);
        if (c.isMatched() || c.isRevealed()) return FlipResult.ALREADY_REVEALED;

        c.setRevealed(true);
        if (listener != null) listener.onCardRevealed(index, c);

        if (firstSelectedIndex == null) {
            firstSelectedIndex = index;
            return FlipResult.OK_REVEALED;
        } 
        else {
            int secondIndex = index;
            Card firstCard = board.get(firstSelectedIndex);
            Card secondCard = board.get(secondIndex);

            attempts++;
            if (listener != null) listener.onAttemptChanged(attempts);

            if (firstCard.getId().equals(secondCard.getId())) {

                firstCard.setMatched(true);
                secondCard.setMatched(true);
                matchesFound++;

                if (listener != null) listener.onCardsMatched(firstSelectedIndex, secondIndex);
                if (listener != null) listener.onMatchCountChanged(matchesFound);

                firstSelectedIndex = null;

                if (isGameWon() && listener != null) 
                    listener.onGameWon(attempts);

                if (attempts >= attemptLimit && listener != null) {
                    listener.onGameOverAttemptLimit(attemptLimit);
                    resetForNewGame();
                }

                return FlipResult.MATCH;
            } 
            else {
                if (listener != null) listener.onCardsHidden(firstSelectedIndex, secondIndex);
                firstSelectedIndex = null;

                if (attempts >= attemptLimit && listener != null) {
                    listener.onGameOverAttemptLimit(attemptLimit);
                    resetForNewGame();
                }

                return FlipResult.NO_MATCH;
            }
        }
    }

    /** Oculta dos cartas (usada si no hicieron pareja) */
    public void hideCards(int index1, int index2) {
        if (index1 >= 0 && index1 < size) board.get(index1).setRevealed(false);
        if (index2 >= 0 && index2 < size) board.get(index2).setRevealed(false);
    }

    /** Indica si todas las parejas fueron encontradas */
    public boolean isGameWon() {
        return matchesFound >= (size / 2);
    }

    /** Reinicia toda la partida desde cero */
    public void resetForNewGame() {
        initBoard(null);
    }

    /**
     * =============================================================
     * Método main
     * -------------------------------------------------------------
     * Permite ejecutar el juego temporalmente por consola 
     * únicamente para probar la lógica del modelo.
     * =============================================================
     */
    public static void main(String[] args) throws InterruptedException {

        MemoAnimaliaEngine engine = new MemoAnimaliaEngine();
        engine.setListener(new GameListener() {

            @Override public void onCardRevealed(int index, Card card) {
                System.out.println("Revelada pos=" + index + " -> " + card.getId());
            }
            @Override public void onCardsHidden(int index1, int index2) {
                System.out.println("No son pareja: pos=" + index1 + "," + index2);
            }
            @Override public void onCardsMatched(int index1, int index2) {
                System.out.println("¡Pareja! pos=" + index1 + "," + index2);
            }
            @Override public void onAttemptChanged(int attempts) {
                System.out.println("Intentos: " + attempts);
            }
            @Override public void onMatchCountChanged(int matchesFound) {
                System.out.println("Aciertos: " + matchesFound);
            }
            @Override public void onGameWon(int attempts) {
                System.out.println("GANASTE en " + attempts + " intentos.");
            }
            @Override public void onGameOverAttemptLimit(int attemptLimit) {
                System.out.println("Superaste el límite de " + attemptLimit + ". Reiniciando juego...");
            }
            @Override public void onGameReset() {
                System.out.println("Juego reiniciado y mezclado.");
            }
        });

        Scanner sc = new Scanner(System.in);

        System.out.println("Mostrando cartas 2 segundos...");
        for (int i = 0; i < engine.getSize(); ++i) {
            System.out.printf("%2d:%-8s  ", i, engine.getCard(i).getId());
            if ((i+1)%engine.getCols()==0) System.out.println();
        }
        Thread.sleep(2000);

        System.out.println("Introduce dos índices por turno:");

        while (true) {

            for (int i = 0; i < engine.getSize(); ++i) {
                Card c = engine.getCard(i);
                String out = c.isMatched() ? c.getId() : (c.isRevealed() ? c.getId() : "[ ]");
                System.out.printf("%2d:%-10s", i, out);
                if ((i+1)%engine.getCols()==0) System.out.println();
            }

            System.out.print("Primera carta: ");
            int a = sc.nextInt();
            engine.flipCard(a);

            System.out.print("Segunda carta: ");
            int b = sc.nextInt();
            FlipResult res = engine.flipCard(b);

            if (res == FlipResult.NO_MATCH) {
                Thread.sleep(1000);
                engine.hideCards(a, b);
            }

            if (engine.isGameWon()) {
                System.out.println("FELICIDADES — Ganaste con " + engine.getAttempts() + " intentos.");
                break;
            }
        }

        sc.close();
    }
}
