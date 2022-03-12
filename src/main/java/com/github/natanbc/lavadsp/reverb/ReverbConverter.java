package com.github.natanbc.lavadsp.reverb;

import java.util.Arrays;
import java.lang.Math;

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
		updateBuffer();
    }

	public void setDecay(float decay) {
        this.decay = decay;
    }

	public void setMixPercent(float mixPercent) {
        this.mixPercent = mixPercent * 100;
    }


	public void updateBuffer(){
		float[] newBuffer = new float[(int) (sampleRate * delayMilliseconds * 1000)];

		for(int i = 0; i < newBuffer.length; ++i){
			if(buffer.length <= i) break;
			newBuffer[i] = buffer[i];
		}
		buffer = newBuffer;
	}
    //https://github.com/Rishikeshdaoo/Reverberator/blob/master/Reverberator/src/com/rishi/reverb/Reverberation.java
    public void process(float[] input, int inputOffset, float[] output, int outputOffset, int samples){

		/*for(int i = 0; i < input.length - inputOffset; ++i){

		}*/

        float[] combFilterSamples1 = combFilter(input, samples, delayMilliseconds, decay, inputOffset);
		float[] combFilterSamples2 = combFilter(input, samples, (delayMilliseconds - 11.73f), (decay - 0.1313f), inputOffset);
		float[] combFilterSamples3 = combFilter(input, samples, (delayMilliseconds + 19.31f), (decay - 0.2743f), inputOffset);
		float[] combFilterSamples4 = combFilter(input, samples, (delayMilliseconds - 7.97f), (decay - 0.31f), inputOffset);

		//Adding the 4 Comb Filters
		float[] outputComb = new float[samples];
		for(int i = 0; i < samples; ++i) {
			outputComb[i] = ((combFilterSamples1[inputOffset + i] + combFilterSamples2[inputOffset + i] + combFilterSamples3[inputOffset + i] + combFilterSamples4[inputOffset + i])) ;
		}	   	

		//Deallocating individual Comb Filter array outputs
		combFilterSamples1 = null;
		combFilterSamples2 = null;
		combFilterSamples3 = null;
		combFilterSamples4 = null;

		//Algorithm for Dry/Wet Mix in the output audio
		float [] mixAudio = new float[samples];
		for(int i=0; i < samples; ++i)
			mixAudio[i] = ((100 - mixPercent) * input[inputOffset + i]) + (mixPercent * outputComb[i]); 


		//Method calls for 2 All Pass Filters. Defined at the bottom
		float[] allPassFilterSamples1 = allPassFilter(mixAudio, samples);
		float[] allPassFilterSamples2 = allPassFilter(allPassFilterSamples1, samples);

		for(int i = 0; i < samples; i++) {
			output[outputOffset + i] = allPassFilterSamples2[i];
		}	
    }

    //Method for Comb Filter
	public float[] combFilter(float[] input, int samples, float delay, float decay, int offset)
	{
		//Calculating delay in samples from the delay in Milliseconds. Calculated from number of samples per millisecond
		int delaySamples = (int) ((float)delay * (sampleRatekHz));

		float[] combFilterSamples = Arrays.copyOf(input, samples);

		//Applying algorithm for Comb Filter
		for (int i = 0; i < samples - delaySamples; i++){
			combFilterSamples[offset + i] += ((float)combFilterSamples[offset + i - delaySamples] * decay);
		}
	    return combFilterSamples;
	}

	//Method for All Pass Filter
	public float[] allPassFilter(float[] input, int samples)
	{
		int delaySamples = (int) ((float)89.27f * (sampleRatekHz)); // Number of delay samples. Calculated from number of samples per millisecond
		float[] allPassFilterInput = new float[samples];
		float decay = 0.131f;

		System.out.println(Integer.toString(delaySamples) + " delay");
		System.out.println(Integer.toString(allPassFilterInput.length) + " length");

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
