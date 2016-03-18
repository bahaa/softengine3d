package info.bahaa.softengine3d.engine;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector2d;
import javax.vecmath.Vector3d;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by bahaazaid on 3/15/2016.
 */
public class Mesh {

    private String name;
    private List<Vertex> vertices = new ArrayList<>();
    private List<Face> faces = new ArrayList<>();
    private Texture texture;

    public Matrix4d worldMatrix = new Matrix4d();

    public Mesh(String name) {
        this.name = name;
    }

    public static List<Mesh> loadFromJson(InputStream inputStream) throws IOException {
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(new InputStreamReader(inputStream)).getAsJsonObject();

        List<Mesh> meshes = new ArrayList<>();
        Map<String, Material> materials = new HashMap<>();

        for (JsonElement jsonMaterialElement : jsonObject.getAsJsonArray("materials")) {
            JsonObject jsonMaterial = jsonMaterialElement.getAsJsonObject();
            Material material = new Material();

            material.name = jsonMaterial.get("name").getAsString();
            material.id = jsonMaterial.get("id").getAsString();

            if (jsonMaterial.get("diffuseTexture") != null) {
                material.diffuseTextureName = jsonMaterial.get("diffuseTexture").getAsJsonObject().get("name").getAsString();
            }

            materials.put(material.id, material);
        }

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

                double nx = verticesArray.get(index * verticesStep + 3).getAsDouble();
                double ny = verticesArray.get(index * verticesStep + 4).getAsDouble();
                double nz = verticesArray.get(index * verticesStep + 5).getAsDouble();

                Vector2d texture = null;
                if (uvCount > 0) {
                    // Loading the texture coordinates
                    double u = verticesArray.get(index * verticesStep + 6).getAsDouble();
                    double v = verticesArray.get(index * verticesStep + 7).getAsDouble();
                    texture = new Vector2d(u, v);
                }

                mesh.addVertex(new Vertex(
                        new Vector3d(x, y, z),
                        new Vector3d(nx, ny, nz),
                        null,
                        texture
                ));
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
            mesh.worldMatrix.transform(new Point3d(
                    positionArray.get(0).getAsDouble(),
                    positionArray.get(1).getAsDouble(),
                    positionArray.get(2).getAsDouble()
            ));

            // Loading texure
            if (uvCount > 0) {
                // Texture
                String meshTextureID = jsonMesh.get("materialId").getAsString();
                String meshTextureName = materials.get(meshTextureID).diffuseTextureName;
                mesh.texture = new Texture(Mesh.class.getResourceAsStream(String.format("/%s", meshTextureName)), 512, 512);
            }

            mesh.calculateFaceNormals();

            meshes.add(mesh);
        }
        return meshes;
    }

    public Mesh addVertex(Vertex vertex) {
        this.vertices.add(vertex);
        return this;
    }

    public Mesh addFace(int a, int b, int c) {
        return this.addFace(new Face(a, b, c));
    }

    public Mesh addFace(Face face) {
        this.faces.add(face);
        return this;
    }

    public void calculateFaceNormals() {
        for (Face face : this.faces) {
            face.normal = new Vector3d();

            face.normal.add(this.vertices.get(face.a).normal);
            face.normal.add(this.vertices.get(face.b).normal);
            face.normal.add(this.vertices.get(face.c).normal);

            face.normal.scale(1.0 / 3.0);
            face.normal.normalize();
        }
    }

    public String getName() {
        return this.name;
    }

    public List<Vertex> getVertices() {
        return this.vertices;
    }

    public List<Face> getFaces() {
        return this.faces;
    }

    public Texture getTexture() {
        return texture;
    }

    public void setTexture(Texture texture) {
        this.texture = texture;
    }
}
