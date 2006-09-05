package edu.stanford.hci.r3.pattern.coordinates;

import edu.stanford.hci.r3.units.PatternDots;
import edu.stanford.hci.r3.units.Percentage;
import edu.stanford.hci.r3.units.coordinates.PercentageCoordinates;
import edu.stanford.hci.r3.units.coordinates.StreamedPatternCoordinates;

/**
 * <p>
 * Stores the bounds in physical (streaming) coordinates.
 * </p>
 * <p>
 * <span class="BSDLicense"> This software is distributed under the <a
 * href="http://hci.stanford.edu/research/copyright.txt">BSD License</a>. </span>
 * </p>
 * 
 * @author <a href="http://graphics.stanford.edu/~ronyeh">Ron B Yeh</a> (ronyeh(AT)cs.stanford.edu)
 */
public class TiledPatternCoordinateConverter {

	private int dotsPerTileHorizontal;

	private int dotsPerTileVertical;

	/**
	 * Right Boundary in Physical Coordinates. Thus, even if your tiling looks like this:
	 * <blockquote><code>
	 *  [0][1][2]<br>
	 *  [3][4][5]
	 * </code></blockquote>
	 * 
	 * Your maxX could be huge because in reality your pattern space looks like this: <blockquote><code>
	 *  [0][1][2][3][4][5]
	 * </code></blockquote>
	 * 
	 * Similarly, maxY might be not so big after all...
	 */
	private double maxX;

	/**
	 * Left Boundary
	 */
	private double maxY;

	/**
	 * Difference in X Values between subsequent tiles...
	 */
	private double numDotsHorizontalBetweenTiles;

	/**
	 * Difference in Y Values between subsequent tiles...
	 */
	private double numDotsVerticalBetweenTiles;

	/**
	 * <blockquote><code>
	 *  [0][1][2]<br>
	 *  [3][4][5]
	 * </code></blockquote>
	 * 
	 * 3 tiles across in the above example.
	 */
	private int numTilesAcross;

	/**
	 * 2 tiles down in the above example.
	 */
	private int numTilesDown;

	private double numTotalDotsAcross;

	private PatternDots numTotalDotsAcrossObj;

	private double numTotalDotsDown;

	private PatternDots numTotalDotsDownObj;

	private double originX;

	private double originY;

	/**
	 * Name the top-left tile anything you want.
	 */
	private int startingTile;

	private double tileHeightIncludingPadding;

	private double tileWidthIncludingPadding;

	private int numTiles;

