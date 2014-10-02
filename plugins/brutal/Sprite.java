package brutal;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

class Sprite {
    BufferedImage image;
    BufferedImage mirroredX;
    int wSize;
    int hSize;
    int pad;

    Sprite(String image, int wSize, int hSize, int pad) throws IOException {
        this.image = BrutalRenderer.load(image);
        this.mirroredX = new BufferedImage(this.image.getWidth(null), this.image.getHeight(null), BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g2 = ((Graphics2D)mirroredX.getGraphics());
        AffineTransform af = AffineTransform.getScaleInstance(-1, 1);
        af.translate(-this.image.getWidth(null), 0);
        g2.drawImage(this.image, af, null);
        g2.dispose();
        this.wSize = wSize;
        this.hSize = hSize;
        this.pad = pad;
    }

    void draw(Graphics2D g2d, Rectangle rect, int number) {
        BrutalRenderer.renderSprite(g2d, rect, image, wSize, hSize, pad, number);
    }
    void drawMirrored(Graphics2D g2d, Rectangle rect, int number) {
        BrutalRenderer.renderSprite(g2d, rect, mirroredX, wSize, hSize, pad, number);
    }
    void drawM(Graphics2D g2d, Rectangle rect, int number) {
        if (number < 0) {
            draw(g2d, rect, -number);
            return;
        }
        int b8 = number % 8;

        if (b8 < 5) {
            BrutalRenderer.renderSprite(g2d, rect, image, wSize, hSize, pad, number / 8 * 5 + b8);
        }
        else {
            BrutalRenderer.renderSprite(g2d, rect, mirroredX, wSize, hSize, pad, number / 8 * 5 + b8 - 1);
        }
    }

    Paint toTexture(int number) {
        BufferedImage img1 = new BufferedImage(wSize - 2*pad, hSize - 2*pad, BufferedImage.TYPE_4BYTE_ABGR);
        int colsInRow = image.getWidth(null) / wSize;
        int row = number / colsInRow;
        int col = number % colsInRow;
        img1.getGraphics().drawImage(image, 0, 0, wSize - 2*pad, hSize - 2*pad, col*wSize+pad, row*hSize+pad, col*wSize+ wSize-pad, row*hSize + hSize-pad, null);
        return new TexturePaint(img1, new Rectangle(
                0, 0, wSize - 2*pad-1, hSize - 2*pad-1
        ));
    }

    Paint toTexture(int number, int wpad) {
        BufferedImage img1 = new BufferedImage(wSize - 2*wpad, hSize - 2*pad, BufferedImage.TYPE_4BYTE_ABGR);
        int colsInRow = image.getWidth(null) / wSize;
        int row = number / colsInRow;
        int col = number % colsInRow;
        img1.getGraphics().drawImage(image, 0, 0, wSize - 2*wpad, hSize - 2*pad, col*wSize+wpad, row*hSize+pad, col*wSize+ wSize-wpad, row*hSize + hSize-pad, null);
        return new TexturePaint(img1, new Rectangle(
                0, 0, wSize - 2*wpad-1, hSize - 2*pad-1
        ));
    }

}
