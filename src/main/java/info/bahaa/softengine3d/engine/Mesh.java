package info.bahaa.softengine3d.engine;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.io.InputStream;
import java.io.InputStreamReader;
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

    public static List<Mesh> loadFromJson(InputStream inputStream) {
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(new InputStreamReader(inputStream)).getAsJsonObject();

        List<Mesh> meshes = new ArrayList<>();

        for (JsonElement jsonMeshElement : jsonObject.getAsJsonArray("meshes")) {
            JsonObject jsonMesh = jsonMeshElement.getAsJsonObject();

            JsonArray verticesArray = jsonMesh.getAsJsonArray("vertices");
            JsonArray indicesArray = jsonMesh.getAsJsonArray("indices"); // Faces

            int uvCount = jsonMesh.get("uvCount").getAsInt();
            int verticesStep = 1;

            // Depending of the number of texture's coordinates per vertex
            // we're jumping in the vertices array  by 6, 8 & 10 windows frame
            switch (uvCount) {
                case 0:
                    verticesStep = 6;
                    break;
                case 1:
                    verticesStep = 8;
                    break;
                case 2:
                    verticesStep = 10;
                    break;
            }

            // the number of interesting vertices information for us
            int verticesCount = verticesArray.size() / verticesStep;
            // number of faces is logically the size of the array divided by 3 (A, B, C)
            int facesCount = indicesArray.size() / 3;

            Mesh mesh = new Mesh(jsonMesh.get("name").getAsString());

            // Filling the Vertices array of our mesh first
            for (int index = 0; index < verticesCount; index++) {
                double x = verticesArray.get(index * verticesStep).getAsDouble();
                double y = verticesArray.get(index * verticesStep + 1).getAsDouble();
                double z = verticesArray.get(index * verticesStep + 2).getAsDouble();

                mesh.addVertex(x, y, z);
            }

            // Then filling the Faces array
            for (int index = 0; index < facesCount; index++) {
                int a = indicesArray.get(index * 3).getAsInt();
                int b = indicesArray.get(index * 3 + 1).getAsInt();
                int c = indicesArray.get(index * 3 + 2).getAsInt();

                mesh.addFace(a, b, c);
            }

            // Getting the position you've set in Blender
            JsonArray positionArray = jsonMesh.get("position").getAsJsonArray();
            mesh.transform.transform(new Point3d(
                    positionArray.get(0).getAsDouble(),
                    positionArray.get(1).getAsDouble(),
                    positionArray.get(2).getAsDouble()
            ));

            meshes.add(mesh);
        }
        return meshes;
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
