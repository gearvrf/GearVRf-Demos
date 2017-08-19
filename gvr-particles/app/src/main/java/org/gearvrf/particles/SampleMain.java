package org.gearvrf.particles;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.particlesystem.GVRPlaneEmitter;
import org.gearvrf.particlesystem.GVRSphericalEmitter;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.io.IOException;
import java.util.ArrayList;


public class SampleMain extends GVRMain {

    private GVRContext mGVRContext;

    //particle emitters
    GVRPlaneEmitter fire, smoke, stars;

    //for fireworks, have more than one emitter with different textures
    ArrayList<GVRSphericalEmitter> fwEmitters;
    GVRSceneObject fw;

    private boolean enableFireworks = false;
    private long mElapsedTime = 0;

    private static final float CUBE_WIDTH = 200.0f;

    private int counter = 0;
    private int NUMBER_OF_SYSTEMS = 3;

    @Override
    public void onInit(GVRContext gvrContext) throws IOException {

        mGVRContext = gvrContext;

        GVRScene scene = gvrContext.getMainScene();

        scene.getMainCameraRig().getTransform().setPosition(0,0,0);


        //attach the stars emitter initially
        stars = createstars();
        stars.getTransform().setPosition(0,0,-50);
        stars.getTransform().setRotationByAxis(90, 1,0,0);
        scene.addSceneObject(stars);

        mElapsedTime = System.currentTimeMillis();
    }


    //tap to toggle between systems
    public void onTap()
    {
        counter = (counter + 1) % NUMBER_OF_SYSTEMS;
        switchSystem();
    }

    //clear all systems
    private void clearSystems()
    {
        if ( stars != null )
            stars.clearSystem();

        if( fire != null )
            fire.clearSystem();

        if(smoke != null )
            smoke.clearSystem();

        if (fwEmitters != null ) {
            for (GVRSphericalEmitter em : fwEmitters)
                if (em != null)
                    em.clearSystem();
            fwEmitters = null;
        }
    }

    private void switchSystem()
    {
        clearSystems();
        mGVRContext.getMainScene().removeAllSceneObjects();

        if ( counter == 2 )
        {
            fire = createFire();
            smoke = createSmoke();
            fire.getTransform().setPosition(0,-3.5f,-9);
            smoke.getTransform().setPosition(0,-3.0f,-9.0f);

            mGVRContext.getMainScene().addSceneObject(fire);
            mGVRContext.getMainScene().addSceneObject(smoke);
            enableFireworks = false;
        }

        else if ( counter == 1)
        {
            fw = createFireworks();
            fw.getTransform().setPosition(0, 10, -20.0f);
            mGVRContext.getMainScene().addSceneObject(fw);
            enableFireworks = true;
        }

        else if (counter == 0 )
        {
            stars = createstars();
            stars.getTransform().setPosition(0,0,-50);
            stars.getTransform().setRotationByAxis(90, 1,0,0);

            mGVRContext.getMainScene().addSceneObject(stars);
            enableFireworks = false;
        }
    }

    @Override
    public void onStep()
    {
        //loop over if the fireworks are enabled
        if (enableFireworks)
        {
            long currTime = System.currentTimeMillis();

            if ( currTime - mElapsedTime > 6000 ) {
                mGVRContext.getMainScene().removeAllSceneObjects();
                clearSystems();
                fw = null;
                fw = createFireworks();
                fw.getTransform().setPosition(0,10, -20);
                mGVRContext.getMainScene().addSceneObject(fw);
                mElapsedTime = currTime;
            }
        }
    }

