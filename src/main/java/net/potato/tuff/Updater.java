package net.potato.tuff;

import com.google.gson.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import java.io.*;
import java.net.*;
import java.nio.channels.*;

public class Updater {

    private final JavaPlugin p;
    private final String cv;

    public Updater(JavaPlugin pl) {
        p = pl;
        cv = pl.getDescription().getVersion();
    }

    public void scheduleCheck() {
        if (!p.getConfig().getBoolean("updater.enabled", true)) return;
        long i = p.getConfig().getLong("updater.check-interval-minutes", 60) * 60 * 20;
        new BukkitRunnable() {
            @Override
            public void run() {
                cfu();
            }
        }.runTaskTimerAsynchronously(p, 0L, i);
    }

    private void cfu() {
        String llv = cv;

        String vu = "https://verytuffautoupdater.netlify.app/version-remote.json";

        try {
            URL u = new URL(vu);
            HttpURLConnection c = (HttpURLConnection) u.openConnection();
            c.setConnectTimeout(5000);

            if (c.getResponseCode() == 200) {
                InputStreamReader r = new InputStreamReader(c.getInputStream());
                JsonObject vi = new JsonParser().parse(r).getAsJsonObject();
                r.close();

                String wlv = vi.get("latestVersion").getAsString();

                if (in(wlv, cv)) {
                    p.getLogger().info("A new version is available: " + wlv + "! Downloading...");
                    String du = vi.get("downloadUrl").getAsString();
                    du(du);
                } else {
                    p.getLogger().info("You are running the latest version (" + cv + ").");
                }
            } else {
                p.getLogger().warning("Could not check for updates. (Response code: " + c.getResponseCode() + ")");
            }
        } catch (Exception e) {
            p.getLogger().warning("An error occurred while checking for updates: " + e.getMessage());
        }
    }

    private boolean in(String l, String c) {
        return !l.trim().equalsIgnoreCase(c);
    }
    
    private void du(String u) {
        try {
            File uf = p.getServer().getUpdateFolderFile();
            if (!uf.exists()) {
                uf.mkdirs();
            }
            File dt = new File(uf, p.getName() + ".jar");

            URL dl = new URL(u);
            ReadableByteChannel rbc = Channels.newChannel(dl.openStream());
            FileOutputStream fos = new FileOutputStream(dt);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            fos.close();
            rbc.close();

            p.getLogger().info("Successfully downloaded the new version to the 'update' folder.");
            p.getLogger().info("The update will be applied on the next server restart.");

            if (p.getConfig().getBoolean("updater.restart-on-update", false)) {
                p.getLogger().info("Restarting server to apply the update...");
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "restart");
                    }
                }.runTask(p);
            }
        } catch (Exception e) {
            p.getLogger().severe("The update download failed: " + e.getMessage());
        }
    }
}
