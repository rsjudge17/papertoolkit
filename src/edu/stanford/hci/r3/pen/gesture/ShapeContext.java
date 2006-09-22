package edu.stanford.hci.r3.pen.gesture;

import java.util.ArrayList;

import edu.stanford.hci.r3.pen.streaming.PenSample;


/**
 * <p>
 * </p>
 * <p>
 * <span class="BSDLicense"> This software is distributed under the <a
 * href="http://hci.stanford.edu/research/copyright.txt">BSD License</a>.</span>
 * </p>
 * 
 * @author Avi Robinson-Mosher
 */
public class ShapeContext {
	
	ArrayList<PenSample> controlPoints = new ArrayList<PenSample>();

	public ShapeContext(ArrayList<PenSample> controlPointsInput) 
	{
		controlPoints = controlPointsInput;
		// there's me ANN done
		// ANN ann = new ANN();
	}
	
	public int size()
	{
		return controlPoints.size();
	}
	
	public double[][] points()
	{
		int N = size();
		double[][] pts = new double[N][2];
		for(int i=0;i<N;i++) {
			pts[i][0] = controlPoints.get(i).x;
			pts[i][1] = controlPoints.get(i).y;
		}
		return pts;
	}
	
	public ArrayList<PenSample> resample(int samples)
	{
		// special case
		if (samples == controlPoints.size()) return (ArrayList<PenSample>)controlPoints.clone();
		// want to return something with time information
		ArrayList<PenSample> sampledPoints = new ArrayList<PenSample>();
		assert(controlPoints.size() > 1);
		// sampling in time is a little weird; I'll sample in "space"
		float fraction = (controlPoints.size()-1)/(float)(samples);
		for(int i=0; i < samples; i++) {
			float position = fraction * i;
			int truncated = (int)position;
			float remainder = position - truncated;
			sampledPoints.add(blendHelper(truncated, remainder));
		}
		return sampledPoints;
	}

	public ArrayList<PenSample> tangents(int samples)
	{
		// want to return something with time information
		ArrayList<PenSample> sampledPoints = new ArrayList<PenSample>();
		assert(controlPoints.size() > 1);
		// sampling in time is a little weird; I'll sample in "space"
		float fraction = (controlPoints.size()-1)/(float)(samples);
		for(int i=0; i < samples; i++) {
			float position = fraction * i;
			int truncated = (int)position;
			float remainder = position - truncated;
			sampledPoints.add(tangent(truncated, remainder));
		}
		return sampledPoints;
	}

	// Catmull-Rom FTW
	PenSample blendHelper(int i, float t)
	{
		PenSample[] samples = new PenSample[4];
		PenSample blendedSample = new PenSample(0, 0, 0, 0);
		if (i == 0) // double up the first
			samples[0] = controlPoints.get(0);
		else samples[0] = controlPoints.get(i-1);
		samples[1] = controlPoints.get(i);
    		samples[2] = controlPoints.get(i+1);
		if (i + 2 >= controlPoints.size())
			samples[3] = controlPoints.get(controlPoints.size() - 1);
		else
			samples[3] = controlPoints.get(i+2);
		for(int ii=0; ii<4; ii++) {
			float b = blend(ii, t);
			blendedSample.x += samples[ii].x * b;
			blendedSample.y += samples[ii].y * b;
			blendedSample.timestamp += samples[ii].timestamp * b;
		}
		return blendedSample;
	}
	
	  float blend(int i, float t) {
		  switch (i) {
		  case 0:
			  return ((-t+2)*t-1)*t/2;
		  case 1:
			  return (((3*t-5)*t)*t+2)/2;
		  case 2:
			  return ((-3*t+4)*t+1)*t/2;
		  case 3:
		      return ((t-1)*t*t)/2;
		  }
		  return 0; // we only get here if an invalid i is specified
	  }

	  float dblend_du(int i, float t) {
		  switch (i) {
		  case 0:
			  return (4*t-1-3*t*t)/2;
		  case 1:
			  return (9*t*t-10*t)/2;
		  case 2:
			  return (8*t-9*t*t+1)/2;
		  case 3:
			  return (3*t*t-2*t)/2;
		  }
		  return 0; // we only get here if an invalid i is specified
	  }

	  
	  // all I actually need is x and y, or even angle. but this will do
	  PenSample tangent(int i, float t)
	  {
			PenSample[] samples = new PenSample[4];
			PenSample blendedSample = new PenSample(0, 0, 0, 0);
			if (i == 0) // double up the first
				samples[0] = controlPoints.get(0);
			else samples[0] = controlPoints.get(i-1);
			samples[1] = controlPoints.get(i);
			samples[2] = controlPoints.get(i+1);
			if (i + 2 >= controlPoints.size())
				samples[3] = controlPoints.get(controlPoints.size() - 1);
			else
				samples[3] = controlPoints.get(i+2);
			for(int ii=0; ii<4; ii++) {
				float b = dblend_du(ii, t);
				blendedSample.x += samples[ii].x * b;
				blendedSample.y += samples[ii].y * b;
				blendedSample.timestamp += samples[ii].timestamp * b;
			}
			return blendedSample;
	  }
	  
