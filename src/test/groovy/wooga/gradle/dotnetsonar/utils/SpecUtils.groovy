package wooga.gradle.dotnetsonar.utils

import groovy.json.StringEscapeUtils
import wooga.gradle.dotnetsonar.tasks.internal.Shell

class SpecUtils {

    static File emptyTmpFile(String name) {
        File file = new File(name)
        file.createNewFile()
        file.deleteOnExit()
        return file
    }

    static String escapedPath(String path) {
        String osName = System.getProperty("os.name").toLowerCase()
        if (osName.contains("windows")) {
            return StringEscapeUtils.escapeJava(path)
        }
        return path
    }

    static Throwable rootCause(Throwable e) {
        if(e.cause == null || e.cause == e) {
            return e
        }
        return rootCause(e.cause)

    }

    static File blocksWindowsFile(Shell shell, File file) {
        if(isWindows()) {
            shell.execute {
                it.executable "powershell.exe"
                it.args = ["Set-Content ${file.absolutePath} -Stream \"Zone.Identifier\" -Value \"[ZoneTransfer]`nZoneId=3\""]
            }.throwsOnFailure()
        }
        return file
    }

    static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows")
    }

    static String wrapValueBasedOnType(Object rawValue, Class type, Closure<String> fallback = null) {
        wrapValueBasedOnType(rawValue, type.simpleName, fallback)
    }

    static String[] executedTasks(String gradleStdOut) {
        //example line: "> Task :sonarScannerInstall SKIPPED"
        def tasks = []
        def taskBaseStart = "> Task "
        gradleStdOut.readLines().each {
            if(it.startsWith(taskBaseStart)) {
                def taskName = it.replace(taskBaseStart, "").split(" ")[0].trim()
                tasks.add(taskName)
            }
        }
        return tasks
    }

    static boolean compareExecutionOrder(String stdOut, String baseTaskName, String otherTaskName, Closure<Boolean> cmpOp) {
        String[] tasks = executedTasks(stdOut)
        return cmpOp(tasks.findIndexOf {it == baseTaskName}, tasks.findIndexOf {it == otherTaskName})
    }

    static boolean wasExecutedBefore(String stdOut, String taskName, String otherTaskName) {
        return compareExecutionOrder(stdOut, taskName, otherTaskName) {
            taskIndex, otherTaskIndex -> taskIndex < otherTaskIndex
        }
    }

    static boolean wasExecutedAfter(String stdOut, String taskName, String otherTaskName) {
        return compareExecutionOrder(stdOut, taskName, otherTaskName) {
            taskIndex, otherTaskIndex -> taskIndex > otherTaskIndex
        }
    }

    static String wrapValueBasedOnType(Object rawValue, String type, Closure<String> fallback = null) {
        def value
        def rawValueEscaped = String.isInstance(rawValue) ? "'${rawValue}'" : rawValue
        def subtypeMatches = type =~ /(?<mainType>\w+)<(?<subType>[\w<>]+)>/
        def subType = (subtypeMatches.matches()) ? subtypeMatches.group("subType") : null
        type = (subtypeMatches.matches()) ? subtypeMatches.group("mainType") : type
        switch (type) {
            case "Closure":
                if (subType) {
                    value = "{${wrapValueBasedOnType(rawValue, subType, fallback)}}"
                } else {
                    value = "{$rawValueEscaped}"
                }
                break
            case "Callable":
                value = "new java.util.concurrent.Callable<${rawValue.class.typeName}>() {@Override ${rawValue.class.typeName} call() throws Exception { $rawValueEscaped }}"
                break
            case "Object":
                value = "new Object() {@Override String toString() { ${rawValueEscaped}.toString() }}"
                break
            case "Provider":
                switch (subType) {
                    case "RegularFile":
                        value = "project.layout.file(${wrapValueBasedOnType(rawValue, "Provider<File>", fallback)})"
                        break
                    case "Directory":
                        value = """
                                project.provider({
                                    def d = project.layout.directoryProperty()
                                    d.set(${wrapValueBasedOnType(rawValue, "File", fallback)})
                                    d.get()
                                })
                        """.trim().stripIndent()
                        break
                    default:
                        value = "project.provider(${wrapValueBasedOnType(rawValue, "Closure<${subType}>", fallback)})"
                        break
                }
                break
            case "String":
                value = "${escapedPath(rawValueEscaped.toString())}"
                break
            case "String[]":
                value = "'${rawValue.collect { it }.join(",")}'.split(',')"
                break
            case "File":
                value = "new File('${escapedPath(rawValue.toString())}')"
                break
            case "String...":
                value = "${rawValue.collect { '"' + it + '"' }.join(", ")}"
                break
            case "List":
                value = "[${rawValue.collect { '"' + it + '"' }.join(", ")}]"
                break
            case "Map":
                value = "[" + rawValue.collect { k, v -> "${wrapValueBasedOnType(k, k.getClass(), fallback)} : ${wrapValueBasedOnType(v, v.getClass(), fallback)}" }.join(", ") + "]"
                value = value == "[]" ? "[:]" : value
                break
            default:
                value = (fallback) ? fallback.call(type) : rawValue
        }
        value
    }


}
