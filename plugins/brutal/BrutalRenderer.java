package brutal;

import model.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class BrutalRenderer {

    static Sprite cacodemon;
    static Sprite elemental;
    static Sprite faces;
    static Sprite textures;
    static Sprite dude;

    static BufferedImage load(String path) throws IOException {
        BufferedImage orig = ImageIO.read(new File(path));
        BufferedImage processed = new BufferedImage(orig.getWidth(null), orig.getHeight(null), BufferedImage.TYPE_4BYTE_ABGR);
        Color rtColor = new Color(orig.getRGB(0, 0));
        Color trColor = new Color(rtColor.getRed(), rtColor.getGreen(), rtColor.getBlue(), 0);
        for (int x = 0; x < orig.getWidth(); x++) {
            for (int y = 0; y < orig.getHeight(); y++) {
                int rgb = orig.getRGB(x, y);
                processed.setRGB(x, y, rgb == rtColor.getRGB() ? trColor.getRGB() : rgb);
            }
        }
        return processed;
    }

    static {
        try {
            cacodemon = new Sprite("plugins/brutal/cacodemon.png", 76, 78, 2);
            elemental = new Sprite("plugins/brutal/elemental.png", 103, 87, 10);
            faces = new Sprite("plugins/brutal/faces.png", 32, 32, 0);
            textures = new Sprite("plugins/brutal/textures.jpg", 66, 66, 1);
            dude = new Sprite("plugins/brutal/dude.png", 40, 69, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void renderSprite(Graphics2D context, Rectangle rect, Image image, int wsize, int hsize, int pad, int number) {
        int colsInRow = image.getWidth(null) / wsize;
        int row = number / colsInRow;
        int col = number % colsInRow;
        context.drawImage(image, rect.x, rect.y, (int)rect.getMaxX(), (int)rect.getMaxY(), col*wsize + pad, row*hsize + pad, col*wsize+wsize-pad-1, row*hsize + hsize-pad-1, null, null);
    }


    static Rectangle toRect(Unit unit) {
        return new Rectangle((int)unit.getX() - (int)unit.getRadius() - 1, (int)unit.getY() - (int)unit.getRadius() - 1, (int)unit.getRadius()*2+2, (int)unit.getRadius()*2+2);
    }
    static Rectangle toRect(Puck unit) {
        return new Rectangle((int)unit.getX() - 16, (int)unit.getY() - 16, 32, 32);
    }

    static int direction(Hockeyist hockeyist) {
        //angle pi/2 -> 0
        //angle pi -> 2 //2pi
        //angle -pi/2 -> 4
        //angle 0 -> 6 //pi
        //(angle + pi)*4 / pi
        return (int)Math.round(6 - -hockeyist.getAngle()*4 / Math.PI) % 8;
    }

    static int faceDirection(Puck puck) {
        if (puck.getOwnerPlayerId() != -1) {
            return 7;
        }
        if (Math.abs(puck.getSpeedX()) + Math.abs(puck.getSpeedY()) < 0.5) {
            return 3;
        }
        if (puck.getSpeedX() > 0) {
            if (puck.getSpeedY() > 0) {
                return 21;
            }
            else {
                return 5;
            }
        }
        else {
            if (puck.getSpeedY() > 0) {
                return 20;
            }
            else {
                return 4;
            }
        }
    }

    static int statusAndDir(Hockeyist hockeyist, int tick, Game game,  int swingStart, int swingCt, int kdStart, int kdCount) {
        int nr = direction(hockeyist);
        if (hockeyist.getState() == HockeyistState.SWINGING) {
            nr = nr + (swingStart + (swingCt * hockeyist.getSwingTicks() / game.getMaxEffectiveSwingTicks()))*8;
        }
        else if (hockeyist.getState() == HockeyistState.KNOCKED_DOWN) {
            nr = -(kdStart + (kdCount - Math.abs(hockeyist.getRemainingKnockdownTicks() - 20)*kdCount/20));
        }
        else if ((hockeyist.getLastAction() == ActionType.STRIKE) && tick - hockeyist.getLastActionTick() < 20) {
            nr = nr + 8*(swingStart + swingCt-1);
        }
        return nr;
    }

    static Image bg = null;
    static void drawBg1(Graphics2D ctx, Game game) {
        if (bg == null) {
            bg = new BufferedImage(1200, 800, BufferedImage.TYPE_4BYTE_ABGR);
            Graphics2D g2d = (Graphics2D) bg.getGraphics();
            g2d.setPaint(textures.toTexture(21*12+9, 18));
            g2d.fillRect(0,0,1200,800);
            g2d.setPaint(textures.toTexture(10*12+4));//91/*+38*/));
            g2d.fillRect((int) game.getRinkLeft() , (int) game.getRinkTop() , (int) game.getRinkRight() - (int) game.getRinkLeft() , (int) game.getRinkBottom() - (int) game.getRinkTop() +1 );
            textures.draw(g2d, new Rectangle(600-30, 30, 66, 66), 93);

            //g2d.setPaint(textures.toTexture(26*12+6, 18));
            g2d.fillRect(0, (int)game.getGoalNetTop(), (int)game.getRinkLeft(), (int)game.getGoalNetHeight());
            g2d.fillRect((int)game.getRinkRight(), (int)game.getGoalNetTop(), (int)game.getRinkLeft(), (int) game.getGoalNetHeight());
            dude.draw(g2d, new Rectangle(20, (int) game.getGoalNetTop() + (int) game.getGoalNetHeight() - dude.image.getHeight(), dude.wSize, dude.image.getHeight()), 0);
            dude.draw(g2d, new Rectangle(20, (int) game.getGoalNetTop(), dude.wSize, dude.image.getHeight()), 1);
            dude.drawMirrored(g2d, new Rectangle(1200 - 20 - dude.wSize, (int) game.getGoalNetTop() + (int) game.getGoalNetHeight() - dude.image.getHeight(), dude.wSize, dude.image.getHeight()), 1);
            dude.drawMirrored(g2d, new Rectangle(1200-20-dude.wSize, (int) game.getGoalNetTop(), dude.wSize, dude.image.getHeight()), 0);
            g2d.dispose();
        }
        ctx.setBackground(Color.white);
        ctx.clearRect(0, 0, 1200, 800);
        ctx.drawImage(bg, 0, 0, null);
    }

    static void drawBg2(Graphics2D ctx, World world, Game game) {
        //ctx.setBackground(Color.white);
        //ctx.clearRect(0, (int) game.getRinkTop() - 10, 1200, (int) game.getRinkBottom() - (int) game.getRinkTop() + 15);
        ctx.drawImage(bg, 0, (int) game.getRinkTop() - 10, 1200, (int) game.getRinkBottom() + 5,
                0, (int) game.getRinkTop() - 10, 1200, (int) game.getRinkBottom() + 5, null);
        if (bloodImg != null) ctx.drawImage(bloodImg, 0, 0, null);
    }

    static boolean wasGoal;

    public static void beforeDrawScene(Graphics graphics, World world, Game game, double scale) {
        Graphics2D g2d = (Graphics2D) graphics.create();
        drawBg1(g2d, game);
        g2d.dispose();
    }


    public static void afterDrawScene(Graphics graphics, World world, Game game, double scale) {
        boolean justGoal = world.getPlayers()[0].isJustMissedGoal() || world.getPlayers()[0].isJustScoredGoal();
        if (!justGoal && wasGoal) {
            bloodImg = null;
            oldPoint = null;
        }
        wasGoal = justGoal;
        Graphics2D g2d = (Graphics2D) graphics.create();
        drawBg2(g2d, world, game);
        for (Hockeyist hoc: world.getHockeyists()) {
            if (hoc.getPlayerId() == 1) {
                cacodemon.drawM(g2d, toRect(hoc), statusAndDir(hoc, world.getTick(), game, 1, 3, 30, 5));
            }
            else {
                elemental.drawM(g2d, toRect(hoc), statusAndDir(hoc, world.getTick(), game, 3, 3, 35, 3));
            }
        }
        faces.draw(g2d, toRect(world.getPuck()), faceDirection(world.getPuck()));

        if (world.getPuck().getOwnerPlayerId() == -1) {
            blood(new Point((int) world.getPuck().getX(), (int) world.getPuck().getY()), world);
        }
        else {
            oldPoint = null;
        }
        g2d.dispose();
    }


    static Color blood = new Color(138,7,7);

    static Image bloodImg = null;
    static Point oldPoint = null;

    static void blood(Point point, World world) {
        if (bloodImg == null) {
            bloodImg = new BufferedImage(1200, 800, BufferedImage.TYPE_4BYTE_ABGR);
            Graphics2D g2d = (Graphics2D) bloodImg.getGraphics();
            g2d.setBackground(new Color(0, 0, 0, 0));
            g2d.clearRect(0, 0, 1200, 800);
            g2d.dispose();
        }
        if (oldPoint != null) {
            Graphics2D g2d = (Graphics2D) bloodImg.getGraphics();
            for (int a = 0; a < Math.random()*5 + 17; a++) {
                int dx = (int)(Math.random()*10 - 5);
                int dy = (int)(Math.random()*10 - 5);
                float d1 = (float)(Math.random()*10);
                Color clr = new Color(
                        blood.getRed()+(int)(Math.random()*100-50),
                        blood.getGreen()+(int)(Math.random()*10-5),
                        blood.getBlue()+(int)(Math.random()*10-5),
                        50+(int)(200*Math.max(1, 1.0*world.getTick() / world.getTickCount()))
                );
                g2d.setColor(clr);
                g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL, 0, new float[]{d1, 20f}, 1f));
                g2d.drawLine(oldPoint.x+dx, oldPoint.y+dy, point.x+dx, point.y+dy);
            }
            g2d.dispose();

        }
        oldPoint = point;
    }
}
