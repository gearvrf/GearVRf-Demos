#version 400

// Uniform variables
uniform float GridSegments;
uniform float GridSoftness;
uniform float LineThickness;
uniform float GlowSize;
uniform float GlowStrength;
uniform vec4 Color;
uniform vec4 GlowColor;
uniform float Visibility;

// Input variables
in vec2 FragTexcoord;

// Output variables
out vec4 OutputColor;

void main(void)
{
  vec2 UV = FragTexcoord;

  vec2 segmentScale = FragTexcoord * GridSegments - 0.5f;
  vec2 segmentOff = abs(fract(segmentScale) - vec2(0.5f, 0.5f)) * 2;
  
  float segmentHardened = 1.0f - clamp(pow(min(segmentOff.x, segmentOff.y) * LineThickness, GridSoftness), 0.0f, 1.0f);
  float glow = 1.0f - pow(smoothstep(0,1,segmentOff.x * segmentOff.y) * GlowSize, GlowStrength);

  OutputColor = vec4((Color.rgb * segmentHardened + GlowColor.rgb * glow) * Visibility, 1);
}
