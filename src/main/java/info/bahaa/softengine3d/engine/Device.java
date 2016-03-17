package info.bahaa.softengine3d.engine;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point2d;
import javax.vecmath.Vector3d;
import java.util.Arrays;
import java.util.List;

/**
 * Created by bahaazaid on 3/15/2016.
 */
public class Device {

    private final int width;
    private final int height;
    private int[] buffer;

    public Device(int width, int height) {
        this.width = width;
        this.height = height;

        this.buffer = new int[width * height];
    }

    public void clear() {
        Arrays.fill(this.buffer, 0xFF000000);
    }

    public void render(Camera camera, List<Mesh> meshes) {
        Matrix4d viewMatrix = lookAt(camera.getPosition(), camera.getTarget(), new Vector3d(0.0, 1.0, 0.0));
        Matrix4d projMatrix = perspectiveFov(0.78, (double) this.width / this.height, 0.01, 1.0);

        for (Mesh mesh : meshes) {
            Matrix4d transformMatrix = new Matrix4d();
            transformMatrix.mul(mesh.transform, viewMatrix);
            transformMatrix.mul(projMatrix);

            renderMeshFaces(mesh, transformMatrix);
        }
    }

    public int[] getBuffer() {
        return this.buffer;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    protected Matrix4d lookAt(Vector3d eye, Vector3d target, Vector3d up) {
        Vector3d vz = new Vector3d();
        vz.sub(target, eye);
        vz.normalize();

        Vector3d vx = new Vector3d();
        vx.cross(up, vz);
        vx.normalize();

        Vector3d vy = new Vector3d();
        vy.cross(vz, vx);
        vy.normalize(); // not needed

        // Eye angles
        double ex = -vx.dot(eye);
        double ey = -vy.dot(eye);
        double ez = -vz.dot(eye);

        return new Matrix4d(vx.x, vy.x, vz.x, 0.0,
                vx.y, vy.y, vz.y, 0.0,
                vx.z, vy.z, vz.z, 0.0,
                ex, ey, ez, 1.0);
    }

    protected Matrix4d perspectiveFov(double fov, double aspect, double zNear, double zFar) {
        double tan = 1.0 / (Math.tan(fov * 0.5));
        return new Matrix4d(
                tan / aspect, 0.0, 0.0, 0.0,
                0.0, tan, 0.0, 0.0,
                0.0, 0.0, -zFar / (zNear - zFar), 1.0,
                0.0, 0.0, (zNear * zFar) / (zNear - zFar), 0.0
        );
    }

    protected Vector3d transformCoordinates(Vector3d vector, Matrix4d transform) {
        double x = (vector.x * transform.m00) + (vector.y * transform.m10) + (vector.z * transform.m20) + transform.m30;
        double y = (vector.x * transform.m01) + (vector.y * transform.m11) + (vector.z * transform.m21) + transform.m31;
        double z = (vector.x * transform.m02) + (vector.y * transform.m12) + (vector.z * transform.m22) + transform.m32;
        double w = (vector.x * transform.m03) + (vector.y * transform.m13) + (vector.z * transform.m23) + transform.m33;

        return new Vector3d(x / w, y / w, z / w);
    }

    protected Point2d project(Vector3d vector, Matrix4d transform) {
        Vector3d point = transformCoordinates(vector, transform);

        // The transformed coordinates will be based on coordinate system
        // starting on the center of the screen. But drawing on screen normally starts
        // from top left. We then need to transform them again to have x:0, y:0 on top left.
        int x = (int) (point.x * this.width + this.width / 2.0);
        int y = (int) (-point.y * this.height + this.height / 2.0);

        return new Point2d(x, y);
    }

    protected void drawPoint(Point2d point) {
        // Clipping what's visible on screen
        if (point.x >= 0 && point.y >= 0 && point.x < this.width
                && point.y < this.height) {
            // Drawing a yellow point
            this.putPixel((int) point.x, (int) point.y, 0xFFFFFF00);
        }
    }

    protected void putPixel(int x, int y, int color) {
        this.buffer[x + y * this.width] = color;
    }

    protected void drawLine(Point2d point0, Point2d point1) {
        double distance = point0.distance(point1);

        if (distance <= 1.0) {
            return;
        }

        Point2d diff = new Point2d();
        diff.sub(point1, point0);
        diff.scale(0.5);

        Point2d midPoint = new Point2d();
        midPoint.add(point0, diff);

        this.drawPoint(midPoint);

        this.drawLine(point0, midPoint);
        this.drawLine(midPoint, point1);
    }

    protected void drawBline(Point2d point0, Point2d point1) {
        int x0 = (int) point0.x;
        int y0 = (int) point0.y;
        int x1 = (int) point1.x;
        int y1 = (int) point1.y;

        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = (x0 < x1) ? 1 : -1;
        int sy = (y0 < y1) ? 1 : -1;
        int err = dx - dy;

        while (true) {
            this.drawPoint(new Point2d(x0, y0));

            if ((x0 == x1) && (y0 == y1)) {
                break;
            }

            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x0 += sx;
            }
            if (e2 < dx) {
                err += dx;
                y0 += sy;
            }
        }
    }

    protected void renderMeshFaces(Mesh mesh, Matrix4d transform) {
        for (Face face : mesh.getFaces()) {
            Vector3d vertexA = mesh.getVertices().get(face.a);
            Vector3d vertexB = mesh.getVertices().get(face.b);
            Vector3d vertexC = mesh.getVertices().get(face.c);

            Point2d pointA = this.project(vertexA, transform);
            Point2d pointB = this.project(vertexB, transform);
            Point2d pointC = this.project(vertexC, transform);

            this.drawBline(pointA, pointB);
            this.drawBline(pointB, pointC);
            this.drawBline(pointC, pointA);
        }
    }
}
