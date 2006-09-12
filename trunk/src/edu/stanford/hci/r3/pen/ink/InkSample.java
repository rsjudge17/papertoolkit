package edu.stanford.hci.r3.pen.ink;

/**
 * <p>Just a low-overhead struct to stored ink samples.</p>
 * <p><span class="BSDLicense">
 * This software is distributed under the <a href="http://hci.stanford.edu/research/copyright.txt">BSD License</a>.
 * </span></p>
 * 
 * @author <a href="http://graphics.stanford.edu/~ronyeh">Ron B Yeh</a> (ronyeh(AT)cs.stanford.edu)
 */
public class InkSample {
	public double x;
	public double y;
	public int force;
	public long timestamp;
}
