package org.gearvrf.bondage;

import org.gearvrf.GVRBehavior;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTransform;
import org.gearvrf.IPickEvents;
import org.gearvrf.utility.Log;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.HashMap;

class BondAnimator extends GVRBehavior implements IPickEvents
{
    static private long TYPE_BOND_ANIMATOR = newComponentType(BondAnimator.class);
    static final public float VELOCITY = 0.1f;
    private float           mMinDist;
    private float           mMaxDist;
    private float           mCurDist;
    private String          mElementName;
    private Vector3f        mTargetPos = new Vector3f(0, 0, 0);
    private Vector3f        mCurPos = new Vector3f(0, 0, 0);
    private GVRSceneObject  mClosest = null;
    private GVRSceneObject  mTarget = null;
    private HashMap<String, String> mMoleculeMap;
    private SoundEffect mGoodSound;
    private SoundEffect mBadSound;

    public boolean         WrongAnswer = false;

    BondAnimator(GVRContext ctx, HashMap<String, String> moleculeMap, SoundEffect good, SoundEffect bad)
    {
        super(ctx);
        mMoleculeMap = moleculeMap;
        mGoodSound = good;
        mBadSound = bad;
        mType = TYPE_BOND_ANIMATOR;
    }

    static public long getComponentType() { return TYPE_BOND_ANIMATOR; }

    public void setTarget(GVRSceneObject target)
    {
        mTarget = target;
        mElementName = getElementName(target);
        WrongAnswer = false;
    }

    GVRSceneObject getTarget()
    {
        return mTarget;
    }

    public GVRSceneObject getBondPoint(GVRSceneObject srcObj)
    {
        String name = srcObj.getName();
        int i = name.indexOf("_");
        if (i <= 0)
        {
            return null;
        }
        name = name.substring(0, i);
        String partners = mMoleculeMap.get(name);
        if (partners != null)
        {
            i = partners.indexOf(mElementName);
            if (i >= 0)
            {
                String objName = partners.substring(i);
                int j = objName.indexOf(" ");
                if (j > 0)
                {
                    objName = objName.substring(0, j);
                }
                GVRSceneObject found = getOwnerObject().getSceneObjectByName(objName);
                if (found != null)
                {
                    partners = partners.replace(objName, "").trim();
                    if (partners.equals(""))
                    {
                        mMoleculeMap.remove(name);
                    }
                    else
                    {
                        mMoleculeMap.put(name, partners);
                    }
                    return found;
                }
            }
        }
        return null;
    }

    static public String getElementName(GVRSceneObject srcObj)
    {
        String name = srcObj.getName();
        String elemName = null;

        for (int i = 0; i < name.length(); ++i)
        {
            if ("0123456789".indexOf(name.charAt(i)) >= 0)
            {
                elemName = name.substring(0, i);
                return elemName;
            }
        }
        return null;
    }

    private void makeBond(GVRSceneObject sceneObj)
    {
        GVRSceneObject partner = getBondPoint(sceneObj);
        if (partner != null)
        {
            String name = partner.getName();
            if ((name != null) && name.startsWith(mElementName))
            {
                mTarget.setEnable(false);
                partner.getRenderData().setEnable(true);
                mTarget = null;
                if (mGoodSound != null)
                {
                    mGoodSound.play();
                }
                return;
            }
        }
        if (mBadSound != null)
        {
            mBadSound.play();
        }
        WrongAnswer = true;
        mTarget = null;
    }

    public void onTouch()
    {
        if (isEnabled() && (mTarget != null) && (mClosest != null))
        {
            makeBond(mClosest);
        }
    }

    public void onEnter(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo) { }
    public void onExit(GVRSceneObject sceneObj) { }
    public void onInside(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo) { }
    public void onNoPick(GVRPicker picker) { }

    public void onPick(GVRPicker picker)
    {
        GVRSceneObject owner = getOwnerObject();
        if ((owner == null) || !isEnabled() || (mTarget == null))
        {
            return;
        }
        for (GVRPicker.GVRPickedObject picked : picker.getPicked())
        {
            GVRSceneObject hit = picked.hitObject;
            GVRRenderData rdata = hit.getRenderData();
            if ((rdata != null) && rdata.isEnabled())
            {
                mClosest = hit;
            }
        }
    }
}

