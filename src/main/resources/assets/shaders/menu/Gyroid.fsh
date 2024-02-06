/*
 * Original shader from: https://www.shadertoy.com/view/sstGRM
 */

#ifdef GL_ES
precision mediump float;
#endif

// glslsandbox uniforms
uniform float time;
uniform vec2 resolution;

// shadertoy emulation
#define iTime time
#define iResolution resolution

// --------[ Original ShaderToy begins here ]---------- //
#define rot(a) mat2(cos(a),sin(a),-sin(a),cos(a))
#define on(a,x) (abs(a)-(x))
#define pi 3.1415926535
#define pmod(p,x) (mod(p,x)*0.5*(x))
#define STEPS 128.0
#define MDIST 150.0
vec3 glow;
float gyr(vec3 p){
    return dot(sin(p),cos(p.yzx));
}
float rand(float a){
    return fract(sin(a*4131.22)*94935.34);
}

//iq stuff
float smin(float d1, float d2, float k){
    float h = max(k-abs(d1-d2),0.0);
    return min(d1, d2) - h*h*0.25/k;
}
float box(vec3 p, vec3 b){
    vec3 q = abs(p) - b;
    return length(max(q,0.0)) + min(max(q.x,max(q.y,q.z)),0.0);
}
float frame( vec3 p, vec3 b, float e ){
    p = abs(p)-b;
    vec3 q = abs(p+e)-e;
    return min(min(
    length(max(vec3(p.x,q.y,q.z),0.0))+min(max(p.x,max(q.y,q.z)),0.0),
    length(max(vec3(q.x,p.y,q.z),0.0))+min(max(q.x,max(p.y,q.z)),0.0)),
    length(max(vec3(q.x,q.y,p.z),0.0))+min(max(q.x,max(q.y,p.z)),0.0));
}
//end iq stuff

vec3 rubik(vec3 p, float t, float id){
    float rt = floor(rand(id)*3.0);
    float s = sign(rand(id*100.0)-0.5);
    float s2 = sign(rand(id*200.0)-0.5);
    if(rt == 0.0){
        p.xy*=rot(s*step(s2*p.z,0.0)*t*pi/2.0);
    }
    if(rt == 1.0){
        p.yz*=rot(s*step(s2*p.x,0.0)*t*pi/2.0);
    }
    if(rt == 2.0){
        p.zx*=rot(s*step(s2*p.y,0.0)*t*pi/2.0);
    }
    return p;
}

float easeOutElastic(float x) {
    float c4 = (2. * pi) / 3.;
    return x = pow(1.5,-10.*x) * sin((x*5.5-.75)*c4)+1.;
}

vec2 map(vec3 p){
    vec3 po = p;
    float tt = iTime*pi/3.0;
    float t = easeOutElastic(fract(tt))+floor(tt);

    p.xy*=rot(t*0.6);
    p.yz*=rot(t*0.6);
    p.zx*=rot(t*0.6);
    p.y+=t*2.0;

    vec2 a = vec2(1.0);
    a.x = on(on(gyr(p*2.0)/2.0,0.1),0.3+(sin(t)*0.5+0.5)*0.2);

    p = po;
    p = rubik(p,t,floor(tt));

    p = abs(p)-3.1-abs(asin(sin(t*pi*0.25)))*2.;
    float clipBox = box(p,vec3(3));

    a.x = smin(-clipBox,a.x,0.3);
    glow+=mix(vec3(0.639,0.180,0.318),vec3(0.412,0.180,0.639),sin(length(p)*3.0)*0.5+0.5)
    *1./(1.+a.x*a.x);

    float fr = frame(p,vec3(3.1),0.2);

    vec2 b = vec2(fr,2);
    a.x = -a.x;

    a=(a.x<b.x)?a:b;

    //anti artifact fuckery
    p = po;
    p = abs(p)-3.1;
    float domBox = box(p-2.2,vec3(6.0));
    float outBox = box(po,vec3(8.25));
    domBox = max(outBox,-domBox);
    a.x = min(domBox,a.x);

    return a;
}
vec3 norm(vec3 p){
    vec2 e= vec2(0.05,0);
    return normalize(map(p).x-vec3(
    map(p-e.xyy).x,
    map(p-e.yxy).x,
    map(p-e.yyx).x));
}
void mainImage( out vec4 fragColor, in vec2 fragCoord )
{
    vec2 uv = (fragCoord-0.5*iResolution.xy)/iResolution.y;
    vec3 col = vec3(0);
    vec3 ro = vec3(0,11,20);
    ro.xz*=rot(iTime*pi/6.0);

    vec3 lk = vec3(0,-2,0);
    vec3 f = normalize(lk-ro);
    vec3 r = normalize(cross(vec3(0,1,0),f));
    vec3 rd = f*0.65+uv.x*r+uv.y*cross(f,r);

    vec3 p = ro;
    float shad, dO;
    vec2 d;
    bool hit = false;

    for(float i = 0.; i <STEPS; i++){
        d = map(p);
        if(abs(d.x)<0.001){
            hit = true;
            shad = i/STEPS;
            break;
        }
        if(d.x>MDIST) {dO = MDIST;break;}
        dO+=d.x*0.8;
        p =ro+rd*dO;
    }
    if(hit){
        vec3 n = norm(p);
        //vec3 r = reflect(rd, n);
        float ao = smoothstep(-.1,.1,map(p+n*.1).x)*smoothstep(-.2,.2,map(p+n*.2).x);
        //ao*=ao;

        col = 1.0-(n*0.5+0.5);
        if(d.y == 2.0) col *=0.2;
        //col*=pow(1.0-shad,3.0);

        //fabe ibl from blackle
        float diff = length(sin(n*3.0)*.4+.6)/sqrt(3.);
        col=col*diff*vec3(0.996,0.784,0.784);
        col*=ao;
    }
    col = mix(col,vec3(0.145,0.067,0.435),(dO/(MDIST)));
    col = pow(col,vec3(0.7));

    col*=(1.0-length(uv*uv));
    col+=glow*0.02;
    fragColor = vec4(col,1.0);
}
// --------[ Original ShaderToy ends here ]---------- //

void main(void)
{
    mainImage(gl_FragColor, gl_FragCoord.xy);
}