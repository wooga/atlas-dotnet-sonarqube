package wooga.gradle.dotnetsonar.tasks.internal

class Downloader {

    void download(URL source, File dest, boolean overrides=false) {
        if(dest.exists() && overrides) {
            dest.delete()
        }
        if (!dest.exists()) {
            dest.createNewFile()
            source.withInputStream{ i ->
                dest.withOutputStream{ it << i }
            }
        }
    }

}
