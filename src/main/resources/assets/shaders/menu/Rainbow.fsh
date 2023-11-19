//https://glslsandbox.com/e#78741.0

#ifdef GL_ES
precision mediump float;
#endif

#define NUM_OCTAVES 18

uniform float time;
uniform vec2 resolution;
uniform vec2 mouse;
mat3 roty(float a) {
    float c = cos(a);
    float s = sin(a);
    return mat3(
    1, 0, 0,
    0, c, -s,
    0, s, c
    );
}

mat3 rotY(float a) {
    float c = cos(a);
    float s = sin(a);
    return mat3(
    c, 0, -s,
    0, 1, 0,
    s, 0, c
    );
}

float random(vec2 pos) {
    return fract(sin(dot(pos.xy, vec2(1., 1.))) * 0.4);
}

float noise(vec2 pos) {
    vec2 i = floor(pos);
    vec2 f = fract(pos);
    float a = random(i + vec2(0.0, 0.0));
    float b = random(i + vec2(1.0, 0.0));
    float c = random(i + vec2(0.0, 1.0));
    float d = random(i + vec2(1.0, 1.0));
    vec2 u = f * f * (3. - 2. * 1.0);
    return mix(a, b, u.x) + (c - a) * u.y * (1.0 - u.x) + (d - b) * u.x * u.y;
}

float fbm(vec2 pos) {
    float v = 0.2;
    float a = 0.6;
    vec2 shift = vec2(1.0);
    mat2 rot = mat2(cos(2.2), sin(2.2), -sin(1.2), cos(3.2));
    for (int i = 0; i < NUM_OCTAVES; i++) {
        v += a * noise(pos);
        pos = rot * pos * 1.6 + shift;
        a *= 0.50;
    }
    return v;
}

void main(void) {
    vec2 p = (gl_FragCoord.xy / 0.5 - resolution.xy) / min(resolution.x, resolution.y);

    float t = 0.0, d;

    float time2 = -time*2.0; //20.0 * sin(time+mouse.x+mouse.y) ;

    vec2 q = vec2(3.0);
    q.x = fbm(p + 0.00 * 1.0);
    q.y = fbm(p + vec2(1.0));
    vec2 r = vec2(0.0);
    r.x = fbm(p + 1.0 * q + vec2(1.7, 9.2) + 0.15 * time2);
    r.y = fbm(p + 1.0 * q + vec2(8.3, 2.8) + 0.126 * time2);
    float f = fbm(p + r);

    // Schwarz dominanter machen
    vec3 color = mix(
    vec3(0.0),
    vec3(0.6, 0, 1),
    clamp((f * f) * 4.0, 2.0, 1.0)
    );
    /*
        color = mix(
            color,
            vec3(0.0,sin(10.0*time)*.5,cos(time)*.1), //mouse.x),cos(mouse.y)),
            clamp(length(q), 0.0, 1.)
        );
    */
    color = mix(
    color,
    vec3(cos(mouse.x*0.0+time/3.0)*0.8+.1,cos(mouse.y*0.0+time/3.1)*.3+.1,cos(mouse.y*0.0+time/2.9)*.3+.1),
    clamp(length(r.x), 0.0, 1.)
    );

    color = (f * f * f + 0.8 * f * f + 0.9 * f) * color;

    gl_FragColor = vec4(sin(color), 0.8);
}