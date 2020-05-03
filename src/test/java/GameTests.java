import game.Game;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GameTests {
    @Test
    public void testPlacement() {
        Game game = new Game("_");
        game.placeByOne(1, 1);
        game.placeByOne(19, 19);
        game.placeByOne(-1, -1); // ignored
        game.placeByOne(20, 20); // ignored

        assertEquals(game.getState(),
                "X__________________\n" +
                "___________________\n" +
                "___________________\n" +
                "___________________\n" +
                "___________________\n" +
                "___________________\n" +
                "___________________\n" +
                "___________________\n" +
                "___________________\n" +
                "___________________\n" +
                "___________________\n" +
                "___________________\n" +
                "___________________\n" +
                "___________________\n" +
                "___________________\n" +
                "___________________\n" +
                "___________________\n" +
                "___________________\n" +
                "__________________O");
    }
}