	  public ArrayList<ShapeHistogram> generateShapeHistogram(int points)
	  {
		  // histogram for each point
		  int dummy_points = this.controlPoints.size();
		  ArrayList<ShapeHistogram> histograms = new ArrayList<ShapeHistogram>();
		  ArrayList<PenSample> samples = resample(dummy_points);
		  ArrayList<PenSample> tangents = new ArrayList<PenSample>();// tangents(points);
		  for (int i=0; i<samples.size();i++) {
			  PenSample last,next;
			  if(i==0) last = samples.get(0);
			  else last = samples.get(i-1);
			  if(i==dummy_points-1) next = samples.get(dummy_points-1);
			  else next = samples.get(i+1);
			  tangents.add(new PenSample(0,next.x-last.x,next.y-last.y,0)); // crude
		  }
		  int bands = 3;
		  int[] bins = new int[3];
		  bins[0] = 5; // log r
		  bins[1] = 12; // theta
		  bins[2] = 2; // t (for the moment, only before/after)
		  // want a bin for "component" - how to discretize? connected ought to be fine. this may
			// be redundant with time...somewhat
		  double[] mins = new double[3];
		  mins[0] = Double.MAX_VALUE; // not; calc this based on actual min
		  mins[1] = -Math.PI; // yup
		  mins[2] = Double.MAX_VALUE; // not; calc on actual mins
		  double[] maxes = new double[3];
		  maxes[0] = -Double.MAX_VALUE;
		  maxes[1] = Math.PI;
		  maxes[2] = -Double.MAX_VALUE;
		  double distance_min=Double.MAX_VALUE;
		  double distance_max=-Double.MAX_VALUE;
		  double timestamp_min=Double.MAX_VALUE;
		  double timestamp_max=-Double.MAX_VALUE;
		  double sum = 0;
		  for (PenSample sample : samples) {
			  	// wrong, should be considering deltas.
				// Alt, normalize all the times ahead.
			  if (sample.timestamp < timestamp_min) timestamp_min = sample.timestamp; 
			  if (sample.timestamp > timestamp_max) timestamp_max = sample.timestamp;
			  for (PenSample secondSample : samples) {
				  if (sample.equals(secondSample)) continue;
				  double distance = Math.sqrt(Math.pow(sample.x - secondSample.x, 2) + Math.pow(sample.y - secondSample.y, 2));
				  sum += distance;
				  if (distance > 0) {
					  distance_min = Math.min(distance_min, distance);
					  distance_max = Math.max(distance_max, distance);}
			  }
		  }
		  sum /= (dummy_points * (dummy_points - 1)) / 2;
		  mins[0] = Math.log(distance_min)-.01;
		  maxes[0] = Math.log(distance_max)+.01;
		  mins[2] = timestamp_min - timestamp_max;
		  maxes[2] = -mins[2];
		  // mins[0] = Math.log(distance_min);
		  // maxes[0] = Math.log(distance_max);
		  for (PenSample sample : samples) {
			  ShapeHistogram histogram = new ShapeHistogram(bins, mins, maxes, bands);
			  PenSample tangent = tangents.get(samples.indexOf(sample)); // sue me, I'm lax
			  double theta = Math.atan2(tangent.y, tangent.x);
			  for (PenSample secondSample : samples) {
				  if (sample.equals(secondSample)) continue;
				  // for rotation invariance, adjust angles by setting normal to curve to some
					// axis. slightly tricky, but not too bad.
				  // I guess I'll do this the easy way (via spline lookups and calculation). Is
					// there an analytic way? Probably. But I'm lazy.
				  histogram.addPoint(logPolarAndTime(sample, secondSample, mins[0]/* sum * sum */, theta));
			  }
			  histograms.add(histogram);
		  }
		  for (int i = 0; i < points - samples.size(); i++) {
			  histograms.add(new ShapeHistogram(bins, mins, maxes, bands)); // blank histograms
		  }
		  return histograms;
	  }
	  
	  static double[] logPolarAndTime(PenSample first, PenSample second, double distanceScaling, double baseRotation)
	  {
		  // normalize the times. actually, probably ought to normalize all of them -
			// scale-invariance? not theta, though.
		  double dx = second.x - first.x;
		  double dy = second.y - first.y;
		  double[] results = new double[3];
		  if (dx == 0 && dy == 0) {
			  results[0] = distanceScaling; // hack
			  results[1] = 0;
		  }
		  else {
			  results[0] = .5 * Math.log((dx*dx + dy*dy)/* / distanceScaling */);
			  results[1] = renormalize(Math.atan2(dy, dx) - baseRotation);
		  }
		  results[2] = second.timestamp - first.timestamp;
		  return results;
	  }
	  
	  static double renormalize(double theta)
	   { // cast into -PI to PI
		  if(theta < -Math.PI) return theta+2*Math.PI;
		  if(theta > Math.PI) return theta-2*Math.PI;
		  return theta;
		  
	  }
	  
	  // pseudocode
		/*
		 * 
		 * constructor(points[]) {create shapehistogram for each point}
		 * 
		 * double[] logpolar(point1,point2) given input points, gets logpolar offset
		 * 
		 * distance(ShapeContext) need to be able to sample a pointset to get the right number of
		 * points to match. do this via spline interpolation
		 * 
		 * 
		 * bipartite matching
		 */

}
