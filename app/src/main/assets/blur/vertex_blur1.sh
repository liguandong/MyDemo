uniform mat4 uMVPMatrix;
uniform mat4 uTexMatrix;
attribute vec4 aPosition;
attribute vec2 aTextureCoord;
varying vec2 vCoordinate;
varying vec4 vPosition;

//uniform vec2 offsetCoordinate;

varying vec2 textureCoordinate[13];
void main(){
    gl_Position=uMVPMatrix*aPosition;
    vCoordinate = aTextureCoord;
    vPosition = gl_Position;

     //gl_Position = uMVPMatrix * aPosition;

	//vec4 textureCoord = uTexMatrix * aTextureCoord;

    float offsetCoordinate = 1.2f * 3.0 /1080.0;
    textureCoordinate[0] = aTextureCoord.xy - 6.0 * offsetCoordinate;
    textureCoordinate[1] = aTextureCoord.xy - 5.0 * offsetCoordinate;
    textureCoordinate[2] = aTextureCoord.xy - 4.0 * offsetCoordinate;
    textureCoordinate[3] = aTextureCoord.xy - 3.0 * offsetCoordinate;
    textureCoordinate[4] = aTextureCoord.xy - 2.0 * offsetCoordinate;
    textureCoordinate[5] = aTextureCoord.xy - 1.0 * offsetCoordinate;
    textureCoordinate[6] = aTextureCoord.xy;
    textureCoordinate[7] = aTextureCoord.xy + 1.0 * offsetCoordinate;
    textureCoordinate[8] = aTextureCoord.xy + 2.0 * offsetCoordinate;
    textureCoordinate[9] = aTextureCoord.xy + 3.0 * offsetCoordinate;
    textureCoordinate[10] = aTextureCoord.xy + 4.0 * offsetCoordinate;
    textureCoordinate[11] = aTextureCoord.xy + 5.0 * offsetCoordinate;
    textureCoordinate[12] = aTextureCoord.xy + 6.0 * offsetCoordinate;
}
