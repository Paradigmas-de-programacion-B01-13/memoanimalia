import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MemoAnimaliaEngineTest {

    private MemoAnimaliaEngine engine;

    @BeforeEach
    void setUp() {
        engine = new MemoAnimaliaEngine(); // usa valores por defecto
    }

    @Test
    void testBoardInitialization() {
        List<MemoAnimaliaEngine.Card> board = engine.getBoardSnapshot();

        // tamaño correcto (3x3)
        assertEquals(9, board.size(), "El tablero debe tener 9 cartas");

        // ninguna carta debe estar revelada o emparejada
        for (MemoAnimaliaEngine.Card c : board) {
            assertFalse(c.isRevealed(), "Las cartas deben iniciar ocultas");
            assertFalse(c.isMatched(), "Las cartas deben iniciar sin pareja");
        }
    }
    @Test
    void testFlipValidCard() {
        MemoAnimaliaEngine.FlipResult result = engine.flipCard(0);
        assertEquals(MemoAnimaliaEngine.FlipResult.OK_REVEALED, result);
    }
    @Test
    void testFlipInvalidIndex() {
        assertEquals(
            MemoAnimaliaEngine.FlipResult.INVALID_INDEX, 
            engine.flipCard(-1)
        );

        assertEquals(
            MemoAnimaliaEngine.FlipResult.INVALID_INDEX, 
            engine.flipCard(999)
        );
    }


    @Test
    void testAlreadyRevealed() {
        engine.flipCard(0);
        MemoAnimaliaEngine.FlipResult r = engine.flipCard(0);

        assertEquals(
            MemoAnimaliaEngine.FlipResult.ALREADY_REVEALED, 
            r
        );
    }
    @Test
    void testGameWon() {
        List<String> ids = Arrays.asList("A", "B", "C", "D");
        MemoAnimaliaEngine engine = new MemoAnimaliaEngine(3, 2, ids, 20);

        List<MemoAnimaliaEngine.Card> board = engine.getBoardSnapshot();

        boolean[][] used = new boolean[board.size()][board.size()];

        // buscamos todas las parejas y las volteamos
        for (int i = 0; i < board.size(); i++) {
            for (int j = i + 1; j < board.size(); j++) {
                if (board.get(i).getId().equals(board.get(j).getId())) {
                    engine.flipCard(i);
                    engine.flipCard(j);
                }
            }
        }

        assertTrue(engine.isGameWon(), "El juego debería estar ganado después de emparejar todas las cartas.");
    }


    
}
