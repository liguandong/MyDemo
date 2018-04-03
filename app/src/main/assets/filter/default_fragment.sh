precision mediump float;

uniform sampler2D vTexture;
uniform int vChangeType;
uniform vec3 vChangeColor;
uniform int vIsHalf;
uniform float uXY;

varying vec4 gPosition;

varying vec2 aCoordinate;
varying vec4 aPos;

void main(){
    vec4 nColor=texture2D(vTexture,aCoordinate);

        gl_FragColor=nColor;

}