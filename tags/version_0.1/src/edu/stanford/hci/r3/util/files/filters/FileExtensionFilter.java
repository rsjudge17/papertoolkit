package edu.stanford.hci.r3.util.files.filters;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;

import edu.stanford.hci.r3.util.files.FileUtils;
import edu.stanford.hci.r3.util.files.Visibility;

/**
 * <p>
 * This software is distributed under the <a href="http://hci.stanford.edu/research/copyright.txt">
 * BSD License</a>.
 * </p>
 * 
 * @author <a href="http://graphics.stanford.edu/~ronyeh">Ron B Yeh</a> (ronyeh(AT)cs.stanford.edu)
 * 
 * A Java FileFilter that accepts file extensions as Strings
 */
public class FileExtensionFilter extends javax.swing.filechooser.FileFilter implements FileFilter,
		FilenameFilter {

	/**
	 * Are Directories OK?
	 */
	private boolean acceptDirectories = true;

	/**
	 * if BOTH, files like .bashrc and .bash_profile will be returned
	 * 
	 * if VISIBLE, these files are effectively hidden.
	 * 
	 * if INVISIBLE, only the hidden/.name files wil be returned
	 */
	private Visibility visibility = Visibility.BOTH;

	/**
	 * file extensions that are accepted. Defaults to all ("" --> ACCEPT ALL)
	 */
	private String[] extensions = { "" };

	/**
	 * @param exts
	 */
	public FileExtensionFilter(String[] exts) {
		this(exts, true, Visibility.BOTH);
	}

	/**
	 * @param exts
	 *            Pass in null or "" to accept all files. Otherwise, pass in an array of extensions
	 *            without dots, such as {"jpg", "jpeg", "mpg", "mpeg"}. The matching is case
	 *            INSENSITIVE.
	 * @param directories
	 *            true --> we will include directories.
	 * @param vis
	 *            BOTH --> we will include files or directories that are hidden or whose names start
	 *            with dots (e.g., .bashrc).
	 */
	public FileExtensionFilter(String[] exts, boolean directories, Visibility vis) {
		acceptDirectories = directories;
		visibility = vis;

		// pass in null to accept all files
		if (exts == null) {
			exts = new String[] {};
		}

		// copy the values over
		extensions = new String[exts.length];

		// lower case them all, for case insensitivity
		for (int i = 0; i < exts.length; i++) {
			extensions[i] = exts[i].toLowerCase();
		}
	}

	/**
	 * This method is for filechoosers, and makes use of acceptDirectories
	 * 
	 * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
	 * @note fixed bug from HCILib (which incorrectly says accept(null, ....))
	 */
	public boolean accept(File f) {
		return accept(f.getParentFile(), f.getName()) || (acceptDirectories && f.isDirectory());
	}

	/*
	 * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
	 */
	public boolean accept(File parentDir, String name) {
		if (name == null) {
			return false;
		}

		// the File or Directory to test for acceptance
		final File testFileOrDir = new File(parentDir, name);

		// try to filter out files/dirs that start with the dot or are hidden
		if (visibility == Visibility.VISIBLE) {
			if (FileUtils.isHiddenOrDotFile(testFileOrDir)) {
				return false;
			}
			// do nothing if it matches the flag
		} else if (visibility == Visibility.INVISIBLE) {
			if (!FileUtils.isHiddenOrDotFile(testFileOrDir)) {
				return false;
			}
			// do nothing if it matches the flag
		} else {
			// BOTH --> do nothing
		}

		// for directories, return false if we are not accepting directories, true otherwise
		if (testFileOrDir.isDirectory()) {
			if (!acceptDirectories) {
				return false;
			} else {
				return true; // all directories are OK, regardless of their name
			}
		}

		// empty array --> all files: because, why would anyone want to _exclude_ all files?
		if (extensions.length == 0) {
			return true;
		}

		// for all files, check whether they match the extension
		for (String extension : extensions) {
			// ends with the correct .extension?
			if (name.toLowerCase().endsWith("." + extension)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * A string description to display in file choosers.
	 * 
	 * @see javax.swing.filechooser.FileFilter#getDescription()
	 */
	public String getDescription() {
		final StringBuffer buf = new StringBuffer();
		int len = extensions.length;
		for (int i = 0; i < len; i++) {
			buf.append("*." + extensions[i]);

			// separate with commas
			if (i != len - 1) {
				buf.append(", ");
			}
		}
		return buf.toString();
	}

	/**
	 * If we are accepting directories, then ALL directories will be accepted. We do not apply
	 * extensions to directory names. This makes it so that directories will appear in the file
	 * chooser even if we have this FileExtensionFilter applied.
	 * 
	 * @param acceptDirs
	 */
	public void setAcceptDirectories(boolean acceptDirs) {
		acceptDirectories = acceptDirs;
	}
}