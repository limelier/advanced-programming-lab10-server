import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try (GameServer server = new GameServer()) {
            server.run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
