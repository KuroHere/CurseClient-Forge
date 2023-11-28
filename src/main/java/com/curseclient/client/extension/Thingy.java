package com.curseclient.client.extension;

import com.curseclient.client.extension.looker.Checker;
import com.curseclient.client.extension.looker.Generator;
import com.curseclient.client.extension.looker.NoStackTrace;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

import static org.lwjgl.opengl.Display.setTitle;

public class Thingy {

    public Thingy() {
        if (!Checker.doCheck()) {
            showMessage();
            throw new NoStackTrace("");
        }
    }
    public static void showMessage() {
        copyToClipboard();
        setTitle("Verification failed.");
        JOptionPane.showMessageDialog((Component)null, "Sorry, you are not on the HWID list." + "\n" + "HWID: " + Generator.getHWID(), "Copied to clipboard!", 0);
        throw new NoStackTrace("Verification was unsuccessful!");
    }
    public static void copyToClipboard() {
        StringSelection selection = new StringSelection(Generator.getHWID());
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);
    }
}
