package edu.stanford.hci.r3.render;

import java.io.File;

import edu.stanford.hci.r3.paper.Region;
import edu.stanford.hci.r3.paper.Sheet;
import edu.stanford.hci.r3.units.Inches;

/**
 * <p>
 * This software is distributed under the <a href="http://hci.stanford.edu/research/copyright.txt">
 * BSD License</a>.
 * </p>
 * 
 * @author <a href="http://graphics.stanford.edu/~ronyeh">Ron B Yeh</a> (ronyeh(AT)cs.stanford.edu)
 * 
 * Turns a Sheet object into a PDF file.
 */
public class SheetRendererTest {

	/**
	 * Creates a Sheet object and populates it with some Regions.
	 * 
	 * @return
	 */
	public static Sheet createSheet() {
		Sheet sheet = new Sheet(new Inches(15), new Inches(15));

		// define some regions
		Region reg = new Region(0, 0, 15, 15);
		reg.setActive(true);

		// add regions to the sheet
		sheet.addRegion(reg);

		return sheet;

	}

	/**
	 * Runs the test.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		sheetToPDF();
	}

	/**
	 * Renders a sheet to a JPEG file.
	 */
	private static void sheetToJPEG() {
		Sheet sheet = createSheet();

		SheetRenderer renderer = new SheetRenderer(sheet);
		renderer.setRenderActiveRegionsWithPattern(false);

		renderer.renderToJPEG(new File("data/testFiles/Test.jpg"));
	}

	/**
	 * Renders a sheet to a PDF file.
	 */
	private static void sheetToPDF() {
		Sheet sheet = createSheet();
		SheetRenderer renderer = new SheetRenderer(sheet);
		renderer.renderToPDF(new File("data/testFiles/Test.pdf"));
	}
}