package org.proxima.example;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import android.os.AsyncTask;

public class SendMessageTask extends AsyncTask<String, Void, String>
{

    @Override
    protected String doInBackground(String... params)
    {
        String address = params[0];
        String msg = params[1];
        String retval = sendMessage(address, msg);
        return retval;
    }

    private String sendMessage(String address, String msg)
    {

        String retval = null;
        DatagramSocket socket = null;
        try
        {
            socket = new DatagramSocket();

            byte buff[] = msg.getBytes();
            int msgLen = buff.length;
            boolean truncated = false;
            if (msgLen > MessageService.MAX_MESSAGE_LENGTH)
            {
                msgLen = MessageService.MAX_MESSAGE_LENGTH;
                truncated = true;
            }

            DatagramPacket packet = new DatagramPacket(buff, msgLen,
                    InetAddress.getByName(address), MessageService.MESSAGE_PORT);
            socket.send(packet);

            if (truncated)
            {
                retval = "Message truncated and sent.";
            }
            else
            {
                retval = "Message sent.";
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
            retval = "Error: " + e.getMessage();
        }
        finally
        {
            if (socket != null)
            {
                socket.close();
            }
        }

        return retval;
    }
};