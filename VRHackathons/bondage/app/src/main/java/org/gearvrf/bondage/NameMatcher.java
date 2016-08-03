package org.gearvrf.bondage;

import org.gearvrf.GVRSceneObject;

public class NameMatcher implements GVRSceneObject.SceneVisitor
{
    private String mElementName;
    public GVRSceneObject Match;

    public NameMatcher(String elementName)
    {
        Match = null;
        mElementName = elementName;
    }

    public boolean visit(GVRSceneObject srcObj)
    {
        String name = srcObj.getName();

        if (name.endsWith(".obj"))
        {
            return true;
        }
        if (name.startsWith(mElementName))
        {
            Match = srcObj;
            return false;
        }
        return true;
    }
}
