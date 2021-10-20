/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.celcom.kenan.adapter.cai;

import LOG4J.LogException;
import LOG4J.LogObj;
import java.io.IOException;
import java.util.*;
import java.text.DateFormat;
import yagg.adapter.adkgeneric.GenericAdapter;
import yagg.adapter.adkgeneric.GwyAdapterException;

/**
 *
 * @author Dell
 */
public class CAIRequest {
    public CAIRequest(GenericAdapter genericadapter, CAIMsgCount caimsgcount, Properties properties)
    {
        serverName = null;
        integName = null;
        intfName = null;
        reqUserData = null;
        rqAckFmt = null;
        ackFwdToGW = null;
        clientVersion = null;
        rplyGrp = null;
        port = 0;
        caiLoginId = null;
        caiPassword = null;
        lastRequest = null;
        debugArgs = new String[2];
        genAdapObj = genericadapter;
        lastMsgSentTime = Calendar.getInstance().getTime();
        v_LogObj = genAdapObj.getLogObj("yagg.cai");
        cmsg = new CAICommon(genAdapObj, caimsgcount, properties);
        int i = (new Integer(properties.getProperty("yagg.adapter.socketReadTimeout", "2000"))).intValue();
        css = new ClientSocketSvc("Enter command:", "\n", 65500, genAdapObj, i);
        genAdapObj.setsregister("RequestChannel", "UNUSED");
    }

    public int getCAIAttr()
    {
        try {
        if((serverName = genAdapObj.getAdapterAttribute("CAI_RQCHAN_IPADDR")) == null) {
            LogHandler.logErr(v_LogObj, "CAIRequest", "32", "");
            return 1;
        }
        port = Integer.parseInt(genAdapObj.getAdapterAttribute("CAI_RQCHAN_TCPPORT"));
        debugArgs[0] = serverName;
        debugArgs[1] = (new Integer(port)).toString();
        v_LogObj.LogInfo("NotUsed", "CAIRequest", "34", "Could not log", debugArgs, null);
        if((rqAckFmt = genAdapObj.getAdapterAttribute("CAI_RQACK_FORMAT")) == null)
            rqAckFmt = "KSIRequestAck";
        if((ackFwdToGW = genAdapObj.getAdapterAttribute("CAI_FORWARD_RQACK")) == null)
            ackFwdToGW = "false";
        
        String idleTimeoutStr;
        if((idleTimeoutStr = genAdapObj.getAdapterAttribute("CAI_IDLE_TIMEOUT")) == null)
            idleTimeout = CAIDef.CAI_IDLE_TIMEOUT;
        else idleTimeout = Integer.parseInt(idleTimeoutStr);

        debugArgs[0] = Integer.toString(idleTimeout);
        v_LogObj.LogInfo("NotUsed", "CAIRequest", "38", "Could not log", debugArgs, null);
        
        if((caiLoginId = genAdapObj.getAdapterAttribute("CAI_LOGIN")) == null) {
            LogHandler.logErr(v_LogObj, "CAIRequest", "35", "");
            return 1;
        }
        if((caiPassword = genAdapObj.getAdapterAttribute("CAI_PASSWORD")) == null) {
            LogHandler.logErr(v_LogObj, "CAIRequest", "36", "");
            return 1;
        }
        try
        {
            v_LogObj.LogInfo("NotUsed", "CAIRequest", "1", "Could not log", null, null);
        }
        catch(NumberFormatException numberformatexception)
        {
            LogHandler.logErr(v_LogObj, "CAIRequest", "32", "");
            return 1;
        }
        } catch(LogException logexception) { }
        return 0;
    }

    public boolean init()
    {
        if(!css.openSocket(serverName, port)) {
            LogHandler.logInform(v_LogObj, "CAIRequest", "27", null);
            return false; 
        } else {
            genAdapObj.setsregister("RequestChannel", "CONNECTED");
            try {
                css.readLoginSocket();
            } catch (IOException ioexception) {
                LogHandler.logErr(v_LogObj, "CAIRequest", "16", ioexception.getMessage());
                return false;
            }
            authenticated = login();
            if ( authenticated == 0 )
                if ( lastRequest != null ) send(lastRequest);
            return true;
        }
    }

