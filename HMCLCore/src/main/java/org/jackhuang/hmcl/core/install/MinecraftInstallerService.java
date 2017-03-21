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
package org.jackhuang.hmcl.core.install;

import org.jackhuang.hmcl.core.service.IMinecraftInstallerService;
import java.io.File;
import org.jackhuang.hmcl.core.service.IMinecraftService;
import org.jackhuang.hmcl.core.install.InstallerVersionList.InstallerVersion;
import org.jackhuang.hmcl.core.install.forge.ForgeInstaller;
import org.jackhuang.hmcl.core.install.liteloader.LiteLoaderInstaller;
import org.jackhuang.hmcl.core.install.liteloader.LiteLoaderInstallerVersion;
import org.jackhuang.hmcl.core.install.optifine.OptiFineInstaller;
import org.jackhuang.hmcl.core.install.optifine.vanilla.OptiFineDownloadFormatter;
import org.jackhuang.hmcl.util.task.Task;
import org.jackhuang.hmcl.util.net.FileDownloadTask;
import org.jackhuang.hmcl.util.task.DeleteFileTask;

/**
 *
 * @author huangyuhui
 */
public final class MinecraftInstallerService extends IMinecraftInstallerService {

    public MinecraftInstallerService(IMinecraftService service) {
        super(service);
    }

    @Override
    public Task download(String installId, InstallerVersion v, InstallerType type) {
        switch (type) {
        case Forge:
            return downloadForge(installId, v);
        case OptiFine:
            return downloadOptiFine(installId, v);
        case LiteLoader:
            return downloadLiteLoader(installId, v);
        default:
            return null;
        }
    }

    @Override
    public Task downloadForge(String installId, InstallerVersion v) {
        File filepath = new File("forge-installer.jar").getAbsoluteFile();
        if (v.installer == null)
            return null;
        else
            return new FileDownloadTask(service.getDownloadType().getProvider().getParsedDownloadURL(v.installer), filepath).setTag("forge")
                .with(new ForgeInstaller(service, filepath))
                .with(new DeleteFileTask(filepath));
    }

    @Override
    public Task downloadOptiFine(String installId, InstallerVersion v) {
        File filepath = new File("optifine-installer.jar").getAbsoluteFile();
        if (v.installer == null)
            return null;
        OptiFineDownloadFormatter task = new OptiFineDownloadFormatter(v.installer);
        return task.with(new FileDownloadTask(filepath).registerPreviousResult(task).setTag("optifine"))
            .with(new OptiFineInstaller(service, installId, v, filepath))
            .with(new DeleteFileTask(filepath));
    }

    @Override
    public Task downloadLiteLoader(String installId, InstallerVersion v) {
        if (!(v instanceof LiteLoaderInstallerVersion))
            throw new Error("Download lite loader but the version is not ll's.");
        File filepath = new File("liteloader-universal.jar").getAbsoluteFile();
        FileDownloadTask task = (FileDownloadTask) new FileDownloadTask(v.universal, filepath).setTag("LiteLoader");
        return task.with(new LiteLoaderInstaller(service, installId, (LiteLoaderInstallerVersion) v).registerPreviousResult(task))
            .with(new DeleteFileTask(filepath));
    }
}
