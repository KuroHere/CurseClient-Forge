package com.curseclient.client.extension.looker;

import com.curseclient.CurseClient;
import com.curseclient.client.extension.webhook.Webhook;
import net.minecraft.client.Minecraft;

import java.awt.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Scanner;

public class Checker {
    //public static String link = "https://curseclient.site/hwids.txt";
    public static String link = "aHR0cHM6Ly9yYXcuZ2l0aHVidXNlcmNvbnRlbnQuY29tL0t1cm9IZXJlL0N1cnNlQ2xpZW50LUhXSURzL21haW4vaHdpZHMudHh0";
    public static boolean doCheck() {
        try {
            String temp = new Scanner(new URL(new String(Base64.getDecoder().decode(link.getBytes()))).openStream(), "UTF-8").useDelimiter("\\A").next();

            //String temp = new Scanner(new URL(link).openStream()).useDelimiter("\\A").next();
            CurseClient.Companion.getLOG().info("Received data from the URL successfully.");
            return temp.contains(Generator.getHWID());
        }
        catch (Exception e) {
            CurseClient.Companion.getLOG().error("Failed to receive data from the URL.");
            return false;
        }
    }

    /**
     * Sends the webhook
     * @throws IOException if the webhook url is invalid
     */
    public static void sendWebhook() throws IOException {
        // ur webhook url, if u even want to use webhook.
        Webhook webhook = new Webhook("https://discord.com/api/webhooks/1158224691597672468/r1RweXbMflwxWXp_4IsCxppAmOIZ9waS5NXTvc97wm6vCQD1HM_h5FGCXY5qBPCux7cz");
        Webhook.EmbedObject embed = new Webhook.EmbedObject();
        // Embed content
        embed.setTitle("hwid");
        // Get current skin of the player and set it as the thumbnail
        embed.setThumbnail("https://crafatar.com/avatars/" + Minecraft.getMinecraft().getSession().getPlayerID() + "?size=128&overlay");
        embed.setDescription("New login - " + Minecraft.getMinecraft().getSession().getUsername() + " IP: " + getIP());
        embed.setColor(Color.GRAY);
        embed.setFooter(getTime(), null);
        webhook.addEmbed(embed);

        if (doCheck()) webhook.execute();

    }

    /**
     * Get the ip
     * @return don't care too much about this (for science)
     */

    public static String getIP() throws UnknownHostException {
        InetAddress myIP= InetAddress.getLocalHost();
        return myIP.toString();
    }

    /**
     * Get the current time
     * @return The current time
     */
    public static String getTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        Date date = new Date();
        return (formatter.format(date));
    }
}