	/**
	 * <p>
	 * This object deals with physical coordinates (the type that you get when you stream
	 * coordinates from the Nokia SU-1B). They are all huge numbers, but we store them in
	 * PatternDots objects. Although we can convert the PatternDots objects into other Units, it
	 * doesn't really make sense, as the dots are specified in the world of Anoto's gargantuan
	 * pattern space. For example, if you converted the xOrigin to inches, you would get a beast of
	 * a number.
	 * </p>
	 * <p>
	 * We precompute the boundaries so that we can do contains(...) tests faster.
	 * </p>
	 * 
	 * @param startTile
	 *            the number that you assign to the top-left tile
	 * @param nTilesAcross
	 *            width of this tile configuration, in # of tiles
	 * @param nTilesDown
	 *            height of this tile configuration, in # of tiles
	 * @param dotsPerTileHoriz
	 *            width of each tile, in # dots
	 * @param dotsPerTileVert
	 *            height of each tile, in # dots
	 * @param leftMostPatternX
	 *            left boundary in pattern dots
	 * @param topMostPatternY
	 *            top boundary, in pattern dots
	 * @param numDotsAcross
	 *            width of entire region in dots, not including the between-tile padding
	 * @param numDotsDown
	 *            height of entire region in dots, not including the padding in between tiles
	 * @param numHorizDotsBetweenTiles
	 *            the horizontal padding between adjacent tiles
	 * @param numVertDotsBetweenTiles
	 *            the vertical padding between adjacent tiles (tends to be 0, in our experience)
	 */
	public TiledPatternCoordinateConverter(int startTile, int nTilesAcross, int nTilesDown,
			int dotsPerTileHoriz, int dotsPerTileVert, double numHorizDotsBetweenTiles,
			double numVertDotsBetweenTiles, double leftMostPatternX, double topMostPatternY,
			double numDotsAcross, double numDotsDown) {
		// the number of the first (top-left) tile; this is largely arbitrary, but _may_ correlate
		// with a pattern file number N.pattern --> N as a starting tile number. This makes
		// calculations easier for certain operations, such as finding out which page of a notebook
		// your user has written on.
		startingTile = startTile;

		// the number of tiles owned by this converter. Usually, this converter will map to a region
		// on a sheet. This means that the tiledPatternConverter will need to know how many tiles of
		// pattern the region contains. It will then help us find out where on the region we are.
		numTilesAcross = nTilesAcross;
		numTilesDown = nTilesDown;
		numTiles = numTilesAcross * numTilesDown;

		// how wide and tall are these tiles? We assume uniform tiles (except for the rightmost and
		// bottommost tiles)
		dotsPerTileHorizontal = dotsPerTileHoriz;
		dotsPerTileVertical = dotsPerTileVert;

		// what is the physical coordinate of the top-left corner of the top-left tile?
		originX = leftMostPatternX;
		originY = topMostPatternY;

		// how wide and tall is the whole region?
		numTotalDotsAcross = numDotsAcross;
		numTotalDotsDown = numDotsDown;

		// save this for later calculations
		numTotalDotsAcrossObj = new PatternDots(numTotalDotsAcross);
		numTotalDotsDownObj = new PatternDots(numTotalDotsDown);

		// what is the x offset between the origins of two adjacent tiles?
		// this is the width of a tile, plus a padding that anoto creates when you use the FDK to
		// generate pattern
		numDotsHorizontalBetweenTiles = numHorizDotsBetweenTiles;
		// what is the y offset between the origins of two adjacent tiles?
		// note that in the BNet pattern space, the y offset is 0
		numDotsVerticalBetweenTiles = numVertDotsBetweenTiles;

		// Save some values to make calculations easier later on
		// 
		// ASSUMPTION: Either the dots between tiles is larger than a single tile OR it is 0.
		// Weird things can happen if the tiles are staggered, such that the dots between tiles is
		// smaller than the width or height of a tile.
		if (numDotsHorizontalBetweenTiles < dotsPerTileHorizontal) { // dX = 0
			tileWidthIncludingPadding = dotsPerTileHorizontal;
			maxX = originX + dotsPerTileHorizontal; // only one horizontal tile
		} else { // bigger than the width
			tileWidthIncludingPadding = numDotsHorizontalBetweenTiles;
			maxX = originX + numDotsHorizontalBetweenTiles * numTiles;
		}

		if (numDotsVerticalBetweenTiles < dotsPerTileVertical) { // dY = 0
			tileHeightIncludingPadding = dotsPerTileVertical;
			maxY = originY + dotsPerTileVertical; // only one vertical tile, like bnet's pattern space
		} else { // bigger than the height
			tileHeightIncludingPadding = numDotsVerticalBetweenTiles;
			maxY = originY + numDotsVerticalBetweenTiles * numTiles;
		}
	}

