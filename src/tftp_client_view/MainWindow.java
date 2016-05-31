/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tftp_client_view;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import tftp_client_model.TFTPClient;

/**
 *
 * @author yannick
 */
public class MainWindow extends JFrame
{
    public static void main(String[] args) 
    {
        MainWindow mw = new MainWindow();
    }
    
    private JButton send;
    private JButton receive;
    private JTextField[] ipServer;
    private TFTPClient tftp;
    
    public MainWindow()
    {
        super("TFTP Client");        
        this.tftp = new TFTPClient();
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        this.initGraphics();
        
        this.setVisible(true);
    }

    private void initGraphics()
    {
        this.setSize(300,200);
        this.setResizable(false);
        this.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        
        c.gridwidth = 4;
        JLabel title = new JLabel("TFTP Client");
        title.setFont(new Font(title.getFont().getName(),Font.PLAIN, 20));
        c.gridx = 0;
        c.gridy = 0;
        this.add(title, c);
        
        c.gridwidth = 1;
        c.gridx = 0;
        c.gridy = 1;
        JLabel ip = new JLabel("IP Server:");
        ip.setFont(new Font(ip.getFont().getName(),Font.PLAIN, 16));
        this.add(ip, c);
        
        c.gridwidth=1;
        c.gridy=2;
        c.gridx=0;
        ipServer = new JTextField[4];
        for(int i=0; i<4; i++)
        {
            ipServer[i] = new JTextField(3);
            this.add(ipServer[i],c);
            c.gridx++;
        }
        
        ipServer[0].setText("192");
        ipServer[1].setText("168");
        ipServer[2].setText("0");
        ipServer[3].setText("11");
        
        JButton applyIP = new JButton("Apply IP");
        applyIP.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                TFTPClient.TFTP_SERVER_IP = ipServer[0].getText()+"."+ipServer[1].getText()+"."+ipServer[2].getText()+"."+ipServer[3].getText();
            }
        });
        this.add(applyIP, c);
        
        c.gridwidth=1;
        c.gridx=1;
        c.gridy=3;
        this.send = new JButton("Send a file");
        this.add(this.send, c);
        
        c.gridx=2;
        c.gridy=3;
        this.receive = new JButton("Receive a file");
        this.add(this.receive, c);
        
        this.receive.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                JFileChooser chooser = new JFileChooser();
                chooser.setMultiSelectionEnabled(false);
                int returnVal = chooser.showOpenDialog(receive);
                if(returnVal == JFileChooser.APPROVE_OPTION)
                {
                    tftp.ReceiveFile(chooser.getSelectedFile().getName(), chooser.getSelectedFile().getPath());
                }
            }
        });
        
        this.send.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                JFileChooser chooser = new JFileChooser();
                chooser.setMultiSelectionEnabled(false);
                int returnVal = chooser.showOpenDialog(send);
                if(returnVal == JFileChooser.APPROVE_OPTION)
                {
                    tftp.SendFile(chooser.getSelectedFile().getName(), chooser.getSelectedFile().getPath());
                }
            }
        });
        
        this.pack();
    }
}
