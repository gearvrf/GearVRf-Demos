function onInit(gvrf)
  local mainScene = gvrf:getNextMainScene()

  -- 3D model
  local model = gvrf:loadModel("astro_boy.dae")
  model:getTransform():setRotationByAxis(45.0, 0.0, 1.0, 0.0)
  model:getTransform():setScale(3, 3, 3)
  model:getTransform():setPosition(0.0, -0.4, -0.5)

  mainScene:addSceneObject(model)

  -- Text
  local textView = utils:newTextViewSceneObject(gvrf, "GVRf scripting in Lua")
  textView:setTextSize(6)
  textView:getTransform():setPosition(0, 0, -2)

  mainScene:addSceneObject(textView)

  -- Animation
  local animationEngine = gvrf:getAnimationEngine()
  local animations = utils:getAnimations(model)
  if #animations ~= 0 then
      animations[1]:setRepeatMode(1):setRepeatCount(-1)
      animations[1]:start(animationEngine)
  end
end

--[[
function onStep()
end
--]]

