precision mediump float;
uniform float uXY;
uniform float uProgress ;
uniform sampler2D uNormalTexture;
uniform sampler2D uBlurTexture;
varying vec2 vCoordinate;
varying vec4 vPosition;
void main(){

//    const float sizeMax = 2.05; //菱形的宽高，包住整个屏幕
        //这里菱形根据宽高比
//     if((abs(vPosition.x) + abs(vPosition.y)) * sizeMax <= sizeMax * sizeMax * uProgress ){
//        gl_FragColor = texture2D(uBlurTexture,vCoordinate);
//     }else{
//        gl_FragColor = texture2D(uNormalTexture,vCoordinate);
//     }

    //这里是正方形
    float width = 2.05f; //菱形的宽高，包住整个屏幕
    float height = width / uXY;
    if((abs(vPosition.x) + abs(vPosition.y / uXY)) * width <= width * height * uProgress ){
        gl_FragColor = texture2D(uBlurTexture,vCoordinate);
     }else{
        gl_FragColor = texture2D(uNormalTexture,vCoordinate);
     }
}





