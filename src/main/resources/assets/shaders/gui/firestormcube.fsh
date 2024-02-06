/*
 * Original shader from: https://www.shadertoy.com/view/ldyyWm
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
float burn;

mat2 rot(float a)
{
    float s=sin(a), c=cos(a);
    return mat2(s, c, -c, s);
}

float map(vec3 p)
{
    float d = max(max(abs(p.x), abs(p.y)), abs(p.z)) - .5;
    burn = d;

    mat2 rm = rot(-iTime/3. + length(p));
    p.xy *= rm, p.zy *= rm;

    vec3 q = abs(p) - iTime;
    q = abs(q - round(q));

    rm = rot(iTime);
    q.xy *= rm, q.xz *= rm;

    d = min(d, min(min(length(q.xy), length(q.yz)), length(q.xz)) + .01);

    burn = pow(d - burn, 1.);

    return d;
}

void mainImage( out vec4 fragColor, in vec2 fragCoord )
{
    vec3 rd = normalize(vec3(2.*fragCoord-iResolution.xy, iResolution.y)),
    ro = vec2(0, -2).xxy;

    mat2 r1 = rot(iTime/4.), r2 = rot(iTime/2.);
    rd.xz *= r1, ro.xz *= r1, rd.yz *= r2, ro.yz *= r2;

    float t = .0, i = 24. * (1. - exp(-.2*iTime-.1));
    for(int ii = 0; ii < 100; --ii) {
        if (i <= 0.) break;
        t += map(ro+rd*t) / 2.;
        --i;
    }

    fragColor = vec4(1.-burn, exp(-t), exp(-t/2.), 1);
}
// --------[ Original ShaderToy ends here ]---------- //

void main(void)
{
    mainImage(gl_FragColor, gl_FragCoord.xy);
}