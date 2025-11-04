package io.github.javaherobrine.net.ui;
import javax.swing.*;
import java.awt.*;
import java.util.function.*;
class SpeedInput extends JFrame{
	private Consumer<Long> up;
	private Consumer<Long> down;
	private static final long serialVersionUID = 1L;
	private static final SpeedInput INSTANCE=new SpeedInput();
	@SuppressWarnings("unused")
	private SpeedInput() {
		SwingUtilities.invokeLater(()->{
			setTitle("Input the max data rate(Byte per second), 0 for no limitation");
			JPanel upload=new JPanel();
			upload.setLayout(new FlowLayout());
			JLabel unit=new JLabel("byte(s)/s");
			JLabel u=new JLabel("Upload: ");
			JLabel d=new JLabel("Download: ");
			JFormattedTextField u1=new JFormattedTextField();
			u1.setValue(0L);
			upload.add(u);
			upload.add(u1);
			upload.add(unit);
			JFormattedTextField u2=new JFormattedTextField();
			u2.setValue(0L);
			JPanel download=new JPanel();
			download.add(d);
			download.add(u2);
			download.add(unit);
			setLayout(new BorderLayout());
			add(upload,BorderLayout.NORTH);
			add(download,BorderLayout.CENTER);
			JPanel button=new JPanel();
			button.setLayout(new FlowLayout());
			JButton OK=new JButton("OK");
			OK.addActionListener(n->{
				up.accept((Long)u1.getValue());
				down.accept((Long)u2.getValue());
			});
			setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			dispose();
		});
	}
	public static void limit(Consumer<Long> u,Consumer<Long> d) {
		INSTANCE.up=u;
		INSTANCE.down=d;
		INSTANCE.setVisible(true);
	}
}
