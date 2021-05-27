package com.github.natanbc.lavadsp.reverse;

import com.github.natanbc.lavadsp.util.FloatToFloatFunction;
import com.github.natanbc.lavadsp.util.VectorSupport;
import com.sedmelluq.discord.lavaplayer.filter.FloatPcmAudioFilter;

/**
 * https://github.com/Rishikeshdaoo/Reverberator/blob/master/Reverberator/src/com/rishi/reverb/Reverberation.javahttps://github.com/jnr/jnr-ffi/issues/86#issuecomment-250325189
 */
public class ReversePcmAudioFilter implements FloatPcmAudioFilter {
    private final FloatPcmAudioFilter downstream;
    private float reverse = 0f;
    
    public ReversePcmAudioFilter(FloatPcmAudioFilter downstream) {
        this.downstream = downstream;
    }
    
    /**
     * @return The current right-to-right factor. The default is 1.0.
     */
    public float getReverse() {
        return this.reverse;
    }
    
    /**
     * @param rightToRight The right-to-right factor. The default is 1.0.
     *
     * @return {@code this}, for chaining calls.
     */
    public ReversePcmAudioFilter setReverse(float reverse) {
        this.reverse = reverse;
        return this;
    }
    
    /**
     * Updates the right-to-right factor, using a function that accepts the current value
     * and returns a new value.
     *
     * @param function Function used to map the factor.
     *
     * @return {@code this}, for chaining calls
     */
    public ReversePcmAudioFilter updateReverse(FloatToFloatFunction function) {
        return setReverse(function.apply(reverse));
    }

    @Override
    public void process(float[][] input, int offset, int length) throws InterruptedException {
        
        if(input.length != 2) {
            if(this.reverse == 1f){
                reverseArray(input[0]);
            }
            downstream.process(input, offset, length);
            return;
        }
        if(this.reverse == 1f){
            reverseArray(input[0]);
            reverseArray(input[1]);
        }
        
        downstream.process(input, offset, length);
    }
    
    @Override
    public void seekPerformed(long requestedTime, long providedTime) {
        //nothing to do
    }
    
    @Override
    public void flush() {
        //nothing to do
    }
    
    @Override
    public void close() {
        //nothing to do
    }

    private float[] reverseArray(float arr[]){
        for (int start = 0, end = arr.length - 1; start <= end; ++start, --end) {
            float temp = arr[start];
            arr[start] = arr[end];
            arr[end] = temp;
        }
        return arr;
    }
}
