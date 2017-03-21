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
package org.jackhuang.hmcl.core.asset;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.jackhuang.hmcl.util.C;
import org.jackhuang.hmcl.api.HMCLog;
import org.jackhuang.hmcl.core.service.IMinecraftAssetService;
import org.jackhuang.hmcl.core.download.IDownloadProvider;
import org.jackhuang.hmcl.core.version.MinecraftVersion;
import org.jackhuang.hmcl.util.task.Task;
import org.jackhuang.hmcl.util.net.FileDownloadTask;
import org.jackhuang.hmcl.util.code.DigestUtils;
import org.jackhuang.hmcl.util.sys.FileUtils;
import org.jackhuang.hmcl.util.sys.IOUtils;
import org.jackhuang.hmcl.util.task.TaskInfo;

/**
 * Assets
 *
 * @author huangyuhui
 */
public abstract class IAssetsHandler {

    protected ArrayList<String> assetsDownloadURLs;
    protected ArrayList<File> assetsLocalNames;
    protected final String name;
    protected List<AssetsObject> assetsObjects;

    public IAssetsHandler(String name) {
        this.name = name;
    }

    public static final IAssetsHandler ASSETS_HANDLER;

    static {
        ASSETS_HANDLER = new AssetsMojangLoader(C.i18n("assets.list.1_7_3_after"));
    }

    /**
     * interface name
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * All the files assets needed
     *
     * @param mv The version that needs assets
     * @param mp Asset Service
     * @return just run it!
     */
    public abstract Task getList(MinecraftVersion mv, IMinecraftAssetService mp);

    /**
     * Will be invoked when the user invoked "Download all assets".
     *
     * @param sourceType Download Source
     *
     * @return Download File Task
     */
    public abstract Task getDownloadTask(IDownloadProvider sourceType);

    public abstract boolean isVersionAllowed(String formattedVersion);

    protected class AssetsTask extends TaskInfo {

        ArrayList<Task> al;
        String u;

        public AssetsTask(String url) {
            super(C.i18n("assets.download"));
            this.u = url;
        }

        @Override
        public void executeTask(boolean areDependTasksSucceeded) {
            if (assetsDownloadURLs == null || assetsLocalNames == null || assetsObjects == null)
                throw new IllegalStateException(C.i18n("assets.not_refreshed"));
            int max = assetsDownloadURLs.size();
            al = new ArrayList<>();
            int hasDownloaded = 0;
            for (int i = 0; i < max; i++) {
                String mark = assetsDownloadURLs.get(i);
                String url = u + mark;
                File location = assetsLocalNames.get(i);
                if (!FileUtils.makeDirectory(location.getParentFile()))
                    HMCLog.warn("Failed to make directories: " + location.getParent());
                if (location.isDirectory())
                    continue;
                boolean need = true;
                try {
                    if (location.exists()) {
                        FileInputStream fis = FileUtils.openInputStream(location);
                        String sha = DigestUtils.sha1Hex(IOUtils.toByteArray(fis));
                        IOUtils.closeQuietly(fis);
                        if (assetsObjects.get(i).getHash().equals(sha)) {
                            ++hasDownloaded;
                            HMCLog.log("File " + assetsLocalNames.get(i) + " has been downloaded successfully, skipped downloading.");
                            if (ppl != null)
                                ppl.setProgress(this, hasDownloaded, max);
                            continue;
                        }
                    }
                } catch (IOException e) {
                    HMCLog.warn("Failed to get hash: " + location, e);
                    need = !location.exists();
                }
                if (need)
                    al.add(new FileDownloadTask(url, location).setTag(assetsObjects.get(i).getHash()));
            }
        }

        @Override
        public Collection<Task> getAfterTasks() {
            return al;
        }
    }
}
