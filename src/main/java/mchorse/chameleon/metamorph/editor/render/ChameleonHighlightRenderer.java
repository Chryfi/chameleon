package mchorse.chameleon.metamorph.editor.render;

import mchorse.chameleon.lib.data.model.ModelBone;
import mchorse.chameleon.lib.data.model.ModelCube;
import mchorse.chameleon.lib.data.model.ModelQuad;
import mchorse.chameleon.lib.data.model.ModelVertex;
import mchorse.chameleon.lib.render.ChameleonPostRenderer;
import mchorse.chameleon.lib.render.IChameleonRenderProcessor;
import mchorse.chameleon.lib.utils.MatrixStack;
import mchorse.mclib.client.Draw;
import mchorse.mclib.client.gui.framework.elements.input.GuiTransformations;
import mchorse.mclib.utils.MatrixUtils;
import mchorse.mclib.utils.RenderingUtils;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector4f;
import java.nio.FloatBuffer;

/**
 * Highlight renderer
 *
 * This bad boy is responsible for rendering given model's bone
 * as a blue box highlight (in addition with axes identifiers)
 */
@SideOnly(Side.CLIENT)
public class ChameleonHighlightRenderer implements IChameleonRenderProcessor
{
    private String boneName;
    private Vector4f vertex = new Vector4f();

    public void setBoneName(String boneName)
    {
        this.boneName = boneName;
    }

    @Override
    public boolean renderBone(BufferBuilder builder, MatrixStack stack, ModelBone bone)
    {
        if (bone.id.equals(this.boneName))
        {
            builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);

            for (ModelCube cube : bone.cubes)
            {
                this.renderCubeForHighlight(builder, stack, cube);
            }

            Tessellator.getInstance().draw();

            GlStateManager.pushMatrix();
            ChameleonPostRenderer.multiplyMatrix(stack, bone);

            if (GuiTransformations.GuiStaticTransformOrientation.getOrientation() == GuiTransformations.TransformOrientation.GLOBAL)
            {
                Vector3d rotation = new Vector3d(bone.current.rotation);
                rotation.x = Math.toRadians(rotation.x);
                rotation.y = Math.toRadians(rotation.y);
                rotation.z = Math.toRadians(rotation.z);

                RenderingUtils.glRevertRotationScale(rotation, new Vector3d(bone.current.scale), MatrixUtils.RotationOrder.XYZ);
            }

            Draw.axis(0.25F * 1.5F);

            GlStateManager.popMatrix();

            return true;
        }

        return false;
    }

    private void renderCubeForHighlight(BufferBuilder builder, MatrixStack stack, ModelCube cube)
    {
        stack.push();
        stack.moveToCubePivot(cube);
        stack.rotateCube(cube);
        stack.moveBackFromCubePivot(cube);

        for (ModelQuad quad : cube.quads)
        {
            for (ModelVertex vertex : quad.vertices)
            {
                this.vertex.set(vertex.position);
                this.vertex.w = 1;
                stack.getModelMatrix().transform(this.vertex);

                builder.pos(this.vertex.getX(), this.vertex.getY(), this.vertex.getZ()).endVertex();
            }
        }

        stack.pop();
    }
}