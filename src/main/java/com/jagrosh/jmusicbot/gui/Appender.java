package com.jagrosh.jmusicbot.gui;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Handles appending text to a JTextArea with a maximum line limit.
 */
class Appender implements Runnable {

    private static final String LINE_SEPARATOR = System.lineSeparator();

    private final JTextArea textArea;
    private final int maxLines;
    private final Queue<String> textQueue = new ConcurrentLinkedQueue<>();
    private final LinkedList<Integer> lineLengths = new LinkedList<>();
    private volatile boolean clear;

    public Appender(JTextArea textArea, int maxLines) {
        this.textArea = textArea;
        this.maxLines = maxLines;
    }

    public void append(String text) {
        textQueue.add(text);
        SwingUtilities.invokeLater(this);
    }

    public void clear() {
        clear = true;
        textQueue.clear();
        lineLengths.clear();
        SwingUtilities.invokeLater(this);
    }

    public void stop() {
        textQueue.clear();
    }

    @Override
    public void run() {
        if (clear) {
            textArea.setText("");
            clear = false;
            return;
        }

        while (!textQueue.isEmpty()) {
            String text = textQueue.poll();
            textArea.append(text);
            trackLineLengths(text);
        }
    }

    private void trackLineLengths(String text) {
        int length = text.length();
        if (text.endsWith(LINE_SEPARATOR)) {
            if (lineLengths.size() >= maxLines) {
                textArea.replaceRange("", 0, lineLengths.removeFirst());
            }
            lineLengths.add(length);
        }
    }
}
