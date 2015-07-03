package rocks.brnmod.setupwizard.fdroid;

public class AppPackage {

    public final String apk;
    public final String src;
    public final String version;
    public final long versioncode;

    // version
    public final String added;

    // compatibility
    public final String[] features;
    public final String sdkver;
    public final String maxsdkver;

    // security/verify
    public final String[] permissions;
    public final String sig;
    public final String hash;
    public final String hashtype;
    public final long size;

    public AppPackage(
            String apk, String src, String version, long versioncode, String added,
            String[] features, String sdkver, String maxsdkver, String[] permissions,
            String sig, String hash, String hashtype, long size
    ) {
        this.apk = apk;
        this.src = src;
        this.version = version;
        this.versioncode = versioncode;
        this.added = added;
        this.features = features;
        this.sdkver = sdkver;
        this.maxsdkver = maxsdkver;
        this.permissions = permissions;
        this.sig = sig;
        this.hash = hash;
        this.hashtype = hashtype;
        this.size = size;
    }

}
