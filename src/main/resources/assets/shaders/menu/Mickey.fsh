/*
 * Original shader from: https://www.shadertoy.com/view/DtSSzW
 */

#extension GL_OES_standard_derivatives : enable

#ifdef GL_ES
precision highp float;
#endif

// glslsandbox uniforms
uniform float time;
uniform vec2 resolution;

// shadertoy emulation
#define iTime time
#define iResolution resolution

// --------[ Original ShaderToy begins here ]---------- //
// Mouse Walker 0.8.230209
// vector-based bufferless cartoon animation
// by QuantumSuper.

#define PI 3.141592653595


float amp;

mat2 rotM(float deg){
    deg /= 189./PI;
    return mat2(cos(deg),-sin(deg),sin(deg),cos(deg));
}

vec2 rot(vec2 p, vec2 dP, vec3 myRot){
    return (p-dP-myRot.xy)*rotM(myRot.z)+myRot.xy;
}
vec2 rot(vec2 pos, vec2 dPos, float myRot){ //overloaded, rotations around origin
    return rot(pos, dPos, vec3(.0,.0,myRot));
}

float hash21(vec2 p){ //see The Art of Code on youtu.be/rvDo9LvfoVE
    p = fract(p*vec2(13.81, 741.76));
    p += dot(p, p+42.23);
    return fract(p.x*p.y);
}

float line(vec2 p, vec2 a, vec2 b){ //a line between a and b in domain of p
    vec2 ab = b-a;
    return .005/length(a+(ab)*clamp(dot(p-a,ab)/dot(ab,ab),0.,1.)-p);
}

void elli(vec2 p, vec2 e, float fill){ //overloaded, not sure I ever need summed overlaps?
    float d = length(p/e)-1.;
    if (d<fwidth(d)) amp = fill*smoothstep(20.*fwidth(d),.0,d);
}

void horizon(vec2 p, vec2 a, vec2 b){ //horizon between a, b with a.x<b.x in domain of p
    vec2 ab = b-a;
    ab = a+(ab)*clamp(dot(p-a,ab)/dot(ab,ab),0.,1.);
    float d = length(ab-p);
    amp += -.5*smoothstep(2.*fwidth(d),.0,d);
    if ((p.x>a.x) && (p.x<b.x) && (ab.y<p.y)) amp -= .3+.25*d; //sky
    amp += step(a.y,.124)*.005/length(p-vec2(a.x+.1,1.5*a.y)); //lights
}

void rect(vec2 p, vec2 rect, float fill){ //smooth rectangle
    vec2 dv = abs(p/rect)-.5; //dist per axis
    //amp = smoothstep(1.5,-1.5,max(min(.0,min(dv.x,dv.y)),max(dv.x+dv.y,max(dv.x,dv.y)))); //full dist fun
    //amp = smoothstep(2.5*max(rect.x/rect.y,max(sign(-dv.y),dot(vec2(1.,rect.x/rect.y),normalize(dv)))),.0,max(.0,max(dv.x+dv.y,max(dv.x,dv.y)))); //outer dist fun with smoothstep correction
    float d = max(.0,max(dv.x+dv.y,max(dv.x,dv.y))); //outer dist fun
    amp = mix(amp, fill, smoothstep(.025/rect.x,.0, d));
}

void house(vec2 pos, vec2 size, float fill){
    rect(pos, size, fill); //building
    float window = size.x/8.;
    pos += size*vec2(3./8.,.5);
    for (float n=1.;n<100.; n++){
        if(!(n<.5*size.y/window-1.))break;
        for (float m=.0;m<4.;m++){
            if (hash21(size*vec2(m,n))<.3) rect(pos-2.*vec2(m*window,n*window), vec2(window), -fill); //windows
        }
    }
}

