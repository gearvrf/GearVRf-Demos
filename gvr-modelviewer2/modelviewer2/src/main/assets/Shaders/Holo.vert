#version 400

// Uniform Variables
uniform mat4 View;
uniform mat4 Proj;

// Input Variables
in vec3 InputPosition;
in vec2 InputTexcoord;


// Output Variables
out vec2 FragTexcoord;

void main(void)
{
  mat4 viewZero = View;
  viewZero[3] = vec4(0,0,0,1);
  gl_Position = Proj * viewZero * vec4(InputPosition, 1.0f);
  FragTexcoord = InputTexcoord;
}
