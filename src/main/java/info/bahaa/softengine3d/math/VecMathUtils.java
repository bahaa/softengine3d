package info.bahaa.softengine3d.math;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

/**
 * Created by bahaazaid on 3/18/2016.
 */
public class VecMathUtils {

    public static Matrix4d rotationYawPitchRoll(double yaw, double pitch, double roll) {
        // Produces a quaternion from Euler angles in the z-y-x orientation (Tait-Bryan angles)
        double halfRoll = roll * 0.5;
        double halfPitch = pitch * 0.5;
        double halfYaw = yaw * 0.5;
        double sinRoll = Math.sin(halfRoll);
        double cosRoll = Math.cos(halfRoll);
        double sinPitch = Math.sin(halfPitch);
        double cosPitch = Math.cos(halfPitch);
        double sinYaw = Math.sin(halfYaw);
        double cosYaw = Math.cos(halfYaw);

        double x = (cosYaw * sinPitch * cosRoll) + (sinYaw * cosPitch * sinRoll);
        double y = (sinYaw * cosPitch * cosRoll) - (cosYaw * sinPitch * sinRoll);
        double z = (cosYaw * cosPitch * sinRoll) - (sinYaw * sinPitch * cosRoll);
        double w = (cosYaw * cosPitch * cosRoll) + (sinYaw * sinPitch * sinRoll);

        double xx = x * x;
        double yy = y * y;
        double zz = z * z;
        double xy = x * y;
        double zw = z * w;
        double zx = z * x;
        double yw = y * w;
        double yz = y * z;
        double xw = x * w;

        return new Matrix4d(
                1.0 - (2.0 * (yy + zz)), 2.0 * (xy + zw), 2.0 * (zx - yw), 0,
                2.0 * (xy - zw), 1.0 - (2.0 * (zz + xx)), 2.0 * (yz + xw), 0,
                2.0 * (zx + yw), 2.0 * (yz - xw), 1.0 - (2.0 * (yy + xx)), 0,
                0, 0, 0, 1
        );
    }

    public static Matrix4d translation(double x, double y, double z) {
        return new Matrix4d(
                1.0, 0.0, 0.0, 0.0,
                0.0, 1.0, 0.0, 0.0,
                0.0, 0.0, 1.0, 0.0,
                x, y, z, 1.0
        );
    }

    public static Matrix4d lookAt(Vector3d eye, Vector3d target, Vector3d up) {
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

    public static Matrix4d perspectiveFov(double fov, double aspect, double zNear, double zFar) {
        double tan = 1.0 / (Math.tan(fov * 0.5));
        return new Matrix4d(
                tan / aspect, 0.0, 0.0, 0.0,
                0.0, tan, 0.0, 0.0,
                0.0, 0.0, -zFar / (zNear - zFar), 1.0,
                0.0, 0.0, (zNear * zFar) / (zNear - zFar), 0.0
        );
    }

    public static Vector3d transformCoordinates(Vector3d vector, Matrix4d transform) {
        double x = (vector.x * transform.m00) + (vector.y * transform.m10) + (vector.z * transform.m20) + transform.m30;
        double y = (vector.x * transform.m01) + (vector.y * transform.m11) + (vector.z * transform.m21) + transform.m31;
        double z = (vector.x * transform.m02) + (vector.y * transform.m12) + (vector.z * transform.m22) + transform.m32;
        double w = (vector.x * transform.m03) + (vector.y * transform.m13) + (vector.z * transform.m23) + transform.m33;

        return new Vector3d(x / w, y / w, z / w);
    }

    public static Vector3d transformNormal(Vector3d normal, Matrix4d transform) {
        return new Vector3d(
                (normal.x * transform.m00) + (normal.y * transform.m10) + (normal.z * transform.m20),
                (normal.x * transform.m01) + (normal.y * transform.m11) + (normal.z * transform.m21),
                (normal.x * transform.m02) + (normal.y * transform.m12) + (normal.z * transform.m22)
        );
    }
}
