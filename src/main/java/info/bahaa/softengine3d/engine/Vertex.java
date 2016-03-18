package info.bahaa.softengine3d.engine;

import javax.vecmath.Vector2d;
import javax.vecmath.Vector3d;

/**
 * Created by bahaazaid on 3/18/2016.
 */
public class Vertex {

    public Vector3d coordinates;
    public Vector3d normal;
    public Vector3d worldCoordinates;
    public Vector2d textureCoordinates;

    public Vertex(Vector3d coordinates, Vector3d normal, Vector3d worldCoordinates, Vector2d textureCoordinates) {
        this.coordinates = coordinates;
        this.normal = normal;
        this.worldCoordinates = worldCoordinates;
        this.textureCoordinates = textureCoordinates;
    }
}
