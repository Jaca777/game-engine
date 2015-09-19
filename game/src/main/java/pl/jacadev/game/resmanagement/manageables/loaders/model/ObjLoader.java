package pl.jacadev.game.resmanagement.manageables.loaders.model;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import pl.jacadev.game.resmanagement.manageables.containers.ModelDataContainer;
import pl.jacadev.engine.graphics.models.Model;
import pl.jacadev.engine.graphics.models.VAOModel;
import pl.jacadev.engine.graphics.utility.BufferTools;
import pl.jacadev.engine.graphics.utility.math.Math3D;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Jaca777
 *         Created 22.12.14 at 15:22
 */
public class ObjLoader {

    private static class Vertex {
        Vector3f pos;
        Vector2f texCoord;
        Vector3f normal;
        private int normalWeight = 1;

        private Vertex(Vector3f pos, Vector2f texCoord, Vector3f normal) {
            this.pos = pos;
            this.texCoord = texCoord;
            this.normal = normal;
        }

        /**
         * @param texCoord Texture coordinate of vertex being mixed.
         * @param normal   Normal of vertex being mixed.
         * @return whether the mixture is possible (both have the same texCoords)
         */
        private boolean mix(Vector2f texCoord, Vector3f normal) {
            if (this.texCoord.x != texCoord.x || this.texCoord.y != texCoord.y) {
                return false;
            } else {
                if (this.normal.x != normal.x || this.normal.y != normal.y || this.normal.z != normal.z) {
                    Math3D.sMul(this.normal, normalWeight / (float) (normalWeight + 1));
                    Math3D.sDiv(normal, normalWeight + 1);
                    Math3D.sSum(this.normal, normal);
                    this.normal.normalise(this.normal);
                }
                return true;
            }
        }
    }

    private Vertex[] vertices;
    private List<Vertex> hVertices;
    private List<Integer> indices;
    private float topPoint = -1;
    private float botPoint = -1;

    private static final String VERTEX = "v";
    private static final String TEXTURE = "vt";
    private static final String NORMAL = "vn";
    private static final String FACE = "f";

    public static ObjLoader read(InputStream objFile) {

        ObjLoader obj = new ObjLoader();
        List<Vector3f> vertices = new ArrayList<>();
        List<Vector3f> normals = new ArrayList<>();
        List<Vector2f> texCoords = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(objFile));

