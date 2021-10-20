/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.celcom.kenan.adapter.cai;

import LOG4J.LogException;
import LOG4J.LogObj;
import java.io.*;
import java.net.*;
import yagg.adapter.adkgeneric.GenericAdapter;

/**
 *
 * @author Dell
 */
public class ServerSocketSvc {
    public ServerSocketSvc(String s, String s1, int i, GenericAdapter genericadapter, String s2)
    {
        sPort = 0;
        maxMsgLen = 0;
        listener = null;
        client = null;
        msgTrailerStr = null;
        fldDelimStr = null;
        ipaddr = null;
        bw = null;
        br = null;
        debugArgs = new String[2];
        v_LogObj = null;
        msgTrailerStr = s;
        fldDelimStr = s1;
        maxMsgLen = i;
        String s3 = s2;
        v_LogObj = genericadapter.getLogObj("yagg.cai");
    }

    public boolean openSocket(int i)
    {
        sPort = i;
        try
        {
            if(ipaddr != null)
                listener = new ServerSocket(sPort, 10, InetAddress.getByName(ipaddr));
            else
                listener = new ServerSocket(sPort);
            debugArg[0] = (new Integer(listener.getLocalPort())).toString();
            v_LogObj.LogInfo("NotUsed", "ServerSocketSvc", "1", "Could not log", debugArg, null);
            client = listener.accept();
            client.setKeepAlive(true);
            v_LogObj.LogInfo("NotUsed", "ServerSocketSvc", "2", "Could not log", null, null);
            serverIn = client.getInputStream();
            serverOut = client.getOutputStream();
            bw = new BufferedWriter(new OutputStreamWriter(serverOut));
            br = new BufferedReader(new InputStreamReader(serverIn));
        }
        catch(IOException ioexception)
        {
            System.out.println(ioexception);
            return false;
        }
        catch(LogException logexception) { }
        return true;
    }

    public void writeSocket(String s)
        throws IOException
    {
        try
        {
            bw.write(s, 0, s.length());
            bw.flush();
            debugArg[0] = (new Integer(s.length())).toString();
            v_LogObj.LogInfo("NotUsed", "ServerSocketSvc", "3", "Could not log", debugArg, null);
        }
        catch(LogException logexception) { }
    }

    public String readSocket()
        throws IOException
    {
        StringBuffer stringbuffer = new StringBuffer();
        try
        {
            v_LogObj.LogInfo("NotUsed", "ServerSocketSvc", "4", "Could not log", null, null);
            String s = br.readLine();
            if(s != null)
                stringbuffer.append(s);
            debugArg[0] = stringbuffer.toString();
            v_LogObj.LogInfo("NotUsed", "ServerSocketSvc", "6", "Could not log", debugArg, null);
        }
        catch(LogException logexception) { }
        return stringbuffer.toString();
    }

    public boolean closeSocket()
    {
        try
        {
            if(bw != null)
                bw.close();
            if(br != null)
                br.close();
            if(client != null)
                client.close();
            if(listener != null)
                listener.close();
            v_LogObj.LogError("NotUsed", "ServerSocketSvc", "5", "Could not log", null, null);
        }
        catch(IOException ioexception)
        {
            System.out.println("Exception in server closeSocket: " + ioexception);
            return false;
        }
        catch(LogException logexception) { }
        finally {
            try
            {
                if(listener != null)
                    listener.close();
            }
            catch(IOException ioexception)
            {
                System.out.println("Exception in server closeSocket: " + ioexception);
                return false;
            }
        }
        return true;
    }

    private int sPort;
    private int maxMsgLen;
    private ServerSocket listener;
    private Socket client;
    private InputStream serverIn;
    private OutputStream serverOut;
    private String msgTrailerStr;
    private String fldDelimStr;
    private String ipaddr;
    private BufferedWriter bw;
    private BufferedReader br;
    private String debugArgs[];
    private String debugArg[] = {
        new String()
    };
    private LogObj v_LogObj;
}
