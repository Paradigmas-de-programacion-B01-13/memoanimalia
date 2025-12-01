//Cesar Augusto Perez Sanchez

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

/**
 * Controlador principal de la interfaz gráfica del juego MemoAnimalia
 * Implementa GameListener para recibir eventos del motor del juego
 */
public class MemoAnimaliaController extends JPanel implements MemoAnimaliaEngine.GameListener {

    private MemoAnimaliaEngine engine;
    private List<JButton> cardButtons;
    private final ImageResourceManager imageManager;

    private JLabel attemptsLabel;
    private JLabel matchesLabel;
    private JLabel statusLabel;

    private final Timer hideTimer;
    private Timer initialRevealTimer;
    private Timer countdownTimer;
    private int firstCardIndex = -1;
    private int secondCardIndex = -1;
    private int remainingSeconds;

    private static final int CARD_SIZE = 120;
    private static final int GRID_PADDING = 10;
    private static final int HIDE_DELAY = 500;
    private static final int INITIAL_REVEAL_TIME_MS = 3000;
    private static final int COUNTDOWN_INTERVAL_MS = 1000; // 1 segundo

    // Constantes para mensajes del juego
    private static final String GAME_WON_MESSAGE = "¡FELICIDADES! 🎉\n\n"
            + "Has ganado el juego encontrando todas las parejas.\n\n"
            + "Intentos utilizados: %d / %d\n\n"
            + "¡Excelente trabajo!";

    private static final String GAME_LOST_MESSAGE = "¡Oh no! 😔\n\n"
            + "Has alcanzado el límite de %d intentos.\n\n"
            + "No te desanimes, ¡puedes intentarlo de nuevo!\n\n"
            + "Consejos:\n"
            + "• Trata de recordar las posiciones de las cartas.\n"
            + "• Sé estratégico con tus selecciones.\n"
            + "• ¡La práctica hace al maestro!\n\n"
            + "El juego se reiniciará automáticamente.";

    /**
     * Resetea los índices de las cartas seleccionadas.
     */
    private void resetIndexes() {
        firstCardIndex = -1;
        secondCardIndex = -1;
    }

    /**
     * Constructor del controlador del juego MemoAnimalia. Se encarga de
     * inicializar el motor del juego, configurar la interfaz gráfica y mostrar
     * el mensaje de bienvenida.
     *
     * @param animalIds Lista de IDs de animales
     * @throws IllegalArgumentException si el tamaño de la lista de animales no
     * es válido
     */
    public MemoAnimaliaController(List<String> animalIds) {
        engine = new MemoAnimaliaEngine(3, 3, animalIds, 12);
        engine.setListener(this);
        imageManager = new ImageResourceManager(animalIds, CARD_SIZE);

        setupUI();

        hideTimer = new Timer(HIDE_DELAY, e -> {
            if (firstCardIndex >= 0 && secondCardIndex >= 0) {
                engine.hideCards(firstCardIndex, secondCardIndex);

                updateCardDisplay(firstCardIndex);
                updateCardDisplay(secondCardIndex);
                resetIndexes();
            }
        });
        hideTimer.setRepeats(false);

        showWelcomeMessage();

        // Mostrar todas las cartas por 3 segundos al inicio
        showAllCardsTemporarily();
    }

    /**
     * Configura la interfaz gráfica del juego. Se encarga de crear los paneles
     * y los botones de cartas con los que el usuario interactuará para jugar
     */
    private void setupUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel superior con información del juego
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        infoPanel.setBackground(new Color(240, 240, 240));

        attemptsLabel = new JLabel("Intentos: 0 / 12");
        attemptsLabel.setFont(new Font("Arial", Font.BOLD, 14));
        infoPanel.add(attemptsLabel);

        matchesLabel = new JLabel("Parejas: 0");
        matchesLabel.setFont(new Font("Arial", Font.BOLD, 14));
        infoPanel.add(matchesLabel);

        statusLabel = new JLabel("¡Encuentra las parejas!");
        statusLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        statusLabel.setForeground(new Color(0, 100, 0));
        infoPanel.add(statusLabel);

