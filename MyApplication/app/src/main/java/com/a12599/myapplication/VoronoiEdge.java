package com.a12599.myapplication;
/**
 * Created by ajwerner on 12/28/13.
 */
public class VoronoiEdge {
    public final Point site1, site2;
    public final double m, b; // parameters for line that the edge lies on
    public final boolean isVertical;
    public Point p1, p2;

    public VoronoiEdge(Point site1, Point site2) {
        this.site1 = site1;
        this.site2 = site2;
        isVertical = (site1.y == site2.y) ? true : false;
        if (isVertical) m = b = 0;
        else {
            m = -1.0 / ((site1.y - site2.y) / (site1.x - site2.x));
            Point midpoint = Point.midpoint(site1, site2);
            b = midpoint.y - m*midpoint.x;
        }
    }

    public Point intersection(VoronoiEdge that) {
        if (this.m == that.m && this.b != that.b) return null; // no intersection
        double x, y;
        if (this.isVertical) {
            x = (this.site1.x + this.site2.x) / 2;
            y = that.m*x + that.b;
        }
        else if (that.isVertical) {
            x = (that.site1.x + that.site2.x) / 2;
            y = this.m*x + this.b;
        }
        else {
            x = (that.b - this.b) / (this.m - that.m);
            y = m * x + b;
        }
        return new Point(x, y);
    }

    public Point intersection(Point that1, Point that2) {
        double thatm=0, thatb=0;
        boolean thatIsVertical= false;
        if (that1.x == that2.x)
            thatIsVertical = true;
        else
        {
            thatm = (that1.y - that2.y) / (that1.x- that2.x);
            thatb = that2.y - thatm * that2.x;
        }
        if (this.m == thatm && this.b != thatb) return null; // no intersection
        double x, y;
        if (this.isVertical) {
            x = (this.site1.x + this.site2.x) / 2;
            y = thatm*x + thatb;
        }
        else if (thatIsVertical) {
            x = that1.x;
            y = this.m*x + this.b;
        }
        else {
            x = (thatb - this.b) / (this.m - thatm);
            y = m * x + b;
        }
        return new Point(x, y);
    }
}
