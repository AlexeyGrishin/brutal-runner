import java.awt.*;

import brutal.BrutalRenderer;
import model.*;

public final class LocalTestRendererListener {
    public void beforeDrawScene(Graphics graphics, World world, Game game, double scale) {
        BrutalRenderer.beforeDrawScene(graphics, world, game, scale);
    }

    public void afterDrawScene(Graphics graphics, World world, Game game, double scale) {
        BrutalRenderer.afterDrawScene(graphics, world, game, scale);
    }

    public static void main(String args[]) {
        //
    }
}
