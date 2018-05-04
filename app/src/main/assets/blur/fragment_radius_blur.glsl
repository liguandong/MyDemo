precision mediump float;
uniform vec2 uCenter;
uniform float uRadius;
uniform float uAlpha;
uniform float uXY;
uniform int uIsShowMask;
varying vec2 vCoordinate;
varying vec4 vPosition;
uniform sampler2D uNormalTexture;
uniform sampler2D uBlurTexture;

uniform float uOffsetX;
uniform float uOffsetY;

const float PI = 3.14159265;

varying vec2 uv;
const float sampleDist = 1.0;
const float sampleStrength = 2.2;


float compositeAlpha(float foregroundAlpha, float backgroundAlpha) {
    return 1.0f - (1.0f - backgroundAlpha) * (1.0f - foregroundAlpha);
}

float compositeComponent(float fgC, float fgA, float bgC, float bgA, float a) {
    if (a == 0.0) return 0.0;
    return (fgC * fgA) + (bgC * bgA * (1.0f - fgA)) / a;
}

vec4 compositeColors( vec4 foreground,vec4 background) {

    float a = compositeAlpha(foreground.a, background.a);
    float r = compositeComponent(foreground.r, foreground.a,
            background.r, background.a,a);
    float g = compositeComponent(foreground.g, foreground.a,
            background.g, background.a, a);
    float b = compositeComponent(foreground.b, foreground.a,
            background.b, background.a,a);
    return vec4(r, g, b, a);
    //return vec4(foreground.r,foreground.g,foreground.b,foreground.a);
}
void main(){
//    if(uIsShowMask == 1){
//        vec4 nNormalColor = texture2D(uNormalTexture,vCoordinate);
//        float dis = distance(vec2(vPosition.x,vPosition.y / uXY),vec2(uCenter.x,uCenter.y /uXY));
//        vec4 nBlurColor = vec4(1.0,1.0,1.0,0.8);
//         if(abs(dis) < uRadius){
//            float rang = uRadius/3.0;
//            if(abs(dis) >= (uRadius - rang)){
//                float alpha = 1.0 - (uRadius - abs(dis))/rang;
//                alpha = clamp(alpha,0.0,1.0f);
//                nBlurColor.a = nBlurColor.a  * alpha;
//            }else{
//                nBlurColor.a = 0.0;
//            }
//        }
//        gl_FragColor = compositeColors(nBlurColor,nNormalColor);
//    }else{
//            vec4 clraverge = vec4(0.0);
//          float dis = distance(vec2(vPosition.x,vPosition.y / uXY),vec2(uCenter.x,uCenter.y /uXY));
//          if(abs(dis) < uRadius){
//                gl_FragColor = texture2D(uNormalTexture,vCoordinate);
//          }else{
//                float range=30.0,count=0.0,x1,y1;
//                vec2 cpos= uCenter;
//                for( float j = 1.0; j<=range ; j += 1.0 )
//                {
//                      float k=(cpos.y-vCoordinate.y)/(cpos.x-vCoordinate.x);
//                       x1=vCoordinate.x+(cpos.x-vCoordinate.x)*j/200.0;
////                        if((cpos.x-vCoordinate.x)*(cpos.x-x1)<0.0) {
////                            x1=cpos.x;
////                        }
//                      y1=cpos.y-cpos.x*k+k*x1;
//                      clraverge+=texture2D( uNormalTexture, vec2(x1,y1) );
//                      count+=1.0;
//                }
//            clraverge/=count;
//           gl_FragColor =clraverge;
//          }
//    }
    float samples[10];
    samples[0] = -0.08;
    samples[1] = -0.05;
    samples[2] = -0.03;
    samples[3] = -0.02;
    samples[4] = -0.01;
    samples[5] =  0.01;
    samples[6] =  0.02;
    samples[7] =  0.03;
    samples[8] =  0.05;
    samples[9] =  0.08;
//    vec2 dir = 0.5 - uv;
    vec2 dir = vec2(0.5 - uv.x,0.5 - uv.y);
    float dist = sqrt(dir.x*dir.x + dir.y*dir.y);
    dir = dir/dist;
    vec4 color = texture2D(uNormalTexture,uv);
    vec4 sum = color;
    for (int i = 0; i < 10; i++){
        sum += texture2D( uNormalTexture, uv + dir * samples[i] * sampleDist );
        }
    sum *= 1.0/11.0;
    float t = dist * sampleStrength;
    t = clamp( t ,0.0,1.0);
    gl_FragColor = mix( color, sum, t );


//          if(vCoordinate.x < 0.0){
//             factorX = - factorX;
//          }else if(vCoordinate.x == 0.0){
//            factorX = 0.0;
//          }
//          if(vCoordinate.y < 0.0){
//               factorY = - factorY;
//           }else{
//            factorX = 0.0;
//           }
//          vec4 color = vec4(0.0);
//          vec2 offset = vCoordinate - uCenter;
//          float factorX = offset.x;
//          float factorY = offset.y;
//          color += texture2D(uBlurTexture,vCoordinate);
//          color += texture2D(uBlurTexture,vec2(vCoordinate.x + factorX *0.01f,vCoordinate.y + factorY * 0.01f ));
//          color += texture2D(uBlurTexture,vec2(vCoordinate.x + factorX *0.02f,vCoordinate.y + factorY * 0.02f ));
//          color += texture2D(uBlurTexture,vec2(vCoordinate.x + factorX *0.04f,vCoordinate.y + factorY * 0.04f ));
//          color += texture2D(uBlurTexture,vec2(vCoordinate.x + factorX *0.08f,vCoordinate.y + factorY * 0.08f ));
//          color += texture2D(uBlurTexture,vec2(vCoordinate.x + factorX *0.16f,vCoordinate.y + factorY * 0.16f ));
//          color /= 6.0;
//          color /= 4.0;
//           gl_FragColor = color;
//        nBlurColor = texture2D(uBlurTexture,vCoordinate);
//        if(abs(dis) < uRadius){
//            float rang = uRadius/3.0;
//            if(abs(dis) >= (uRadius - rang)){
//                float alpha = 1.0 - (uRadius - abs(dis))/rang;
//                alpha = clamp(alpha,0.0,1.0f);
//                nBlurColor.a = nBlurColor.a * uAlpha * alpha;
//            }else{
//                nBlurColor.a = 0.0;
//            }
//        }else{
//            nBlurColor.a = nBlurColor.a * uAlpha;
//        }
//        gl_FragColor = compositeColors(nBlurColor,nNormalColor);
//    }

//     vec4 clraverge=vec4(0.0);
//     float range=30.0,count=0.0,x1,y1;
//      vec2 cpos=vec2(0,0);
//         for( float j = 1.0; j<=range ; j += 1.0 )
//      {
//             if(cpos.x-vCoordinate.x==0.0)
//             {
//                x1=vCoordinate.x;
//                y1=vCoordinate.y+(cpos.y-vCoordinate.y)*j/(6.0*range);
//             }
//             else
//            {
//                float k=(cpos.y-vCoordinate.y)/(cpos.x-vCoordinate.x);
//                   x1=vCoordinate.x+(cpos.x-vCoordinate.x)*j/200.0;
//                      if((cpos.x-vCoordinate.x)*(cpos.x-x1)<0.0) x1=cpos.x;
//                y1=cpos.y-cpos.x*k+k*x1;
//                if(x1<0.0||y1<0.0||x1>1.0||y1>1.0)
//                {
//                  continue;
//                }
//            }
//            clraverge+=texture2D( uNormalTexture, vec2(x1,y1) );
//            count+=1.0;
//      }
//      clraverge/=count;
//     gl_FragColor =clraverge;


//        float R = distance(vec2(vPosition.x,vPosition.y / uXY),vec2(0,0));
//        float num = 5.0;
//        for (float i=0;i<num;i++)      //num：均值力度 ，i为变化幅度;
//        {
//            float tmpR = R - i;
//
//            float newX = tmpR*cos(angle) + center.x;
//            float newY = tmpR*sin(angle) + center.y;
//
//            if(newX<0)newX=0;
//            if(newX>width-1)newX=width-1;
//            if(newY<0)newY=0;
//            if(newY>heigh-1)newY=heigh-1;
//
//            tmp0 += src1u[0].at<uchar>(newY,newX);
//            tmp1 += src1u[1].at<uchar>(newY,newX);
//            tmp2 += src1u[2].at<uchar>(newY,newX);
//
//        }
//        imgP[3*x]=(uchar)(tmp0/num);
//        imgP[3*x+1]=(uchar)(tmp1/num);
//        imgP[3*x+2]=(uchar)(tmp2/num);
//        vec4 clraverge =texture2D( uNormalTexture, vec2(x1,y1) );



}





