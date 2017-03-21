/*
 * Hello Minecraft! Launcher.
 * Copyright (C) 2013  huangyuhui <huanghongxun2008@126.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see {http://www.gnu.org/licenses/}.
 */
package org.jackhuang.hmcl.util;

import org.jackhuang.hmcl.util.C;
import org.jackhuang.hmcl.util.StrUtils;
import org.jackhuang.hmcl.util.MessageBox;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.jackhuang.hmcl.api.HMCLog;
import static org.jackhuang.hmcl.Main.LAUNCHER_VERSION;
import org.jackhuang.hmcl.setting.Settings;
import org.jackhuang.hmcl.util.net.NetUtils;
import org.jackhuang.hmcl.util.sys.OS;
import org.jackhuang.hmcl.ui.LogWindow;

/**
 *
 * @author huangyuhui
 */
public class CrashReporter implements Thread.UncaughtExceptionHandler {

    private static final Logger LOGGER = Logger.getLogger(CrashReporter.class.getName());

    private static final HashMap<String, String> SOURCE = new HashMap<String, String>() {
        {
            put("MessageBox", "");
            put("AWTError", "");
            put("JFileChooser", "Has your operating system been installed completely or is a ghost system?");
            put("JSystemFileChooser", "Has your operating system been installed completely or is a ghost system?");
            put("Jce", "Has your operating system been installed completely or is a ghost system?");
            put("couldn't create component peer", "Fucking computer!");
            put("sun.awt.shell.Win32ShellFolder2", "crash.user_fault");
            put("UnsatisfiedLinkError", "crash.user_fault");
            put("java.awt.HeadlessException", "crash.headless");
            put("java.lang.NoClassDefFoundError", "crash.NoClassDefFound");
            put("java.lang.VerifyError", "crash.NoClassDefFound");
            put("java.lang.NoSuchMethodError", "crash.NoClassDefFound");
            put("java.lang.IncompatibleClassChangeError", "crash.NoClassDefFound");
            put("java.lang.ClassFormatError", "crash.NoClassDefFound");
            put("java.lang.OutOfMemoryError", "FUCKING MEMORY LIMIT!");
            put("Trampoline", "ui.message.update_java");
            put("NoSuchAlgorithmException", "Has your operating system been installed completely or is a ghost system?");
        }
    };

    boolean enableLogger = false;

    public CrashReporter(boolean enableLogger) {
        this.enableLogger = enableLogger;
    }

    public boolean checkThrowable(Throwable e) {
        String s = StrUtils.getStackTrace(e);
        for (HashMap.Entry<String, String> entry : SOURCE.entrySet())
            if (s.contains(entry.getKey())) {
                if (StrUtils.isNotBlank(entry.getValue())) {
                    String info = C.i18n(entry.getKey());
                    LOGGER.severe(info);
                    try {
                        showMessage(info);
                    } catch (Throwable t) {
                        LOGGER.log(Level.SEVERE, "Failed to show message", t);
                    }
                }
                return false;
            }
        return true;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        String s = StrUtils.getStackTrace(e);
        if (!s.contains("org.jackhuang"))
            return;
        try {
            String text = "\n---- Hello Minecraft! Crash Report ----\n";
            text += "  Version: " + LAUNCHER_VERSION + "\n";
            text += "  Time: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "\n";
            text += "  Thread: " + t.toString() + "\n";
            text += "\n  Content: \n    ";
            text += s + "\n\n";
            text += "-- System Details --\n";
            text += "  Operating System: " + OS.getSystemVersion() + "\n";
            text += "  Java Version: " + System.getProperty("java.version") + ", " + System.getProperty("java.vendor") + "\n";
            text += "  Java VM Version: " + System.getProperty("java.vm.name") + " (" + System.getProperty("java.vm.info") + "), " + System.getProperty("java.vm.vendor") + "\n";
            if (enableLogger)
                HMCLog.err(text);
            else
                System.out.println(text);

            if (checkThrowable(e) && !System.getProperty("java.vm.name").contains("OpenJDK")) {
                SwingUtilities.invokeLater(() -> LogWindow.INSTANCE.showAsCrashWindow(Settings.UPDATE_CHECKER.isOutOfDate()));
                if (!Settings.UPDATE_CHECKER.isOutOfDate())
                    reportToServer(text, s);
            }
        } catch (Throwable ex) {
            LOGGER.log(Level.SEVERE, "Failed to caught exception", ex);
            LOGGER.log(Level.SEVERE, "There is the original exception", e);
        }
    }

    void showMessage(String s) {
        try {
            MessageBox.show(s, "ERROR", MessageBox.ERROR_MESSAGE);
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, "ERROR", e);
        }
    }

    private static final HashSet<String> THROWABLE_SET = new HashSet<>();

    void reportToServer(final String text, String stacktrace) {
        if (THROWABLE_SET.contains(stacktrace) || stacktrace.contains("Font") || stacktrace.contains("InternalError"))
            return;
        THROWABLE_SET.add(stacktrace);
        Thread t = new Thread(() -> {
            HashMap<String, String> map = new HashMap<>();
            map.put("CrashReport", text);
            try {
                NetUtils.post(NetUtils.constantURL("http://huangyuhui.duapp.com/crash.php"), map);
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "Failed to post HMCL server.", ex);
            }
        });
        t.setDaemon(true);
        t.start();
    }

}
