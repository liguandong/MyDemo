precision mediump float;

uniform sampler2D vTexture;
uniform vec3 vChangeColor;

varying vec4 gPosition;

varying vec2 aCoordinate;
varying vec4 aPos;


void main()
{
    //高斯滤波核
    // 1 2 1
    // 2 4 2
    // 1 2 1
    vec4 color=vec4(0.0);
    int coreSize=3;
    float texelOffset=1/150.0;
    float kernel[9];

    kernel[6]=1;kernel[7]=2;kernel[8]=1;
    kernel[3]=2;kernel[4]=4;kernel[5]=2;
    kernel[0]=1;kernel[1]=2;kernel[2]=1;

    //移动高斯核,对原图做卷积计算
    int index=0;
    for(int y=0;y<coreSize;y++)
    {
        for(int x=0;x<coreSize;x++)
        {

            //原图像素点
            vec4 currentColor=texture2D(vTexture,aCoordinate+vec2((-1+x)*texelOffset,(-1+y)*texelOffset));
            //卷积计算
            color+=currentColor*kernel[index++];
        }
    }
    //根据邻域内像素的加权平均灰度值去替代模板中心像素点的值
    color/=16.0;
    gl_FragColor=color;
}


void main()
{
    vec4 color = vec4(0.0);
    int coreSize=3;
    int halfSize=coreSize/2;
    float texelOffset = 1/100.0;
    float kernel[9];
    kernel[6] = 1; kernel[7] = 2; kernel[8] = 1;
    kernel[3] = 2; kernel[4] = 4; kernel[5] = 2;
    kernel[0] = 1; kernel[1] = 2; kernel[2] = 1;
    int index = 0;
    for(int y=0;y<coreSize;y++)
    {
        for(int x = 0;x<coreSize;x++)
        {
            vec4 currentColor = texture2D(U_MainTexture,V_Texcoord+vec2((-1+x)*texelOffset,(-1+y)*texelOffset));
            color += currentColor*kernel[index];
            index++;
        }
    }
    color/=16.0;
    gl_FragColor=color;
}



void main()
{
    float weight[3] = {0.4026,0.2442,0.0545};
    float texelOffset = 1/100.0;
     //only show vertical here...
    float uv[5];
    uv[0]=V_Texcoord;
    uv[1]=uv + vec2(0.0, texelOffset*1.0 );
    uv[2]=uv - vec2(0.0, texelOffset*1.0 );
    uv[3]=uv + vec2(0.0, texelOffset*2.0 );
    uv[4]=uv - vec2(0.0, texelOffset*2.0 );
    fixed3 sum = tex2D(U_MainTexture,uv[0]).rgb *weight[0];
    for(int i = 1;i<3;++i)
    {
        sum += tex2D(U_MainTexture,uv[2i-1]).rgb * weight[i];
        sum += tex2D(U_MainTexture,uv[2i]).rgb * weight[i];
    }
    gl_FragColor=sum;
}