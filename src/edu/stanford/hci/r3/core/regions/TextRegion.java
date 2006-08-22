package edu.stanford.hci.r3.core.regions;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import edu.stanford.hci.r3.core.Region;
import edu.stanford.hci.r3.units.Points;
import edu.stanford.hci.r3.units.Units;

/**
 * <p>
 * This software is distributed under the <a href="http://hci.stanford.edu/research/copyright.txt">
 * BSD License</a>.
 * </p>
 * 
 * @author <a href="http://graphics.stanford.edu/~ronyeh">Ron B Yeh</a> (ronyeh(AT)cs.stanford.edu)
 * 
 * 
 * 
 */
public class TextRegion extends Region {

	private Font font;

	private String text;

	private Units originY;

	private Units originX;

	private Rectangle2D bounds;

	/**
	 * 
	 * @param theText
	 *            What text is displayed.
	 * @param theFont
	 *            The Font Family. Specify the size in points through the font object. We will
	 *            consider the point size of the font as an exact 1/72nd of an inch translation,
	 *            regardless of the device.
	 * @param origX
	 * @param origY
	 */
	public TextRegion(String theText, Font theFont, Units origX, Units origY) {
		super(origX);
		text = theText;
		font = theFont;

		originX = origX;
		originY = origY;

		// determine the font's boundaries
		// represent it as a Rectangle (x, y, w, h)
		final Dimension stringSize = getStringSize(text);
		final Rectangle2D rect = new Rectangle2D.Double(origX.getValue(), origY.getValueIn(units),
				new Points(stringSize.getWidth()).getValueIn(units), new Points(stringSize
						.getHeight()).getValueIn(units));
		bounds = rect;
		setShape(rect);
	}

	/**
	 * @return
	 */
	private Dimension getStringSize(String textToMeasure) {
		FontRenderContext fontRenderContext = new FontRenderContext(null, true, true);

		// break it up by lines (in case there are newlines "\n")
		String[] strings = textToMeasure.split("\n");

		double totalHeight = 0;
		double maxWidth = 0;

		for (String s : strings) {
			Rectangle2D stringBounds = font.getStringBounds(s, fontRenderContext);
			maxWidth = Math.max(maxWidth, stringBounds.getWidth());
			totalHeight += stringBounds.getHeight();
			System.out.println("Bounds: " + stringBounds);
		}

		Dimension dimension = new Dimension();
		dimension.setSize(maxWidth, totalHeight);
		return dimension;
	}

	/**
	 * @see edu.stanford.hci.r3.core.Region#toString()
	 */
	public String toString() {
		return "Text: {" + text + "} " + font.getSize() + "pt " + font.getName()
				+ " at Bounds: [x=" + originX.getValue() + " y=" + originY.getValue() + " w="
				+ bounds.getWidth() + " h=" + bounds.getHeight() + "] in " + units.getUnitName();
	}
}
