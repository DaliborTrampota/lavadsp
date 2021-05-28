package com.github.natanbc.lavadsp.reverb;

import java.util.Arrays;

public class ReverbConverter {
    private final int sampleRate;
    private final int sampleRatekHz;
    private float delayMilliseconds = 0.0f;
    private float decay = 0.0f;
	private float mixPercent = 0.0f;

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
    
	public void setMixPercent(float mixPercent) {
        this.mixPercent = mixPercent;
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

    //https://github.com/Rishikeshdaoo/Reverberator/blob/master/Reverberator/src/com/rishi/reverb/Reverberation.java
	public void process(float[] input, int inputOffset, float[] output, int outputOffset, int samples){
		
		for(int i = 0; i < samples; ++i){

			int curFrame = i + inputOffset;
			float in = input[curFrame];

			float comb1 = this.combFilterFrame(input, curFrame, samples, delayMilliseconds, decay);
			float comb2 = this.combFilterFrame(input, curFrame, samples, (delayMilliseconds - 11.73f), (decay - 0.1313f));
			float comb3 = this.combFilterFrame(input, curFrame, samples, (delayMilliseconds + 19.31f), (decay - 0.2743f));
			float comb4 = this.combFilterFrame(input, curFrame, samples, (delayMilliseconds - 7.97f), (decay - 0.31f));

			in = (comb1 + comb2 + comb3 + comb4);

			float out = ((100 - this.mixPercent) * input[curFrame]) + (this.mixPercent * in);

			//out = this.allPassFilterFrame(input, curFrame, samples);
			//out = this.allPassFilterFrame(input, curFrame, samples);

			output[i + outputOffset] = out;
		}
	}
	
	public float combFilterFrame(float[] input, int curFrame, int samples, float delayinMilliSeconds, float decayFactor){
		int delaySamples = (int)delayinMilliSeconds * this.sampleRatekHz;

		if(curFrame - delaySamples < 0) return input[curFrame];
		return input[curFrame - delaySamples] + input[curFrame] * decayFactor;
	}

	public float allPassFilterFrame(float[] input, int curFrame, int samples){
		int delaySamples = (int)(89.27f * this.sampleRatekHz); // Number of delay samples. Calculated from number of samples per millisecond

		//input[]
		return input[curFrame];
	}
	
	//Method for All Pass Filter
	public float[] allPassFilter(float[] input, int samples){
		int delaySamples = (int)(89.27f * this.sampleRatekHz); // Number of delay samples. Calculated from number of samples per millisecond
		float[] allPassFilterInput = new float[samples];
		float decay = 0.131f;

		//Applying algorithm for All Pass Filter
		for(int i = 0; i < samples; ++i){
			allPassFilterInput[i] = input[i];
		
			if(i - delaySamples >= 0)
				allPassFilterInput[i] += -decay * allPassFilterInput[i-delaySamples];
		
			if(i - delaySamples >= 1)
				allPassFilterInput[i] += decay * allPassFilterInput[i+20-delaySamples];
		}
		
	
		//This is for smoothing out the samples and normalizing the audio. Without implementing this, the samples overflow causing clipping of audio
		float value = allPassFilterInput[0];
		float max = 0.0f;
		
		for(int i = 0; i < samples; ++i) {
			if(Math.abs(allPassFilterInput[i]) > max)
				max = Math.abs(allPassFilterInput[i]);
		}
		
		for(int i = 0; i < allPassFilterInput.length; ++i) {
			float currentValue = allPassFilterInput[i];
			value = ((value + (currentValue - value))/max);

			allPassFilterInput[i] = value;
		}		
	    return allPassFilterInput;
	}
}
