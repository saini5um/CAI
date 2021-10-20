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
public class ClientSocketSvc {
    public ClientSocketSvc(String s, String s1, int i, GenericAdapter genericadapter, int j)
    {
        serverHostname = null;
        sPort = 0;
        maxMsgLen = 0;
        readTimeout = 0;
        sock = null;
        msgTrailerStr = null;
        fldDelimStr = null;
        bw = null;
        br = null;
        v_LogObj = null;
        debugArgs = new String[2];
        msgTrailerStr = s;
        fldDelimStr = s1;
        maxMsgLen = i;
        readTimeout = j;
        v_LogObj = genericadapter.getLogObj("yagg.cai");
    }

    public boolean openSocket(String s, int i)
    {
        LogHandler.logInform(v_LogObj, "PKDebug", "1", null);
        serverHostname = s;
        sPort = i;
        try
        {
            sock = new Socket(serverHostname, sPort);
            sock.setSoTimeout(readTimeout);
            sock.setKeepAlive(true);
            debugArgs[0] = sock.getInetAddress().getHostAddress();
            debugArgs[1] = (new Integer(sock.getPort())).toString();
            v_LogObj.LogInfo("NotUsed", "ClientSocketSvc", "1", "Could not log", debugArgs, null);
            clientIn = sock.getInputStream();
            clientOut = sock.getOutputStream();
            bw = new BufferedWriter(new OutputStreamWriter(clientOut));
            br = new BufferedReader(new InputStreamReader(clientIn));
        }
        catch(UnknownHostException unknownhostexception)
        {
            logErr(v_LogObj, "ClientSocketSvc", "6", unknownhostexception.getMessage());
            return false;
        }
        catch(IOException ioexception)
        {
            logErr(v_LogObj, "ClientSocketSvc", "6", ioexception.getMessage());
            return false;
        }
        catch(LogException logexception) { }
        return true;
    }

    public void writeSocket(String s)
        throws IOException
    {
        if(bw == null)
            throw new IOException();
        try
        {
            bw.write(s, 0, s.length());
            bw.flush();
            debugArg[0] = (new Integer(s.length())).toString();
            v_LogObj.LogInfo("NotUsed", "ClientSocketSvc", "2", "Could not log", debugArg, null);
        }
        catch(LogException logexception) { }
    }

    public String readLoginSocket()
        throws IOException
    {
        StringBuffer stringbuffer = new StringBuffer();
        try
        {
            for ( int i = 0; i < 5; i++ )
            {
                String s = br.readLine();
                if ( s != null )
                    stringbuffer.append(s);
            }
            debugArg[0] = stringbuffer.toString();
            v_LogObj.LogInfo("NotUsed", "ClientSocketSvc", "7", "Could not log", debugArg, null);
        }
        catch(InterruptedIOException interruptedioexception) { }
        catch(LogException logexception) { }
        return stringbuffer.toString();
    }

    public String readSocket()
        throws IOException
    {
        StringBuffer stringbuffer = new StringBuffer();
        try
        {
            String s = br.readLine();
            if(s != null && s.startsWith("Enter Command")) s = br.readLine();
            if(s != null)
                stringbuffer.append(s);
            
            debugArg[0] = s;
            v_LogObj.LogInfo("NotUsed", "ClientSocketSvc", "8", "Could not log", debugArg, null);
//            String prompt = br.readLine();
//            debugArg[0] = prompt;
//            v_LogObj.LogInfo("NotUsed", "ClientSocketSvc", "9", "Could not log", debugArg, null);            
        }
        catch(InterruptedIOException interruptedioexception) { }
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
            if(sock != null)
                sock.close();
        }
        catch(IOException ioexception)
        {
            logErr(v_LogObj, "ClientSocketSvc", "5", ioexception.getMessage());
        }
        return true;
    }

    private void logErr(LogObj logobj, String s, String s1, String s2)
    {
        try
        {
            String as[] = {
                new String()
            };
            as[0] = s2;
            logobj.LogError("NotUsed", s, s1, "Cannot log", as, null);
        }
        catch(LogException logexception)
        {
            System.out.println("Log Exception");
        }
    }

    private String serverHostname;
    private int sPort;
    private int maxMsgLen;
    private int readTimeout;
    private Socket sock;
    private InputStream clientIn;
    private OutputStream clientOut;
    private static final int ERROR = 1;
    private String msgTrailerStr;
    private String fldDelimStr;
    private BufferedWriter bw;
    private BufferedReader br;
    private LogObj v_LogObj;
    private String debugArgs[];
    private String debugArg[] = {
        new String()
    };
}
