pluginManagement {

    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven(url = "http://maven.restlet.org")
        jcenter()
        google()
    }

    resolutionStrategy {
        eachPlugin {
            if (requested.id.id.startsWith("org.docbook")) {
                useModule("org.docbook:docbook-xslt2:2.3.11")
            }

            if(requested.id.id.startsWith("com.xmlcalabash")) {
                useModule("com.xmlcalabash:xmlcalabash1-print:1.1.5")
                useModule("com.xmlcalabash:xmlcalabash1-gradle:1.3.5")
            }
        }
    }
}
