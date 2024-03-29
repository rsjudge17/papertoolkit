package papertoolkit.actions.types;

import papertoolkit.actions.Action;
import papertoolkit.util.DebugUtils;

/**
 * <p>
 * Allows you to pass some information. Nothing happens if you invoke() it. The sole purpose of this
 * class is to pass an object to the other device. This is arguably the most powerful action, because
 * it allows the receiver to do anything it wants once it receives the action.
 * </p>
 * <p>
 * <span class="BSDLicense"> This software is distributed under the <a
 * href="http://hci.stanford.edu/research/copyright.txt">BSD License</a>. </span>
 * </p>
 * 
 * @author <a href="http://graphics.stanford.edu/~ronyeh">Ron B Yeh</a> (ronyeh(AT)cs.stanford.edu)
 */
public class MessageAction implements Action {

	/**
	 * The message to pass. It can be of any object (String is a good choice...).
	 * TODO: change this to a generic?
	 */
	private Object information;

	private String name;

	/**
	 * @param msgValue
	 */
	public MessageAction(String messageName, Object msgValue) {
		name = messageName;
		information = msgValue;
	}

	public Object getInformation() {
		return information;
	}

	public String getName() {
		return name;

	}

	/**
	 * @see papertoolkit.actions.Action#invoke()
	 */
	public void invoke() {
		DebugUtils.println("Got a message: " + name);
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "Message: " + name;
	}
}
