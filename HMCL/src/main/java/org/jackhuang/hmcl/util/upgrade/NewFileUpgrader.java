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
package org.jackhuang.hmcl.util.upgrade;

import java.io.File;
import java.io.IOException;
import org.jackhuang.hmcl.api.event.SimpleEvent;
import org.jackhuang.hmcl.api.HMCLog;
import org.jackhuang.hmcl.util.task.TaskWindow;
import org.jackhuang.hmcl.util.net.FileDownloadTask;
import org.jackhuang.hmcl.util.ArrayUtils;
import org.jackhuang.hmcl.api.VersionNumber;
import org.jackhuang.hmcl.util.sys.FileUtils;
import org.jackhuang.hmcl.util.sys.IOUtils;

/**
 *
 * @author huangyuhui
 */
public class NewFileUpgrader extends IUpgrader {

    @Override
    public void parseArguments(VersionNumber nowVersion, String[] args) {
        int i = ArrayUtils.indexOf(args, "--removeOldLauncher");
        if (i != -1 && i < args.length - 1) {
            File f = new File(args[i + 1]);
            if (f.exists())
                f.deleteOnExit();
        }
    }

    @Override
    public void accept(SimpleEvent<VersionNumber> event) {
        String str = requestDownloadLink();
        File newf = new File(FileUtils.getName(str));
        if (TaskWindow.factory().append(new FileDownloadTask(str, newf)).execute()) {
            try {
                new ProcessBuilder(new String[] { newf.getCanonicalPath(), "--removeOldLauncher", IOUtils.getRealPath() }).directory(new File("").getAbsoluteFile()).start();
            } catch (IOException ex) {
                HMCLog.err("Failed to start new app", ex);
            }
            System.exit(0);
        }
    }

    private String requestDownloadLink() {
        return null;
    }

}