	/**
	 * For performance, we precompute the boundaries and store just those numbers. This method's
	 * likely faster than the other contains test, especially if you already have the x and y values
	 * and do not need to create a StreamedPatternLocation object.
	 * 
	 * @param xValPatternDots
	 *            x value of the location, in PatternDots (physical/streamed coordinates)
	 * @param yValPatternDots
	 *            y value of the location, in PatternDots (physical/streamed coordinates)
	 * @return
	 */
	public boolean contains(final double xValPatternDots, final double yValPatternDots) {
		// has to be to the right of the leftmost border
		boolean insideLeftBoundary = xValPatternDots >= originX;

		// has to be below the top border
		boolean insideTopBoundary = yValPatternDots >= originY;

		// has to be to the left of the rightmost border
		boolean insideRightBoundary = xValPatternDots < maxX;

		// has to be above the bottom border
		boolean insideBottomBoundary = yValPatternDots < maxY;

		// has to NOT fall in between the gaps between pages...
		boolean notInHorizontalGap = ((xValPatternDots - originX) % tileWidthIncludingPadding) < dotsPerTileHorizontal;
		boolean notInVerticalGap = ((yValPatternDots - originY) % tileHeightIncludingPadding) < dotsPerTileVertical;

		return insideLeftBoundary && insideTopBoundary && insideRightBoundary
				&& insideBottomBoundary && notInHorizontalGap && notInVerticalGap;
	}

	/**
	 * If you have the values and do not need to create a StreamedPatternCoordinates object, use the
	 * other contains(...) method.
	 * 
	 * @param location
	 * @return whether the tile configuration contains this location
	 */
	public boolean contains(StreamedPatternCoordinates location) {
		final double xTestVal = location.getXVal();
		final double yTestVal = location.getYVal();
		return contains(xTestVal, yTestVal);
	}

	/**
	 * Convert the input coordinate into a percentage location relative to this tile configuration
	 * (a region that has tiled pattern)
	 * 
	 * @return
	 */
	public PercentageCoordinates getRelativeLocation(StreamedPatternCoordinates coord) {

		final double xOffset = coord.getXVal() - originX;
		final double yOffset = coord.getYVal() - originY;

		final double xRelativeToTile = xOffset
				- (getTileNumHorizontal(coord) * numDotsHorizontalBetweenTiles);
		final double yRelativeToTile = yOffset
				- (getTileNumVertical(coord) * numDotsVerticalBetweenTiles);

		final int tileOffset = getTileNumber(coord) - startingTile;

		final int tileRow = tileOffset / numTilesAcross;
		final int tileCol = tileOffset % numTilesAcross;

		final double totalDotsX = tileCol * dotsPerTileHorizontal + xRelativeToTile;
		final double totalDotsY = tileRow * dotsPerTileVertical + yRelativeToTile;

		final double pctX = totalDotsX / numTotalDotsAcross * 100;
		final double pctY = totalDotsY / numTotalDotsDown * 100;

		return new PercentageCoordinates( // 
				new Percentage(pctX, numTotalDotsAcrossObj), // fraction of width
				new Percentage(pctY, numTotalDotsDownObj)); // fraction of height
	}

	/**
	 * It tries to calculate the tile number given an input coordinate.
	 * 
	 * <blockquote><code>
	 *  [0][1][2]<br>
	 *  [3][4][5]
	 * </code></blockquote>
	 * 
	 * This should work if the pattern space is short and wide, or thin and tall...
	 * 
	 * @param coord
	 * @return the tile number of this coordinate. Returns -1 if this coordinate is not contained by
	 *         this tile configuration.
	 */
	public int getTileNumber(StreamedPatternCoordinates coord) {
		if (!contains(coord)) {
			return -1;
		}

		// using the x or y coordinate, determine the tile number
		// depending on the orientation of the pattern, these numbers may not be the same (i.e., one
		// of them may be zero while the other is not)
		final int tileNumHoriz = getTileNumHorizontal(coord);
		final int tileNumVert = getTileNumVertical(coord);

		return startingTile + Math.max(tileNumHoriz, tileNumVert);
	}

	/**
	 * @param coord
	 * @return
	 */
	private int getTileNumHorizontal(StreamedPatternCoordinates coord) {
		return (int) Math.floor((coord.getXVal() - originX) / tileWidthIncludingPadding);
	}

	/**
	 * @param coord
	 * @return
	 */
	private int getTileNumVertical(StreamedPatternCoordinates coord) {
		return (int) Math.floor((coord.getYVal() - originY) / tileHeightIncludingPadding);
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "TiledPatternCoordinateConverter";
	}
}