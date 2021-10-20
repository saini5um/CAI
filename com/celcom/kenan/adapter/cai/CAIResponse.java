/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.celcom.kenan.adapter.cai;

import LOG4J.LogException;
import LOG4J.LogObj;
import java.io.IOException;
import java.util.*;
import yagg.adapter.adkgeneric.GenericAdapter;
import yagg.adapter.adkgeneric.GwyAdapterException;

/**
 *
 * @author Dell
 */
public class CAIResponse {
    public CAIResponse(GenericAdapter genericadapter, CAIMsgCount caimsgcount, Properties properties)
    {
        this(genericadapter, caimsgcount, properties, true);
    }

    public CAIResponse(GenericAdapter genericadapter, CAIMsgCount caimsgcount, Properties properties, boolean isprimary)
    {
        isPrimary = isprimary;
        respFmtType = null;
        msgid = "";
        userData = "";
        port = 0;
        debugArgs = new String[2];
        v_LogObj = null;
        genAdapObj = genericadapter;
        v_LogObj = genAdapObj.getLogObj("yagg.cai");
        respFmtType = genAdapObj.getAdapterAttribute("CAI_RESP_FORMAT");
        prompt = "ACKOK>";
        String s = genAdapObj.getAdapterAttribute("CAI_RESPCHAN_IPADDR");
        cmsg = new CAICommon(genAdapObj, caimsgcount, properties);
        sss = new ServerSocketSvc("$END$", "\n", 65500, genAdapObj, s);
        genAdapObj.setsregister("ResponseChannel", "UNUSED");
    }

    public boolean init()
    {
        if((respFmtType = genAdapObj.getAdapterAttribute("CAI_RESP_FORMAT")) == null)
            respFmtType = "CAIResponse";
        if ( isPrimary )
            port = Integer.parseInt(genAdapObj.getAdapterAttribute("CAI_RESP_TCPPORT"));
        else
            port = Integer.parseInt(genAdapObj.getAdapterAttribute("CAI_RESP_SECONDARY_PORT"));
            
        debugArg[0] = (new Integer(port)).toString();
        LogHandler.logInform(v_LogObj, "CAIResponse", "2", debugArg[0]);
        if(!sss.openSocket(port)) {
            LogHandler.logErr(v_LogObj, "CAIResponse", "17", "");
            return false;
        }
/*        port = Integer.parseInt(genAdapObj.getAdapterAttribute("CAI_RESP_SECONDARY_PORT"));
        debugArg[0] = (new Integer(port)).toString();
        LogHandler.logInform(v_LogObj, "CAIResponse", "2", debugArg[0]);
        if(!ssss.openSocket(port)) {
            LogHandler.logErr(v_LogObj, "CAIResponse", "17", "");
            return false;
        }*/
        LogHandler.logInform(v_LogObj, "CAIResponse", "1", null);
        genAdapObj.setsregister("ResponseChannel", "CONNECTED");
        responseAck(prompt);
        login();
        return true;        
    }

    public int login()
    {
        String logincmd = null;
        try {
            logincmd = sss.readSocket();
        } catch (IOException ioexception) {
            LogHandler.logErr(v_LogObj, "CAIResponse", "14", ioexception.getMessage());
            return 2;
        }
        if (logincmd.startsWith(CAIDef.CMD_TYPE[2])) {
            responseAck("LOGIN_SUCCESS;\n");
            responseAck(prompt);
        }
        return 0;
    }

    public int receive()
    {
        String resp = null;
        LogHandler.logInform(v_LogObj, "CAIResponse", "4", null);
        try {
            resp = sss.readSocket();
            genAdapObj.resetWriteLatency(cmsg.getSess());
            genAdapObj.updateWriteLatency(cmsg.getSess());
        } catch (IOException ioexception) {
            LogHandler.logErr(v_LogObj, "CAIResponse", "14", ioexception.getMessage());
            return 2;
        } catch (GwyAdapterException gwyadapterexception) {
            LogHandler.logErr(v_LogObj, "CAIResponse", "15", gwyadapterexception.getMessage());
        }
        int i = 0;
        debugArg[0] = (new Integer(i)).toString();
        LogHandler.logInform(v_LogObj, "CAIResponse", "3", debugArg[0]);
        i = response(resp);
        if(i != 1) {
            genAdapObj.incrcount("nProtocolErrors");
            LogHandler.logErr(v_LogObj, "CAIResponse", "4", null);
            return 2;
        } else {
            i = responseAck(prompt);
            if(i != 5) {
                genAdapObj.incrcount("nProtocolErrors");
                LogHandler.logErr(v_LogObj, "CAIResponse", "4", null);
                return 1;
            }
        }
        return 0;
    }

