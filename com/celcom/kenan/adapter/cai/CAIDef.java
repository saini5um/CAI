/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.celcom.kenan.adapter.cai;

/**
 *
 * @author Dell
 */
public class CAIDef {
    public static final String PACKAGE_NAME = "yagg.cai";
    public static final String EOM = "$END$";
    public static final int EOM_LEN = 6;
    public static final String CMD_TYPE[] = {
        "LOGOUT", "RESP", "LOGIN"
    };
    public static final String ACK_STATUS_REASON = "ACK_REASON";
    public static final String ACK_STATUS = "ACK_STATUS";
    public static final String MSG_ID = "MSG_ID";
    public static final String USER_DATA = "USER_DATA";
    public static final int MAX_MSG_LEN = 65500;
    public static final int VERSION_LEN = 21;
    public static final int CMD_TYPE_LEN = 19;
    public static final int CMD_ACTION_LEN = 19;
    public static final int ELEMENT_TYPE_LEN = 19;
    public static final int REPLYTO_GROUP_LEN = 6;
    public static final int MSGLEN_LEN = 11;
    public static final int MAX_USERDATA_LEN = 256;
    public static final int USERDATA_VAL_OFFSET = 11;
    public static final int VERSION_OFFSET = 0;
    public static final int CMD_TYPE_OFFSET = 21;
    public static final int CMD_ACTION_OFFSET = 40;
    public static final int ELEMENT_TYPE_OFFSET = 59;
    public static final int REPLYTO_GROUP_OFFSET = 78;
    public static final int MSGLEN_OFFSET = 84;
    public static final int HDR_LEN = 95;
    public static final int CAI_IDLE_TIMEOUT = 240;
    public static final String FLD_DELIM = "\n";
    public static final String NAME_VAL_DELIM = "=";
    public static final int ACK_STATUS_VAL[] = {
        0, 1, 2
    };
    public static final String ACK_STATUS_REASON_VAL[] = {
        "successful", "resend", "corrupt", "unknown"
    };
    public static final int SUCCESS = 0;
    public static final int FAILURE = 1;
    public static final int RE_ATTEMPT = 2;
}
