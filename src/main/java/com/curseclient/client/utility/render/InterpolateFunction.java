package com.curseclient.client.utility.render;

@FunctionalInterface
public interface InterpolateFunction {

    float invoke(long time, float prev, float current);
}
