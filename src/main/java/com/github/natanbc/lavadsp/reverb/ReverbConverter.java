package com.github.natanbc.lavadsp.reverb;

import java.util.Arrays;

public class ReverbConverter {
    private final int sampleRate;
    private float delayMilliseconds = 0.0f;
    private float decay = 0.0f;
	private float mixPercent = 0.0f;
    
    public ReverbConverter(int sampleRate) {
        if(sampleRate < 1) {
            throw new IllegalArgumentException("Sample rate < 1");
        }
        this.sampleRate = sampleRate;
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
    
    public void process(float[] leftIn, float[] rightIn, int inputOffset,
                        float[] leftOut, float[] rightOut, int outputOffset, int samples) {

        int delaySamples = (int)(this.delayMilliseconds * (this.sampleRate / 1000)); // assumes kHz sample rate
        for(int i = 0; i < samples - delaySamples; ++i) {
            // WARNING: overflow potential
            float l = leftIn[inputOffset + i];
            float r = rightIn[inputOffset + i];
            
			leftOut[i + delaySamples + outputOffset] += (l * decay);
			rightOut[i + delaySamples + outputOffset] += (r * decay);
        }
    }
    //https://github.com/Rishikeshdaoo/Reverberator/blob/master/Reverberator/src/com/rishi/reverb/Reverberation.java
    public void process2(float[] input, int inputOffset, float[] output, int outputOffset, int samples){

        float[] combFilterSamples1 = this.combFilter(input, samples, delayMilliseconds, decay);
		float[] combFilterSamples2 = this.combFilter(input, samples, (delayMilliseconds - 11.73f), (decay - 0.1313f));
		float[] combFilterSamples3 = this.combFilter(input, samples, (delayMilliseconds + 19.31f), (decay - 0.2743f));
		float[] combFilterSamples4 = this.combFilter(input, samples, (delayMilliseconds - 7.97f), (decay - 0.31f));
		
		//Adding the 4 Comb Filters
		float[] outputComb = new float[samples];
		for(int i = 0; i < samples; i++) {
			outputComb[i] = ((combFilterSamples1[i] + combFilterSamples2[i] + combFilterSamples3[i] + combFilterSamples4[i])) ;
		}	   	
	
		//Deallocating individual Comb Filter array outputs
		combFilterSamples1 = null;
		combFilterSamples2 = null;
		combFilterSamples3 = null;
		combFilterSamples4 = null;
	
		//Algorithm for Dry/Wet Mix in the output audio
		float [] mixAudio = new float[samples];
		for(int i=0; i < samples; i++)
			mixAudio[i] = ((100 - mixPercent) * input[i]) + (mixPercent * outputComb[i]); 

		
		//Method calls for 2 All Pass Filters. Defined at the bottom
		float[] allPassFilterSamples1 = this.allPassFilter(mixAudio, samples);
		float[] allPassFilterSamples2 = this.allPassFilter(allPassFilterSamples1, samples);
    }

    //Method for Comb Filter
	public float[] combFilter(float[] input, int samples, float delayinMilliSeconds, float decayFactor)
	{
		//Calculating delay in samples from the delay in Milliseconds. Calculated from number of samples per millisecond
		int delaySamples = (int) ((float)delayinMilliSeconds * (sampleRate / 1000));
		
		float[] combFilterSamples = Arrays.copyOf(input, samples);
	
		//Applying algorithm for Comb Filter
		for (int i = 0; i < samples - delaySamples; i++){
			combFilterSamples[i+delaySamples] += ((float)combFilterSamples[i] * decayFactor);
		}
	    return combFilterSamples;
	}
	
	//Method for All Pass Filter
	public float[] allPassFilter(float[] input, int samples)
	{
		int delaySamples = (int) ((float)89.27f * (sampleRate / 1000)); // Number of delay samples. Calculated from number of samples per millisecond
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
