package org.gearvrf.util;


import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRSceneObject;
import org.joml.Vector3f;

public class BoundingBoxCreator {
    private GVRMesh mesh;

    public BoundingBoxCreator(GVRContext gvrContext, GVRSceneObject.BoundingVolume bv){
        mesh = new GVRMesh(gvrContext);

        Vector3f min_corner = bv.minCorner;
        Vector3f max_corner = bv.maxCorner;

        float min_x = min_corner.x;
        float min_y = min_corner.y;
        float min_z = min_corner.z;
        float max_x = max_corner.x;
        float max_y = max_corner.y;
        float max_z = max_corner.z;

        float vertices[] = {
                min_x, min_y, min_z,
                max_x, min_y, min_z,
                min_x, max_y, min_z,
                max_x, max_y, min_z,

                min_x, min_y, max_z,
                max_x, min_y, max_z,
                min_x, max_y, max_z,
                max_x, max_y, max_z
        };

        mesh.setVertices(vertices);

        final float[] normals = {
                0.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f
        };

        mesh.setNormals(normals);

        char indices[] = {
                0, 2, 1,
                1, 2, 3,
                1, 3, 7,
                1, 7, 5,
                4, 5, 6,
                5, 7, 6,
                0, 6, 2,
                0, 4, 6,
                0, 1, 5,
                0, 5, 4,
                2, 7, 3,
                2, 6, 7
        };

        mesh.setIndices(indices);

        final float[] textureCoords = {
                0.0f, 0.0f,
                0.0f, 1.0f,
                1.0f, 0.0f,
                1.0f, 1.0f
        };

        mesh.setTexCoords(textureCoords);
    }

    public GVRMesh getMesh(){
        return mesh;
    }
}