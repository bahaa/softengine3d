package info.bahaa.softengine3d.engine;

import javax.vecmath.Vector3d;

/**
 * Created by bahaazaid on 3/17/2016.
 */
public class Face {
    public int a;
    public int b;
    public int c;
    public Vector3d normal;

    public Face(int a, int b, int c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }
}
