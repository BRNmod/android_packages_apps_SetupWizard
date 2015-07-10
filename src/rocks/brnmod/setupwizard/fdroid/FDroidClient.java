package rocks.brnmod.setupwizard.fdroid;

import android.util.Xml;
import android.util.Base64;

import org.apache.harmony.security.utils.JarUtils;

import android.util.Log;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class FDroidClient {

    public final static byte[] fDroidKey = new byte[]{
          48,-126,   1,  34,  48,  13,   6,   9,  42,-122,  72,-122,  -9,  13,   1,   1,   1,   5,
           0,   3,-126,   1,  15,   0,  48,-126,   1,  10,   2,-126,   1,   1,   0,-106, -48, 117,
         -28, 124,   1,  78, 120,  34, -56, -97, -42, 127, 121,  93,  35,  32,  62,  42,-120,  67,
         -11,  59, -92, -26, -79, -65,  95,  47, -48, -30,  37,-109,-126, 103, -49, -54, -25,  -5,
         -12,  -2,  89,  99,  70, -81, -70, -12,   7,  15, -37,-111, -10, 111, -68, -33,  35,  72,
         -93, -39,  36,  48,  80,  40,  36,  -8,   5,  23, -79,  86,  -6, -80,   8,   9, -67, -56,
         -26,  49, -65, -87, -81, -44,  45,-112,  69, -85,  95, -42, -46,-115, -98,  20,  10,  -4,
          19,   0,-111, 123,  25, -73, -58, -60, -33,  74,  73,  76, -15,  -9, -53,  74,  99, -56,
          13, 115,  66, 101, -41,  53, -81, -98,  79,   9,  69,  95,  66, 122, -90,  90,  83,  86,
          63,-121, -77,  54, -54,  44,  25, -46,  68,  -4, -69, -90,  23, -70,  11,  25, -27, 110,
         -45,  74,  -2,  11,  37,  58, -71,  30,  47, -37,  18, 113, -15, -71, -29, -61,  35,  32,
          39, -19,-120,  98, -95,  18, -16, 112, 110,  35,  76, -14,  54,-111,  75,-109,-101, -49,
        -107,-104,  33, -20, -78, -90, -63,-128,  87, -32, 112, -34,  52,  40,   4, 109,-108, -79,
         117, -31, -40,-101, -41,-107, -27,  53,  73,-102,   9,  31,  91, -58,  90, 121, -43,  57,
         -88, -44,  56,-111, -20,  80,  64,  88, -84, -78,-116,   8,  57,  59,  87,  24, -75, 118,
           0, -94,  17, -24,   3, -12, -90,  52, -27, -59, 127,  37, -71, -72, -60,  66,  44, 111,
         -39,   2,   3,   1,   0,   1
    };

    private final static Set<String> acceptFields;
    static {
        acceptFields = new HashSet<String>();
        // fields needed for install
        acceptFields.add("id");
        acceptFields.add("name");
        acceptFields.add("apk");
        acceptFields.add("versioncode");
        acceptFields.add("hash");
        acceptFields.add("hashtype");
    }

    private final byte[][] acceptedKeys;
    private final String repo;

    public volatile Map<String, AppInfo> apps = Collections.emptyMap();

    public FDroidClient(String repo, byte[][] acceptedKeys) {
        this.repo = repo;
        this.acceptedKeys = acceptedKeys;
    }

    public FDroidClient() {
        this.repo = "http://f-droid.org/repo";
        this.acceptedKeys = new byte[][]{fDroidKey};
    }

    public void load(
            DownloadProgressListener progressListener
    ) throws IOException {
        load(null, progressListener);
    }

    public void load() throws IOException {
        load(null, null);
    }

    public void load(
        Set<String> fieldWhitelist,
        DownloadProgressListener progressListener
    ) throws IOException {
        Log.d("FDC", "Load app list");

        File tmp = File.createTempFile("index", ".jar");
        download(this.repo + "/index.jar", tmp, progressListener);

        if (!verify(tmp, acceptedKeys)) {
            Log.d("FDC", "Index verify failed");
            throw new SecurityException("Can't verify repository index index.jar");
        }

        if (progressListener != null) progressListener.update(0.95f);

        loadIndex(tmp, fieldWhitelist);

        if (progressListener != null) progressListener.update(1f);
    }

    public void download(String url, File target, DownloadProgressListener progressListener) throws IOException {
        Log.d("FDC", "Downloading " + url + " -> " + target);

        int length = (progressListener == null) ? 0 : getLength(url);

        // download index file

        OutputStream out = new FileOutputStream(target);
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        InputStream in = con.getInputStream();
        byte buf[] = new byte[4096];
        int read = 0;
        int written = 0;
        while ((read = in.read(buf)) >= 0) {
            if (read == 0) continue;
            written += read;
            out.write(buf,0,read);
            if (progressListener != null && length > 0) progressListener.update(0.9f * (written / (float)length));
        }
        out.close();
        in.close();
        con.disconnect();
    }

    public static boolean verify(File jar, byte[][] keys) throws IOException {
        JarFile jf = new JarFile(jar, false);

        Enumeration<JarEntry> i = jf.entries();
        while (i.hasMoreElements()) {
            JarEntry entry = i.nextElement();
            if (!entry.getName().startsWith("META-INF/")) continue;
            String s[] = entry.getName().split("/");
            if (s.length != 2) continue;
            if (!s[1].endsWith(".RSA") && !s[1].endsWith(".DSA") && !s[1].endsWith(".EC")) continue;

            Log.d("FDC", "Testing " + entry.getName());
            try {
                JarEntry sf = jf.getJarEntry("META-INF/" +
                        s[1].substring(0, s[1].lastIndexOf(".")) + ".SF");
                Certificate certs[] = JarUtils.verifySignature(
                    jf.getInputStream(sf),
                    jf.getInputStream(entry)
                );

                if (certs == null) continue;

                // check if we "like" one of the certs
                boolean goodCert = false;
                for (Certificate cert : certs) {
                    Log.d("FDC", "Testing " + cert);
                    byte b[] = cert.getPublicKey().getEncoded();
                    byte x[] = new byte[16];
                    for (int j = 0; j < b.length; j += 16) {
                        int len = Math.min(16, b.length - j);
                        if (len < 16) {
                            x = new byte[len];
                            System.arraycopy(b, j, x, 0, len);
                        } else {
                            System.arraycopy(b, j, x, 0, len);
                        }
                    }
                    for (byte[] k : keys) {
                        if (Arrays.equals(k, b)) {
                            goodCert = true;
                            break;
                        }
                    }
                    if (goodCert) break;
                }
                if (!goodCert) continue;

                Log.d("FDC", "Cert found! verifying the manifest");

                // we got a "good" cert, see if we can find and verify the manifest

                boolean manifestOk = false;

                LineNumberReader lnr = new LineNumberReader(new InputStreamReader(jf.getInputStream(sf)));
                String line = null;
                while ((line = lnr.readLine()) != null) {
                    if (!line.contains("-Digest-Manifest:")) continue;

                    String kv[] = line.split(":");
                    kv[0] = kv[0].trim();
                    String hash = kv[1].trim();
                    String algorithm = kv[0].substring(0, kv[0].lastIndexOf("-Digest-Manifest"));
                    String computed = computeIndexDigest(jf, jf.getJarEntry("META-INF/MANIFEST.MF"), algorithm);
                    Log.d("FDC", "algorithm=" + algorithm + ", hash=" + hash + ", computed=" + computed);
                    if (hash.contentEquals(computed)) {
                        // OK!
                        Log.d("FDC", "manifest hash match");
                        manifestOk = true;
                        break;
                    }
                }
                lnr.close();

                if (!manifestOk) return false;

                Log.d("FDC", "Manifest OK, verify index.xml");

                // read the manifest
                lnr = new LineNumberReader(new InputStreamReader(jf.getInputStream(jf.getJarEntry("META-INF/MANIFEST.MF"))));
                while ((line = lnr.readLine()) != null) {
                    if (!line.trim().equals("Name: index.xml")) continue;

                    while ((line = lnr.readLine()).contains("-Digest:")) {
                        String kv[] = line.split(":");
                        kv[0] = kv[0].trim();
                        String hash = kv[1].trim();
                        String algorithm = kv[0].substring(0, kv[0].lastIndexOf("-Digest"));
                        String computed = computeIndexDigest(jf, jf.getJarEntry("index.xml"), algorithm);
                        Log.d("FDC", "algorithm=" + algorithm + ", hash=" + hash + ", computed=" + computed);
                        if (hash.contentEquals(computed)) {
                            // OK!
                            Log.d("FDC", "index hash match - I'm done");
                            lnr.close();
                            return true;
                        }
                    }
                }
                lnr.close();

            } catch (GeneralSecurityException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private static String computeIndexDigest(JarFile jf, JarEntry entry, String algorithm) {
        if (entry == null) return null;
        try {
            InputStream in = jf.getInputStream(entry);
            MessageDigest md = MessageDigest.getInstance(algorithm);

            byte buf[] = new byte[4096];
            int read;
            while ((read = in.read(buf)) >= 0) {
                if (read > 0) md.update(buf, 0, read);
            }

            in.close();

            return Base64.encodeToString(md.digest(), 0).trim();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static int getLength(final String url) throws IOException {
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        con.setRequestMethod("HEAD");
        int length = Math.max(0, con.getHeaderFieldInt("Content-Length", 0));
        con.disconnect();
        return length;
    }

    public void loadIndex(final File jar, final Set<String> fieldWhitelist) throws IOException {
        Log.d("FDC", "Load index.xml");

        JarFile jf = new JarFile(jar, true);
        JarEntry entry = jf.getJarEntry("index.xml");

        InputStream in = jf.getInputStream(entry);
        Reader reader = new InputStreamReader(in, "UTF-8");

        final ArrayList<AppInfo> apps = new ArrayList<AppInfo>();

        try {
            Xml.parse(reader, new ContentHandler() {

                HashMap<String,String> current = new HashMap<String, String>();

                StringBuilder str = new StringBuilder();
                HashMap<String,String> application = new HashMap<String,String>();
                HashMap<String,String> pkg = new HashMap<String,String>();

                ArrayList<AppPackage> pkgs = new ArrayList<AppPackage>();

                private boolean fieldAllowed(String name) {
                    return fieldWhitelist == null || fieldWhitelist.contains(name) || acceptFields.contains(name);
                }
                private String getString(String name) {
                    return (fieldAllowed(name)) ? current.get(name) : null;
                }
                private String[] getStringArray(String name) {
                    String data = getString(name);
                    return (data == null) ? null : data.split(",");
                }
                private long getLong(String name) {
                    String data = getString(name);
                    return (data == null) ? -1l : Long.parseLong(data.trim());
                }

                @Override
                public void setDocumentLocator(Locator locator) {
                }

                @Override
                public void startDocument() throws SAXException {
                }

                @Override
                public void endDocument() throws SAXException {
                }

                @Override
                public void startPrefixMapping(String prefix, String uri) throws SAXException {
                }

                @Override
                public void endPrefixMapping(String prefix) throws SAXException {
                }

                @Override
                public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
                    if (localName.equals("application")) {
                        application.clear();
                        pkgs.clear();
                        current = application;
                        return;
                    }
                    if (localName.equals("package")) {
                        pkg.clear();
                        current = pkg;
                        return;
                    }
                    if (localName.equals("hash")) {
                        for (int i = 0; i < atts.getLength(); i++) {
                            if (!"type".equals(atts.getLocalName(i))) continue;
                            current.put("hashtype", atts.getValue(i));
                            break;
                        }
                    }
                    str.setLength(0);
                }

                @Override
                public void endElement(String uri, String localName, String qName) throws SAXException {
                    if (localName.equals("application")) {
                        // generate application
                        apps.add(new AppInfo(
                            getString("id"),
                            getString("name"),
                            getString("summary"),
                            getString("desc"),
                            getString("icon"),
                            getString("license"),
                            getString("provides"),
                            getString("requirements"),
                            getStringArray("antifeatures"),
                            getString("category"), getStringArray("categories"),
                            getString("added"), getString("lastupdated"),
                            getString("web"), getString("changelog"), getString("source"), getString("tracker"),
                            getString("flattr"), getString("bitcoin"), getString("litecoin"), getString("donate"),
                            pkgs.toArray(new AppPackage[pkgs.size()])
                        ));
                        pkgs.clear();
                        return;
                    }
                    if (localName.equals("package")) {
                        // generate package
                        pkgs.add(new AppPackage(
                            getString("apkname"),
                            getString("srcname"),
                            getString("version"),
                            getLong("versioncode"),
                            getString("added"),
                            getStringArray("features"),
                            getString("sdkver"),
                            getString("maxsdkver"),
                            getStringArray("permissions"),
                            getString("sig"),
                            getString("hash"),
                            getString("hashtype"),
                            getLong("size")
                        ));
                        current = application;
                        pkg.clear();                       
                        return;
                    }
                    if (str.length() > 0) {
                        current.put(localName, str.toString());
                    }
                }

                @Override
                public void characters(char[] ch, int start, int length) throws SAXException {
                    str.append(ch, start, length);
                }

                @Override
                public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
                }
                @Override
                public void processingInstruction(String target, String data) throws SAXException {
                }
                @Override
                public void skippedEntity(String name) throws SAXException {
                }
            });
        } catch (SAXException e) {
            throw new RuntimeException("Could not parse index.xml", e);
        }

        HashMap<String, AppInfo> result = new HashMap<String, AppInfo>();
        for (AppInfo app : apps) {
            result.put(app.id, app);
        }
        Log.d("FDC", "Loaded " + result.size() + " apps.");
        this.apps = Collections.unmodifiableMap(result);
    }

    public File downloadApk(String id, DownloadProgressListener progressListener) throws IOException {
        Log.d("FDC", "Downloading " + id);
        AppInfo app = apps.get(id);
        if (app == null) {
            Log.d("FDC", id + " not found.");
            return null;
        }

        long version = -1;
        AppPackage pkg = null;
        for (AppPackage p : app.appPackages) {
            if (p.versioncode <= version) continue;
            version = p.versioncode;
            pkg = p;
        }

        if (pkg == null) return null;

        File tmp = File.createTempFile(pkg.apk.substring(0, pkg.apk.lastIndexOf(".")) + "_", ".apk");
        download(this.repo + "/" + pkg.apk, tmp, progressListener);

        // verify the file
        try {
            if ("sha256".equals(pkg.hashtype)) {
                MessageDigest md = MessageDigest.getInstance("SHA256");
                InputStream in = new FileInputStream(tmp);
                byte[] buf = new byte[4096];
                int read = 0;
                while ((read = in.read(buf)) >= 0) {
                    if (read == 0) continue;
                    md.update(buf,0,read);
                }
                in.close();
                byte sha256[] = md.digest();
                StringBuilder sb = new StringBuilder(70);
                for (byte b : sha256) {
                    int i = b & 0xff;
                    if (i < 16) sb.append("0");
                    sb.append(Integer.toHexString(i));
                }
                if (!sb.toString().equalsIgnoreCase(pkg.hash)) {
                    Log.d("FDC", "Hash mismatch on apk.");
                    tmp.delete();
                    return null;
                }
            } else {
                Log.d("FDC", "No valid hash for apk.");
                tmp.delete();
                return null;
            }
        } catch (NoSuchAlgorithmException e) {
            Log.d("FDC", "Cannot verify APK.");
            tmp.delete();
            return null;
        }

        if (progressListener != null) progressListener.update(1f);

        return tmp;
    }

}
