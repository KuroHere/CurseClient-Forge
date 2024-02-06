/*
 * Original shader from: https://www.shadertoy.com/view/7ljGWV
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
// https://twitter.com/kamoshika_vrc/status/1370713495960911876

const float PI2 = acos(-1.)*2.;

float hash(in float x)
{
    return fract(sin(x) * 43758.5453);
}

float hash2D(in vec2 x)
{
    return fract(sin(dot(x, vec2(12.9898, 78.233))) * 43758.5453);
}

void mainImage( out vec4 fragColor, in vec2 fragCoord )
{
    vec2 p = (fragCoord * 2.0 - iResolution.xy ) / min(iResolution.x, iResolution.y);
    float v = 0.;

    for(float i = 0.;i < 30.;i++){
        float L = 1. + i - fract(iTime);
        vec2 q = p / atan(.005, L) / 500.;
        L = dot(q, q) * 3e2 + L * L;
        float n = hash(ceil(iTime) + i) * PI2;
        q = ceil(q * 20. + vec2(cos(n), sin(n)) * 2.) / 20.;
        if(v == 0.){
            v += step(1., hash2D(q + n * .01) + dot(q, q) * 2.) * exp(-L * .01);
        }
    }

    fragColor = vec4(vec3(v), 1.);
}
// --------[ Original ShaderToy ends here ]---------- //

void main(void)
{
    mainImage(gl_FragColor, gl_FragCoord.xy);
}