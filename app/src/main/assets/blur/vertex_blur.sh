uniform mat4 uMVPMatrix;
uniform mat4 uTexMatrix;
attribute vec4 aPosition;
attribute vec2 aTextureCoord;
varying vec2 vCoordinate;
varying vec4 vPosition;
void main(){
    gl_Position=uMVPMatrix*aPosition;
    vCoordinate = aTextureCoord;
}
