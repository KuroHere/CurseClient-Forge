package com.curseclient.client.extension;

import com.curseclient.client.extension.looker.Checker;
import com.curseclient.client.extension.looker.Generator;
import com.curseclient.client.extension.looker.NoStackTrace;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

//TODO: make this less ugly

public class Thingy {

    public static int deltaTime;

    public Thingy() {
        if (!Checker.doCheck()) {
            showMessage();
            throw new NoStackTrace("");
        }
    }
    public static void showMessage() {
        copyToClipboard();
        JOptionPane.showMessageDialog((Component)null, "HWID: " + Generator.getHWID(), "Copied to clipboard!", 0);
        throw new NoStackTrace("Verification was unsuccessful!");
    }
    public static void copyToClipboard() {
        StringSelection selection = new StringSelection(Generator.getHWID());
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);
    }
}
