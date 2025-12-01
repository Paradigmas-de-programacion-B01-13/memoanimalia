
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * Gestor de recursos de imágenes para el juego MemoAnimalia Se encarga de
 * cargar y gestionar todas las imágenes del juego
 */
public class ImageResourceManager {

    private static final String ASSETS_PATH = "assets" + File.separator;
    private static final String BACK_IMAGE_NAME = "card_back.jpeg";

    private final Map<String, ImageIcon> animalImageMap;
    private final ImageIcon backImage;
    private final int cardSize;

    /**
     * Constructor del gestor de recursos
     *
     * @param animalIds Lista de IDs de animales a cargar
     * @param cardSize Tamaño al que redimensionar las imágenes
     */
    public ImageResourceManager(List<String> animalIds, int cardSize) {
        this.cardSize = cardSize;
        this.animalImageMap = new HashMap<>();

        // Cargar imagen de reverso
        this.backImage = loadBackImage();

        // Cargar imágenes de animales
        loadAnimalImages(animalIds);
    }

    /**
     * Carga la imagen de reverso de las cartas
     *
     * @return ImageIcon con la imagen de reverso redimensionada
     */
    private ImageIcon loadBackImage() {
        try {
            String backImagePath = ASSETS_PATH + BACK_IMAGE_NAME;
            BufferedImage backImg = ImageIO.read(new File(backImagePath));
            return createScaledIcon(backImg);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo cargar la imagen de reverso: " + e.getMessage(), e);
        }
    }

    /**
     * Carga las imágenes de todos los animales
     *
     * @param animalIds Lista de IDs de animales
     */
    private void loadAnimalImages(List<String> animalIds) {
        for (String animalId : animalIds) {
            try {
                String imagePath = ASSETS_PATH + animalId + ".png";
                File imageFile = new File(imagePath);

                if (imageFile.exists()) {
                    BufferedImage img = ImageIO.read(imageFile);
                    animalImageMap.put(animalId.toLowerCase(), createScaledIcon(img));
                } else {
                    System.err.println("No se encontró la imagen: " + imagePath);
                    // Crear imagen placeholder
                    animalImageMap.put(animalId.toLowerCase(), createPlaceholderIcon(animalId));
                }
            } catch (IOException e) {
                System.err.println("Error cargando imagen para " + animalId + ": " + e.getMessage());
                animalImageMap.put(animalId.toLowerCase(), createPlaceholderIcon(animalId));
            }
        }
    }

    /**
     * Crea un ImageIcon redimensionado a cardSize
     *
     * @param image Imagen original
     * @return ImageIcon redimensionado
     */
    private ImageIcon createScaledIcon(BufferedImage image) {
        if (image.getWidth() != cardSize || image.getHeight() != cardSize) {
            Image scaledImg = image.getScaledInstance(cardSize, cardSize, Image.SCALE_SMOOTH);
            return new ImageIcon(scaledImg);
        } else {
            return new ImageIcon(image);
        }
    }

    /**
     * Crea un ImageIcon placeholder cuando no se encuentra una imagen
     *
     * @param animalId ID del animal
     * @return ImageIcon placeholder
     */
    private ImageIcon createPlaceholderIcon(String animalId) {
        BufferedImage placeholder = new BufferedImage(cardSize, cardSize, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = placeholder.createGraphics();
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fillRect(0, 0, cardSize, cardSize);
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        String displayName = animalId.length() > 8 ? animalId.substring(0, 8) : animalId;
        g2d.drawString(displayName, 10, cardSize / 2);
        g2d.dispose();
        return new ImageIcon(placeholder);
    }

    /**
     * Obtiene la imagen de un animal por su ID
     *
     * @param animalId ID del animal (case-insensitive)
     * @return ImageIcon del animal, o null si no existe
     */
    public ImageIcon getAnimalImage(String animalId) {
        return animalImageMap.get(animalId.toLowerCase());
    }

    /**
     * Obtiene la imagen de reverso de las cartas
     *
     * @return ImageIcon de reverso
     */
    public ImageIcon getBackImage() {
        return backImage;
    }
}
