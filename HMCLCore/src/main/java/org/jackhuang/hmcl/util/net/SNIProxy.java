package org.jackhuang.hmcl.util.net;

import org.jackhuang.hmcl.api.HMCLApi;
import org.jackhuang.hmcl.api.event.config.DownloadTypeChangedEvent;
import org.jackhuang.hmcl.api.func.Consumer;
import org.xbill.DNS.*;
import sun.net.util.IPAddressUtil;

import java.io.*;
import java.net.InetAddress;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SNIProxy {
    public static String sniProxyFile = "sniproxy.txt";
    public static boolean available = false;
    public static boolean enabled = false;
    private static File file;
    final Consumer<DownloadTypeChangedEvent> onDownloadTypeChanged = event -> {
        if (event.getValue() != null && (event.getValue().equalsIgnoreCase("SNIProxyDownloadProvider"))) {
            enable();
        } else {
            disable();
        }
    };

    public SNIProxy() {
        HMCLApi.EVENT_BUS.channel(DownloadTypeChangedEvent.class).register(onDownloadTypeChanged);
    }

    public static void init() {
        available = false;
        Path currentDir = Paths.get(".");
        file = new File(currentDir.toFile(), sniProxyFile);
        Lookup.getDefaultCache(Type.A).clearCache();
        if (file != null && file.isFile()) {
            load();
        }
    }

    public static boolean isAvailable() {
        return available;
    }

    public static void enable() {
        if (!enabled) {
            System.out.println("Enable SNI Proxy");
            init();
            enabled = true;
        }
    }

    public static void disable() {
        System.out.println("Disable SNI Proxy");
        Lookup.getDefaultCache(Type.A).clearCache();
        enabled = false;
    }

    public static void load() {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = bufferedReader.readLine()) != null && line.length() > 9) {
                String[] str = line.split(" ");
                if (str != null && str.length == 2) {
                    addCache(str[1], str[0]);
                }
            }
        } catch (Exception e) {
            available = false;
            e.printStackTrace();
        }
        available = true;
    }

    public static void addCache(String hostname, String ip) {
        try {
            InetAddress inetAddress = getInetAddress(ip);
            if (inetAddress != null) {
                Record record = new ARecord(new Name(hostname + "."), Type.A, 12 * 3600, inetAddress);
                Lookup.getDefaultCache(Type.A).addRecord(record, Credibility.NORMAL, "");
            }
        } catch (TextParseException e) {
            e.printStackTrace();
        }
    }

    public static InetAddress getInetAddress(String ip) {
        try {
            return InetAddress.getByAddress(IPAddressUtil.textToNumericFormatV4(ip));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
