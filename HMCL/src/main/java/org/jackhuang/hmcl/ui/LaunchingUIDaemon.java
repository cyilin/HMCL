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
package org.jackhuang.hmcl.ui;

import java.io.File;
import java.io.IOException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.jackhuang.hmcl.api.HMCLApi;
import org.jackhuang.hmcl.api.event.process.JVMLaunchFailedEvent;
import org.jackhuang.hmcl.api.event.process.JavaProcessExitedAbnormallyEvent;
import org.jackhuang.hmcl.api.event.process.JavaProcessStoppedEvent;
import org.jackhuang.hmcl.api.event.launch.LaunchEvent;
import org.jackhuang.hmcl.api.event.launch.LaunchSucceededEvent;
import org.jackhuang.hmcl.api.event.launch.LaunchingStateChangedEvent;
import org.jackhuang.hmcl.util.LauncherVisibility;
import org.jackhuang.hmcl.core.launch.GameLauncher;
import org.jackhuang.hmcl.setting.Profile;
import org.jackhuang.hmcl.setting.Settings;
import org.jackhuang.hmcl.util.HMCLGameLauncher;
import org.jackhuang.hmcl.util.MinecraftCrashAdvicer;
import org.jackhuang.hmcl.util.C;
import org.jackhuang.hmcl.util.MessageBox;
import org.jackhuang.hmcl.api.func.Consumer;
import org.jackhuang.hmcl.api.HMCLog;
import org.jackhuang.hmcl.api.event.launch.LaunchingState;
import org.jackhuang.hmcl.util.DefaultPlugin;
import org.jackhuang.hmcl.util.sys.FileUtils;
import org.jackhuang.hmcl.util.sys.PrintlnEvent;
import org.jackhuang.hmcl.util.sys.ProcessMonitor;

/**
 *
 * @author huangyuhui
 */
public class LaunchingUIDaemon {

    public LaunchingUIDaemon() {
        HMCLApi.EVENT_BUS.channel(LaunchingStateChangedEvent.class).register(LAUNCHING_STATE_CHANGED);
        HMCLApi.EVENT_BUS.channel(LaunchEvent.class).register(p -> {
            GameLauncher obj = (GameLauncher) p.getSource();
            HMCLGameLauncher.GameLauncherTag tag = (HMCLGameLauncher.GameLauncherTag) obj.getTag();
            if (tag.launcherVisibility == LauncherVisibility.CLOSE && !LogWindow.INSTANCE.isVisible()) {
                HMCLog.log("Without the option of keeping the launcher visible, this application will exit and will NOT catch game logs, but you can turn on \"Debug Mode\".");
                System.exit(0);
            } else if (tag.launcherVisibility == LauncherVisibility.KEEP)
                MainFrame.INSTANCE.closeMessage();
            else {
                if (LogWindow.INSTANCE.isVisible())
                    LogWindow.INSTANCE.setExit(() -> true);
                HMCLApi.EVENT_BUS.fireChannel(new LaunchingStateChangedEvent(obj, LaunchingState.WaitingForGameLaunching));
            }
            // We promise that JavaProcessMonitor.tag is LauncherVisibility
            // See events below.
            ProcessMonitor monitor = new ProcessMonitor(p.getValue());
            monitor.registerPrintlnEvent(PRINTLN);
            monitor.setTag(tag.launcherVisibility);
            monitor.start();
        });
        HMCLApi.EVENT_BUS.channel(LaunchSucceededEvent.class).register(p -> {
            int state = ((HMCLGameLauncher.GameLauncherTag) ((GameLauncher) p.getSource()).getTag()).state;
            if (state == 1)
                LAUNCH_FINISHER.accept(p);
            else if (state == 2)
                LAUNCH_SCRIPT_FINISHER.accept(p);
        });
        HMCLApi.EVENT_BUS.channel(JavaProcessStoppedEvent.class).register(event -> checkExit((LauncherVisibility) ((ProcessMonitor) event.getSource()).getTag()));
        HMCLApi.EVENT_BUS.channel(JavaProcessExitedAbnormallyEvent.class).register(event -> {
            ProcessMonitor monitor = (ProcessMonitor) event.getSource();
            int exitCode = event.getValue().getExitCode();
            HMCLog.err("The game exited abnormally, exit code: " + exitCode);
            monitor.waitForCommandLineCompletion();
            String[] logs = event.getValue().getStdOutLines().toArray(new String[0]);
            String errorText = null;
            for (String s : logs) {
                int pos = s.lastIndexOf("#@!@#");
                if (pos >= 0 && pos < s.length() - "#@!@#".length() - 1) {
                    errorText = s.substring(pos + "#@!@#".length()).trim();
                    break;
                }
            }
            String msg = C.i18n("launch.exited_abnormally") + " exit code: " + exitCode;
            if (errorText != null)
                msg += ", advice: " + MinecraftCrashAdvicer.getAdvice(FileUtils.readQuietly(new File(errorText)));
            HMCLog.err(msg);
            SwingUtilities.invokeLater(() -> LogWindow.INSTANCE.setVisible(true));
            noExitThisTime = true;
        });
        HMCLApi.EVENT_BUS.channel(JVMLaunchFailedEvent.class).register(event -> {
            int exitCode = event.getValue().getExitCode();
            HMCLog.err("Cannot create jvm, exit code: " + exitCode);
            SwingUtilities.invokeLater(() -> LogWindow.INSTANCE.setVisible(true));
            noExitThisTime = true;
        });
    }

