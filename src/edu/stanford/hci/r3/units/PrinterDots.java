/**
 * 
 */
package edu.stanford.hci.r3.units;

import edu.stanford.hci.r3.printing.Printer;
import edu.stanford.hci.r3.printing.Printers;

/**
 * <p>
 * This software is distributed under the <a href="http://hci.stanford.edu/research/copyright.txt">
 * BSD License</a>.
 * </p>
 * 
 * @author <a href="http://graphics.stanford.edu/~ronyeh">Ron B Yeh</a> (ronyeh(AT)cs.stanford.edu)
 */
public class PrinterDots extends Units {

	/**
	 * An Identity Element, representing one printer dot of the default printer (at default DPI).
	 * For convenience.
	 */
	public static final Units ONE = new PrinterDots(1.0);

	private Printer printer;

	/**
	 * Create it for the current printer.
	 */
	public PrinterDots(double val) {
		value = val;
		printer = Printers.getDefaultPrinter();
	}

	/**
	 * @param p
	 * @param val
	 *            value of printer dots will represent a different physical length, depending on the
	 *            printer's DPI
	 */
	public PrinterDots(Printer p, double val) {
		value = val;
		printer = p;
	}

	/**
	 * @see edu.stanford.hci.r3.units.Units#getNumberOfUnitsInOneInch()
	 */
	@Override
	public double getNumberOfUnitsInOneInch() {
		return printer.getDPI();
	}
}