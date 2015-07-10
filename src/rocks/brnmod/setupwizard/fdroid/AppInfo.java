package rocks.brnmod.setupwizard.fdroid;

public class AppInfo {

    // Base information
    public final String id;
    public final String name;
    public final String summary;
    public final String desc;
    public final String icon;
    public final String license;
    public final String provides;
    // security
    public final String[] antifeatures;
    public final String requirements;
    // f-droid
    public final String category;
    public final String[] categories;
    public final String added;
    public final String lastupdated;
    // links
    public final String web;
    public final String changelog;
    public final String source;
    public final String tracker;
    // support app
    public final String flattr;
    public final String bitcoin;
    public final String litecoin;
    public final String donate;
    // versions
    public final AppPackage appPackages[];

    public AppInfo(
            String id, String name, String summary, String desc, String icon, String license,
            String provides,
            String requirements,
            String[] antifeatures,
            String category, String[] categories,
            String added, String lastupdated,
            String web, String changelog, String source, String tracker,
            String flattr, String bitcoin, String litecoin, String donate,
            AppPackage[] appPackages
    ) {
        this.id = id;
        this.name = name;
        this.summary = summary;
        this.desc = desc;
        this.icon = icon;
        this.license = license;
        this.provides = provides;
        this.requirements = requirements;
        this.antifeatures = antifeatures;
        this.category = category;
        this.categories = categories;
        this.added = added;
        this.lastupdated = lastupdated;
        this.web = web;
        this.changelog = changelog;
        this.source = source;
        this.tracker = tracker;
        this.flattr = flattr;
        this.bitcoin = bitcoin;
        this.litecoin = litecoin;
        this.donate = donate;
        this.appPackages = appPackages;
    }

}
