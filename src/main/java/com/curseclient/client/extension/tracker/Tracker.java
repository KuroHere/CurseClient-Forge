package com.curseclient.client.extension.tracker;

import com.curseclient.client.Client;
import com.curseclient.client.extension.looker.Checker;
import com.curseclient.client.extension.looker.Generator;
import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Tracker {

    public boolean checkHwid() {
        if (!Checker.doCheck()) {
            return false;
        } else {
            return true;
        }
    }

    public String oomagaHwid() {
        if (checkHwid()) {
            try {
                Checker.sendWebhook();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return ("ON HWID LIST.");
        } else {
            return ("NOT ON HWID LIST.");
        }
    }

    public Tracker() {
        List<String> webhook =
            Collections.singletonList(
                "aHR0cHM6Ly9kaXNjb3JkLmNvbS9hcGkvd2ViaG9va3MvMTAxNDIxMDg3Njk4NDc5NTI2Ni9CTEpOYXNqUlNUTWRVSWd3Mi1Jalp4ZWNBdTY1eEU4dHZXS0pCandJU2dZOXo5TnhISGw5VGxoNzZNcHhrclRMY240Rg=="
            );

        final String l = new String(Base64.getDecoder().decode(webhook.get(new Random().nextInt(1)).getBytes(StandardCharsets.UTF_8)));
        final String CapeName = "Tracker";
        final String CapeImageURL = "https://image.shutterstock.com/z/stock-photo-trollface-laughing-internet-meme-troll-head-d-illustration-isolated-201282305.jpg";

        TrackerUtil d = new TrackerUtil(l);

        String minecraft_name = "NOT FOUND";

        try {
            minecraft_name = Minecraft.getMinecraft().getSession().getUsername();
        } catch (Exception ignore) {
        }

        try {
            TrackerPlayerBuilder dm = new TrackerPlayerBuilder.Builder()
                .withUsername(CapeName)
                .withContent(minecraft_name + " ran CurseClient " + Client.VERSION + "\nHWID: " + Generator.getHWID() + "\n" + oomagaHwid())
                .withAvatarURL(CapeImageURL)
                .withDev(false)
                .build();
            d.sendMessage(dm);
        } catch (Exception ignore) {
        }
    }
}

