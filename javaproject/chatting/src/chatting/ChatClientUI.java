/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatting;

import com.baidu.speech.serviceapi.Sample;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.logging.Level;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;


/**
 *
 * @author jj
 */
public class ChatClientUI extends javax.swing.JFrame 
{
    /**
     * Creates new form ChatClientUI
     */

    /*以下定义数据流和网络变量*/
	Socket soc = null; 
	PrintStream ps = null; 
	// 客户端侦听服务器消息的线程
	ClentListener listener = null; 
        Tape tape;
        Sample sample;
     
        ChatClientUI(String str) {
                initComponents();
                chatContentTextArea.setEditable(false);
                sample = new Sample();
		init(str);
	}

	//	初始化图形界面
	public void init(String str) {
		if (soc == null) {
			try {
				// 使用端口2525实例化一个本地套接字
				soc = new Socket(InetAddress.getLocalHost(), Constants.SERVER_PORT); 
				// 在控制台打印实例化的结果
				System.out.println(soc); 
				//将ps指向soc的输出流
				ps = new PrintStream(soc.getOutputStream()); 
				//定义一个字符缓冲存储发送信息
				StringBuffer info = new StringBuffer(Constants.CONNECT_IDENTIFER).append(Constants.SEPERATOR); 
				//其中INFO为关键字让服务器识别为连接信息
				//并将name和ip用"："分开，在服务器端将用一个
				//StringTokenizer类来读取数据
				String userinfo = str + Constants.SEPERATOR
						+ InetAddress.getLocalHost().getHostAddress();
				ps.println(info.append(userinfo));

				ps.flush();
				//将客户端线程实例化，并启动
				listener = new ClentListener(this, str, soc);   
				listener.start(); 
			} catch (IOException e) {
				System.out.println("Error:" + e);
				disconnect();
			}
		}
		this.setTitle("聊天室客户端");
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			SwingUtilities.updateComponentTreeUI(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 关闭聊天室客户端事件
	 */
        @Override
	protected void processWindowEvent(WindowEvent e){
		super.processWindowEvent(e);
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			// 如果是关闭聊天室客户端，则断开连接
			disconnect();
			dispose();
			System.exit(0);
		}
	}
	

	/**
	 * 断开与服务器的连接
	 */
	public void disconnect(){
		if (soc != null) {
			try {
				// 用打印流发送QUIT信息通知服务器断开此次通信
				ps.println(Constants.QUIT_IDENTIFER); 
				ps.flush();
				soc.close(); //关闭套接字
				listener.toStop();
				soc = null;
			} catch (IOException e) {
				System.out.println("Error:" + e);
			} 
		}
	}
	


	/**
	 * 客户端线程类用来监听服务器传来的信息
	 */
	class ClentListener extends Thread	{
		//存储客户端连接后的name信息
		String name = null;
		//客户端接受服务器数据的输入流
		BufferedReader br = null;
		//实现从客户端发送数据到服务器的打印流
		PrintStream ps = null;

		//存储客户端的socket信息
		Socket socket = null;
		//存储当前运行的ChatClient实例
		ChatClientUI parent = null;

		boolean running = true;

		//构造方法
		public ClentListener(ChatClientUI p, String n, Socket s)	{

			//接受参数
			parent = p;
			name = n;
			socket = s;

			try {
				//实例化两个数据流
				br = new BufferedReader(new InputStreamReader(s
						.getInputStream()));
				ps = new PrintStream(s.getOutputStream());

			} catch (IOException e) {
				System.out.println("Error:" + e);
				parent.disconnect();
			}
		} 
		
		// 停止侦听
		public void toStop(){
			this.running = false;
		}

		//线程运行方法
                @Override
		public void run(){
			String msg = null;
			while (running) {
				msg = null;
				try {
					// 读取从服务器传来的信息
					msg = br.readLine();
					System.out.println("receive msg: " + msg);
				} catch (IOException e) {
					System.out.println("Error:" + e);
					parent.disconnect();
				}
				// 如果从服务器传来的信息为空则断开此次连接
				if (msg == null) {
					parent.listener = null;
					parent.soc = null;
					parent.peopleList.removeAll();
					running = false;
					return;
				}
				
				//用StringTokenizer类来实现读取分段字符
				StringTokenizer st = new StringTokenizer(msg, Constants.SEPERATOR); 
				//读取信息头即关键字用来识别是何种信息
				String keyword = st.nextToken(); 

				if (keyword.equals(Constants.PEOPLE_IDENTIFER)) 
                                {
					//如果是PEOPLE则是服务器发来的客户连接信息
					//主要用来刷新客户端的用户列表
					parent.peopleList.removeAll();
					//遍历st取得目前所连接的客户
					while (st.hasMoreTokens()) 	
                                        {
						String str = st.nextToken();
						parent.peopleList.add(str);
					}
					
				} 
                                else if (keyword.equals(Constants.MSG_IDENTIFER)) 
                                {
					//如果关键字是MSG则是服务器传来的聊天信息, 
					//主要用来刷新客户端聊天信息区将每个客户的聊天内容显示出来
					String usr = st.nextToken();
					Document doc = chatContentTextArea.getDocument(); 
					SimpleAttributeSet attrSet = new SimpleAttributeSet(); 
					try {
						StyleConstants.setForeground(attrSet, Color.RED); 
                                                StyleConstants.setFontFamily(attrSet, "华文彩云");
						doc.insertString(doc.getLength(), usr, attrSet);
					} catch (BadLocationException e) {
						// TODO 自动生成的 catch 块
						e.printStackTrace();
					}
					try {
						StyleConstants.setForeground(attrSet, Color.BLACK); 
                                                StyleConstants.setFontFamily(attrSet, "微软雅黑");
						doc.insertString(doc.getLength(), st.nextToken("\0"), attrSet);
                                                doc.insertString(doc.getLength(), "\r\n", attrSet);
					} catch (BadLocationException e) {
						// TODO 自动生成的 catch 块
						e.printStackTrace();
					}
					chatContentTextArea.setCaretPosition(chatContentTextArea.getDocument().getLength()); 					
				} 
                                else if (keyword.equals(Constants.PRICHAT_IDENTIFER)){
                                        String usr = st.nextToken();
					Document doc = chatContentTextArea.getDocument(); 
					SimpleAttributeSet attrSet = new SimpleAttributeSet(); 
					try {
						StyleConstants.setForeground(attrSet, Color.RED);
                                                StyleConstants.setFontFamily(attrSet, "华文彩云");
						doc.insertString(doc.getLength(), usr, attrSet);
					} catch (BadLocationException e) {
						// TODO 自动生成的 catch 块
						e.printStackTrace();
					}
					try {
						StyleConstants.setForeground(attrSet, Color.BLUE); 
                                                StyleConstants.setFontFamily(attrSet, "华文行楷");
						doc.insertString(doc.getLength(), st.nextToken(), attrSet);
                                              
					} catch (BadLocationException e) {
						// TODO 自动生成的 catch 块
						e.printStackTrace();
					}
                                        try {
                                                StyleConstants.setForeground(attrSet, Color.BLACK); 
                                                StyleConstants.setFontFamily(attrSet, "微软雅黑");
						doc.insertString(doc.getLength(), st.nextToken("\0"), attrSet);
                                              doc.insertString(doc.getLength(), "\r\n", attrSet);
                                        } catch (BadLocationException e) {
						// TODO 自动生成的 catch 块
						e.printStackTrace();
                                        }
					chatContentTextArea.setCaretPosition(chatContentTextArea.getDocument().getLength());   
                                    
                                }
                                
                                else if (keyword.equals(Constants.QUIT_IDENTIFER)) 
                                {
					//如果关键字是QUIT则是服务器关闭的信息, 切断此次连接
					System.out.println("Quit");
                                        String usr = st.nextToken();
					Document doc = chatContentTextArea.getDocument(); 
					SimpleAttributeSet attrSet = new SimpleAttributeSet(); 
					try {
						StyleConstants.setForeground(attrSet, Color.GREEN); 
                                                StyleConstants.setFontFamily(attrSet, "微软雅黑");
						doc.insertString(doc.getLength(), usr, attrSet);
                                                doc.insertString(doc.getLength(), "\r\n", attrSet);
					} catch (BadLocationException e) {
						// TODO 自动生成的 catch 块
						e.printStackTrace();
					}
					chatContentTextArea.setCaretPosition(chatContentTextArea.getDocument().getLength()); 	
					try 
                                        {
						running = false;
						parent.listener = null;
						parent.soc.close();
						parent.soc = null;
					}
                                        catch (IOException e) 
                                        {
						System.out.println("Error:" + e);
					} 
                                        finally {
						parent.soc = null;
						parent.peopleList.removeAll();
					}
					
					break;
				}
			}
			//清除用户列表
			parent.peopleList.removeAll();
		}
	} 
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList<>();
        sendMsgButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        chatContentTextArea = new javax.swing.JTextPane();
        jScrollPane4 = new javax.swing.JScrollPane();
        chatSendTextArea = new javax.swing.JTextPane();
        typepanel = new javax.swing.JPanel(){                 
            public void paint(Graphics g) {
                super.paint(g);
                g.drawImage(new ImageIcon("image/tape.png").getImage(), 0, 0, 41, 41, this);
            } 
        };
        peopleList = new java.awt.List();

        jList1.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        jScrollPane1.setViewportView(jList1);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                formMouseClicked(evt);
            }
        });

        sendMsgButton.setText("发送");
        sendMsgButton.setBorderPainted(false);
        sendMsgButton.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        sendMsgButton.setName("sendMsgButton"); // NOI18N
        sendMsgButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendMsgButtonActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("宋体", 0, 14)); // NOI18N
        jLabel1.setText("用户列表");

        chatContentTextArea.setFont(new java.awt.Font("宋体", 0, 15)); // NOI18N
        jScrollPane3.setViewportView(chatContentTextArea);

        chatSendTextArea.setFont(new java.awt.Font("宋体", 0, 14)); // NOI18N
        jScrollPane4.setViewportView(chatSendTextArea);

        typepanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                typepanelMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                typepanelMouseReleased(evt);
            }
        });

        javax.swing.GroupLayout typepanelLayout = new javax.swing.GroupLayout(typepanel);
        typepanel.setLayout(typepanelLayout);
        typepanelLayout.setHorizontalGroup(
            typepanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 41, Short.MAX_VALUE)
        );
        typepanelLayout.setVerticalGroup(
            typepanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 41, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 539, Short.MAX_VALUE)
                    .addComponent(jScrollPane3))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(sendMsgButton, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(typepanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(peopleList, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(jLabel1)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 326, Short.MAX_VALUE)
                    .addComponent(peopleList, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(sendMsgButton, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(typepanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(20, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void sendMsgButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sendMsgButtonActionPerformed
        Object source = evt.getSource();
        if (source == sendMsgButton) {
			//如果点击发送按钮
			if (soc != null) {
				StringBuffer msg;
                                int i = peopleList.getSelectedIndex();
                                if(i == -1){
                                     //定义并实例化一个字符缓冲存储发送的聊天信息
				     msg = new StringBuffer(Constants.MSG_IDENTIFER).append(Constants.SEPERATOR);
                                    
                                }
                                else{
                                     msg = new StringBuffer(Constants.PRICHAT_IDENTIFER).append(Constants.SEPERATOR);
                                     msg.append(i).append(Constants.SEPERATOR);                                  
                                } 
                                ps.println(msg.append(chatSendTextArea.getText())); 				
				ps.flush();
				chatSendTextArea.setText(""); 
			}
		}
    }//GEN-LAST:event_sendMsgButtonActionPerformed

    private void formMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseClicked
        peopleList.select(-1);
    }//GEN-LAST:event_formMouseClicked

    private void typepanelMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_typepanelMousePressed
       tape = new Tape();
       tape.capture();
    }//GEN-LAST:event_typepanelMousePressed

    private void typepanelMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_typepanelMouseReleased
        tape.save();
        try { 
                String ans;
                String path = this.getClass().getResource("/").getPath()+"sound.wav";
                System.out.println(path);
                ans = sample.method1(path);
                Document doc = chatSendTextArea.getDocument(); 
                SimpleAttributeSet attrSet = new SimpleAttributeSet(); 
                doc.insertString(doc.getLength(), ans, attrSet);
                chatSendTextArea.setCaretPosition(chatSendTextArea.getDocument().getLength());   
            } 
        catch (Exception ex)
        {
                java.util.logging.Logger.getLogger(ChatClientUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    
     
    }//GEN-LAST:event_typepanelMouseReleased


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextPane chatContentTextArea;
    private javax.swing.JTextPane chatSendTextArea;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JList<String> jList1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private java.awt.List peopleList;
    private javax.swing.JButton sendMsgButton;
    private javax.swing.JPanel typepanel;
    // End of variables declaration//GEN-END:variables
}
