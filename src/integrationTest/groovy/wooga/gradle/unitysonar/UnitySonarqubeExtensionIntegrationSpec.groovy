package wooga.gradle.unitysonar

import com.wooga.gradle.test.IntegrationSpec
import com.wooga.gradle.test.PropertyLocation
import com.wooga.gradle.test.writers.PropertyGetterTaskWriter
import com.wooga.gradle.test.writers.PropertySetterWriter
import spock.lang.Unroll

import static com.wooga.gradle.test.writers.PropertySetInvocation.getAssignment
import static com.wooga.gradle.test.writers.PropertySetInvocation.getNone
import static com.wooga.gradle.test.writers.PropertySetInvocation.getProviderSet
import static com.wooga.gradle.test.writers.PropertySetInvocation.getSetter

class UnitySonarqubeExtensionIntegrationSpec extends IntegrationSpec {

    def setup() {
        buildFile << "${applyPlugin(UnitySonarqubePlugin)}\n"
    }

    @Unroll("can set property unitySonarqube.#property with #invocation and type #type")
    def "can set property on unitySonarqube extension with build.gradle"() {
        given:
        when:
        set.location = invocation == none ? PropertyLocation.none : set.location
        def propertyQuery = runPropertyQuery(get, set)

        then:
        propertyQuery.matches(rawValue)

        where:
        property                | invocation  | rawValue       | type
        "buildDotnetVersion"    | providerSet | "7.0.200"      | "Provider<String>"
        "buildDotnetVersion"    | assignment  | "7.0.200"      | "Provider<String>"
        "buildDotnetVersion"    | assignment  | "7.0.200"      | "String"
        "buildDotnetVersion"    | setter      | "7.0.200"      | "Provider<String>"
        "buildDotnetVersion"    | setter      | "7.0.200"      | "String"
        "buildDotnetVersion"    | none        | "7.0.100"      | "String"

        "buildDotnetExecutable" | providerSet | "dot_net"      | "Provider<String>"
        "buildDotnetExecutable" | assignment  | "dir/dot_net"  | "Provider<String>"
        "buildDotnetExecutable" | assignment  | "dot_net"      | "String"
        "buildDotnetExecutable" | setter      | "d/sd/dot_net" | "Provider<String>"
        "buildDotnetExecutable" | setter      | "dot_net"      | "String"
        "buildDotnetExecutable" | none        | null           | "String"

        set = new PropertySetterWriter("unitySonarqube", property)
                .set(rawValue, type)
                .toScript(invocation)
        get = new PropertyGetterTaskWriter(set)
    }
}
