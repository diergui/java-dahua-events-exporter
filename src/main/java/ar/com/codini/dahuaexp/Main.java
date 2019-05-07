/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ar.com.codini.dahuaexp;

import com.google.gson.Gson;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Base64.getEncoder().encodeToString("admin".getBytes()),
 *
 * @author Administrador
 */
public class Main {

    public static final String NVP_IP = "10.11.12.237";
    public static final String NVP_USER = "admin";
    public static final String NVP_PASS = "password";

    public static final String EVENTS_CHANNEL = "3";
    public static final String EVENTS_STARTTIME = "2019-02-01%2012:00:00";
    public static final String EVENTS_ENDTIME = "2019-02-01%2013:00:00";

    public static void main(String... args) {
        Integer finder = 0;

        try {

            finder = createFinder();

            createSearch(finder);

            final List<CamEvent> camEvents = getEventList(finder);

            System.out.println(new Gson().toJson(camEvents));

            downloadEvents(camEvents);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        destroyFinder(finder);

    }

    /**
     * Create a media file finder. Throws an exception if bad user/password
     *
     * @return result=465
     * @throws IOException
     */
    public static Integer createFinder() throws IOException {

        final String urlStr = "http://" + NVP_IP + "/cgi-bin/mediaFileFind.cgi?action=factory.create";
        System.out.println(urlStr);

        final DefaultHttpClient client = new DefaultHttpClient();
        client.getCredentialsProvider().setCredentials(
                AuthScope.ANY,
                new UsernamePasswordCredentials(NVP_USER, NVP_PASS)
        );

        final HttpGet httpGet = new HttpGet(urlStr);
        final HttpResponse response = client.execute(httpGet);

        System.out.println(" >>> Response = " + response);

        final BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        final StringBuilder responseString = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            responseString.append(line);
        }

        reader.close();

        final String responseStr = responseString.toString().trim();
        System.out.println(responseStr);

        if (responseStr.contains("result=")) {
            final String result = responseStr.replaceAll("result=", "");
            return Integer.parseInt(result);
        } else {
            throw new IOException("Respuesta inesperada: " + responseStr);
        }

    }

    /**
     * Start to find files.
     * @param finder ID created by createFinder();
     * @throws IOException 
     */
    public static void createSearch(final Integer finder) throws IOException {

        final String urlStr = "http://" + NVP_IP + "/cgi-bin/mediaFileFind.cgi?"
                + "action=findFile"
                + "&object=" + finder + ""
                + "&condition.Channel=" + EVENTS_CHANNEL
                + "&condition.StartTime=" + EVENTS_STARTTIME
                + "&condition.EndTime=" + EVENTS_ENDTIME
                + "&condition.Events[1]=VideoMotion"
                + "&condition.Types[0]=dav";
        System.out.println(urlStr);

        final DefaultHttpClient client = new DefaultHttpClient();
        client.getCredentialsProvider().setCredentials(
                AuthScope.ANY,
                new UsernamePasswordCredentials(NVP_USER, NVP_PASS)
        );

        final HttpGet httpGet = new HttpGet(urlStr);
        final HttpResponse response = client.execute(httpGet);

        System.out.println(" >>> Response = " + response);

        final BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        final StringBuilder responseString = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            responseString.append(line);
        }

        reader.close();

        final String responseStr = responseString.toString().trim();
        System.out.println(responseStr);

