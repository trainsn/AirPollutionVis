package com.a12599.myapplication;

/**
 * Created by dell on 2016/11/23.
 */
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A Triangle is an immutable Set of exactly three Pnts.
 *
 * All Set operations are available. Individual vertices can be accessed via
 * iterator() and also via triangle.get(index).
 *
 * Note that, even if two triangles have the same vertex set, they are
 * *different* triangles. Methods equals() and hashCode() are consistent with
 * this rule.
 *
 * @author Paul Chew
 *
 * Created December 2007. Replaced general simplices with geometric triangle.
 *
 */
class Triangle extends ArraySet<Pnt> {

    private int idNumber;                   // The id number
    private Pnt circumcenter = null;        // The triangle's circumcenter

    private static int idGenerator = 0;     // Used to create id numbers
    public static boolean moreInfo = false; // True iff more info in toString

    /**
     * @param vertices the vertices of the Triangle.
     * @throws IllegalArgumentException if there are not three distinct vertices
     */
    public Triangle (Pnt... vertices) {
        this(Arrays.asList(vertices));
    }

    /**
     * @param collection a Collection holding the Simplex vertices
     * @throws IllegalArgumentException if there are not three distinct vertices
     */
    public Triangle (Collection<? extends Pnt> collection) {
        super(collection);
        idNumber = idGenerator++;
        if (this.size() != 3)
            throw new IllegalArgumentException("Triangle must have 3 vertices");
    }

    @Override
    public String toString () {
        if (!moreInfo) return "Triangle" + idNumber;
        return "Triangle" + idNumber + super.toString();
    }

    /**
     * Get arbitrary vertex of this triangle, but not any of the bad vertices.
     * @param badVertices one or more bad vertices
     * @return a vertex of this triangle, but not one of the bad vertices
     * @throws NoSuchElementException if no vertex found
     */
    public Pnt getVertexButNot (Pnt... badVertices) {
        Collection<Pnt> bad = Arrays.asList(badVertices);
        for (Pnt v: this) if (!bad.contains(v)) return v;
        throw new NoSuchElementException("No vertex found");
    }

    /**
     * True iff triangles are neighbors. Two triangles are neighbors if they
     * share a facet.
     * @param triangle the other Triangle
     * @return true iff this Triangle is a neighbor of triangle
     */
    public boolean isNeighbor (Triangle triangle) {
        int count = 0;
        for (Pnt vertex: this)
            if (!triangle.contains(vertex)) count++;
        return count == 1;
    }

    /**
     * Report the facet opposite vertex.
     * @param vertex a vertex of this Triangle
     * @return the facet opposite vertex
     * @throws IllegalArgumentException if the vertex is not in triangle
     */
    public ArraySet<Pnt> facetOpposite (Pnt vertex) {
        ArraySet<Pnt> facet = new ArraySet<Pnt>(this);
        if (!facet.remove(vertex))
            throw new IllegalArgumentException("Vertex not in triangle");
        return facet;
    }

    /**
     * @return the triangle's circumcenter
     */
    public Pnt getCircumcenter () {
        if (circumcenter == null)
            circumcenter = Pnt.circumcenter(this.toArray(new Pnt[0]));
        return circumcenter;
    }

    /* The following two methods ensure that a Triangle is immutable */

    @Override
    public boolean add (Pnt vertex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<Pnt> iterator () {
        return new Iterator<Pnt>() {
            private Iterator<Pnt> it = Triangle.super.iterator();
            public boolean hasNext() {return it.hasNext();}
            public Pnt next() {return it.next();}
            public void remove() {throw new UnsupportedOperationException();}
        };
    }

    /* The following two methods ensure that all triangles are different. */

    @Override
    public int hashCode () {
        return (int)(idNumber^(idNumber>>>32));
    }

    @Override
    public boolean equals (Object o) {
        return (this == o);
    }

}