    //---------------------------------------STARS-----------------------------------
    private GVRPlaneEmitter createstars()
    {
        GVRTexture starsTexture = mGVRContext.getAssetLoader().loadTexture(
                new GVRAndroidResource(mGVRContext, R.drawable.stars));

        GVRPlaneEmitter starsEmitter = new GVRPlaneEmitter(mGVRContext);

        starsEmitter.setPlaneWidth(100);
        starsEmitter.setPlaneHeight(100);
        starsEmitter.setParticleSize(5.0f);
        starsEmitter.setVelocityRange(new Vector3f(0,2,0), new Vector3f(0,15.5f,0));
        starsEmitter.setEmitRate(300);
        starsEmitter.setFadeWithAge(false);
        starsEmitter.setEnvironmentAcceleration(new Vector3f(0,0,0));
        starsEmitter.setParticleVolume(50.0f, 100.0f, 50.0f);
        starsEmitter.setParticleAge(10);
        starsEmitter.setParticleTexture(starsTexture);

        return starsEmitter;
    }


    //-----------------------------------------SMOKE-----------------------------------
    private  GVRPlaneEmitter createSmoke()
    {
        GVRTexture smokeTexture = mGVRContext.getAssetLoader().loadTexture(
                new GVRAndroidResource(mGVRContext, R.drawable.smoke));

        GVRPlaneEmitter smokeEmitter = new GVRPlaneEmitter(mGVRContext);

        smokeEmitter.setPlaneWidth(1.5f);
        smokeEmitter.setPlaneHeight(1.5f);
        smokeEmitter.setParticleSize(80.0f);
        smokeEmitter.setVelocityRange(new Vector3f(0,2.0f,0), new Vector3f(0,5.0f,0));
        smokeEmitter.setEmitRate(100);
        smokeEmitter.setFadeWithAge(true);
        smokeEmitter.setEnvironmentAcceleration(new Vector3f(0,0,0));
        smokeEmitter.setParticleVolume(10.0f, 20.0f, 10.0f);
        smokeEmitter.setParticleAge(1.5f);
        smokeEmitter.setParticleTexture(smokeTexture);
        smokeEmitter.setParticleSizeChangeRate(6.0f);
        smokeEmitter.setColorMultiplier(new Vector4f(1.0f, 1.0f, 1.0f, 0.06f));

        return smokeEmitter;
    }


    //----------------------------------------------FIRE-----------------------------------
    private GVRPlaneEmitter createFire()
    {
        GVRPlaneEmitter fireEmitter = new GVRPlaneEmitter(mGVRContext);
        GVRTexture texture = mGVRContext.getAssetLoader().loadTexture(
                new GVRAndroidResource(mGVRContext, R.drawable.fire));

        fireEmitter.setEmitRate(250);
        fireEmitter.setBurstMode(false);
        fireEmitter.setPlaneWidth(1.0f);
        fireEmitter.setPlaneHeight(1.0f);
        fireEmitter.setParticleAge(0.7f);
        fireEmitter.setVelocityRange(new Vector3f(0,1.0f,0), new Vector3f(0,4.0f,0));
        fireEmitter.setEnvironmentAcceleration(new Vector3f(0,0.0f,0));
        fireEmitter.setParticleVolume(50.0f, 100.0f, 50.0f);
        fireEmitter.setParticleSizeChangeRate(-6.0f);
        fireEmitter.setFadeWithAge(true);
        fireEmitter.setParticleSize(60.0f);
        fireEmitter.setParticleTexture(texture);
        fireEmitter.setNoiseFactor(0.07f);

        return fireEmitter;
    }


    //---------------------------------------------FIREWORKS----------------------------------
    private GVRSceneObject createFireworks()
    {
        ArrayList<GVRSphericalEmitter> fwEmits = createFireworkEmitters();
        GVRSceneObject fw = new GVRSceneObject(mGVRContext);
        for ( GVRSphericalEmitter emitter : fwEmits )
            fw.addChildObject(emitter);

        return fw;
    }


