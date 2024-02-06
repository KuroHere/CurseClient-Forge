/*
 * Original shader from: https://www.shadertoy.com/view/ssd3WH
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

// Emulate some GLSL ES 3.x
#define round(x) (floor((x) + 0.5))

// --------[ Original ShaderToy begins here ]---------- //
// Code by Flopine

// Thanks to wsmind, leon, XT95, lsdlive, lamogui,
// Coyhot, Alkama,YX, NuSan, slerpy, wwrighter
// BigWings and FabriceNeyret for teaching me

// Thanks LJ for giving me the spark :3

// Thanks to the Cookie Collective, which build a cozy and safe environment for me
// and other to sprout :)
// https://twitter.com/CookieDemoparty


#define PI acos(-1.)
#define TAU (2.*PI)

#define time iTime
#define frt(sp,off) fract((time+off)*sp)
#define flt(sp,off) floor((time+off)*sp)

#define rot(a) mat2(cos(a),sin(a),-sin(a),cos(a))
#define crep(p,c,l) p-=(c*clamp(round(p/c),-l,l))
#define mo(p,d) p=abs(p)-d;if(p.y>p.x)p=p.yx

struct obj{
    float d;
    vec3 sha;
    vec3 li;
};

obj minobj(obj a, obj b)
{
    if (a.d<b.d) return a; else return b;
}


float box (vec3 p , vec3 c)
{
    vec3 q = abs(p)-c;
    return min(0.,max(q.x,max(q.y,q.z)))+length(max(q,0.));
}

obj base (vec3 p)
{
    vec3 pp = p;
    float per = 5.;
    vec3 id = round(p/per);
    float off = clamp(length(id*0.52),-3.,3.);
    crep(p,per,2.);
    float d = box(p,vec3(per*0.1+off));
    float b = box(p,vec3(0.6));

    p=pp;
    d = max(-d,box(p-vec3(1.9,-1.9,1.8),vec3(11.)));
    d = max(-box(p+vec3(1.,-3.,1.),vec3(8.)),d);

    return obj(d, vec3(0.0,0.01,0.05),vec3(0.,0.1,0.2));
}

obj octas (vec3 p)
{
    p.y -= 2.;
    mo(p.zx,vec2(5.5));
vec2 id = round(p.xy/3.);
crep(p.xy,3.,2.);
float off = length(id),
anim = (PI/4.)*(flt(2.,off)+pow(frt(2.,off),8.));
p.xz *= rot(anim);
float d = dot(p,normalize(sign(p)))-0.8;

return obj(d,vec3(0.2,0.,0.),vec3(1.,0.5,0.));
}

float g1=0.;
obj pillars (vec3 p)
{
    p -= vec3(1.9,2.,1.8);
    mo(p.xz,vec2(11.));
float d = box(p,vec3(.5,15.,.5));
g1 += 0.1/(0.1+d*d);

return obj(d,vec3(0.),vec3(0.1));
}

vec2 id;
obj SDF (vec3 p)
{
    p.yz *= rot(-atan(1./sqrt(2.)));
    p.xz *= rot(PI/4.);

    id = round(p.xz/29.);
    crep(p.xz,29.,1.);
    obj scene = base(p);
    scene = minobj(scene, octas(p));
    scene = minobj(scene, pillars(p));

    return scene;
}

vec3 getnorm (vec3 p)
{
    vec2 eps = vec2(0.001,0.);
    return normalize(SDF(p).d-vec3(SDF(p-eps.xyy).d,SDF(p-eps.yxy).d,SDF(p-eps.yyx).d));
}

float AO (float eps, vec3 p, vec3 n)
{return SDF(p+eps*n).d/eps;}

void mainImage (out vec4 fragColor, in vec2 fragCoord)
{
    vec2 uv = (2.*fragCoord.xy-iResolution.xy)/iResolution.y;
    vec2 ouv = fragCoord.xy/iResolution.xy;

    vec3 ro=vec3(uv*20.,-50.), rd=vec3(0.,0.,1.),p=ro,
    col=vec3(0.), l=normalize(vec3(2.,3.,-2.));

    bool hit = false; obj O;
    for (float i=0.; i<64.;i++)
    {
        O = SDF(p);
        if (O.d<0.001)
        {hit=true;break;}
        p += O.d*rd;
    }

    if (hit)
    {
        vec3 n=getnorm(p);
        float light = max(dot(n,l),0.);
        float ao = AO(0.1,p,n)+AO(0.5,p,n)+AO(0.9,p,n);
        col = mix(O.sha, O.li, light)*(ao/3.);
    }

    col += g1*(sin(length(id)-frt(.3,0.)*TAU)+1.);
    fragColor = vec4(sqrt(col),1.);
}
// --------[ Original ShaderToy ends here ]---------- //

void main(void)
{
    mainImage(gl_FragColor, gl_FragCoord.xy);
}