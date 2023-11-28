#version 120

uniform sampler2D texture;
uniform vec3 rgb;
uniform vec3 rgb1;
uniform vec3 rgb2;
uniform vec3 rgb3;
uniform float step;
uniform float offset;
uniform float mix;
uniform float roundedEdge; // Additional uniform for rounded edge

void main() {
    float alpha = texture2D(texture, gl_TexCoord[0].xy).a;
    if (alpha != 0f) {
        float distance = sqrt(gl_FragCoord.x * gl_FragCoord.x + gl_FragCoord.y * gl_FragCoord.y) + offset;
        float distance2 = sqrt((gl_FragCoord.x - 800.0) * (gl_FragCoord.x - 800.0) + gl_FragCoord.y * gl_FragCoord.y) + offset;
        float distance3 = sqrt((gl_FragCoord.x - 400.0) * (gl_FragCoord.x - 400.0) + (gl_FragCoord.y - 400.0) * (gl_FragCoord.y - 400.0)) + offset;
        distance = distance / step;
        distance2 = distance2 / step;
        distance3 = distance3 / step;

        distance = ((sin(distance) + 1.0) / 2.0);
        distance2 = ((sin(distance2) + 1.0) / 2.0);
        distance3 = ((sin(distance3) + 1.0) / 2.0);

        float distanceInv = 1 - distance;
        float r = rgb.r * distance + rgb1.r * distanceInv + rgb2.r * distance2 + rgb3.r * distance3;
        float g = rgb.g * distance + rgb1.g * distanceInv + rgb2.g * distance2 + rgb3.g * distance3;
        float b = rgb.b * distance + rgb1.b * distanceInv + rgb2.b * distance2 + rgb3.b * distance3;

        // Adding rounded edge effect
        float aspect = gl_FragCoord.x / gl_FragCoord.y;
        float edge = 1.0 - smoothstep(1.0 - roundedEdge, 1.0, length(vec2(2.0 * aspect - 1.0, 2.0 * gl_FragCoord.y / 800.0 - 1.0)));
        float finalAlpha = mix * alpha * edge;

        gl_FragColor = vec4(r, g, b, finalAlpha);
    }
}