package pl.jacadev.engine.graphics;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;
import pl.jacadev.engine.graphics.textures.MultisampleTexture2D;
import pl.jacadev.engine.graphics.utility.BufferTools;

import static org.lwjgl.opengl.GL30.*;

/**
 * @author Jaca777
 *         Created 07.04.15 at 19:55
 */
public class MultisampleFramebuffer extends Framebuffer {

    public MultisampleFramebuffer(MultisampleTexture2D destTex) {
        super(destTex, GL30.glGenFramebuffers(), GL30.glGenRenderbuffers());
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, this.name);
        glBindRenderbuffer(GL_RENDERBUFFER, this.depthBuff);
        GL30.glRenderbufferStorageMultisample(GL_RENDERBUFFER, destTex.getSamples(), GL_DEPTH_COMPONENT32F, destTex.getWidth(), destTex.getHeight());

        GL20.glDrawBuffers(BufferTools.toDirectBuffer(new int[]{GL_COLOR_ATTACHMENT0}));
        GL11.glBindTexture(GL32.GL_TEXTURE_2D_MULTISAMPLE, destTex.getTexture());
        GL30.glFramebufferTexture2D(GL_DRAW_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL32.GL_TEXTURE_2D_MULTISAMPLE, this.destTex.getTexture(), 0);
        GL30.glFramebufferRenderbuffer(GL_DRAW_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, this.depthBuff);

        int status = glCheckFramebufferStatus(GL_DRAW_FRAMEBUFFER);
        if(status != GL30.GL_FRAMEBUFFER_COMPLETE) throw new RuntimeException("Incomplete framebuffer: " + status);
    }

    @Override
    public void resize(int w, int h) {
        glBindRenderbuffer(GL_RENDERBUFFER, this.depthBuff);
        GL30.glRenderbufferStorageMultisample(GL_RENDERBUFFER, ((MultisampleTexture2D) destTex).getSamples(), GL_DEPTH_COMPONENT32F, w, h);
    }
}