    private ArrayList<GVRSphericalEmitter> createFireworkEmitters()
    {

        GVRTexture fireworksTexture1 = mGVRContext.getAssetLoader().loadTexture(
                new GVRAndroidResource(mGVRContext, R.drawable.stars));

        GVRTexture fireworksTexture2 = mGVRContext.getAssetLoader().loadTexture(
                new GVRAndroidResource(mGVRContext, R.drawable.fire));

        GVRTexture fireworksTexture3 = mGVRContext.getAssetLoader().loadTexture(
                new GVRAndroidResource(mGVRContext, R.drawable.fworks));

        GVRTexture fireworksTexture4 = mGVRContext.getAssetLoader().loadTexture(
                new GVRAndroidResource(mGVRContext, R.drawable.smoke));


        GVRSphericalEmitter fworksEmitter1 = new GVRSphericalEmitter(mGVRContext);
        GVRSphericalEmitter fworksEmitter2 = new GVRSphericalEmitter(mGVRContext);
        GVRSphericalEmitter fworksEmitter3 = new GVRSphericalEmitter(mGVRContext);
        GVRSphericalEmitter fworksEmitter4 = new GVRSphericalEmitter(mGVRContext);

        fworksEmitter1.setRadius(0.1f);
        fworksEmitter1.setFadeWithAge(true);
        fworksEmitter1.setParticleSize(10.0f);
        fworksEmitter1.setEmitRate(100);
        fworksEmitter1.setParticleAge(6.0f);
        fworksEmitter1.setBurstMode(true);
        fworksEmitter1.setVelocityRange(new Vector3f(0.1f,0.5f,0.3f), new Vector3f(1.5f,2.0f,3.0f));
        fworksEmitter1.setEnvironmentAcceleration(new Vector3f(0,-2.0f,0));
        fworksEmitter1.setParticleTexture(fireworksTexture1);

        fworksEmitter2.setRadius(0.1f);
        fworksEmitter2.setFadeWithAge(true);
        fworksEmitter2.setParticleSize(15.0f);
        fworksEmitter2.setEmitRate(150);
        fworksEmitter2.setParticleAge(6f);
        fworksEmitter2.setBurstMode(true);
        fworksEmitter2.setVelocityRange(new Vector3f(1.0f,1.0f,1.0f), new Vector3f(2.0f,2.0f,2.0f));
        fworksEmitter2.setEnvironmentAcceleration(new Vector3f(0,-2.0f,0));
        fworksEmitter2.setParticleTexture(fireworksTexture2);

        fworksEmitter3.setRadius(0.1f);
        fworksEmitter3.setFadeWithAge(true);
        fworksEmitter3.setParticleSize(12.0f);
        fworksEmitter3.setEmitRate(100);
        fworksEmitter3.setParticleAge(6.0f);
        fworksEmitter3.setBurstMode(true);
        fworksEmitter3.setEnvironmentAcceleration(new Vector3f(0,-2.0f,0));
        fworksEmitter3.setVelocityRange(new Vector3f(1.5f,0.7f,1.0f), new Vector3f(1.5f,1.8f,2.5f));
        fworksEmitter3.setParticleTexture(fireworksTexture3);

        fworksEmitter4.setRadius(0.1f);
        fworksEmitter4.setFadeWithAge(true);
        fworksEmitter4.setParticleSize(11.0f);
        fworksEmitter4.setEmitRate(100);
        fworksEmitter4.setParticleAge(6);
        fworksEmitter4.setBurstMode(true);
        fworksEmitter4.setVelocityRange(new Vector3f(0.1f,1.0f,0.3f), new Vector3f(1.3f,1.3f,4.0f));
        fworksEmitter4.setEnvironmentAcceleration(new Vector3f(0,-2.0f,0));
        fworksEmitter4.setParticleTexture(fireworksTexture4);


        fworksEmitter1.setParticleVolume(100, 100, 100);
        fworksEmitter2.setParticleVolume(100, 100, 100);
        fworksEmitter3.setParticleVolume(100, 100, 100);
        fworksEmitter4.setParticleVolume(100, 100, 100);

        fwEmitters = new ArrayList<GVRSphericalEmitter>();

        fwEmitters.add(fworksEmitter1);
        fwEmitters.add(fworksEmitter2);
        fwEmitters.add(fworksEmitter3);
        fwEmitters.add(fworksEmitter4);

        return fwEmitters;
    }
}
