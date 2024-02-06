/*
 * Original shader from: https://www.shadertoy.com/view/wtSyDK
 */

#ifdef GL_ES
precision mediump float;
#endif

// glslsandbox uniforms
uniform vec4 color;
uniform float time;
uniform vec2 resolution;

// shadertoy emulation
#define iTime time
#define iResolution resolution

float rand(vec2 uv)
{
    return fract(sin(dot(uv, vec2(12.9898, 78.233))) * 43758.5453);
}

vec3 hue2rgb(float h)
{
    h = fract(h) * 6.0 - 2.0;
    return clamp(vec3(abs(h - 1.0) - 1.0, 2.0 - abs(h), 2.0 - abs(h - 2.0)), 0.0, 1.0);
}

vec2 uv2tri(vec2 uv)
{
    float sx = uv.x - uv.y / 2.0; // skewed x
    float sxf = fract(sx);
    float offs = step(fract(1.0 - uv.y), sxf);
    return vec2(floor(sx) * 2.0 + sxf + offs, uv.y);
}

vec3 tri(vec2 uv)
{
    vec2 p = floor(uv2tri(uv));
    float h = rand(p + 0.1) * 0.2 + iTime * 0.2;
    float s = sin((rand(p + 0.2) * 3.3 + 1.2) * iTime) * 0.5 + 0.5;
    return hue2rgb(h) * s + 0.5;
}


void mainImage( out vec4 fragColor, in vec2 fragCoord )
{

    vec2 uv = (fragCoord.xy - iResolution.xy / 2.0) / iResolution.y;

    float t1 = iTime / 2.0;
    float t2 = t1 + 0.5;

    vec3 c1 = tri(uv * (8.0 - 4.0 * fract(t1)) + floor(t1) * 4.0);
    vec3 c2 = tri(uv * (8.0 - 4.0 * fract(t2)) + floor(t2) * 4.0 + 2.0);

    fragColor = vec4(mix(c1, c2, abs(1.0 - 2.0 * fract(t1))), 0.5);

}

void main()
{
    mainImage(gl_FragColor, gl_FragCoord.xy);
}