    boolean noExitThisTime = false;

    void runGame(Profile profile) {
        MainFrame.INSTANCE.showMessage(C.i18n("ui.message.launching"));
        profile.launcher().genLaunchCode(value -> {
            DefaultPlugin.INSTANCE.saveAuthenticatorConfig();
            ((HMCLGameLauncher.GameLauncherTag) value.getTag()).state = 1;
        }, MainFrame.INSTANCE::failed, Settings.getInstance().getAuthenticator().getPassword());
    }

    void testGame(Profile profile) {
        MainFrame.INSTANCE.showMessage(C.i18n("ui.message.launching"));
        profile.launcher().genLaunchCode(value -> {
            DefaultPlugin.INSTANCE.saveAuthenticatorConfig();
            ((HMCLGameLauncher.GameLauncherTag) value.getTag()).state = 1;
            ((HMCLGameLauncher.GameLauncherTag) value.getTag()).launcherVisibility = LauncherVisibility.KEEP;
        }, MainFrame.INSTANCE::failed, Settings.getInstance().getAuthenticator().getPassword());
    }

    void makeLaunchScript(Profile profile) {
        MainFrame.INSTANCE.showMessage(C.i18n("ui.message.launching"));
        profile.launcher().genLaunchCode(value -> {
            DefaultPlugin.INSTANCE.saveAuthenticatorConfig();
            ((HMCLGameLauncher.GameLauncherTag) value.getTag()).state = 2;
        }, MainFrame.INSTANCE::failed, Settings.getInstance().getAuthenticator().getPassword());
    }

    private static final Consumer<PrintlnEvent> PRINTLN = t -> {
        LauncherVisibility l = ((LauncherVisibility) ((ProcessMonitor) t.getSource()).getTag());
        if (t.getLine().contains("LWJGL Version: ") && l != LauncherVisibility.KEEP)
            if (l != LauncherVisibility.HIDE_AND_REOPEN)
                MainFrame.INSTANCE.dispose();
            else
                MainFrame.INSTANCE.setVisible(false);
    };

    private static final Consumer<LaunchingStateChangedEvent> LAUNCHING_STATE_CHANGED = t -> {
        String message = null;
        switch (t.getValue()) {
            case LoggingIn:
                message = "launch.state.logging_in";
                break;
            case GeneratingLaunchingCodes:
                message = "launch.state.generating_launching_codes";
                break;
            case DownloadingLibraries:
                message = "launch.state.downloading_libraries";
                break;
            case DecompressingNatives:
                message = "launch.state.decompressing_natives";
                break;
            case WaitingForGameLaunching:
                message = "launch.state.waiting_launching";
                break;
            case Done:
                return;
        }
        MainFrame.INSTANCE.showMessage(C.i18n(message));
    };

    private static final Consumer<LaunchSucceededEvent> LAUNCH_FINISHER = event -> {
        try {
            ((GameLauncher) event.getSource()).launch(event.getValue());
        } catch (IOException e) {
            MainFrame.INSTANCE.failed(C.i18n("launch.failed_creating_process") + "\n" + e.getMessage());
            HMCLog.err("Failed to launch when creating a new process.", e);
        }
    };

    private void checkExit(LauncherVisibility v) {
        if (v == LauncherVisibility.HIDE_AND_REOPEN) {
            HMCLog.log("Launcher will not exit now.");
            MainFrame.INSTANCE.setVisible(true);
        } else if (v != LauncherVisibility.KEEP && !LogWindow.INSTANCE.isVisible() && !noExitThisTime) {
            HMCLog.log("Launcher will exit now.");
            System.exit(0);
        } else {
            HMCLog.log("Launcher will not exit now.");
            noExitThisTime = false;
        }
    }

    private static final Consumer<LaunchSucceededEvent> LAUNCH_SCRIPT_FINISHER = event -> {
        try {
            String s = JOptionPane.showInputDialog(C.i18n("mainwindow.enter_script_name"));
            if (s != null)
                MessageBox.show(C.i18n("mainwindow.make_launch_succeed") + " " + ((GameLauncher) event.getSource()).makeLauncher(s, event.getValue()).getAbsolutePath());
        } catch (IOException ex) {
            MessageBox.show(C.i18n("mainwindow.make_launch_script_failed"));
            HMCLog.err("Failed to create script file.", ex);
        }
        MainFrame.INSTANCE.closeMessage();
    };
}
