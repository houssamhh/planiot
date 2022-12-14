/**
 * Copyright (C) 2016, Laboratorio di Valutazione delle Prestazioni - Politecnico di Milano

 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 */
package jmt.gui.common.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import jmt.gui.common.CommonConstants;
import jmt.gui.common.JMTImageLoader;

/**
 * <p>Title: Warning Window</p>
 * <p>Description: This class provides a window to show warnings generated by model conversion.
 * Warnings are expected tobe ctored in a Vector of Strings.</p>
 *
 * @author Bertoli Marco
 *         Date: 1-mar-2006
 *         Time: 14.34.42
 */
public class WarningWindow {

	private JDialog dialog;
	private List<String> warnings;
	private JLabel header;

	private static final int BORDER = 20;

	/**
	 * Creates a new WarningWindow to show specified warnings
	 * @param warnings warnings to be shown
	 * @param owner owner of the shown dialog. If it is not a Dialog nor a Frame, created
	 * window will not be modal.
	 */
	public WarningWindow(List<String> warnings, Window owner, String from, String to) {
		if (owner instanceof Dialog) {
			dialog = new JDialog((Dialog) owner, true);
		} else if (owner instanceof Frame) {
			dialog = new JDialog((Frame) owner, true);
		} else {
			dialog = new JDialog();
		}
		this.warnings = warnings;
		initDialog(from, to);
	}

	/**
	 * Initialize internal dialog to be shown
	 */
	private void initDialog(String from, String to) {
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		dialog.setTitle("Model conversion terminated with warnings");
		int width = 600, height = 450;

		// Centers this dialog on the screen
		Dimension scrDim = Toolkit.getDefaultToolkit().getScreenSize();
		dialog.setBounds((scrDim.width - width) / 2, (scrDim.height - height) / 2, width, height);

		// Creates a main panel with
		JPanel main = new JPanel(new BorderLayout(BORDER / 2, BORDER / 2));
		main.setBorder(BorderFactory.createEmptyBorder(BORDER, BORDER, BORDER, BORDER));

		// Creates header
		String text = CommonConstants.CONVERSION_WARNING_DESCRIPTION;
		text = text.replaceAll(CommonConstants.PAR1, from);
		text = text.replaceAll(CommonConstants.PAR2, to);
		header = new JLabel(text);
		main.add(header, BorderLayout.NORTH);

		// Creates a box to show warnings
		ScrollablePanel box = new ScrollablePanel();
		box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
		Iterator<String> i = warnings.iterator();
		while (i.hasNext()) {
			box.add(getEntry(i.next()));
		}

		box.setBackground(Color.WHITE);

		main.add(new JScrollPane(box), BorderLayout.CENTER);

		dialog.getContentPane().add(main, BorderLayout.CENTER);

		// Adds a lower panel with okay button
		JPanel lower = new JPanel();
		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dialog.dispose();
			}
		});
		lower.add(okButton);
		dialog.getContentPane().add(lower, BorderLayout.SOUTH);
	}

	/**
	 * This is used to create components to be shown inside box
	 * @param text text to be displayed inside box
	 * @return created component
	 */
	private Component getEntry(String text) {
		JLabel label = new JLabel("<HTML>" + text + "</HTML>");
		label.setIcon(JMTImageLoader.loadImage("Warning"));
		label.setIconTextGap(BORDER);
		label.setHorizontalTextPosition(SwingConstants.RIGHT);
		label.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		return label;
	}

	/**
	 * Sets title of shown dialog
	 * @param title title of shown dialog
	 */
	public void setTitle(String title) {
		dialog.setTitle(title);
	}

	/**
	 * Shows warning window dialog
	 */
	public void show() {
		dialog.show();
	}

	/**
	 * Inner class to display a panel with vertical scrollbars only
	 */
	public class ScrollablePanel extends JPanel implements Scrollable {

		private static final long serialVersionUID = 1L;

		@Override
		public void setBounds(int x, int y, int width, int height) {
			super.setBounds(x, y, getParent().getWidth(), height);
		}

		@Override
		public Dimension getPreferredSize() {
			return new Dimension(getWidth(), getPreferredHeight());
		}

		public Dimension getPreferredScrollableViewportSize() {
			return super.getPreferredSize();
		}

		public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
			int hundredth = (orientation == SwingConstants.VERTICAL ? getParent().getHeight() : getParent().getWidth()) / 100;
			return (hundredth == 0 ? 1 : hundredth);
		}

		public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
			return orientation == SwingConstants.VERTICAL ? getParent().getHeight() : getParent().getWidth();
		}

		public boolean getScrollableTracksViewportWidth() {
			return true;
		}

		public boolean getScrollableTracksViewportHeight() {
			return false;
		}

		private int getPreferredHeight() {
			int rv = 0;
			for (int k = 0, count = getComponentCount(); k < count; k++) {
				Component comp = getComponent(k);
				Rectangle r = comp.getBounds();
				int height = r.y + r.height;
				if (height > rv) {
					rv = height;
				}
			}

			if (getParent().getHeight() > rv) {
				return getParent().getHeight();
			} else {
				return rv;
			}
		}

	}

}