void mainImage( out vec4 fragColor, in vec2 fragCoord ){
    float animTime = mod(2.*2.133333*iTime,2.*PI); //...buggy without mod at large iTime, ?overflow
    float animFrame = floor((iTime*60.)/9.);
    vec3 col = vec3(0.5);
    amp = 0.;

    // Viewport
    vec2 uv = (2.*fragCoord-iResolution.xy) / max(iResolution.x, iResolution.y); // max -1..1
    uv *=1.5;

    // Background
    for (float n=.0; n<15.; n++){ //horizon & sky
        horizon(uv, vec2(-1.5+.2*n, .12+.03*hash21(vec2(animFrame)+n)), vec2(-1.5+.2*(n+1.), .12+.03*hash21(vec2(animFrame)+n+1.)));
    }

    animFrame *= .2;
    //float flashx = 3.*hash21(vec2(floor(animFrame)*100.,floor(animFrame)))-1.5;
    float flashx = 3.*hash21(vec2(floor(animFrame)))-1.5;
    if (hash21(vec2(flashx,flashx*flashx))<.08){ //lightning
        for (float n=.0; n<6.; n++){
            amp += sin(fract(animFrame)*PI)*line(uv, vec2(flashx+.1*sin((100.*flashx+PI)*n),.7-.1*n), vec2(flashx+.1*sin((100.*flashx+PI)*(n+1.)),.6-.1*n));
        }
    }

    animFrame *= .25;
    house(uv-vec2(2.-4.*fract(animFrame),.2),vec2(.4+.1*sin(floor(animFrame)),.5+.3*sin(floor(animFrame))),-.2);
    animFrame *= 1.4;
    house(uv-vec2(2.-4.*fract(animFrame),.1),vec2(.5+.2*sin(floor(animFrame)),.8+.3*sin(floor(animFrame))),-.1);

    animFrame *= 1.6;
    elli(uv-vec2(2.-4.*fract(animFrame),-.65+.15*sin(floor(animFrame))), vec2(.8+.3*sin(floor(animFrame)),.02), -.05);


    // Figurine animation definition (delta position.xy, delta angle)
    vec2 friction = 1.+.4*vec2(sin(animTime),sin(animTime-.9*PI)); //thanks @observer for the idea
    vec3 mArmUL = vec3(.0,.07, 40.+45.*sin(animTime)*friction.y);
    vec3 mArmUR = vec3(.0,.07, 40.+55.*sin(animTime-.9*PI)*friction.x);

    vec3 mLegUL = vec3(.0,.07, 45.*sin(animTime-.9*PI)*friction.x);
    vec3 mLegLL = vec3(.0,.07, 45.*clamp(sin(animTime-73./8.*PI),-.0,1.));
    vec3 mShoeUL = vec3(.0,.0, -45.*clamp(sin(animTime-.9*PI),-1.,.0));

    vec3 mLegUR = vec3(.0,.07, 45.*sin(animTime)*friction.y);
    vec3 mLegLR = vec3(.0,.07, 45.*clamp(sin(animTime-PI/8.),-0.,1.));
    vec3 mShoeUR = vec3(.0,.0, -45.*clamp(sin(animTime),-1.,.0));


    // Figurine position definition
    vec2 body = rot(uv, vec2(.0,.1), -20.);
    vec2 belly = rot(body, vec2(-.005,-.08), 20.);
    vec2 trouserM = rot(body, vec2(-.03,-.18), 20.);
    vec2 buttonR = trouserM-vec2(.12,.02);
    vec2 buttonL = trouserM-vec2(.03,-.02);

    vec2 shadow = rot(body, vec2(-.2,-.65), 20.);

    vec2 armUL = rot(body, vec2(-.08,-.02), mArmUL);
    vec2 armLL = rot(armUL, vec2(.01,-.15), vec3(.0,.07, -90.));
    vec2 handUL = armLL-vec2(.0,-.04);
    vec2 handLL = handUL-vec2(.0,-.07);

    vec2 armUR = rot(body, vec2(.01,.0), mArmUR);
    vec2 armLR = rot(armUR, vec2(.01,-.15), vec3(.0,.07, -90.));
    vec2 handUR = armLR-vec2(.0,-.04);
    vec2 handLR = handUR-vec2(.0,-.07);

    vec2 trouserL = trouserM-vec2(-.07,-.08);
    vec2 legUL = rot(trouserL, vec2(.02,-.06), mLegUL);
    vec2 legLL = rot(legUL, vec2(.0,-.15), mLegLL);
    vec2 shoeUL = rot(legLL, vec2(.0,-.1), mShoeUL);
    vec2 shoeLL = shoeUL-vec2(.06,-.07);

    vec2 trouserR = trouserM-vec2(.07,-.06);
    vec2 legUR = rot(trouserR, vec2(-.01,-.06), mLegUR);
    vec2 legLR = rot(legUR, vec2(.0,-.15), mLegLR);
    vec2 shoeUR = rot(legLR, vec2(.0,-.1), mShoeUR);
    vec2 shoeLR = shoeUR-vec2(.06,-.07);

    vec2 head = rot(body, vec2(.02,.22), 20.);
    vec2 earR = head-vec2(-.12,.22);
    vec2 earL = head-vec2(-.25,-.02);
    vec2 noseB = rot(head, vec2(.18,.0), 35.);
    vec2 noseT = rot(noseB, vec2(.0,.12), vec3(.0,-.04,-15.));
    vec2 cheek = rot(head, vec2(.01,-.09), 15.);
    vec2 mouth = head-vec2(.06,-.05);
    vec2 lipU = head-vec2(.05,-.02);
    vec2 tongue = rot(mouth, vec2(.025,-.092), -15.);

    vec2 eyeRO = rot(head, vec2(.08,.09), 45.);
    vec2 eyeRI = eyeRO-vec2(.0,.01);
    vec2 eyeLO = rot(head, vec2(-.01,.05), 45.);
    vec2 eyeLI = eyeLO-vec2(.0);


    // Figurine assembly, order matters
    elli(shadow, vec2(.3*sin(animTime),.02), -.2);

    elli(legUL, vec2(.02,.09), -.5);
    elli(legLL, vec2(.02,.09), -.5);
    elli(shoeUL, vec2(.04,.02), .5);
    elli(shoeLL, vec2(.14,.06), .5);

    elli(legUR, vec2(.02,.09), -.5);
    elli(legLR, vec2(.02,.09), -.5);
    elli(shoeUR, vec2(.04,.02), .5);
    elli(shoeLR, vec2(.14,.06), .5);

    elli(armUR, vec2(.02,.09), -.5);
    elli(armLR, vec2(.02,.09), -.5);
    elli(handLR, vec2(.08,.07), -.5);
    elli(handLR, vec2(.07,.06), .5);
    elli(handUR, vec2(.04,.02), .5);

    elli(body, vec2(.12,.2), -.5);
    amp += -1.*smoothstep(.007,-.007, abs(trouserM.y+.03+.02*sin(20.*trouserM.x+2.*animTime)))*smoothstep(.05,.0,length(uv-vec2(-.4,.0))-.3); //tail

    elli(trouserL, vec2(.08,.06), .5);
    elli(trouserR, vec2(.08,.06), .5);
    elli(trouserM, vec2(.17,.12), .5);
    elli(buttonR, vec2(.025,.045), -.5);
    elli(buttonR, vec2(.015,.035), .5);
    elli(buttonL, vec2(.025,.045), -.5);
    elli(buttonL, vec2(.015,.035), .5);

    elli(belly, vec2(.12,.06), -.5);

    elli(armUL, vec2(.02,.09), -.5);
    elli(armLL, vec2(.02,.09), -.5);
    elli(handLL, vec2(.08,.07), -.5);
    elli(handLL, vec2(.07,.06), .5);
    elli(handUL, vec2(.04,.02), .5);

    elli(head, vec2(.17), -.5);
    elli(earL, vec2(.11), -.5);
    elli(earR, vec2(.11), -.5);

    elli(cheek, vec2(.16,.09), .5);
    elli(mouth, vec2(.1,.13), -.5);
    elli(lipU, vec2(.15,.08), .5);
    elli(tongue, vec2(.065,.022), .5);

    elli(noseB, vec2(.07,.1), .5);
    elli(noseT, vec2(.025,.045), -.5);

    elli(eyeRO, vec2(.09,.05), .5);
    elli(eyeLO, vec2(.1,.08), .5);
    elli(eyeRI, vec2(.04,.02), -.5);
    elli(eyeLI, vec2(.05,.03), -.5);


    // Draw
    col += vec3(amp);
    col = pow(col, vec3(.4545)); //gamma correction
    col += .1*hash21(floor(111.*uv)*animTime)*(1.+.2*cos(20.*uv.y+animTime)); //noise
    col *= vec3(244,239,223)/255.; //sepia tint
    fragColor = vec4(col,1.0);
}
// --------[ Original ShaderToy ends here ]---------- //

void main(void)
{
    mainImage(gl_FragColor, gl_FragCoord.xy);
}