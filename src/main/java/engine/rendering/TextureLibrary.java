package engine.rendering;

import com.jogamp.opengl.*;
import com.jogamp.opengl.util.texture.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/* Lab Code*/
/* Author Dr Steve Maddock
*/
/**
 * Loads and caches textures for reuse across materials and meshes.
 */
public class TextureLibrary {
    private static final Map<String, Texture> cache = new HashMap<>();

    private TextureLibrary() {} // static-only

    public static Texture LoadTexture(GL3 gl, String path) {
        if (path == null || path.isEmpty()) return null;
        if (cache.containsKey(path)) return cache.get(path);

        try {
            File f = new File(path);
            Texture tex = TextureIO.newTexture(f, true);
            tex.bind(gl);
            tex.setTexParameteri(gl, GL3.GL_TEXTURE_WRAP_S, GL3.GL_CLAMP_TO_EDGE);
            tex.setTexParameteri(gl, GL3.GL_TEXTURE_WRAP_T, GL3.GL_CLAMP_TO_EDGE);
            float[] maxAniso = new float[1];
            gl.glGetFloatv(GL.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, maxAniso, 0);
            gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAX_ANISOTROPY_EXT, maxAniso[0]);
            tex.setTexParameteri(gl, GL3.GL_TEXTURE_MIN_FILTER, GL3.GL_LINEAR_MIPMAP_LINEAR);
            tex.setTexParameteri(gl, GL3.GL_TEXTURE_MAG_FILTER, GL3.GL_LINEAR);
            gl.glGenerateMipmap(GL3.GL_TEXTURE_2D);
            cache.put(path, tex);
            return tex;
        } catch (Exception e) {
            System.err.println("Failed to load texture: " + path + " (" + e.getMessage() + ")");
            return null;
        }
    }

    public static void destroy(GL3 gl) {
        for (Texture t : cache.values()) {
            t.destroy(gl);
        }
        cache.clear();
    }
}
