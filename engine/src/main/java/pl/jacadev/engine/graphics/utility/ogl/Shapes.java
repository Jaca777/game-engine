package pl.jacadev.engine.graphics.utility.ogl;

import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import pl.jacadev.engine.graphics.utility.BufferTools;

import static org.lwjgl.opengl.GL11.GL_FLOAT;

/**
 * @author Jaca777
 *         Created 2015-06-25 at 14
 */
public class Shapes {

    private static final float[] QUAD_VERTICES = new float[]{
            -1.0f, -1.0f, 0.0f, 1.0f,
            1.0f, -1.0f, 0.0f, 1.0f,
            1.0f, 1.0f, 0.0f, 1.0f,
            -1.0f, 1.0f, 0.0f, 1.0f
    };

    private static final float[] QUAD_TEX_COORDS = new float[]{
            0.0f, 0.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 1.0f
    };

    private static final int[] QUAD_INDICES = new int[]{
            0, 1, 2, 0, 2, 3
    };

    public static final int QUAD_INDICES_AMOUNT = QUAD_INDICES.length;

    public static int mkFullRect(int vertexAttr, int texCoordAttr) {
        int screenrectVAO = GL30.glGenVertexArrays();

        int vertBuffer = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertBuffer);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, BufferTools.toDirectBuffer(QUAD_VERTICES), GL15.GL_STATIC_DRAW);

        int texCoordBuffer = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, texCoordBuffer);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, BufferTools.toDirectBuffer(QUAD_TEX_COORDS), GL15.GL_STATIC_DRAW);

        int indexBuffer = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, indexBuffer);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, BufferTools.toDirectBuffer(QUAD_INDICES), GL15.GL_STATIC_DRAW);

        GL30.glBindVertexArray(screenrectVAO);
        GL20.glEnableVertexAttribArray(vertexAttr);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertBuffer);
        GL20.glVertexAttribPointer(vertexAttr, 4, GL_FLOAT, false, 0, 0);

        GL20.glEnableVertexAttribArray(texCoordAttr);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, texCoordBuffer);
        GL20.glVertexAttribPointer(texCoordAttr, 2, GL_FLOAT, false, 0, 0);

        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, indexBuffer);
        GL30.glBindVertexArray(0);
        return screenrectVAO;
    }
}
