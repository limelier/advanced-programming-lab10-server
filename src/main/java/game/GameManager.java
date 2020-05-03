package game;

import util.GameCode;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

public class GameManager {
    private static final GameManager instance = new GameManager();
    private GameManager() {}

    public static GameManager getInstance() {
        return instance;
    }

    Map<String, WeakReference<Game>> map = new HashMap<>();

    public Game createGame() {
        String code = GameCode.generate();
        while (map.containsKey(code) && map.get(code).get() != null) {
            code = GameCode.generate();
        }
        Game game = new Game(code);
        WeakReference<Game> ref = new WeakReference<>(game);
        map.put(code, new WeakReference<Game>(game));
        return game;
    }

    public Game getGame(String code) {
        WeakReference<Game> ref = map.get(code);
        return ref != null ? ref.get() : null;
    }
}
