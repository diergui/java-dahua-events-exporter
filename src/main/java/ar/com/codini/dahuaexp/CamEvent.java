/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ar.com.codini.dahuaexp;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Administrador
 */
public class CamEvent implements Serializable {

    public int index = -1;
    public String channel;
    public String cluster;
    public String disk;
    public String startTime;
    public String endTime;
    public String event;
    public String filePath;
    public String length;
    public String partition;
    public String videostream;

    public void assignValue(final String s) {

        if (s.contains(".Channel")) {
            channel = getValue(s);

        } else if (s.contains(".Cluster")) {
            cluster = getValue(s);

        } else if (s.contains(".Disk")) {
            disk = getValue(s);

        } else if (s.contains(".StartTime")) {
            startTime = getValue(s);

        } else if (s.contains(".EndTime")) {
            endTime = getValue(s);

        } else if (s.contains(".Events")) {
            event = getValue(s);

        } else if (s.contains(".FilePath")) {
            filePath = getValue(s);

        } else if (s.contains(".Length")) {
            length = getValue(s);

        } else if (s.contains(".Partition")) {
            partition = getValue(s);

        } else if (s.contains(".VideoStream")) {
            videostream = getValue(s);
        }

    }

    private String getValue(String s) {
        final String[] split = s.split("=");

        if (split.length == 2) {
            return split[1];
        } else {
            return "";
        }
    }

    public String generateFilePath() throws ParseException {
        // FilePath: '/mnt/sd/2018-05-19/001/dav/10/10.36.45-10.45.00[R][0@0][0].dav',
        
        //http://10.11.12.237/cgi-bin/RPC_Loadfile/mnt/sd/2019-02-01/001/dav/2/11.41.43-12.04.06[R][0@0][0].dav
        
        final Date startDate = stringToDate(startTime);
        final Date endDate = stringToDate(endTime);

        String f = "mnt/sd/";
        f += extractDate(startDate);
        f += "/001/dav/";
        f += "02" + "/";
        f += extractTime(startDate) + "-" + extractTime(endDate);
        f += "[R][0@0][0].dav";

        return f;
    }

    public String generateFilename() throws ParseException {

        String filename = Main.NVP_IP + "_ch" + channel + "_";
        filename += dateToString(stringToDate(startTime));
        filename += '_' + dateToString(stringToDate(endTime));
        filename += ".dav";

        return filename;

    }

    private Date stringToDate(final String strDate) throws ParseException {
        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final Date result = df.parse(strDate);
        return result;
    }

    private String dateToString(final Date fecha) {
        final SimpleDateFormat sdf = new SimpleDateFormat("YYYYMMDDhhmmss");
        //System.err.println("'" + sdf.format(fecha.getTime()) + "'");
        return (sdf.format(fecha.getTime()));
    }

    private String extractDate(final Date fecha) {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        //System.err.println("'" + sdf.format(fecha.getTime()) + "'");
        return (sdf.format(fecha.getTime()));
    }

    private String extractTime(final Date fecha) {
        final SimpleDateFormat sdf = new SimpleDateFormat("HH.mm.ss");
        //System.err.println("'" + sdf.format(fecha.getTime()) + "'");
        return (sdf.format(fecha.getTime()));
    }

}
