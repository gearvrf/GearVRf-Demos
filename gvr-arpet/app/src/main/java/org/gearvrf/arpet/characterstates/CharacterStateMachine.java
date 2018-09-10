package org.gearvrf.arpet.characterstates;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRDrawFrameListener;

import java.util.HashMap;
import java.util.Map;

public class CharacterStateMachine implements IStateMachine {
    private final GVRContext mContext;
    private final Map<Integer, IState> mStates;
    private GVRDrawFrameListener mDrawFrameHandler;
    private IState mCurrentState;

    public CharacterStateMachine(GVRContext context) {
        mContext = context;
        mStates = new HashMap<>();

        mDrawFrameHandler = null;
        mCurrentState = null;
    }

    public void start() {
        if (mDrawFrameHandler == null) {
            mDrawFrameHandler = new DrawFrameHandler();
            mContext.registerDrawFrameListener(mDrawFrameHandler);
        }
    }

    public void stop() {
        if (mDrawFrameHandler != null) {
            mContext.unregisterDrawFrameListener(mDrawFrameHandler);
            mDrawFrameHandler = null;
        }
    }

    public void addState(IState state) {
        mStates.put(state.id(), state);
    }

    public void setCurrentState(int state) {
        mCurrentState = mStates.get(state);
    }

    private class DrawFrameHandler implements GVRDrawFrameListener {
        IState state = null;
        @Override
        public void onDrawFrame(float v) {
            if (mCurrentState != state) {
                if (state != null) {
                    state.exit();
                }
                state = mCurrentState;
                state.entry();
            } else if (state != null) {
                state.run();
            }
        }
    }


}
