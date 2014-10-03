package brutal;

import model.Unit;

import java.awt.*;
import java.util.*;

public class Wall {
    public static final Comparator<Integer> REVERSE = new Comparator<Integer>() {
        @Override
        public int compare(Integer o1, Integer o2) {
            return o2 - o1;
        }
    };

    private Polygon top = null;
    private Polygon bottom = null;
    private Polygon left = null;
    private Polygon right = null;

    int x0, y0, x1, y1;


    java.util.Map<Integer, Integer> topXes = new TreeMap<Integer, Integer>();
    java.util.Map<Integer, Integer> bottomXes = new TreeMap<Integer, Integer>(REVERSE);
    java.util.Map<Integer, Integer> leftYes = new TreeMap<Integer, Integer>(REVERSE);
    java.util.Map<Integer, Integer> rightYes = new TreeMap<Integer, Integer>();

    public Wall(int x0, int y0, int x1, int y1) {
        this.x0 = x0;
        this.y0 = y0;
        this.x1 = x1;
        this.y1 = y1;
    }

    int baseDeformationD = 5;
    int maxDeformationD = 25;
    int baseDeformationR = 5;

    void update() {
        top = new Polygon();
        top.addPoint(x0,y0);

        //top
        for (int x: topXes.keySet()) {
            int st = topXes.get(x);
            int lastX = top.xpoints[top.npoints-1];
            if (x - lastX > baseDeformationR*2) {
                if (lastX != x0) top.addPoint(lastX+baseDeformationR, y0);
                top.addPoint(x - baseDeformationR, y0);
            }
            top.addPoint(x, y0-st);

        }
        int lastX = top.xpoints[top.npoints-1];
        if (x1 - lastX > baseDeformationR*2 ) {
            top.addPoint(lastX+baseDeformationR, y0);
        }
        top.addPoint(x1,y0);
        top.addPoint(x1,y0+5);
        top.addPoint(x0,y0+5);

        right = new Polygon();
        right.addPoint(x1,y0);
        //right
        for (int y: rightYes.keySet()) {
            int st = rightYes.get(y);
            int lastY = right.ypoints[right.npoints-1];
            if (y - lastY > baseDeformationR*2) {
                if (lastY != y0) right.addPoint(x1, lastY + baseDeformationR);
                right.addPoint(x1, y - baseDeformationR);
            }
            right.addPoint(x1 + st, y);

        }
        int lastY = right.ypoints[right.npoints-1];
        if (y1 - lastY > baseDeformationR*2 ) {
            right.addPoint(x1, lastY + baseDeformationR);
        }
        right.addPoint(x1,y1);
        right.addPoint(x1-5,y1);
        right.addPoint(x1-5,y0);

        bottom = new Polygon();
        bottom.addPoint(x1, y1);
        //bottom
        for (int x: bottomXes.keySet()) {
            int st = bottomXes.get(x);
            lastX = bottom.xpoints[bottom.npoints-1];
            if (lastX - x > baseDeformationR*2) {
                if (lastX != x1) bottom.addPoint(lastX-baseDeformationR, y1);
                bottom.addPoint(x+baseDeformationR, y1);
            }

            bottom.addPoint(x, y1+st);

        }
        lastX = bottom.xpoints[bottom.npoints-1];
        if (lastX - x0 > baseDeformationR*2) {
            bottom.addPoint(lastX-baseDeformationR, y1);
        }
        bottom.addPoint(x0,y1);
        bottom.addPoint(x0,y1-5);
        bottom.addPoint(x1,y1-5);

        left = new Polygon();
        left.addPoint(x0,y1);
        //left
        for (int y: leftYes.keySet()) {
            int st = leftYes.get(y);
            lastY = left.ypoints[left.npoints-1];
            if (lastY - y0 > baseDeformationR*2) {
                if (lastY != y1) left.addPoint(x0, lastY - baseDeformationR);
                left.addPoint(x0, y + baseDeformationR);
            }
            left.addPoint(x0 - st, y);

        }
        lastY = left.ypoints[left.npoints-1];
        if (lastY - y0 > baseDeformationR*2) {
            left.addPoint(x0, lastY - baseDeformationR);
        }

        left.addPoint(x0, y0);
        left.addPoint(x0+5, y0);
        left.addPoint(x0+5, y1);
    }

    int rnd(double crd) {
        return (int)(crd / baseDeformationR/2)*baseDeformationR*2;
    }

    boolean add(Map<Integer, Integer> map, double key, int val, boolean neigh) {
        int k = rnd(key);
        int oldVal = map.containsKey(k) ? map.get(k) : 0;
        int newVal = Math.min(maxDeformationD, oldVal + val);
        boolean changed = newVal > oldVal;
        map.put(k, newVal);
        if (neigh) {
            changed = changed || add(map, key - baseDeformationR, val / 2, false);
            changed = changed || add(map, key + baseDeformationR, val / 2, false);
        }
        if (changed) {
            top = null;
        }
        return changed;
    }

    int strength(Unit unit, double vpart) {
        if (vpart < 0) return 0;
        return (int)((unit.getMass() > 5 ? 10 : 5) * Math.abs(vpart)) / 10;
    }

    public void addCollision(Unit unit) {
        if (Math.abs(unit.getY() - y0) < unit.getRadius() ) {
            //top
            add(topXes, unit.getX(), strength(unit, -unit.getSpeedY()), unit.getRadius() > 20);

        }
        else if (Math.abs(unit.getY() - y1) < unit.getRadius() ) {
            //bottom
            add(bottomXes, unit.getX(), strength(unit, unit.getSpeedY()), unit.getRadius() > 20);
        }
        else if (Math.abs(unit.getX() - x0) < unit.getRadius()) {
            //left
            add(leftYes, unit.getY(), strength(unit, -unit.getSpeedX()), unit.getRadius() > 20);
        }
        else if (Math.abs(unit.getX() - x1) < unit.getRadius()) {
            //right
            add(rightYes, unit.getY(), strength(unit, unit.getSpeedX()), unit.getRadius() > 20);
        }

    }

    public boolean updated() {
        return top == null;
    }


    public Polygon getTop() {
        if (top == null) {
            update();
        }
        return top;
    }

    public Polygon getBottom() {
        if (top == null) {
            update();
        }
        return bottom;
    }

    public Polygon getLeft() {
        if (top == null) {
            update();
        }
        return left;
    }

    public Polygon getRight() {
        if (top == null) {
            update();
        }
        return right;
    }
}
