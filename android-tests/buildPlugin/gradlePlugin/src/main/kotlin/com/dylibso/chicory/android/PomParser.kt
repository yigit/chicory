package com.dylibso.chicory.android

import org.apache.maven.model.Dependency
import org.apache.maven.model.Model
import org.apache.maven.model.io.xpp3.MavenXpp3Reader
import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.File
import java.io.InputStream
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

/**
 * Utility class to parse pom files from the host projects so that we can find
 * their dependencies to be added to the created flavor.
 */
object PomParser {
    fun parse(pomFile: File, parent: ParsedPom?): ParsedPom {
        return pomFile.inputStream().use {
            parse(it, parent)
        }
    }
    fun parse(fileContents: String, parent: ParsedPom?): ParsedPom {
        return fileContents.byteInputStream(Charsets.UTF_8).use {
            parse(it, parent)
        }
    }

    fun parse(inputStream: InputStream, parent: ParsedPom?): ParsedPom {
        val reader = MavenXpp3Reader()
        val model = reader.read(inputStream)
        return ParsedPom(parent = parent, model = model)
    }

//    private fun parse(fileStream: InputStream): ParsedPom {
//        val xPathFactory = XPathFactory.newInstance()
//        val documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
//        val doc = documentBuilder.parse(fileStream)
//
//        // Find all dependency elements
//        val properties = extractProperties(xPathFactory, doc)
//        val dependencies = extractDependencies(xPathFactory, doc).map { dependency ->
//            // swap versions with properties
//            val propertyReference = dependency.version?.extractPropertyReference()
//            if (propertyReference == null) {
//                dependency
//            } else {
//                val version = properties[propertyReference]
//                dependency.copy(
//                    version = version ?: error("Cannot find $propertyReference version")
//                )
//            }
//        }.toList()
//        return ParsedPom(properties, dependencies)
//    }
//
//    private fun extractProperties(xpathFactory: XPathFactory, doc: Document): Map<String, String> {
//        val versionExpression = "/*[local-name()='project']/*[local-name()='version']"
//
//        val node = xpathFactory.newXPath().evaluate(versionExpression, doc, XPathConstants.NODE) as org.w3c.dom.Node?
//
//        // Will return null if the version node doesn't exist
//        val version = node?.textContent
//        val expression = "/project/properties/*"
//        val nodes = xpathFactory.newXPath().evaluate(expression, doc, XPathConstants.NODESET) as NodeList
//        return buildMap {
//            if (version != null) {
//                put("project.version", version)
//            }
//            nodes.asSequence().forEach {
//                put(it.nodeName, it.textContent)
//            }
//        }
//    }
//
//    private fun extractDependencies(xPathFactory: XPathFactory, doc: Document?): Sequence<PomDependency> {
//        val xPath = xPathFactory.newXPath()
//        val expression = "//dependencies/dependency"
//        val nodes = xPath.evaluate(expression, doc, XPathConstants.NODESET) as NodeList
//
//        return nodes.asSequence().map { dependency ->
//            // Extract fields using XPath relative to each dependency node
//            PomDependency(
//                groupId = xPath.evaluate("groupId/text()", dependency) ?: error("missing group id"),
//                artifactId = xPath.evaluate("artifactId/text()", dependency)
//                    ?: error("missing artifact id"),
//                version = xPath.evaluate("version/text()", dependency)?.takeIf { it.isNotEmpty() },
//                scope = xPath.evaluate("scope/text()", dependency).takeIf { it.isNotEmpty() }
//            )
//        }
//    }
}
private fun String.extractPropertyReference(): String? {
    if (this.startsWith("\${") && this.endsWith("}")) {
        return substring(startIndex = 2, endIndex = length - 1)
    }
    return null
}
private fun NodeList.asSequence() = sequence<Node> {
    repeat(length) {
        yield(item(it))
    }
}

class ParsedPom(
    private val model: Model,
    private val parent: ParsedPom?,
) {
    val version: String?
        get() = model.version ?: parent?.version
    val groupId: String?
        get() = model.groupId ?: parent?.groupId
    val artifactId: String?
        get() = model.artifactId
    val dependencies by lazy {
        model.dependencies.map { dependency ->
            dependency.version = resolveProperties(dependency.version)
            dependency
        }
    }
    val properties by lazy {
        buildMap {
            model.properties.forEach { key, value ->
                put(key.toString(), value.toString())
            }
            model.version?.let {
                put("project.version", model.version!!)
            }
            model.groupId?.let {
                put("project.groupId", model.groupId!!)
            }
            model.artifactId?.let {
                put("project.artifactId", model.artifactId!!)
            }
        }
    }

    val dependencyManagementDependencies by lazy {
        model.dependencyManagement.dependencies.map { dependency ->
            dependency.version = resolveProperties(dependency.version)
            dependency
        }
    }
    private fun resolveProperties(value: String?): String {
        if (value == null) return ""
        if (!value.contains("$")) return value
        var result: String = value
        propertyPattern.findAll(value).forEach { matchResult ->
            val key = matchResult.groupValues[1]
            val propValue: String = properties[key] ?: parent?.properties?.get(key) ?: error(
                "Cannot find property: $key"
            )
            result = result.replace("\${$key}", propValue)
        }
        return result
    }

    companion object {
        val propertyPattern = """\$\{([^}]+)}""".toRegex()
    }
}

internal fun Dependency.toGradleNotation() = listOfNotNull(
    groupId,
    artifactId,
    version.takeIf { it.isNotBlank() }
).joinToString(":")