package papertoolkit.events.handlers;

import papertoolkit.events.EventHandler;
import papertoolkit.events.PenEvent;

/**
 * <p>
 * Unlike Java Swing's MouseListener, the Pen & Paper Click Handler cannot sense mouseover. Therefore, there
 * is no analogue to mouseEntered, mouseExited.
 * </p>
 * <p>
 * <span class="BSDLicense"> This software is distributed under the <a
 * href="http://hci.stanford.edu/research/copyright.txt">BSD License</a>. </span>
 * </p>
 * 
 * @author <a href="http://graphics.stanford.edu/~ronyeh">Ron B Yeh</a> (ronyeh(AT)cs.stanford.edu)
 */
public abstract class ClickHandler extends EventHandler {

	/**
	 * <p>
	 * Keep it with its abstract parent. Makes code that needs a ClickHandler a wee bit cleaner. <br>
	 * Extend this instead of ClickHandler.
	 * </p>
	 */
	public static class ClickAdapter extends ClickHandler {
		public void clicked(PenEvent e) {
		}

		public void pressed(PenEvent e) {
		}

		public void released(PenEvent e) {
		}
		
		public String toString() {
			return "ClickAdapter";
		}
	}

	/**
	 * <p>
	 * Does not notify a click until after a sufficiently long delay.
	 * </p>
	 */
	private class ClickNotifier implements Runnable {

		private boolean doNotNotify;

		private PenEvent event;

		public ClickNotifier(PenEvent myEvent) {
			event = myEvent;
		}

		/**
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
			try {
				Thread.sleep(MILLIS_TO_DELAY);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (doNotNotify) {
				// someone told us to cancel
				return;
			}
			released(event);
			clicked(event);
			lastClickTime = event.getTimestamp();
			penDownHappened = false;
		}

		/**
		 * @param b
		 */
		public void setDoNotNotify(boolean b) {
			doNotNotify = b;
		}
	}

	/**
	 * We assume a human cannot pen up and down within a 30 ms span. TODO: Move this functionality down into
	 * the PenServer...
	 */
	private static final long MILLIS_TO_DELAY = 30;

	/**
	 * Use this variable to see if there was a double click, while handing a clicked() event.
	 */
	private int consecutiveClickCount = 1;

	/**
	 * Whether we should filter out bad data received from bad streaming pens or poorly printed dot patterns.
	 */
	private boolean filterJitteryPenEvents = true;

	/**
	 * 
	 */
	private ClickNotifier lastClickNotifier;
	
	/**
	 * If the current click time is really close to the last click time, we can signal a double click.
	 */
	private long lastClickTime = 0;

	/**
	 * Keeps the most recent event, so that when pen up happens, we can give it an event that does not have
	 * ZERO coordinates.
	 */
	private PenEvent lastEvent;

	private long lastPenUpTime;

	/**
	 * 
	 */
	protected int maxMillisBetweenMultipleClicks = 300; // 300 ms for a double-click

	/**
	 * 
	 */
	private boolean penDownHappened = false;

	/**
	 * @param e
	 */
	public abstract void clicked(PenEvent e);

	/**
	 * @return
	 */
	public int getConsecutiveClickCount() {
		return consecutiveClickCount;
	}

	/**
	 * This method does the hard work of figuring out when a pen pressed, released, and clicked. It is up to
	 * the subclass to do something interesting with it once the events are triggered.
	 * 
	 * We also use the 20 ms filter heuristic that the InkCollector uses. We assume people can't click faster
	 * than 20ms.
	 * 
	 * @see papertoolkit.events.EventHandler#handleEvent(papertoolkit.events.PenEvent)
	 */
	public void handleEvent(PenEvent event) {
		if (event.isTypePenDown()) {
			long currPenDownTime = System.currentTimeMillis();
			long diff = currPenDownTime - lastPenUpTime;
			if (diff > MILLIS_TO_DELAY) {
				// long enough... so a new pen down!
				pressed(event);
				penDownHappened = true;
			} else {
				// just filter this out by cancelling the notifier
				if (lastClickNotifier != null) {
					lastClickNotifier.setDoNotNotify(true);
					lastClickNotifier = null;
				}
			}
		} else if (event.isTypePenUp()) {

			lastPenUpTime = System.currentTimeMillis();

			// really, this should always be true
			if (penDownHappened) {
				if (event.getTimestamp() - lastClickTime <= maxMillisBetweenMultipleClicks) {
					consecutiveClickCount++;
				} else {
					consecutiveClickCount = 1; // reset the click count
				}

				if (filterJitteryPenEvents) {
					lastClickNotifier = new ClickNotifier(lastEvent);
					new Thread(lastClickNotifier).start();
				} else {
					released(lastEvent);
					clicked(lastEvent);
					lastClickTime = event.getTimestamp();
					penDownHappened = false;
				}
			}
		}
		lastEvent = event;

		// do not consume the event (event has a consumed property that we do not set here)
	}

	/**
	 * @param e
	 */
	public abstract void pressed(PenEvent e);

	/**
	 * @param e
	 */
	public abstract void released(PenEvent e);

	/**
	 * @see papertoolkit.events.EventHandler#toString()
	 */
	public String toString() {
		return "ClickHandler";
	}
}
