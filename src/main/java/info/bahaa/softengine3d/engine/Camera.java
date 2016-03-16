package info.bahaa.softengine3d.engine;


import javax.vecmath.Vector3d;

/**
 * Created by bahaazaid on 3/15/2016.
 */
public class Camera {

    private Vector3d position = new Vector3d();
    private Vector3d target = new Vector3d();

    public Camera() {
    }

    public Vector3d getPosition() {
        return position;
    }

    public void setPosition(Vector3d position) {
        this.position = position;
    }

    public Vector3d getTarget() {
        return target;
    }

    public void setTarget(Vector3d target) {
        this.target = target;
    }
}
