package iDPS.gui;

import iDPS.Player;
import iDPS.gear.Gear;
import iDPS.gear.Enchant;
import iDPS.gear.EnchantComparison;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.border.EmptyBorder;

public class SelectEnchantPanel extends JPanel {
	
	public SelectEnchantPanel(int slot) {
		super();
		EnchantComparison ec;
		
		setLayout(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		
		int j = 0;
		Enchant e, curEnchant;
		Gear gear = Player.getInstance().getEquipped();
		curEnchant = gear.getEnchant(slot);
		ec = new EnchantComparison(gear, slot);
		ArrayList<Enchant> comparedEnchants = ec.getComparedEnchants();
		Iterator<Enchant> iter = comparedEnchants.iterator();

		JLabel label;
		SelectEnchantButton button;
		float diff;
		while (iter.hasNext()) {
			e = iter.next();
			button = new SelectEnchantButton(e, slot);
			if (e == curEnchant)
				button.setSelected(true);
			c.anchor = GridBagConstraints.WEST;
			c.insets = new Insets(0, 0, 0, 10);
			c.gridx = 0; c.gridy = j; c.gridheight = 2; c.gridwidth = 1;
			add(button, c);
			
			label = new JLabel(e.getName());
			c.insets = new Insets(0, 0, 0, 0);
			c.gridx = 1; c.gridy = j; c.gridheight = 1;
			add(label, c);
			
			if (curEnchant != null)
				diff = e.getComparedDPS() - curEnchant.getComparedDPS();
			else
				diff = 0;
			label = new JLabel(String.format("%.2f (%+.2f)", e.getComparedDPS(), diff));
			label.setHorizontalAlignment(JLabel.RIGHT);
			c.gridx = 2; c.gridy = j;
			add(label, c);
			
			c.insets = new Insets(5, 0, 0, 0);
			c.gridx = 1; c.gridy = j+1; c.gridwidth = 2;
			add(new RatingPanel(e, ec.getMaxDPS()), c);
			
			if (iter.hasNext()) {
				JSeparator sep = new JSeparator();
				c.insets = new Insets(3, 0, 0, 0);
				c.gridx = 0; c.gridy = j+2; c.gridwidth = 3;
				add(sep, c);
				j += 1;
			}
			
			j += 2;
		}
		setBorder(new EmptyBorder(new Insets(3,6,6,6)));

	}
	
	private class SelectEnchantButton extends JRadioButton implements ActionListener {
		
		private Enchant enchant;
		private int slot;
		
		public SelectEnchantButton(Enchant enchant, int slot) {
			this.enchant = enchant;
			this.slot = slot;
			setFocusable(false);
			addActionListener(this);
		}

		public void actionPerformed(ActionEvent e) {
			MainFrame.getInstance().getSideScroll().setViewportView(new JPanel());
			Player p = Player.getInstance();
			p.getEquipped().setEnchant(slot, enchant);
			MainFrame.getInstance().refreshItem(slot);
			MainFrame.getInstance().showStats();
		}

	}

}