package es.olgierd.radiocontrol;

import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

class GUI extends JPanel implements KeyListener {

    private static final long serialVersionUID = 1L;
    JLabel frequency;
    JLabel indicator;
    RadioControl rc;

    public GUI(RadioControl radio) {

	rc = radio;

	addKeyListener(this);
	setFocusable(true);
	this.setLayout(null);

	frequency = new JLabel("172.200.000");
	frequency.setFont(new Font("Courier", Font.BOLD, 30));
	frequency.setBounds(10, 10, 240, 30);
	this.add(frequency);

	indicator = new JLabel("↑                ");
	indicator.setFont(new Font("Courier", Font.BOLD, 30));
	indicator.setBounds(10, 35, 300, 30);
	this.add(indicator);

    }

    @Override
    public void keyPressed(KeyEvent e) {
	if (e.getKeyCode() == KeyEvent.VK_LEFT)
	    rc.indPosLeft();
	if (e.getKeyCode() == KeyEvent.VK_RIGHT)
	    rc.indPosRight();
	if (e.getKeyCode() == KeyEvent.VK_UP)
	    rc.changeFreq(1);
	if (e.getKeyCode() == KeyEvent.VK_DOWN)
	    rc.changeFreq(-1);

	refresh();
    }

    public void refresh() {
	frequency.setText(new String(rc.getFreqLabel()));
	indicator.setText(new String(rc.generateIndicator()));
    }

    public void keyReleased(KeyEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }

}

public class RadioControl {

    int indPos, freq;
    DatagramSocket s;
    Inet4Address ia;

    public RadioControl() {

	try {
	    ia = (Inet4Address) Inet4Address.getLocalHost();
	    s = new DatagramSocket();
	    s.connect(ia, 6020);
	} catch (UnknownHostException e1) {
	    e1.printStackTrace();
	} catch (SocketException e) {
	    e.printStackTrace();
	}

    }

    public void indPosLeft() {
	if (indPos < 8)
	    indPos++;
    }

    public void indPosRight() {
	if (indPos > 0)
	    indPos--;
    }

    public void freqUp() {
	freq += Math.pow(10, indPos);
    }

    public void freqDown() {
	freq -= Math.pow(10, indPos);
    }

    public char[] generateIndicator() {

	char s[] = new char[11];

	for (int i = 0; i < s.length; i++) {
	    s[i] = ' ';
	}

	int diff = 0;

	if (indPos > 2)
	    diff++;
	if (indPos > 5)
	    diff++;

	s[10 - indPos - diff] = '↑';

	return s;

    }

    public char[] getFreqLabel() {

	char[] l = new char[11];

	int shift = 2;
	for (int i = 8; i >= 0; i--) {

	    l[i + shift] = (char) ('0' + ((freq / Math.pow(10, 8 - i)) % 10));
	    if (i == 6 || i == 3)
		shift--;
	}

	l[3] = '.';
	l[7] = '.';

	return l;

    }

    private byte[] intToByteTab(int num) {

	byte tab[] = new byte[4];

	int i = 0;

	while (i < 4) {
	    tab[i] = (byte) (num & 0xff);
	    num >>= 8;
	    i++;
	}

	return tab;

    }

    public void changeFreq(int dir) {

	freq += Math.pow(10, indPos) * dir;
	if (freq < 0)
	    freq = 0;
	if (freq > 999999999)
	    freq = 999999999;

	try {
	    s.send(new DatagramPacket(intToByteTab(freq), 4));
	} catch (IOException e) {
	    System.out.println("failure!");
	}

    }

    public static void main(String args[]) {

	RadioControl rc = new RadioControl();
	GUI g = new GUI(rc);
	JFrame window = new JFrame();
	window.add(g);
	window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	window.setVisible(true);
	window.setSize(250, 90);

    }

}
