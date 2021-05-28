package com.github.natanbc.lavadsp.reverb;

import java.util.Arrays;
import java.lang.Math;

public class ReverbConverter {
    private final int sampleRate;
    private final int sampleRatekHz;
    private float delayMilliseconds = 0.0f;
	private float reverbTime = 0.0f;
    private float decay = 0.0f;

	private float[] buffer;
    
    public ReverbConverter(int sampleRate) {
        if(sampleRate < 1) {
            throw new IllegalArgumentException("Sample rate < 1");
        }
        this.sampleRate = sampleRate;
		this.sampleRatekHz = sampleRate / 1000;
    }
    
    public void setDelay(float ms) {
        this.delayMilliseconds = ms;
    }
    
    public void setDecay(float decay) {
        this.decay = decay;
    }

    public void setReverbTime(float reverbTime) {
        this.reverbTime = reverbTime;
    }

    
    /*public void process(float[] leftIn, float[] rightIn, int inputOffset,
                        float[] leftOut, float[] rightOut, int outputOffset, int samples) {

        int delaySamples = (int)(this.delayMilliseconds * (this.sampleRate / 1000)); // assumes kHz sample rate
        for(int i = 0; i < samples - delaySamples; ++i) {
            // WARNING: overflow potential
            float l = leftIn[inputOffset + i];
            float r = rightIn[inputOffset + i];
            
			leftOut[i + delaySamples + outputOffset] += (l * decay);
			rightOut[i + delaySamples + outputOffset] += (r * decay);
        }
    }*/

	public void process(float[] input, int inputOffset, float[] output, int outputOffset, int samples){
		
		int M = (int)this.sampleRatekHz * this.delayMilliseconds;

		for(int i = 0; i < samples; ++i){

			int curFrame = i + inputOffset;
			float in = input[curFrame];

			float[] copy = Arrays.copyOf(input, samples);
			copy[i]  = input[curFrame] + (this.calculateGain(this.delayMilliseconds) * copy[curFrame - M]);
			copy[i] += input[curFrame] + (this.calculateGain(this.delayMilliseconds + 200f) * copy[curFrame - M]);
			copy[i] += input[curFrame] + (this.calculateGain(this.delayMilliseconds + 400f) * copy[curFrame - M]);
			copy[i] += input[curFrame] + (this.calculateGain(this.delayMilliseconds + 600f) * copy[curFrame - M]);

			copy[i] = (-0.7 * input[curFrame]) + input[curFrame - M] + (0.7 * copy[curFrame - M])

			output[i + outputOffset] = copy[i];
		}
	}

	public float calculateGain(float delay){
		return Math.pow(2, ((-this.reverbTime) / 3) * delay);
	}

    //https://github.com/Rishikeshdaoo/Reverberator/blob/master/Reverberator/src/com/rishi/reverb/Reverberation.java
	
}
