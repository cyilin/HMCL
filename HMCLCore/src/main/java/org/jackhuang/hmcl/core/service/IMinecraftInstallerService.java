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
package org.jackhuang.hmcl.core.service;

import org.jackhuang.hmcl.core.install.InstallerType;
import org.jackhuang.hmcl.core.install.InstallerVersionList;
import org.jackhuang.hmcl.util.task.Task;

/**
 *
 * @author huangyuhui
 */
public abstract class IMinecraftInstallerService extends IMinecraftBasicService {

    public IMinecraftInstallerService(IMinecraftService service) {
        super(service);
    }

    public abstract Task download(String installId, InstallerVersionList.InstallerVersion v, InstallerType type);

    public abstract Task downloadForge(String installId, InstallerVersionList.InstallerVersion v);

    public abstract Task downloadOptiFine(String installId, InstallerVersionList.InstallerVersion v);

    public abstract Task downloadLiteLoader(String installId, InstallerVersionList.InstallerVersion v);

}