    private int login()
    {
        int auth = 0;
        String loginRequest = "LOGIN:" + caiLoginId + ":" + caiPassword + ";\n";
        debugArgs[0] = (new Integer(loginRequest.length())).toString();
        debugArgs[1] = "LOGIN:" + caiLoginId + ":****;\n";
        try {
            v_LogObj.LogInfo("NotUsed", "CAIRequest", "13", "Could not log", debugArgs, null);
            css.writeSocket(loginRequest);
        } catch (IOException ioexception) {
            LogHandler.logErr(v_LogObj, "CAIRequest", "14", ioexception.getMessage());
            return 4;            
        } catch (Exception exception) {
            LogHandler.logErr(v_LogObj, "CAIRequest", "24", exception.getMessage());            
        }
        String resp;
        try {
            resp = css.readSocket();
        } catch (IOException ioexception) {
            LogHandler.logErr(v_LogObj, "CAIRequest", "16", ioexception.getMessage());
            return 6;
        } catch (Exception exception) {
            LogHandler.logErr(v_LogObj, "CAIRequest", "26", exception.getMessage());
            return 6;
        }
        if(resp.length() == 0) {
            LogHandler.logErr(v_LogObj, "CAIRequest", "23", null);
            return 6;
        }
        
        LogHandler.logInform(v_LogObj, "CAIRequest", "37", resp);
        int respBegin = resp.indexOf("RESP");
        if ( respBegin != -1 ) resp = resp.substring(respBegin);        
        int lastSepIdx = resp.lastIndexOf(":");
        int respTerm = resp.lastIndexOf(";");
        String respCode = resp.substring(lastSepIdx+1, respTerm);
        int intRespCode = (new Integer(respCode)).intValue();
        if(intRespCode != 0)
        {
            genAdapObj.incrcount("nProtocolErrors");
            LogHandler.logErr(v_LogObj, "CAIRequest", "28", "");
            auth = 1;
        }
        
        lastMsgSentTime = Calendar.getInstance().getTime();
        return auth;
    }

    public void reconnect()
    {
        if ( idleTimeout == 0 ) return;
        long lstmsgtime = lastMsgSentTime.getTime();
        long now = new Date().getTime();
        long diff = now - lstmsgtime;
        if ( diff > idleTimeout*1000 ) {
            LogHandler.logInform(v_LogObj, "CAIRequest", "39", null);
            cleanup();  
            init();            
        }
    }
    
    public int send(String s)
    {
//        reconnect();
        lastRequest = s;
        int i = 0;
        debugArg[0] = (new Integer(i)).toString();
        LogHandler.logInform(v_LogObj, "CAIRequest", "3", debugArg[0]);
        i = request(s);
        if ( i == 1 ) {
            i = requestAck();
            if ( i == 1 ) {
                lastMsgSentTime = Calendar.getInstance().getTime();
                lastRequest = null;
                return 0;
            } else {
                rollbackMsg();
                genAdapObj.incrcount("nProtocolErrors");
                return 2;
            }
        } else {
            rollbackMsg();
            return 2; 
        }
    }

    public int request(String msg)
    {
        debugArgs[0] = (new Integer(msg.length())).toString();
        debugArgs[1] = msg.toString();        
        try {
            v_LogObj.LogInfo("NotUsed", "CAIRequest", "13", "Could not log", debugArgs, null);
            css.writeSocket(msg);
        } catch (IOException ioexception) {
            LogHandler.logErr(v_LogObj, "CAIRequest", "14", ioexception.getMessage());
            return 4;            
        } catch (Exception exception) {
            LogHandler.logErr(v_LogObj, "CAIRequest", "24", exception.getMessage());
            return 5;
        }

        try {
            genAdapObj.commit(cmsg.getSess());
            genAdapObj.updateReadLatency(cmsg.getSess());
            v_LogObj.LogInfo("NotUsed", "CAIRequest", "22", "Could not log", null, null);
        } catch (LogException logexception) { }
        catch (GwyAdapterException gwyadapterexception) {
            LogHandler.logErr(v_LogObj, "CAIRequest", "25", gwyadapterexception.getMessage());
        }
        return 1;
    }