    private int response(String s)
    {
        LogHandler.logInform(v_LogObj, "CAIResponse", "5", s, s.length());
        if(s.length() == 0) {
            LogHandler.logErr(v_LogObj, "CAIResponse", "18", "");
            return 5;
        }
        if (s.startsWith(CAIDef.CMD_TYPE[0])) {
            debugArg[0] = s; // LOGOUT
            LogHandler.logErr(v_LogObj, "CAIResponse", "26", debugArg[0]);
            if (cleanup()) {
                if (init()) return 1;
                else return 2;
            } else return 2;
        }
        if (!s.startsWith(CAIDef.CMD_TYPE[1])) {
            debugArg[0] = s;
            LogHandler.logErr(v_LogObj, "CAIResponse", "14", debugArg[0]);
            return 2;
        }
        if(!cmsg.validateCommand(s, CAIDef.CMD_TYPE[1])) {
            debugArg[0] = s;
            LogHandler.logErr(v_LogObj, "CAIResponse", "14", debugArg[0]);
            return 2;
        }
        Vector vector = new Vector();
        StringTokenizer stringtokenizer = cmsg.getMsgHdrFlds(s);
        int i = stringtokenizer.countTokens();
        for(int j = 0; j < i; j++) {
            String token = stringtokenizer.nextToken();
            if ( token.indexOf(";") == -1 ) token = token + ";";
            if (!token.startsWith("Enter Command"))
                vector.addElement(token);
        }

        String mainResponse = cmsg.getMainResponse(s);
        String errorDescription = cmsg.getErrorDescription(s);
        
        StringTokenizer stringtokenizer1 = cmsg.getMsgFlds(mainResponse);
        int k = stringtokenizer1.countTokens();
        if ( k < 3 ) {
            LogHandler.logErr(v_LogObj, "CAIResponse", "14", debugArg[0]);
            return 2;            
        }
        int m = mainResponse.lastIndexOf(":");
        if ( m == -1 ) {
            LogHandler.logErr(v_LogObj, "CAIResponse", "14", debugArg[0]);
            return 2;                        
        }
        String respCode = mainResponse.substring(m + 1, mainResponse.length());
        debugArg[0] = (new Integer(k)).toString();
        LogHandler.logInform(v_LogObj, "CAIResponse", "6", debugArg[0]);
        for(int l = 0; l < k; l++)
        {
            String s1 = stringtokenizer1.nextToken();
            debugArgs[0] = (new Integer(l)).toString();
            debugArgs[1] = s1;
            try {
                v_LogObj.LogInfo("NotUsed", "CAIResponse", "22", "Could not log", debugArgs, null);
            } catch (LogException le) {}
            if(s1.startsWith("TRANSID"))
                msgid = s1.substring(s1.indexOf(",")+1, s1.length());
            if(s1.startsWith("USER_DATA"))
                userData = s1;
        }

        LogHandler.logInform(v_LogObj, "CAIResponse", "23", msgid);
        LogHandler.logInform(v_LogObj, "CAIResponse", "24", mainResponse);
        LogHandler.logInform(v_LogObj, "CAIResponse", "24", respCode);
        if(msgid.equals("") || respCode.equals("")) {
            LogHandler.logErr(v_LogObj, "CAIResponse", "19", "");
            return 2;
        }
        if(!cmsg.buildGWmsg(vector, respFmtType)) {
            LogHandler.logErr(v_LogObj, "CAIResponse", "20", "");
            return 3;
        }
        return 1;
    }

    private int responseAck(String s)
    {
//        s = s + "\n";
        LogHandler.logInform(v_LogObj, "CAIResponse", "2", null);
        debugArgs[0] = (new Integer(s.length())).toString();
        debugArgs[1] = s.toString();
        try {
            v_LogObj.LogInfo("NotUsed", "CAIResponse", "10", "Could not log", debugArgs, null);
        } catch (LogException le) {}
        
//        LogHandler.logInform(v_LogObj, "CAIResponse", "10", debugArgs);
        try
        {
            sss.writeSocket(s);
        }
        catch(IOException ioexception)
        {
            LogHandler.logErr(v_LogObj, "CAIResponse", "11", "");
//            return 4;
        }
        return 5;
    }

    public boolean cleanup()
    {
        try
        {
            v_LogObj.LogInfo("NotUsed", "CAIResponse", "25", "Could not log", null, null);
            sss.closeSocket();
            if(genAdapObj.getsregister("ResponseChannel").equals("CONNECTED"))
                genAdapObj.setsregister("ResponseChannel", "CLOSED");
            v_LogObj.LogInfo("NotUsed", "CAIResponse", "12", "Could not log", null, null);
        }
        catch(LogException logexception) { }
        return true;
    }

    public CAICommon getCAICommonObj()
    {
        return cmsg;
    }

    private ServerSocketSvc sss;
    private GenericAdapter genAdapObj;
    private CAICommon cmsg;
    private String respFmtType;
    private String msgid;
    private String userData;
    private String prompt;
    private int port;
    private boolean isPrimary;
    private static final int STATE_RESP = 0;
    private static final int STATE_RESP_ACK = 1;
    private static final int STATE_RESP_ERR = 2;
    private static final int STATE_RESP_RESEND = 3;
    private static final int STATE_RESP_DONE = 4;
    private static final int STATE_RESP_FAIL = 5;
    private String debugArg[] = {
        new String()
    };
    private String debugArgs[];
    private LogObj v_LogObj;

}
