package papertoolkit.paper.regions;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.geom.Rectangle2D;

import papertoolkit.paper.Region;
import papertoolkit.render.RegionRenderer;
import papertoolkit.render.regions.TextRenderer;
import papertoolkit.units.Inches;
import papertoolkit.units.Points;
import papertoolkit.units.Units;
import papertoolkit.util.StringUtils;


/**
 * <p>
 * Represents some text that can be drawn on a page.
 * </p>
 * <p>
 * <span class="BSDLicense"> This software is distributed under the <a
 * href="http://hci.stanford.edu/research/copyright.txt">BSD License</a>. </span>
 * </p>
 * 
 * @author <a href="http://graphics.stanford.edu/~ronyeh">Ron B Yeh</a> (ronyeh(AT)cs.stanford.edu)
 */
public class TextRegion extends Region {

	private static final Font DEFAULT_FONT = new Font("Trebuchet MS", Font.PLAIN, 12);

	/**
	 * The bounds of the text region... in the units of referenceUnit.
	 */
	private Rectangle2D bounds;

	/**
	 * 
	 */
	private Color color = new Color(100, 100, 100, 128);

	/**
	 * 
	 */
	private Font font;

	/**
	 * 
	 */
	private Points heightInPoints;

	/**
	 * True if the text should be automatically line wrapped to the width of the region. If this is true, the
	 * lines will not go beyond the region in either dimension.
	 */
	private boolean isLineWrapped = false;

	/**
	 * In case the 'text' variable is multiline, we store each individual line in this array.
	 */
	private String[] lines;

	/**
	 * Maximum number of lines to typeset. If <= 0, all lines will be typeset (up to the size of the region)
	 */
	private int maxLines = -1;

	/**
	 * 
	 */
	private Units originX;

	/**
	 * 
	 */
	private Units originY;

	/**
	 * 
	 */
	private String text;

	/**
	 * 
	 */
	private Points widthInPoints;

	/**
	 * The quick and dirty constructor... 12 Point, Trebuchet MS
	 * 
	 * @param theText
	 * @param xInches
	 * @param yInches
	 */
	public TextRegion(String theText, double xInches, double yInches) {
		this("Text_" + theText, theText, DEFAULT_FONT, new Inches(xInches), new Inches(yInches));
	}

	/**
	 * 
	 * @param theText
	 *            What text is displayed.
	 * @param theFont
	 *            The Font Family. Specify the size in points through the font object. We will consider the
	 *            point size of the font as an exact 1/72nd of an inch translation, regardless of the device.
	 * @param origX
	 * @param origY
	 */
	public TextRegion(String name, String theText, Font theFont, Units origX, Units origY) {
		super(name, origX);
		text = theText;
		font = theFont;

		originX = origX;
		originY = origY;

		lines = theText.split("\n");

		// determine the font's boundaries
		// represent it as a Rectangle (x, y, w, h)
		final Dimension stringSize = StringUtils.getStringSize(text, font);
		heightInPoints = new Points(stringSize.getHeight());
		widthInPoints = new Points(stringSize.getWidth());
		final Rectangle2D rect = new Rectangle2D.Double(origX.getValue(), origY.getValueIn(referenceUnits),
				widthInPoints.getValueIn(referenceUnits), heightInPoints.getValueIn(referenceUnits));
		bounds = rect;
		setShape(rect);
	}

	/**
	 * @param name
	 * @param theText
	 * @param theFont
	 * @param origX
	 * @param origY
	 * @param width
	 *            override the text's actual width with this value
	 * @param height
	 *            override the text's actual height with this value
	 */
	public TextRegion(String name, String theText, Font theFont, Units origX, Units origY, Units width,
			Units height) {
		super(name, origX);
		text = theText;
		font = theFont;

		originX = origX;
		originY = origY;

		lines = theText.split("\n");

		// determine the font's boundaries
		// represent it as a Rectangle (x, y, w, h)
		final Dimension stringSize = StringUtils.getStringSize(text, font);
		heightInPoints = new Points(stringSize.getHeight());
		widthInPoints = new Points(stringSize.getWidth());
		final Rectangle2D rect = new Rectangle2D.Double(origX.getValue(), origY.getValueIn(referenceUnits),
				width.getValueIn(referenceUnits), height.getValueIn(referenceUnits));
		bounds = rect;
		setShape(rect);
	}

	/**
	 * @return
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * @return
	 */
	public Font getFont() {
		return font;
	}

	/**
	 * @return
	 */
	public String[] getLinesOfText() {
		return lines;
	}

	/**
	 * @return the maximum number of lines to be set. If <= 0, all lines will be set.
	 */
	public int getMaxLines() {
		return maxLines;
	}

	/**
	 * @see papertoolkit.paper.Region#getRenderer()
	 */
	public RegionRenderer getRenderer() {
		return new TextRenderer(this);
	}

	/**
	 * @return the internal text to be rendered.
	 */
	public String getText() {
		return text;
	}

	/**
	 * @return
	 */
	public Units getX() {
		return originX;
	}

	/**
	 * @return
	 */
	public Units getY() {
		return originY;
	}

	/**
	 * @return whether automatic line wrapping should occur. If true, the text will be constrained to the
	 *         region boundaries.
	 */
	public boolean isLineWrapped() {
		return isLineWrapped;
	}

	/**
	 * @param c
	 */
	public void setColor(Color c) {
		color = c;
	}

	/**
	 * @param b
	 *            whether automatic line wrapping should occur. If ture, the text will be constrained to the
	 *            region boundaries.
	 */
	public void setLineWrapped(boolean b) {
		isLineWrapped = b;
	}

	/**
	 * @param i
	 *            the maximum number of lies to set. If <= 0, all lines will be set.
	 */
	public void setMaxLines(int i) {
		maxLines = i;
	}

	/**
	 * @see papertoolkit.paper.Region#toString()
	 */
	public String toString() {
		return "Text: {" + text + "} " + font.getSize() + "pt " + font.getName() + " at Bounds: [x="
				+ originX.getValue() + " y=" + originY.getValue() + " w=" + bounds.getWidth() + " h="
				+ bounds.getHeight() + "] in " + referenceUnits.getUnitName();
	}
}
