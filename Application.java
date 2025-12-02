
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;

/**
 * Aplicación principal del juego MemoAnimalia Interfaz gráfica en Java Swing
 */
public class Application extends JFrame {

    public Application() {
        setTitle("MemoAnimalia - Juego de Memoria");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // Lista de animales basada en las imágenes disponibles
        List<String> animalIds = Arrays.asList("abeja", "gato", "elefante", "tigre", "mono", "pajaro", "vaca", "caballo", "conejo");

        // Crear el panel principal del juego
        MemoAnimaliaController gamePanel = new MemoAnimaliaController(animalIds);

        // Configurar el layout
        setLayout(new BorderLayout());
        add(gamePanel, BorderLayout.CENTER);

        // Ajustar tamaño y centrar
        pack();
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        // Configurar Look and Feel del sistema
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Ejecutar la aplicación
        SwingUtilities.invokeLater(() -> {
            new Application().setVisible(true);
        });
    }
}
