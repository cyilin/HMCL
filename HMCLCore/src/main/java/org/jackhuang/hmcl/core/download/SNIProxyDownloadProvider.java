package org.jackhuang.hmcl.core.download;


import org.jackhuang.hmcl.core.install.InstallerVersionList;
import org.jackhuang.hmcl.util.net.SNIProxy;

public class SNIProxyDownloadProvider extends MojangDownloadProvider {
    public SNIProxyDownloadProvider() {
        super();
    }
    
    @Override
    public InstallerVersionList getForgeInstaller() {
        SNIProxy.enable();
        return super.getForgeInstaller();
    }

    @Override
    public InstallerVersionList getLiteLoaderInstaller() {
        SNIProxy.enable();
        return super.getLiteLoaderInstaller();
    }

    @Override
    public InstallerVersionList getOptiFineInstaller() {
        SNIProxy.enable();
        return super.getOptiFineInstaller();
    }

    @Override
    public String getLibraryDownloadURL() {
        SNIProxy.enable();
        return super.getLibraryDownloadURL();
    }

    @Override
    public String getVersionsDownloadURL() {
        SNIProxy.enable();
        return super.getVersionsDownloadURL();
    }

    @Override
    public String getIndexesDownloadURL() {
        SNIProxy.enable();
        return super.getIndexesDownloadURL();
    }

    @Override
    public String getVersionsListDownloadURL() {
        SNIProxy.enable();
        return super.getVersionsListDownloadURL();
    }

    @Override
    public String getAssetsDownloadURL() {
        SNIProxy.enable();
        return super.getAssetsDownloadURL();
    }

    @Override
    public boolean isAllowedToUseSelfURL() {
        return true;
    }

    @Override
    public String getParsedDownloadURL(String str) {
        SNIProxy.enable();
        return super.getParsedDownloadURL(str);
    }
}
