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

    public Matrix4d transform = new Matrix4d();

    public Mesh(String name) {
        this.name = name;
    }

    public void addVertex(Vector3d vertex) {
        this.vertices.add(vertex);
    }

    public void addVertex(double x, double y, double z) {
        this.vertices.add(new Vector3d(x, y, z));
    }

    public String getName() {
        return name;
    }

    public List<Vector3d> getVertices() {
        return vertices;
    }
}
