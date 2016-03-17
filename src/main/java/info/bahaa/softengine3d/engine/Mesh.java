package info.bahaa.softengine3d.engine;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by bahaazaid on 3/15/2016.
 */
public class Mesh {

    private String name;
    private List<Vector3d> vertices = new ArrayList<>();
    private List<Face> faces = new ArrayList<>();

    public Matrix4d transform = new Matrix4d();

    public Mesh(String name) {
        this.name = name;
    }

    public Mesh addVertex(Vector3d vertex) {
        this.vertices.add(vertex);
        return this;
    }

    public Mesh addVertex(double x, double y, double z) {
        return this.addVertex(new Vector3d(x, y, z));
    }

    public Mesh addFace(int a, int b, int c) {
        return this.addFace(new Face(a, b, c));
    }

    public Mesh addFace(Face face) {
        this.faces.add(face);
        return this;
    }

    public String getName() {
        return this.name;
    }

    public List<Vector3d> getVertices() {
        return this.vertices;
    }

    public List<Face> getFaces() {
        return this.faces;
    }
}
