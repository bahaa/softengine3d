package info.bahaa.softengine3d.engine;

import javax.vecmath.Matrix4d;
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
    private double[] depthBuffer;

    public Device(int width, int height) {
        this.width = width;
        this.height = height;

        this.buffer = new int[width * height];
        this.depthBuffer = new double[width * height];
    }

    public void clear() {
        Arrays.fill(this.buffer, 0xFF000000);
        Arrays.fill(this.depthBuffer, Double.MAX_VALUE);
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

    protected Vector3d project(Vector3d vector, Matrix4d transform) {
        Vector3d point = transformCoordinates(vector, transform);

        // The transformed coordinates will be based on coordinate system
        // starting on the center of the screen. But drawing on screen normally starts
        // from top left. We then need to transform them again to have x:0, y:0 on top left.
        int x = (int) (point.x * this.width + this.width / 2.0);
        int y = (int) (-point.y * this.height + this.height / 2.0);

        return new Vector3d(x, y, point.z);
    }

    protected void drawPoint(Vector3d point, int color) {
        // Clipping what's visible on screen
        if (point.x >= 0 && point.y >= 0 && point.x < this.width
                && point.y < this.height) {
            // Drawing a yellow point
            this.putPixel((int) point.x, (int) point.y, point.z, color);
        }
    }

    protected void putPixel(int x, int y, double z, int color) {
        int index = x + y * this.width;

        if (this.depthBuffer[index] < z) {
            return;
        }
        this.depthBuffer[index] = z;

        this.buffer[index] = color;
    }

    protected void renderMeshFaces(Mesh mesh, Matrix4d transform) {
        int faceCount = mesh.getFaces().size();
        int faceIndex = 0;

        for (Face face : mesh.getFaces()) {
            Vector3d vertexA = mesh.getVertices().get(face.a);
            Vector3d vertexB = mesh.getVertices().get(face.b);
            Vector3d vertexC = mesh.getVertices().get(face.c);

            Vector3d pointA = this.project(vertexA, transform);
            Vector3d pointB = this.project(vertexB, transform);
            Vector3d pointC = this.project(vertexC, transform);

            float componentColor = 0.25f + (faceIndex % faceCount) * 0.75f / faceCount;
            drawTriangle(pointA, pointB, pointC, this.getIntFromColor(componentColor, componentColor, componentColor));

            faceIndex++;
        }
    }

    protected double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(value, max));
    }

    protected double clamp(double value) {
        return clamp(value, 0.0, 1.0);
    }

    protected double interpolate(double min, double max, double gradient) {
        return min + (max - min) * this.clamp(gradient);
    }

    protected void processScanLine(int y, Vector3d pa, Vector3d pb, Vector3d pc, Vector3d pd, int color) {
        double gradient1 = pa.y != pb.y ? (y - pa.y) / (pb.y - pa.y) : 1;
        double gradient2 = pc.y != pd.y ? (y - pc.y) / (pd.y - pc.y) : 1;

        int sx = (int) interpolate(pa.x, pb.x, gradient1);
        int ex = (int) interpolate(pc.x, pd.x, gradient2);

        // starting Z & ending Z
        double z1 = interpolate(pa.z, pb.z, gradient1);
        double z2 = interpolate(pc.z, pd.z, gradient2);

        // drawing a line from left (sx) to right (ex)
        for (int x = sx; x < ex; x++) {
            double gradient = (x - sx) / (double) (ex - sx);
            double z = interpolate(z1, z2, gradient);

            drawPoint(new Vector3d(x, y, z), color);
        }
    }

    protected void drawTriangle(Vector3d p1, Vector3d p2, Vector3d p3, int color) {
        // Sorting points on y
        Vector3d temp;

        if (p1.y > p2.y) {
            temp = p2;
            p2 = p1;
            p1 = temp;
        }

        if (p2.y > p3.y) {
            temp = p2;
            p2 = p3;
            p3 = temp;
        }

        if (p1.y > p2.y) {
            temp = p2;
            p2 = p1;
            p1 = temp;
        }

        // Inverse slopes
        double dP1P2, dP1P3;
        if (p2.y - p1.y > 0) {
            dP1P2 = (p2.x - p1.x) / (p2.y - p1.y);
        } else {
            dP1P2 = 0;
        }

        if (p3.y - p1.y > 0) {
            dP1P3 = (p3.x - p1.x) / (p3.y - p1.y);
        } else {
            dP1P3 = 0;
        }

        if (dP1P2 > dP1P3) {
            for (int y = (int) p1.y; y <= (int) p3.y; y++) {
                if (y < p2.y) {
                    processScanLine(y, p1, p3, p1, p2, color);
                } else {
                    processScanLine(y, p1, p3, p2, p3, color);
                }
            }
        } else {
            for (int y = (int) p1.y; y <= (int) p3.y; y++) {
                if (y < p2.y) {
                    processScanLine(y, p1, p2, p1, p3, color);
                } else {
                    processScanLine(y, p2, p3, p1, p3, color);
                }
            }
        }
    }

    protected int getIntFromColor(float red, float green, float blue) {
        int r = Math.round(255 * red);
        int g = Math.round(255 * green);
        int b = Math.round(255 * blue);

        r = (r << 16) & 0x00FF0000;
        g = (g << 8) & 0x0000FF00;
        b = b & 0x000000FF;

        return 0xFF000000 | r | g | b;
    }
}
