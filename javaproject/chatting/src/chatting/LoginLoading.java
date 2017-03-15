package chatting;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Toolkit;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;


public class LoginLoading extends JFrame {
    public LoginLoading() {    
        this.setUndecorated(true); 
	init();
	this.setSize(200 ,210);
	int w = (Toolkit.getDefaultToolkit().getScreenSize().width - 200) / 2;
        int h = (Toolkit.getDefaultToolkit().getScreenSize().height - 210) / 2;
        this.setLocation(w, h);    
        this.validate();        
    }
    
    public void init(){
        JPanel pic;
        pic = new JPanel() {                 
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                g.drawImage(new ImageIcon("image/loading.gif").getImage(), 0, 0, 200, 210, this);
            } 
        };    
        this.getContentPane().setLayout(new BorderLayout());
	this.getContentPane().add(pic, BorderLayout.CENTER);
    }

            
    
}