        try {
            String line = null;
            boolean facesReached = false;
            while (!facesReached && (line = reader.readLine()) != null) {
                String[] data = line.split(" ");
                switch (data[0]) {
                    case VERTEX:
                        vertices.add(new Vector3f(Float.parseFloat(data[1]), Float.parseFloat(data[2]), Float.parseFloat(data[3])));
                        break;
                    case TEXTURE:
                        texCoords.add(new Vector2f(Float.parseFloat(data[1]), Float.parseFloat(data[2])));
                        break;
                    case NORMAL:
                        normals.add(new Vector3f(Float.parseFloat(data[1]), Float.parseFloat(data[2]), Float.parseFloat(data[3])));
                        break;
                    case FACE:
                        facesReached = true;
                        break;
                }
            }

            obj.setupArrays(vertices.size());


            while (line != null) {
                if (!line.startsWith("f")) {
                    line = reader.readLine();
                    continue;
                }
                String[] faceData = line.split(" ");
                obj.processVertex(faceData[1].split("/"), vertices, normals, texCoords);
                obj.processVertex(faceData[2].split("/"), vertices, normals, texCoords);
                obj.processVertex(faceData[3].split("/"), vertices, normals, texCoords);
                line = reader.readLine();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return obj;
    }

    private void setupArrays(int size) {
        this.hVertices = new ArrayList<>();
        this.vertices = new Vertex[size];
        this.indices = new ArrayList<>(size);
    }


    public void processVertex(String[] vertexData, List<Vector3f> verticesList, List<Vector3f> normalsList, List<Vector2f> texCoordsList) {
        int pointer = Integer.parseInt(vertexData[0]) - 1;
        Vector3f pos = verticesList.get(pointer);
        Vector2f texCoord = texCoordsList.get(Integer.parseInt(vertexData[1]) - 1);
        Vector3f normal = normalsList.get(Integer.parseInt(vertexData[2]) - 1);
        if (this.vertices[pointer] != null) {
            Vertex v = this.vertices[pointer];
            if (v.mix(texCoord, normal)) {
                this.indices.add(pointer);
            } else {
                Vertex vertex = new Vertex(pos, texCoord, normal);
                this.hVertices.add(vertex);
                this.indices.add(this.vertices.length + hVertices.size() - 1);
            }
        } else {
            this.vertices[pointer] = new Vertex(pos, texCoord, normal);
            this.indices.add(pointer);
        }
    }

    public Model toModel(int[] attributes) {
        int size = this.vertices.length + hVertices.size();
        float[] v = new float[size * 4];
        float[] t = new float[size * 2];
        float[] n = new float[size * 3];
        int[] indices = new int[this.indices.size()];
        int i = 0;
        for (; i < this.vertices.length; i++)
            if (this.vertices[i] != null) {
                Vector3f pos = this.vertices[i].pos;
                Vector2f texCoord = this.vertices[i].texCoord;
                Vector3f normal = this.vertices[i].normal;
                v[i * 4] = pos.x;
                v[i * 4 + 1] = pos.y;
                v[i * 4 + 2] = pos.z;
                v[i * 4 + 3] = 1f;
                t[i * 2] = texCoord.x;
                t[i * 2 + 1] = 1 - texCoord.y;
                n[i * 3] = normal.x;
                n[i * 3 + 1] = normal.y;
                n[i * 3 + 2] = normal.z;
            }

        for (Vertex vertex : this.hVertices) {
            Vector3f pos = vertex.pos;
            Vector2f texCoord = vertex.texCoord;
            Vector3f normal = vertex.normal;
            v[i * 4] = pos.x;
            v[i * 4 + 1] = pos.y;
            v[i * 4 + 2] = pos.z;
            v[i * 4 + 3] = 1f;
            t[i * 2] = texCoord.x;
            t[i * 2 + 1] = 1 - texCoord.y;
            n[i * 3] = normal.x;
            n[i * 3 + 1] = normal.y;
            n[i * 3 + 2] = normal.z;
            i++;
        }
        for (i = 0; i < this.indices.size(); i++) {
            indices[i] = this.indices.get(i);
        }
        return new Model(v, t, n, indices, attributes);
    }

    public VAOModel toVAOModel(int[] attributes) {
        int size = this.vertices.length + hVertices.size();
        float[] v = new float[size * 4];
        float[] t = new float[size * 2];
        float[] n = new float[size * 3];
        int[] indices = new int[this.indices.size()];
        int i = 0;
        for (; i < this.vertices.length; i++)
            if (this.vertices[i] != null) {
                Vector3f pos = this.vertices[i].pos;
                Vector2f texCoord = this.vertices[i].texCoord;
                Vector3f normal = this.vertices[i].normal;
                v[i * 4] = pos.x;
                v[i * 4 + 1] = pos.y;
                v[i * 4 + 2] = pos.z;
                v[i * 4 + 3] = 1f;
                t[i * 2] = texCoord.x;
                t[i * 2 + 1] = 1 - texCoord.y;
                n[i * 3] = normal.x;
                n[i * 3 + 1] = normal.y;
                n[i * 3 + 2] = normal.z;
            }

        for (Vertex vertex : this.hVertices) {
            Vector3f pos = vertex.pos;
            Vector2f texCoord = vertex.texCoord;
            Vector3f normal = vertex.normal;
            v[i * 4] = pos.x;
            v[i * 4 + 1] = pos.y;
            v[i * 4 + 2] = pos.z;
            v[i * 4 + 3] = 1f;
            t[i * 2] = texCoord.x;
            t[i * 2 + 1] = 1 - texCoord.y;
            n[i * 3] = normal.x;
            n[i * 3 + 1] = normal.y;
            n[i * 3 + 2] = normal.z;
            i++;
        }
        for (i = 0; i < this.indices.size(); i++) {
            indices[i] = this.indices.get(i);
        }
        return new VAOModel(v, t, n, indices, attributes);
    }

    public void store(ModelDataContainer dataContainer){
        int size = this.vertices.length + hVertices.size();
        float[] v = new float[size * 4];
        float[] t = new float[size * 2];
        float[] n = new float[size * 3];
        int[] indices = new int[this.indices.size()];
        int i = 0;
        for (; i < this.vertices.length; i++)
            if (this.vertices[i] != null) {
                Vector3f pos = this.vertices[i].pos;
                Vector2f texCoord = this.vertices[i].texCoord;
                Vector3f normal = this.vertices[i].normal;
                v[i * 4] = pos.x;
                v[i * 4 + 1] = pos.y;
                v[i * 4 + 2] = pos.z;
                v[i * 4 + 3] = 1f;
                t[i * 2] = texCoord.x;
                t[i * 2 + 1] = 1 - texCoord.y;
                n[i * 3] = normal.x;
                n[i * 3 + 1] = normal.y;
                n[i * 3 + 2] = normal.z;
            }

        for (Vertex vertex : this.hVertices) {
            Vector3f pos = vertex.pos;
            Vector2f texCoord = vertex.texCoord;
            Vector3f normal = vertex.normal;
            v[i * 4] = pos.x;
            v[i * 4 + 1] = pos.y;
            v[i * 4 + 2] = pos.z;
            v[i * 4 + 3] = 1f;
            t[i * 2] = texCoord.x;
            t[i * 2 + 1] = 1 - texCoord.y;
            n[i * 3] = normal.x;
            n[i * 3 + 1] = normal.y;
            n[i * 3 + 2] = normal.z;
            i++;
        }
        for (i = 0; i < this.indices.size(); i++) {
            indices[i] = this.indices.get(i);
        }


        dataContainer.load(
                BufferTools.toDirectBuffer(v),
                BufferTools.toDirectBuffer(t),
                BufferTools.toDirectBuffer(n),
                BufferTools.toDirectBuffer(indices),
                indices.length
        );
    }

}
