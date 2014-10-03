package brutal;

import model.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.awt.image.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class BrutalRenderer {

    static Sprite cacodemon;
    static Sprite elemental;
    static Sprite faces;
    static Sprite textures;
    static Sprite dude;
    static Sprite barrels;
    static Sprite time;
    static Font doom;

    static BufferedImage load(String path) throws IOException {
        BufferedImage orig = ImageIO.read(new File(path));
        BufferedImage processed = new BufferedImage(orig.getWidth(null), orig.getHeight(null), BufferedImage.TYPE_INT_ARGB);
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
            doom = Font.createFont(Font.TRUETYPE_FONT, new File("plugins/brutal/amazdoomleft.ttf"));
            barrels = new Sprite("plugins/brutal/barrels.png", 42, 57, 0);
            time = new Sprite("plugins/brutal/time.png", 39, 48, 0);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (FontFormatException e) {
            e.printStackTrace();
        }
    }

    static void renderSprite(Graphics2D context, Rectangle rect, Image image, int wsize, int hsize, int pad, int number) {
        int colsInRow = image.getWidth(null) / wsize;
        int row = number / colsInRow;
        int col = number % colsInRow;
        context.drawImage(image, rect.x, rect.y, (int)rect.getMaxX(), (int)rect.getMaxY(), col*wsize + pad, row*hsize + pad, col*wsize+wsize-pad-1, row*hsize + hsize-pad-1, null, null);
    }


    static Rectangle toRect(Hockeyist hoc) {
        double posY = hoc.getState() == HockeyistState.RESTING ? hoc.getY() + 60 : hoc.getY();
        return new Rectangle((int)hoc.getX() - (int)hoc.getRadius() - 1, (int)posY - (int)hoc.getRadius() - 1, (int)hoc.getRadius()*2+2, (int)hoc.getRadius()*2+2);
    }
    static Rectangle toRect(Puck unit) {
        return new Rectangle((int)unit.getX() - 16, (int)unit.getY() - 16, 32, 32);
    }

    static int direction(double angle) {
        //angle pi/2 -> 0
        //angle pi -> 2 //2pi
        //angle -pi/2 -> 4
        //angle 0 -> 6 //pi
        //(angle + pi)*4 / pi
        return (int)Math.round(6 + angle * 4 / Math.PI) % 8;
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

    static int statusAndDir(Hockeyist hockeyist, World world, Game game,  int swingStart, int swingCt, int kdStart, int kdCount) {
        int nr = direction(hockeyist.getAngle());
        if (hockeyist.getState() == HockeyistState.SWINGING) {
            nr = nr + (swingStart + (swingCt * hockeyist.getSwingTicks() / game.getMaxEffectiveSwingTicks()))*8;
        }
        else if (hockeyist.getState() == HockeyistState.KNOCKED_DOWN) {
            nr = -(kdStart + (kdCount - Math.abs(hockeyist.getRemainingKnockdownTicks() - 20)*kdCount/20));
        }
        else if ((hockeyist.getLastAction() == ActionType.STRIKE) && world.getTick() - hockeyist.getLastActionTick() < 20) {
            nr = nr + 8*(swingStart + swingCt-1);
        }
        else if (hockeyist.getState() == HockeyistState.RESTING) {
            nr = direction(hockeyist.getAngle() + hockeyist.getAngleTo(world.getPuck()));
        }
        return nr;
    }

    static Image bg = null;
    static Wall wall = null;
    static Image wallImg = null;
    static void drawBg1(Graphics2D ctx, Game game, World world) {
        if (wall == null) {
            wall = new Wall((int)game.getRinkLeft(), (int)game.getRinkTop(), (int)game.getRinkRight(), (int)game.getRinkBottom());
        }
        if (bg == null) {
            bg = new BufferedImage(1200, 800, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = (Graphics2D) bg.getGraphics();
            g2d.setPaint(textures.toTexture(21 * 12 + 9, 18));
            g2d.fillRect(0, 0, 1200, 800);
            g2d.setPaint(textures.toTexture(10 * 12 + 4));//91/*+38*/));
           g2d.fillRect((int) game.getRinkLeft() , (int) game.getRinkTop() , (int) game.getRinkRight() - (int) game.getRinkLeft() , (int) game.getRinkBottom() - (int) game.getRinkTop() +1 );

            //g2d.setPaint(textures.toTexture(26*12+6, 18));
            g2d.setPaint(textures.toTexture(10 * 12 + 4));
            g2d.fillRect(0, (int)game.getGoalNetTop(), (int)game.getRinkLeft(), (int)game.getGoalNetHeight());
            g2d.fillRect((int)game.getRinkRight(), (int)game.getGoalNetTop(), (int)game.getRinkLeft(), (int) game.getGoalNetHeight());
            dude.draw(g2d, new Rectangle(20, (int) game.getGoalNetTop() + (int) game.getGoalNetHeight() - dude.image.getHeight(), dude.wSize, dude.image.getHeight()), 0);
            dude.draw(g2d, new Rectangle(20, (int) game.getGoalNetTop(), dude.wSize, dude.image.getHeight()), 1);
            dude.drawMirrored(g2d, new Rectangle(1200 - 20 - dude.wSize, (int) game.getGoalNetTop() + (int) game.getGoalNetHeight() - dude.image.getHeight(), dude.wSize, dude.image.getHeight()), 1);
            dude.drawMirrored(g2d, new Rectangle(1200-20-dude.wSize, (int) game.getGoalNetTop(), dude.wSize, dude.image.getHeight()), 0);

            time.draw(g2d, new Rectangle(1150, 760, time.wSize, time.hSize), 0);
            g2d.dispose();
        }
        ctx.drawImage(bg, 0, 0, null);
        drawWall(ctx);
        if (bloodImg != null) ctx.drawImage(bloodImg, 0, 0, null);

        drawTopLine(ctx, game, world);
    }

    static int polRedrawn = 0;
    static void drawWall(Graphics2D ctx) {
        if (wall.updated()) {
            wallImg = new BufferedImage(1200, 800, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = (Graphics2D) wallImg.getGraphics();
            g2d.setPaint(textures.toTexture(10 * 12 + 4));//91/*+38*/));
            g2d.fillPolygon(wall.getTop());
            g2d.fillPolygon(wall.getBottom());
            g2d.fillPolygon(wall.getLeft());
            g2d.fillPolygon(wall.getRight());
            polRedrawn++;
            g2d.setPaint(null);
            g2d.setColor(Color.YELLOW);
            //g2d.drawString("WALL: " + polRedrawn, 0, 700);
            g2d.dispose();
        }
        ctx.drawImage(wallImg, 0, 0, null);

    }


    static BufferedImage topLine = null;

    static void drawTopLine(Graphics2D ctx, Game game, World world) {

        if (topLine == null) {
            topLine = new BufferedImage(1200, 66, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = topLine.createGraphics();

            g2d.setPaint(textures.toTexture(15 * 12, 2));
            g2d.fillRect(0, 0, 1200, 64);

            g2d.setFont(doom.deriveFont(24f));
            g2d.setColor(new Color(199, 52, 63));
            String n1 = world.getPlayers()[0].getName() + ": " + world.getPlayers()[0].getGoalCount();
            g2d.drawString(n1, 40, 50);
            cacodemon.draw(g2d, new Rectangle(10, 25, 25, 25), 0);
            String n2 = world.getPlayers()[1].getName() + ": " + world.getPlayers()[1].getGoalCount();
            g2d.setColor(new Color(199, 74, 41));
            g2d.drawString(n2, 1200 - 40 - g2d.getFontMetrics().stringWidth(n2), 50);
            elemental.draw(g2d, new Rectangle(1200 - 10 - 25, 25, 25, 25), 0);

            String txt = null;
            boolean justGoal = world.getPlayers()[0].isJustMissedGoal() || world.getPlayers()[0].isJustScoredGoal();
            if (justGoal) {
                txt = "GoaL";
            }
            else if (world.getTick() - world.getTickCount() > 0 && world.getTick() - world.getTickCount() < 500 ) {
                txt = "OvertimE";
            }
            if (txt != null) {
                Map<TextAttribute, Object> attributes = new HashMap<TextAttribute, Object>();
                attributes.put(TextAttribute.TRACKING, 0.15);
                g2d.setFont(doom.deriveFont(48f).deriveFont(attributes));
                g2d.setPaint(textures.toTexture(12*1+3));
                g2d.drawString(txt, 600 - g2d.getFontMetrics().stringWidth(txt) / 2, 60);
            }

            g2d.dispose();
        }
        ctx.drawImage(topLine, 0, 0, null);
        textures.draw(ctx, new Rectangle(450, 10, 46, 46), 15 * 12 + 1);
        textures.draw(ctx, new Rectangle(684, 10, 46, 46), 15 * 12 + 1);
        barrels.draw(ctx, new Rectangle(450, 20, barrels.wSize, barrels.hSize), (world.getTick() / 5) % 3 );
        barrels.draw(ctx, new Rectangle(684, 20, barrels.wSize, barrels.hSize), (world.getTick() / 10) % 3 );
    }


    static boolean wasGoal;

    public static void beforeDrawScene(Graphics graphics, World world, Game game, double scale) {
        //Graphics2D g2d = (Graphics2D) graphics.create();
        //drawBg1(g2d, game, world);
        //g2d.dispose();
    }



    public static void afterDrawScene(Graphics graphics, World world, Game game, double scale) {

        boolean justGoal = world.getPlayers()[0].isJustMissedGoal() || world.getPlayers()[0].isJustScoredGoal();
        if (!justGoal && wasGoal) {
            bloodImg = null;
            oldPoint = null;
            topLine = null;
        }

        if (justGoal && !wasGoal) {
            topLine = null;
        }
        if (world.getTick() == world.getTickCount() || world.getTick() - world.getTickCount() == 500) {
            topLine = null;
        }
        wasGoal = justGoal;
        Graphics2D g2d = (Graphics2D) graphics.create();
        drawBg1(g2d, game, world);
        for (Hockeyist hoc: world.getHockeyists()) {
            if (hoc.getPlayerId() == 1) {
                cacodemon.drawM(g2d, toRect(hoc), statusAndDir(hoc, world, game, 1, 3, 30, 5));
            }
            else {
                elemental.drawM(g2d, toRect(hoc), statusAndDir(hoc, world, game, 3, 3, 35, 3));
            }
            wall.addCollision(hoc);
        }
        faces.draw(g2d, toRect(world.getPuck()), faceDirection(world.getPuck()));

        wall.addCollision(world.getPuck());

        if (world.getPuck().getOwnerPlayerId() == -1) {
            blood(new Point((int) world.getPuck().getX(), (int) world.getPuck().getY()), world);
        }
        else {
            oldPoint = null;
        }

        g2d.setFont(doom.deriveFont(20f));
        g2d.setColor(Color.red);
        String t = "" + world.getTick();

        g2d.drawString(t, 1170 - g2d.getFontMetrics().stringWidth(t)/2, 760);

        g2d.dispose();

    }

    static long totalTime = 0;


    static Color blood = new Color(138,7,7);

    static BufferedImage bloodImg = null;
    static Point oldPoint = null;

    static void blood(Point point, World world) {
        if (bloodImg == null) {
            bloodImg = new BufferedImage(1200, 800, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = (Graphics2D) bloodImg.getGraphics();
            //g2d.setBackground(new Color(0, 0, 0, 0));
            //g2d.clearRect(0, 0, 1200, 800);
            g2d.dispose();
        }
        if (oldPoint != null) {
            Graphics2D g2d = (Graphics2D) bloodImg.getGraphics();
            if (world.getTick() % 10 == 0) {

                DataBuffer buffer = bloodImg.getRaster().getDataBuffer();
                if (buffer instanceof DataBufferByte) {
                    byte[] imagePixelData = ((DataBufferByte) buffer).getData();
                    for (int i = 3; i < imagePixelData.length; i += 4) {
                        if (imagePixelData[i] != 0) {
                            short abs = imagePixelData[i];
                            if (abs < 0) abs += 256;
                            abs = (short) (abs * 0.99);
                            if (abs >= 128) abs -= 256;
                            imagePixelData[i] = (byte) abs;
                        }

                    }
                }
            }

            for (int a = 0; a < 5; a++) {
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