        add(infoPanel, BorderLayout.NORTH);

        // Panel central con el tablero de cartas
        JPanel boardPanel = new JPanel(new GridLayout(3, 3, GRID_PADDING, GRID_PADDING));
        boardPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        cardButtons = new ArrayList<>();
        int boardSize = engine.getSize();

        for (int i = 0; i < boardSize; i++) {
            JButton cardButton = createCardButton(i);
            cardButtons.add(cardButton);
            boardPanel.add(cardButton);
        }

        add(boardPanel, BorderLayout.CENTER);

        // Panel inferior con botón de reinicio
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton resetButton = new JButton("Nuevo Juego");
        resetButton.setFont(new Font("Arial", Font.BOLD, 12));
        resetButton.addActionListener(e -> {
            engine.resetForNewGame();
            updateAllCards();
            resetIndexes();
            hideTimer.stop();
        });
        controlPanel.add(resetButton);

        add(controlPanel, BorderLayout.SOUTH);
    }

    /**
     * Crea un botón de carta para el juego. Se encarga de crear el botón y
     * configurar su tamaño, bordes y listener, además de cargar la imagen de
     * reverso.
     *
     * @param index Índice de la carta
     * @return Botón de carta
     */
    private JButton createCardButton(int index) {
        JButton button = new JButton();
        button.setPreferredSize(new Dimension(CARD_SIZE, CARD_SIZE));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createRaisedBevelBorder());

        // Mostrar reverso inicialmente para el boton de la carta
        button.setIcon(imageManager.getBackImage());

        button.addActionListener(new CardButtonListener(index));
        return button;
    }

    /**
     * Actualiza la visualización de una carta en la interfaz gráfica. Se
     * encarga de mostrar la imagen del animal o el reverso de la carta según el
     * estado de la carta.
     *
     * @param index Índice de la carta seleccionada
     */
    private void updateCardDisplay(int index) {
        if (index < 0 || index >= cardButtons.size()) {
            return;
        }

        JButton button = cardButtons.get(index);
        MemoAnimaliaEngine.Card card = engine.getCard(index);

        if (card.isMatched()) {
            // Carta emparejada - mostrar imagen del animal
            String animalId = card.getId().toLowerCase();
            ImageIcon icon = imageManager.getAnimalImage(animalId);
            if (icon != null) {
                button.setIcon(icon);
                button.setText(null);
            } else {
                button.setIcon(null);
                button.setText(card.getId());
            }
            button.setEnabled(false);
            button.setBorder(BorderFactory.createLoweredBevelBorder());
        } else if (card.isRevealed()) {
            // Carta revelada - mostrar imagen del animal
            String animalId = card.getId().toLowerCase();
            ImageIcon icon = imageManager.getAnimalImage(animalId);
            if (icon != null) {
                button.setIcon(icon);
                button.setText(null);
            } else {
                button.setIcon(null);
                button.setText(card.getId());
            }
        } else {
            // Carta oculta - mostrar reverso
            button.setIcon(imageManager.getBackImage());
            button.setText(null);
        }
    }

    /**
     * Actualiza la visualización de todas las cartas en la interfaz gráfica.
     */
    private void updateAllCards() {
        for (int i = 0; i < cardButtons.size(); i++) {
            updateCardDisplay(i);
            cardButtons.get(i).setEnabled(true);
        }
    }

    /**
     * Muestra un mensaje de bienvenida con las reglas del juego
     */
    private void showWelcomeMessage() {
        String message = "¡Bienvenido a MemoAnimalia!\n\n"
                + "REGLAS DEL JUEGO:\n\n"
                + "• Encuentra todas las parejas de animales haciendo clic en las cartas.\n"
                + "• Tienes un máximo de " + engine.getAttemptLimit() + " intentos para encontrar todas las parejas.\n"
                + "• Las cartas se mostrarán por 3 segundos al inicio del juego.\n"
                + "• Si dos cartas no coinciden, se ocultarán automáticamente.\n"
                + "• Si dos cartas coinciden, permanecerán visibles.\n\n"
                + "¡Buena suerte!";

        JOptionPane.showMessageDialog(
                this,
                message,
                "MemoAnimalia - Instrucciones",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    /**
     * Muestra todas las cartas temporalmente por 3 segundos al inicio del juego
     */
    private void showAllCardsTemporarily() {
        // Revelar todas las cartas visualmente (sin modificar el estado del motor)
        for (int i = 0; i < cardButtons.size(); i++) {
            MemoAnimaliaEngine.Card card = engine.getCard(i);
            JButton button = cardButtons.get(i);

            // Mostrar la imagen del animal temporalmente
            String animalId = card.getId().toLowerCase();
            ImageIcon icon = imageManager.getAnimalImage(animalId);
            if (icon != null) {
                button.setIcon(icon);
                button.setText(null);
            } else {
                button.setIcon(null);
                button.setText(card.getId());
            }
            button.setEnabled(false); // Deshabilitar clicks durante la vista previa
        }

        // Inicializar contador de tiempo
        remainingSeconds = INITIAL_REVEAL_TIME_MS / COUNTDOWN_INTERVAL_MS; // 3 segundos
        updateCountdownDisplay();

        // Crear timer para actualizar el contador cada segundo
        countdownTimer = new Timer(COUNTDOWN_INTERVAL_MS, e -> {
            remainingSeconds--;
            updateCountdownDisplay();

            if (remainingSeconds <= 0) {
                countdownTimer.stop();
            }
        });
        countdownTimer.start();

        // Crear timer para ocultar todas las cartas después de 3 segundos
        initialRevealTimer = new Timer(INITIAL_REVEAL_TIME_MS, e -> {
            // Detener el contador
            if (countdownTimer != null) {
                countdownTimer.stop();
                countdownTimer = null;
            }

            // Ocultar todas las cartas
            for (int i = 0; i < cardButtons.size(); i++) {
                updateCardDisplay(i);
                cardButtons.get(i).setEnabled(true);
            }

            // Restaurar el mensaje de estado normal
            statusLabel.setText("¡Encuentra las parejas!");
            statusLabel.setForeground(new Color(0, 100, 0));

            initialRevealTimer = null;
        });
        initialRevealTimer.setRepeats(false);
        initialRevealTimer.start();
    }

    /**
     * Actualiza la visualización del contador de tiempo restante
     */
    private void updateCountdownDisplay() {
        if (remainingSeconds > 0) {
            statusLabel.setText("Tiempo restante: " + remainingSeconds + " segundo" + (remainingSeconds != 1 ? "s" : ""));
            statusLabel.setForeground(new Color(255, 140, 0)); // Color naranja
        } else {
            statusLabel.setText("¡Comienza el juego!");
            statusLabel.setForeground(new Color(0, 150, 0));
        }
    }

    /**
     * Implementación de GameListener. Se encarga de actualizar la visualización
     * de la carta seleccionada.
     *
     * @param index Índice de la carta seleccionada
     * @param card Carta seleccionada para actualizar su visualización
     */
    @Override
    public void onCardRevealed(int index, MemoAnimaliaEngine.Card card) {
        SwingUtilities.invokeLater(() -> {
            updateCardDisplay(index);
        });
    }

    /**
     * Implementación de GameListener. Se encarga de ocultar las cartas
     * seleccionadas cuando no son parejas.
     *
     * @param index1 Índice de la primera carta seleccionada
     * @param index2 Índice de la segunda carta seleccionada
     */
    @Override
    public void onCardsHidden(int index1, int index2) {
        SwingUtilities.invokeLater(() -> {
            firstCardIndex = index1;
            secondCardIndex = index2;
            hideTimer.start();
        });
    }

    /**
     * Implementación de GameListener. Se encarga de actualizar la visualización
     * de las cartas seleccionadas cuando no son parejas.
     *
     * @param index1 Índice de la primera carta seleccionada
     * @param index2 Índice de la segunda carta seleccionada
     */
    @Override
    public void onCardsMatched(int index1, int index2) {
        SwingUtilities.invokeLater(() -> {
            updateCardDisplay(index1);
            updateCardDisplay(index2);
            statusLabel.setText("¡Pareja encontrada!");
            statusLabel.setForeground(new Color(0, 150, 0));
        });
    }

    /**
     * Implementación de GameListener. Se encarga de actualizar el contador de
     * intentos.
     *
     * @param attempts Intentos realizados
     */
    @Override
    public void onAttemptChanged(int attempts) {
        SwingUtilities.invokeLater(() -> {
            attemptsLabel.setText("Intentos: " + attempts + " / " + engine.getAttemptLimit());
            if (attempts > engine.getAttemptLimit()) {
                attemptsLabel.setForeground(Color.RED);
            } else {
                attemptsLabel.setForeground(Color.BLACK);
            }
        });
    }

    /**
     * Implementación de GameListener. Se encarga de actualizar el contador de
     * parejas encontradas.
     *
     * @param matchesFound Parejas encontradas
     */
    @Override
    public void onMatchCountChanged(int matchesFound) {
        SwingUtilities.invokeLater(() -> {
            matchesLabel.setText("Parejas: " + matchesFound);
        });
    }

    /**
     * Implementación de GameListener. Se encarga de mostrar el mensaje de
     * victoria cuando el jugador gana el juego.
     *
     * @param attempts Intentos realizados para ganar el juego
     */
    @Override
    public void onGameWon(int attempts) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("¡Felicidades! Ganaste en " + attempts + " intentos.");
            statusLabel.setForeground(new Color(0, 150, 0));

            String message = String.format(GAME_WON_MESSAGE, attempts, engine.getAttemptLimit());

            JOptionPane.showMessageDialog(
                    this,
                    message,
                    "¡Victoria!",
                    JOptionPane.INFORMATION_MESSAGE
            );
        });
    }

    /**
     * Implementación de GameListener. Se encarga de mostrar el mensaje de
     * derrota cuando el jugador alcanza el límite de intentos.
     *
     * @param attemptLimit Límite de intentos
     */
    @Override
    public void onGameOverAttemptLimit(int attemptLimit) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("Límite de intentos alcanzado. Reiniciando...");
            statusLabel.setForeground(Color.RED);

            String message = String.format(GAME_LOST_MESSAGE, attemptLimit);

            JOptionPane.showMessageDialog(
                    this,
                    message,
                    "Fin del Juego",
                    JOptionPane.WARNING_MESSAGE
            );
        });
    }

    /**
     * Implementación de GameListener. Se encarga de reiniciar el juego cuando
     * el jugador lo desea.
     */
    @Override
    public void onGameReset() {
        SwingUtilities.invokeLater(() -> {
            // Detener cualquier timer activo
            if (initialRevealTimer != null) {
                initialRevealTimer.stop();
                initialRevealTimer = null;
            }
            if (countdownTimer != null) {
                countdownTimer.stop();
                countdownTimer = null;
            }
            hideTimer.stop();

            updateAllCards();
            attemptsLabel.setText("Intentos: 0 / " + engine.getAttemptLimit());
            attemptsLabel.setForeground(Color.BLACK);
            matchesLabel.setText("Parejas: 0");
            statusLabel.setText("¡Encuentra las parejas!");
            statusLabel.setForeground(new Color(0, 100, 0));
            resetIndexes();

            // Mostrar todas las cartas por 3 segundos nuevamente
            showAllCardsTemporarily();
        });
    }

    /**
     * Listener para los botones de cartas. Se encarga de manejar el evento de
     * clic en un botón de carta.
     *
     * @param cardIndex Índice de la carta seleccionada
     */
    private class CardButtonListener implements ActionListener {

        private final int cardIndex;

        public CardButtonListener(int cardIndex) {
            this.cardIndex = cardIndex;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            MemoAnimaliaEngine.Card card = engine.getCard(cardIndex);

            // No permitir click en cartas ya emparejadas o reveladas
            if (card.isMatched() || card.isRevealed()) {
                return;
            }

            // Intentar voltear la carta
            engine.flipCard(cardIndex);
        }
    }
}
