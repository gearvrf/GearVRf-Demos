package org.gearvrf.bondage;


import org.gearvrf.GVRBehavior;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRPhongShader;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRSphereCollider;
import org.gearvrf.GVRTexture;
import org.gearvrf.GVRTransform;
import org.gearvrf.scene_objects.GVRSphereSceneObject;
import org.gearvrf.utility.Log;

public class ElementGrid extends GVRBehavior implements GVRSceneObject.SceneVisitor
{
    static private long TYPE_ELEMENT_GRID = newComponentType(ElementGrid.class);
    static private final float ELEMENT_SCALE = 0.5f;

    public ElementGrid(GVRContext ctx)
    {
        super(ctx);
        mType = TYPE_ELEMENT_GRID;
        mGridPositions = new float[][]
        {
                { -2.0f, 2.0f, 0.0f },
                { 0.0f, 2.0f, 0.0f },
                { 2.0f, 2.0f, 0.0f },
                { -2.0f, 0.0f, 0.0f },
                { 0.0f, 0.0f, 0.0f },
                { 2.0f, 0.0f, 0.0f },
                { -2.0f, -2.0f, 0.0f },
                { 0.0f, -2.0f, 0.0f },
                { 2.0f, -2.0f, 0.0f },
        };
    }

    public void onAttach(GVRSceneObject owner)
    {
        if (owner.getChildrenCount() == 0)
        {
            for (float[] pos : mGridPositions)
            {
                GVRSceneObject element = new GVRSceneObject(owner.getGVRContext());
                element.getTransform().setPosition(pos[0], pos[1], pos[2]);
                owner.addChildObject(element);
            }
        }
    }

    private GVRSceneObject findEmptyGridSlot()
    {
        for (GVRSceneObject child : owner.children())
        {
            if (child.getChildrenCount() == 0)
            {
                return child;
            }
        }
        return null;
    }

    static public long getComponentType() { return TYPE_ELEMENT_GRID; }

    private float[][] mGridPositions;
    private int mGridIndex = 0;

    public void makeGrid(GVRSceneObject srcRoot)
    {
        srcRoot.forAllDescendants(this);
    }

    public void addToGrid(GVRSceneObject newElem)
    {
        GVRSceneObject gridParent = findEmptyGridSlot();
        if (gridParent != null)
        {
            GVRTransform trans = newElem.getTransform();
            trans.setPosition(0, 0, 0);
            trans.setScale(ELEMENT_SCALE, ELEMENT_SCALE, ELEMENT_SCALE);
            gridParent.addChildObject(newElem);
        }
    }

    public boolean visit(GVRSceneObject srcObj)
    {
        GVRRenderData srcRender = srcObj.getRenderData();
        GVRSceneObject owner = getOwnerObject();
        GVRSceneObject dstRoot = findEmptyGridSlot();

        if ((srcRender != null) && (dstRoot != null))
        {
            GVRContext ctx = srcObj.getGVRContext();
            GVRMaterial srcMtl = srcRender.getMaterial();
            GVRMesh srcMesh = srcRender.getMesh();

            if ((srcMtl == null) || (srcMesh == null))
            {
                return true;
            }
            GVRSceneObject dstObj = new GVRSphereSceneObject(ctx);
            GVRRenderData dstRender = new GVRRenderData(ctx);
            GVRTransform dstTrans = dstObj.getTransform();
            GVRSphereCollider collider = new GVRSphereCollider(ctx);
            GVRMaterial dstMtl = new GVRMaterial(ctx);
            String name = srcObj.getName();
            GVRTexture tex = srcMtl.getTexture("diffuseTexture");
            float[]     temp;

            if (tex != null)
            {
                dstMtl.setTexture("diffuseTexture", tex);
            }
            dstMtl.setSpecularColor(1.0f, 1.0f, 1.0f, 1.0f);
            dstMtl.setSpecularExponent(10.0f);
            srcMtl.setSpecularExponent(10.0f);
            temp = srcMtl.getAmbientColor();
            dstMtl.setAmbientColor(temp[0], temp[1], temp[2], temp[3]);
            temp = srcMtl.getDiffuseColor();
            dstMtl.setDiffuseColor(temp[0], temp[1], temp[2], temp[3]);
            temp = srcMtl.getSpecularColor();
            dstMtl.setSpecularColor(temp[0], temp[1], temp[2], temp[3]);
            dstObj.setName(name);
            dstTrans.setScale(ELEMENT_SCALE, ELEMENT_SCALE, ELEMENT_SCALE);

            dstRender.setMaterial(dstMtl);
            dstRender.setMesh(dstObj.getRenderData().getMesh());
            dstRender.setShaderTemplate(GVRPhongShader.class);
            dstObj.detachComponent(GVRRenderData.getComponentType());
            dstObj.attachComponent(dstRender);
            dstObj.attachComponent(collider);
            collider = new GVRSphereCollider(ctx);
            srcObj.attachComponent(collider);
            dstRoot.addChildObject(dstObj);
            owner.addChildObject(dstRoot);
            srcRender.setEnable(false);
        }
        return true;
    }
}