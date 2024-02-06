// from https://www.shadertoy.com/view/DdKBzz

#extension GL_OES_standard_derivatives : enable

precision highp float;

uniform float time;
uniform vec2 resolution;

// hash without sine: https://www.shadertoy.com/view/4djSRW
float hash11(float p) {
    vec3 p3  = fract(vec3(p) * vec3(.1031, .11369, .13787));
    p3 += dot(p3, p3.yzx + 19.19);
    return fract((p3.x + p3.y) * p3.z);
}

// 1d smooth noise
float snoise1d(float f) {
    return
    mix(
    hash11(floor(f)),
    hash11(floor(f+1.)),
    smoothstep(0., 1., fract(f))
    );
}

/* star shape (2d distance estimate)
   p = input coordinate
   n = number of sides
   r = radius
   i = inset amount (0.0=basic polygon, 1.0=typical star
*/
float StarDE(vec2 p, float n, float r, float i) {
    float rep = floor(-atan(p.x, p.y)*(n/6.28)+.5) / (n/6.28);
    float s, c;
    p = mat2(c=cos(rep), s=-sin(rep), -s, c) * p;
    float a = (i+1.) * 3.14 / n;
    p = mat2(c=cos(a), s=-sin(a), -s, c) * vec2(-abs(p.x), p.y-r);
    return length(max(vec2(0.), p));
}

// StarDE, but with eyes
float Starguy(vec2 p, float n, float r, float i, vec2 l) {

    // blink
    float b = pow(abs(fract(.087*time+.1)-.5)*2.,6.);

    // eye look
    vec2 p2 = p + l;

    return
    max(
    StarDE(p, n, r, i),
    // eyes
    //-length(vec2(abs(p.x)-r*.18, min(0., -abs(p.y)+r*.1)))+r*.11
    -length(
    vec2(
    min(0., -abs(abs(p2.x)-r*.2)+r*b*.1),
    min(0., -abs(p2.y)+r*(1.-b)*.1)
    )
    )+r*.13

    );
}

void main( void ) {

    //vec2 position = ( gl_FragCoord.xy / resolution.xy ) + mouse / 4.0;

    vec2 p = (gl_FragCoord.xy-resolution.xy/2.) / resolution.y;

    // time
    float t = .7 * time;

    // bob up and down
    vec2 p2 = p;
    p2.y += .025 * sin(8.*t);

    // warping (pinned inversion)
    p2 = p2 / dot(p2, p2) - .17 * vec2(sin(t), cos(4.*t));
    p2 = p2 / dot(p2, p2);

    vec2 look = .01 * vec2(cos(.71*t), sin(.24*t));

    // Starguy
    float star = Starguy(p2, 5., .25, .7-length(p), look);

    // radiation base
    float rad = pow(Starguy(p, 5., .27, .7-length(p), look), .5);

    // radiating rainbow waves
    vec3 colRad =
    vec3(1., 0., 0.) * snoise1d(11.*rad-5.*time) +
    vec3(0., 1.2, 0.) * snoise1d(31.7*rad-3.3*time-1.34) +
    vec3(0., 0., 1.5) * snoise1d(58.2*rad-1.1*time+2.27);

    // radiating waves
    rad = snoise1d(24.*rad-2.*time) + .5*snoise1d(48.*rad-4.*time);

    // Starguy + radiation
    vec3 col =
    mix(
    vec3(1.),
    vec3(-.1, .0, .2),
    clamp(star/.01, 0., 1.)
    ) + 4.5 * vec3(1., .5, .23) * (1.05 - pow(star, .1) ) + .05 * colRad;



    gl_FragColor = vec4(col, 1.);

}