    public int requestAck()
    {
        Vector vector;
        vector = new Vector();
//        v_LogObj.LogInfo("NotUsed", "KSIRequest", "15", "Could not log", null, null);
        String resp;
        try {
            resp = css.readSocket();
        } catch (IOException ioexception) {
            LogHandler.logErr(v_LogObj, "CAIRequest", "16", ioexception.getMessage());
            return 6;
        } catch (Exception exception) {
            LogHandler.logErr(v_LogObj, "CAIRequest", "26", exception.getMessage());
            return 6;
        }
        if(resp.length() == 0) {
            LogHandler.logErr(v_LogObj, "CAIRequest", "23", null);
            return 6;
        }
        if(!cmsg.validateMsgLen(resp) && !cmsg.validateCommand(resp, CAIDef.CMD_TYPE[0])) {
            LogHandler.logErr(v_LogObj, "CAIRequest", "21", "");
            return 5;
        }
        int intRespCode;
        StringTokenizer stringtokenizer = cmsg.getMsgHdrFlds(resp);
        int i = stringtokenizer.countTokens();
        for(int j = 0; j < i; j++)
            vector.addElement(stringtokenizer.nextToken());

        LogHandler.logInform(v_LogObj, "CAIRequest", "37", resp);
        int lastSepIdx = resp.lastIndexOf(":");
        int respTerm = resp.lastIndexOf(";");
        String respCode = "";
        if ( lastSepIdx == -1 || respTerm == -1 ) respCode = "3007";
        else respCode = resp.substring(lastSepIdx+1, respTerm);
        
        intRespCode = (new Integer(respCode)).intValue();
        if(intRespCode != 0)
        {
            genAdapObj.incrcount("nProtocolErrors");
            LogHandler.logErr(v_LogObj, "CAIRequest", "28", "");
        }
        if(intRespCode == 3007)
        {
            authenticated = 0;
            authenticated = login();
        }
        if(ackFwdToGW.equals("true"))
        {
            if(!cmsg.buildGWmsg(vector, rqAckFmt))
                return 7;
        }
        if(intRespCode != 0)
        {
            return intRespCode;
        } else
        {
            msgDiscardedCnt("Number of discarded Request Ack messages");
        }
        return 1;
    }

    public boolean cleanup()
    {
        css.closeSocket();
        if(genAdapObj.getsregister("RequestChannel").equals("CONNECTED"))
            genAdapObj.setsregister("RequestChannel", "CLOSED");
        return true;
    }

    public CAICommon getCAICommonObj()
    {
        return cmsg;
    }

    public void rollbackMsg()
    {
        try
        {
            genAdapObj.rollback(cmsg.getSess());
        }
        catch(GwyAdapterException gwyadapterexception)
        {
            LogHandler.logInform(v_LogObj, "CAIRequest", "27", gwyadapterexception.getMessage());
        }
    }

    private void msgDiscardedCnt(String s)
    {
        if(genAdapObj.getcount(s) == null)
            genAdapObj.resetcount(s);
        genAdapObj.incrcount(s);
    }

    private ClientSocketSvc css;
    private CAICommon cmsg;
    private String serverName;
    private String integName;
    private String intfName;
    private String reqUserData;
    private String rqAckFmt;
    private String ackFwdToGW;
    private String clientVersion;
    private String rplyGrp;
    private int port;
    private int authenticated = 0;
    private Date lastMsgSentTime;
    private int idleTimeout = 0;
    private String caiLoginId;
    private String caiPassword;
    private String lastRequest;
    private static final int STATE_REQ = 0;
    private static final int STATE_REQ_ACK = 1;
    private static final int STATE_REQ_DONE = 2;
    private static final int STATE_REQ_ERR = 3;
    private static final int STATE_REQ_FAIL = 4;
    private static final int STATE_REQACK_ERR = 5;
    private static final int STATE_REQACK_FAIL = 6;
    private static final int STATE_REQ_MQERR = 7;
    private static final int ACK_SUCCESS = 0;
    private static final int ACK_RESEND = 1;
    private static final int ACK_CORRUPTED = 2;
    private String debugArg[] = {
        new String()
    };
    private String debugArgs[];
    private LogObj v_LogObj;
    private GenericAdapter genAdapObj;
}
