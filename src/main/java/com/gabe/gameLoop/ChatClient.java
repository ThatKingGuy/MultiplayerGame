package com.gabe.gameLoop;

import org.lwjgl.Sys;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ChatClient {

    String serverAddress;
    int port = 59001;
    Scanner in;
    PrintWriter out;
    JFrame frame = new JFrame("Chat thing");
    JTextField textField = new JTextField(50);
    JTextArea messageArea = new JTextArea(16, 50);



    public ChatClient() {

        textField.setEditable(false);
        messageArea.setEditable(false);
        try {
            //create the font to use. Specify the size!
            Font customFont = Font.createFont(Font.TRUETYPE_FONT, new File("Fonts\\Minecraftia.ttf")).deriveFont(12f);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            //register the font
            ge.registerFont(customFont);
            textField.setFont(customFont);
            messageArea.setFont(customFont);
        } catch (IOException | FontFormatException e) {
            e.printStackTrace();
        }

        frame.getContentPane().add(textField, BorderLayout.SOUTH);
        frame.getContentPane().add(new JScrollPane(messageArea), BorderLayout.CENTER);
        frame.pack();

        // Send on enter then clear to prepare for next message
        textField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                out.println(textField.getText());
                System.out.println("Sent packet: "+textField.getText());
                textField.setText("");
            }
        });

    }

    private String getName() {
        return JOptionPane.showInputDialog(frame, "Choose a screen name:", "Screen name selection",
                JOptionPane.PLAIN_MESSAGE);
    }

    private String getIp() {
        return JOptionPane.showInputDialog(frame, "Choose an ip to connect to:", "IP selection",
                JOptionPane.PLAIN_MESSAGE);
    }

    private void run() throws IOException {
        try {
            var socket = new Socket(serverAddress, port);
            in = new Scanner(socket.getInputStream());
            out = new PrintWriter(socket.getOutputStream(), true);

            while (in.hasNextLine()) {

                var line = in.nextLine();
                System.out.println("Received packet: " + line);
                if (line.startsWith("SUBMITNAME")) {
                    String name = getName();
                    out.println(name);
                    System.out.println("Sent packet: "+name);
                } else if (line.startsWith("NAMEACCEPTED")) {
                    this.frame.setTitle("Chat thing - " + line.substring(13));
                    textField.setEditable(true);
                } else if (line.startsWith("MESSAGE")) {
                    messageArea.append(line.substring(8) + "\n");
                }
            }
        } catch(UnknownHostException | SocketException e){
            frame.dispose();

            var client = new ChatClient();
            client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            client.frame.setVisible(true);
            client.setServerAddress();
            client.run();
        }
        finally {
            frame.setVisible(false);
            frame.dispose();
        }
    }

    public void setServerAddress() throws IOException {
        String str = getIp();
        if(str != null) {
            if (str.split(":").length > 0) {
                if (str.split(":").length == 1) {
                    serverAddress = str.split(":")[0];
                } else if (str.split(":").length > 1) {
                    serverAddress = str.split(":")[0];
                    port = Integer.parseInt(str.split(":")[1]);
                }
            }
        }else{
            frame.dispose();

            var client = new ChatClient();
            client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            client.frame.setVisible(true);
            client.setServerAddress();
            client.run();
        }
    }

    public static void main(String[] args) throws Exception {

        var client = new ChatClient();
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.frame.setVisible(true);
        client.setServerAddress();
        client.run();
    }
}