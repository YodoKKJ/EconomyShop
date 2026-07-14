package com.brunobeduschi.economyshop.chatpack;

import com.brunobeduschi.economyshop.EconomyShopPlugin;
import com.sun.net.httpserver.HttpServer;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ResourcePackServer {

    private final EconomyShopPlugin plugin;
    private HttpServer server;
    private File sourceDir;
    private File packFile;
    private volatile String hash;
    private String publicUrl;

    public ResourcePackServer(EconomyShopPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        if (!plugin.getConfig().getBoolean("resource-pack.enabled", true)) {
            return;
        }
        try {
            sourceDir = new File(plugin.getDataFolder(), "resourcepack_src");
            packFile = new File(plugin.getDataFolder(), "resourcepack.zip");

            if (!sourceDir.exists()) {
                extractPristineSource(sourceDir);
            }
            rebuildPack();

            int port = plugin.getConfig().getInt("resource-pack.port", 34567);
            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/resourcepack.zip", exchange -> {
                byte[] bytes = Files.readAllBytes(packFile.toPath());
                exchange.getResponseHeaders().set("Content-Type", "application/zip");
                exchange.sendResponseHeaders(200, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            });
            server.setExecutor(null);
            server.start();

            String configuredUrl = plugin.getConfig().getString("resource-pack.public-url", "");
            if (configuredUrl != null && !configuredUrl.isBlank()) {
                publicUrl = configuredUrl;
            } else {
                publicUrl = "http://127.0.0.1:" + port + "/resourcepack.zip";
                plugin.getLogger().warning("resource-pack.public-url não configurado no config.yml; usando "
                        + publicUrl + ", que só funciona para jogadores na mesma máquina do servidor. "
                        + "Configure um endereço público (ex: seu domínio ou IP) para jogadores remotos.");
            }
        } catch (IOException | NoSuchAlgorithmException e) {
            plugin.getLogger().severe("Falha ao iniciar o servidor de resource pack: " + e.getMessage());
        }
    }

    private void extractPristineSource(File targetDir) throws IOException {
        targetDir.mkdirs();
        try (InputStream in = plugin.getResource("resourcepack/pack.zip")) {
            if (in == null) {
                throw new IOException("resourcepack/pack.zip não encontrado dentro do jar");
            }
            try (ZipInputStream zis = new ZipInputStream(in)) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    if (entry.getName().startsWith("META-INF/")) {
                        continue;
                    }
                    File outFile = new File(targetDir, entry.getName());
                    if (entry.isDirectory()) {
                        outFile.mkdirs();
                        continue;
                    }
                    outFile.getParentFile().mkdirs();
                    try (OutputStream out = new FileOutputStream(outFile)) {
                        zis.transferTo(out);
                    }
                }
            }
        }
    }

    public synchronized void rebuildPack() throws IOException, NoSuchAlgorithmException {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(packFile))) {
            Path root = sourceDir.toPath();
            try (var stream = Files.walk(root)) {
                java.util.List<Path> files = stream.filter(Files::isRegularFile).toList();
                for (Path path : files) {
                    String entryName = root.relativize(path).toString().replace('\\', '/');
                    zos.putNextEntry(new ZipEntry(entryName));
                    Files.copy(path, zos);
                    zos.closeEntry();
                }
            }
        }
        hash = sha1(packFile);
    }

    public synchronized void updateBadgeColor(String tagId, String label, Color backgroundColor)
            throws IOException, NoSuchAlgorithmException {
        File textureFile = new File(sourceDir, "assets/minecraft/textures/font/badges/" + tagId + ".png");
        textureFile.getParentFile().mkdirs();
        BufferedImage image = BadgeRenderer.render(label, backgroundColor);
        ImageIO.write(image, "png", textureFile);
        rebuildPack();
    }

    private String sha1(File file) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        try (InputStream in = Files.newInputStream(file.toPath())) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
        }
        StringBuilder sb = new StringBuilder();
        for (byte b : digest.digest()) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    public boolean isRunning() {
        return server != null;
    }

    public String getHash() {
        return hash;
    }

    public String getPublicUrl() {
        return publicUrl;
    }
}
