// SPDX-License-Identifier: Apache-2.0

package com.kylemayes.generator

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.findOrSetObject
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.kylemayes.generator.generate.generateRustFiles
import com.kylemayes.generator.registry.parseRegistry
import com.kylemayes.generator.support.time
import mu.KotlinLogging
import org.kohsuke.github.GitHub
import java.io.File
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.system.exitProcess

private val log = KotlinLogging.logger { /* */ }

fun main(args: Array<String>) =
    Generator()
        .subcommands(Update())
        .main(args)

data class GeneratorContext(
    val directory: Path,
    val github: GitHub,
    val token: String?,
)

class Generator : CliktCommand(help = "Manages generated Vulkan bindings") {
    private val directory by option(help = "Vulkanalia directory").required()
    private val token by option(help = "GitHub personal access token")
    private val tokenFile by option(help = "GitHub personal access token file")

    private val context by findOrSetObject {
        val directory = Path.of(directory).toAbsolutePath().normalize()

        val token =
            if (this.token != null) {
                this.token
            } else if (tokenFile != null) {
                File(tokenFile!!).readText().trim()
            } else {
                null
            }

        if (token != null) {
            GeneratorContext(directory, GitHub.connectUsingOAuth(token), token)
        } else {
            GeneratorContext(directory, GitHub.connectAnonymously(), null)
        }
    }

    override fun run() {
        log.info { "Working in $directory" }
        if (context.github.isAnonymous) {
            log.info { "Acting as an anonymous GitHub user" }
        } else {
            log.info { "Acting with GitHub OAuth token" }
        }
    }
}

class Update : CliktCommand(help = "Updates generated Vulkan bindings") {
    private val context by requireObject<GeneratorContext>()

    override fun run() {
        val inputs = getRepositoryInputs(context)

        // Parse

        val xmlVersion = inputs.registry.latest
        val xml = log.time("Fetch Registry") { xmlVersion.lazy.value }
        val registry = log.time("Parse Registry") { parseRegistry(xml) }

        // Generate

        val files = log.time("Generate Files") { generateRustFiles(registry) }

        // Format

        val format = log.time("Format Files") { files.all { it.format() } }

        // Write (files)

        log.time("Write Files") {
            context.directory.createDirectories()
            files.forEach { context.directory.resolve(it.path.parent).createDirectories() }
            files.forEach { it.write(context.directory) }
        }

        if (!format) {
            log.error { "One or more files could not be formatted." }
            exitProcess(1)
        }
    }
}
