package chatting;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ImageIcon;



public class ClientLogin extends JFrame implements ActionListener{
	
	// 登陆聊天室的名字标签和输入框
	JLabel nameLabel = new JLabel();
	JTextField nameTextField = new JTextField(15);
	JButton connectButton = new JButton();
	static ClientLogin login;


	
	ClientLogin()
	{       
		this.setUndecorated(true); 
		init();
		this.setSize(430 ,330);
		//设置窗体位置为屏幕中间
		int w = (Toolkit.getDefaultToolkit().getScreenSize().width - 510) / 2;
        int h = (Toolkit.getDefaultToolkit().getScreenSize().height - 290) / 2;
        this.setLocation(w, h);    
        this.validate();    
	}
	
	public void init()
	{
		this.setTitle("登录");
		nameLabel.setText("昵称：");
	    connectButton.setText("连 接");
	    connectButton.addActionListener(this);	   
	    JPanel pic = new JPanel() {             
			@Override 
            public void paint(Graphics g) { 
                super.paint(g);                 
                g.drawImage(new ImageIcon("image/bpic.gif").getImage(), 0, 0, 430, 330, this);
            } 
        };
        JPanel panel1=new JPanel();
        panel1.setLayout(new BorderLayout());
        JPanel panel2 = new JPanel();
	panel2.add(nameLabel);
	panel2.add(nameTextField);
	JPanel panel3 = new JPanel();
	panel3.add(connectButton);
	panel1.add(panel2,BorderLayout.NORTH);
	panel1.add(panel3, BorderLayout.CENTER);

    	this.getContentPane().setLayout(new BorderLayout());
	this.getContentPane().add(pic, BorderLayout.CENTER);
	this.getContentPane().add(panel1, BorderLayout.SOUTH);

	}
	public void actionPerformed(ActionEvent event)
        {
		if(nameTextField.getText().equals("")){
			JOptionPane.showMessageDialog(null, "昵称不能为空","给自己取个萌萌哒的名字吧^_^", JOptionPane.NO_OPTION);
		}
		else
		{
		    login.setVisible(false);     
                    //LoginLoading load = new LoginLoading(); 
                    //load.setVisible(true);
		    ChatClientUI cc = new ChatClientUI(nameTextField.getText());
                    cc.setVisible(true);
                    //load.setVisible(false);		    
		}
		
	}
        
	public static void main(String[] args){
	    login = new ClientLogin();
		login.setVisible(true);
	}



}
