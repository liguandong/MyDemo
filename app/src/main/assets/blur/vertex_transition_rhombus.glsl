uniform mat4 uMVPMatrix;
uniform mat4 uTexMatrix;
attribute vec4 aPosition;
attribute vec2 aTextureCoord;
varying vec2 vCoordinate;
varying vec4 vPosition;
void main(){
    gl_Position=uMVPMatrix*aPosition;
    vCoordinate = aTextureCoord;
    vPosition = gl_Position;
//    uv = (vec2( gl_Position.x, - gl_Position.y ) + vec2(1.0,1.0) ) / vec2(2.0);
//    uv = vec2((gl_Position.x + 1.0)/2.0,(-gl_Position.y+1.0)/2.0);
}
