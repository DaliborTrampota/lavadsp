/*
 * Copyright 2018 natanbc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.natanbc.lavadsp.reverb;

import com.github.natanbc.lavadsp.util.FloatToFloatFunction;
import com.sedmelluq.discord.lavaplayer.filter.FloatPcmAudioFilter;

public class ReverbPcmAudioFilter implements FloatPcmAudioFilter {
    private final FloatPcmAudioFilter downstream;
    private final ReverbConverter converter;

    private volatile float delayMilliseconds = 0.0f;
    private volatile float decay = 0.0f;
    private volatile float mixPercent = 0.0f;

    public ReverbPcmAudioFilter(FloatPcmAudioFilter downstream, int channelCount, int sampleRate) {
        this.downstream = downstream;
        if(channelCount == 2) {
            this.converter = new ReverbConverter(sampleRate);
        } else {
            this.converter = null;
        }
    }

    /**
     * Returns whether or not this converter is enabled. It will only be enabled if
     * the channel count given to the constructor is {@code 2}. If it's a different value,
     * this filter is no-op.
     *
     * @return Whether or not this filter is enabled.
     */
    public boolean isEnabled() {
        return converter != null;
    }

    /**
     * Returns the current level.
     *
     * @return The current level.
     */
    public float getDecay() {
        return this.decay;
    }

    /**
     * Sets the effect decay.
     *
     * @param level Decay to set.
     *
     * @return {@code this}, for chaining calls
     */
    public ReverbPcmAudioFilter setDecay(float decay) {
        this.decay = decay;
        if(converter != null) {
            converter.setDecay(decay);
        }
        return this;
    }

    /**
     * Updates the effect level, using a function that accepts the current value
     * and returns a new value.
     *
     * @param function Function used to map the level.
     *
     * @return {@code this}, for chaining calls
     */
    public ReverbPcmAudioFilter updateDecay(FloatToFloatFunction function) {
        return setDecay(function.apply(decay));
    }

    /**
     * Returns the current mono level.
     *
     * @return The current mono level.
     */
    public float getDelay() {
        return this.delayMilliseconds;
    }

    /**
     * Sets the effect mono level.
     *
     * @param level Mono level to set.
     *
     * @return {@code this}, for chaining calls
     */
    public ReverbPcmAudioFilter setDelay(float delay) {
        this.delayMilliseconds = delay;
        if(converter != null) {
            converter.setDelay(delay);
        }
        return this;
    }

    /**
     * Updates the effect mono level, using a function that accepts the current value
     * and returns a new value.
     *
     * @param function Function used to map the mono level.
     *
     * @return {@code this}, for chaining calls
     */
    public ReverbPcmAudioFilter updateDelay(FloatToFloatFunction function) {
        return setDelay(function.apply(delayMilliseconds));
    }

/**
     * Returns the current mono level.
     *
     * @return The current mono level.
     */
    public float getMixPercent() {
        return this.mixPercent;
    }

    /**
     * Sets the effect mono level.
     *
     * @param level Mono level to set.
     *
     * @return {@code this}, for chaining calls
     */
    public ReverbPcmAudioFilter setMixPercent(float mixPercent) {
        this.mixPercent = mixPercent;
        if(converter != null) {
            converter.setMixPercent(mixPercent);
        }
        return this;
    }

    /**
     * Updates the effect mono level, using a function that accepts the current value
     * and returns a new value.
     *
     * @param function Function used to map the mono level.
     *
     * @return {@code this}, for chaining calls
     */
    public ReverbPcmAudioFilter updateMixPercent(FloatToFloatFunction function) {
        return setMixPercent(function.apply(mixPercent));
    }

    @Override
    public void process(float[][] input, int offset, int length) throws InterruptedException {
        if(converter == null || input.length != 2) {
            float[] stream = input[0];
            converter.processOne(stream, offset, stream, 0, length);
            downstream.process(input, offset, length);
            return;
        }
        float[] left = input[0];
        float[] right = input[1];

        converter.process(left, right, offset, left, right, 0, length);
        downstream.process(input, 0, length);
    }

    @Override
    public void seekPerformed(long requestedTime, long providedTime) {
        //nothing to do here
    }

    @Override
    public void flush() {
        //nothing to do here
    }

    @Override
    public void close() {
        //nothing to do here
    }
}
