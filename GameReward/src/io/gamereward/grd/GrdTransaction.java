package io.gamereward.grd;

import java.math.BigDecimal;
import java.util.Date;

public class GrdTransaction {
    public long transid;
    public long transdate;
    public String tx;
    public String from;
    public String to;
    public BigDecimal amount;
    public int transtype;
    public int status;
    public Date getTime(){
        Date date = new java.util.Date(transdate*1000L);
        return date;
    }
}