        if (!responseStr.contains("OK")) {
            throw new IOException("Respuesta inesperada: " + responseStr);
        }

    }

    /**
     * Gets and create a list of recoding events.
     * @param finder ID created by createFinder();
     * @return List of camera events.
     * @throws IOException 
     */
    public static List<CamEvent> getEventList(final Integer finder) throws IOException {
        final List<CamEvent> camEvents = new ArrayList<>();

        final String urlStr = "http://" + NVP_IP + "/cgi-bin/mediaFileFind.cgi?"
                + "action=findNextFile"
                + "&object=" + finder + ""
                + "&count=100";
        System.out.println(urlStr);

        final DefaultHttpClient client = new DefaultHttpClient();
        client.getCredentialsProvider().setCredentials(
                AuthScope.ANY,
                new UsernamePasswordCredentials(NVP_USER, NVP_PASS)
        );

        final HttpGet httpGet = new HttpGet(urlStr);
        final HttpResponse response = client.execute(httpGet);

        System.out.println(" >>> Response = " + response);

        final BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        final StringBuilder responseString = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            responseString.append(line);
        }

        reader.close();

        final String responseStr = responseString.toString().trim();
        System.out.println(responseStr);

        {// Parsing events...
            final String[] splited = responseStr.split("items\\[");

            CamEvent currentCamEvent = null;

            for (final String s : splited) {

                if (s.contains("]")) {
                    final CharSequence subSequence = s.subSequence(0, s.indexOf("]"));

                    final int currentIndex = Integer.parseInt(String.valueOf(subSequence));

                    if (currentCamEvent == null || currentCamEvent.index != currentIndex) {
                        currentCamEvent = new CamEvent();
                        currentCamEvent.index = currentIndex;

                        camEvents.add(currentCamEvent);
                    }

                    currentCamEvent.assignValue(s);
                }
            }

        }

        return camEvents;

    }

    /**
     * Stop find. 
     * @param finder ID created by createFinder();
     */
    public static void destroyFinder(final Integer finder) {

        try {
            final String urlStr = "http://" + NVP_IP + "/cgi-bin/mediaFileFind.cgi?action=close&object=" + finder;
            System.out.println(urlStr);

            final DefaultHttpClient client = new DefaultHttpClient();
            client.getCredentialsProvider().setCredentials(
                    AuthScope.ANY,
                    new UsernamePasswordCredentials(NVP_USER, NVP_PASS)
            );

            final HttpGet httpGet = new HttpGet(urlStr);
            final HttpResponse response = client.execute(httpGet);

            System.out.println(" >>> Response = " + response);

            final BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            final StringBuilder responseString = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                responseString.append(line);
            }

            reader.close();

            final String responseStr = responseString.toString().trim();
            System.out.println(responseStr);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            final String urlStr = "http://" + NVP_IP + "/cgi-bin/mediaFileFind.cgi?action=destroy&object=" + finder;
            System.out.println(urlStr);

            final DefaultHttpClient client = new DefaultHttpClient();
            client.getCredentialsProvider().setCredentials(
                    AuthScope.ANY,
                    new UsernamePasswordCredentials(NVP_USER, NVP_PASS)
            );

            final HttpGet httpGet = new HttpGet(urlStr);
            final HttpResponse response = client.execute(httpGet);

            System.out.println(" >>> Response = " + response);

            final BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            final StringBuilder responseString = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                responseString.append(line);
            }

            reader.close();

            final String responseStr = responseString.toString().trim();
            System.out.println(responseStr);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    /**
     * Download listed events. WITHOUT TESTING, because my NVR never returns the FilePath attribute :-(
     * @param events 
     */
    public static void downloadEvents(final List<CamEvent> events) {

        for (int i = 0; i < events.size(); i++) {

            try {
                final String urlStr = "http://" + NVP_IP + "/cgi-bin/RPC_Loadfile/" + events.get(i).generateFilePath();
                System.out.println(urlStr);

                BufferedInputStream in = new BufferedInputStream(new URL(urlStr).openStream());
                FileOutputStream fileOutputStream = new FileOutputStream(events.get(i).generateFilename());
                byte dataBuffer[] = new byte[1024];
                int bytesRead;

                while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                    fileOutputStream.write(dataBuffer, 0, bytesRead);
                }

                Thread.sleep(750);

            } catch (Exception ex) {
                System.err.println(ex);
            }

        }

    }

}
