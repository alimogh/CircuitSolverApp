package com.cpen321.circuitsolver.ui.draw;

import android.graphics.Point;

import com.cpen321.circuitsolver.model.SimplePoint;

// Class to hold the position of touch events to allow for the circuit to be zoomed in and
// pan around
public class DrawController {
    private SimplePoint fingerOne;
    private SimplePoint fingerTwo;

    private SimplePoint startFingerOne;
    private SimplePoint startFingerTwo;

    private SimplePoint lastFingerOne;
    private SimplePoint lastFingerTwo;

    public DrawController() {

    }

    public void setFingerOne(SimplePoint fingerOne) {
        this.lastFingerOne = this.fingerOne;
        this.fingerOne = fingerOne;
    }
    public void setFingerTwo(SimplePoint fingerTwo) {
        this.lastFingerTwo = this.fingerTwo;
        this.fingerTwo = fingerTwo;
    }
    public void setStartFingerOne(SimplePoint startFingerOne) {
        this.startFingerOne = startFingerOne;
    }
    public void setStartFingerTwo(SimplePoint startFingerTwo) {
        this.startFingerTwo = startFingerTwo;
    }

    public float getZoomScale() {
        if (this.startFingerOne == null)
            this.startFingerOne = this.fingerOne;
        if (this.startFingerTwo == null)
            this.startFingerTwo = this.fingerTwo;

        Double hypoStart = this.startFingerOne.distanceFrom(this.startFingerTwo);
        Double hypoEnd = this.fingerOne.distanceFrom(this.fingerTwo);

        if (hypoEnd <= 0 || hypoStart <= 0)
            return 1f;

        return (float) (hypoEnd/hypoStart);
    }

    public SimplePoint getDelta() {
        return new SimplePoint(this.fingerOne.getX() - this.lastFingerOne.getX(),
                this.fingerOne.getY() - this.lastFingerOne.getY());
    }

    public Point getMiddlePoint() {
        int middleX, middleY;

        int diffX, diffY;
        diffX = (this.fingerOne.getX() - this.fingerTwo.getX());
        diffY = (this.fingerOne.getY() - this.fingerTwo.getY());

        middleX = diffX > 0 ? this.fingerOne.getX() + diffX : this.fingerTwo.getX() + Math.abs(diffX);
        middleY = diffY > 0 ? this.fingerOne.getY() + diffY : this.fingerTwo.getY() + Math.abs(diffY);

        return new Point(middleX, middleY);
    }


}
