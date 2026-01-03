// SPDX-License-Identifier: Apache-2.0

package com.kylemayes.generator

import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import org.apache.commons.codec.digest.DigestUtils
import org.kohsuke.github.GHCommit
import org.kohsuke.github.GHContent
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createParentDirectories

private val log = KotlinLogging.logger { /* */ }

private val mapper = ObjectMapper()

private val registryPath = RepositoryPath("KhronosGroup/Vulkan-Docs", "main", "xml/vk.xml")

/** The generator inputs pulled from GitHub repositories. */
data class RepositoryInputs(
    /** The Vulkan API registry. */
    val registry: RepositoryInput<String>,
) {
    val list = listOf(registry)
}

/** Gets the generator inputs pulled from GitHub repositories. */
fun getRepositoryInputs(context: GeneratorContext): RepositoryInputs {
    return RepositoryInputs(
        registry = getRepositoryInput(context, registryPath, ::getFile),
    )
}

// ===============================================
// Input
// ===============================================

/** A reference to a file or directory in a GitHub repository. */
data class RepositoryPath(
    val name: String,
    val branch: String,
    val path: String,
)

/** A generator input pulled from a GitHub repository. */
data class RepositoryInput<out T>(
    val path: RepositoryPath,
    val local: RepositoryInputVersion<T>,
    val latest: RepositoryInputVersion<T>,
) {
    val stale: Boolean get() = local.commit.shA1 != latest.commit.shA1
}

/** A version of a generator input pulled from a GitHub repository. */
data class RepositoryInputVersion<out T>(
    val commit: GHCommit,
    val lazy: Lazy<T>,
)

/** Gets a generator input pulled from a GitHub repository. */
private inline fun <reified T> getRepositoryInput(
    context: GeneratorContext,
    path: RepositoryPath,
    crossinline get: (commit: GHCommit, path: RepositoryPath) -> T,
): RepositoryInput<T> {
    val repository = context.github.getRepository(path.name)
    val latest = repository.queryCommits().from(path.branch).path(path.path).pageSize(1).list().first()
    return RepositoryInput(
        path = path,
        local = RepositoryInputVersion(latest, lazy { getCached(latest, path, get) }),
        latest = RepositoryInputVersion(latest, lazy { getCached(latest, path, get) }),
    )
}

private inline fun <reified T> getCached(
    commit: GHCommit,
    path: RepositoryPath,
    get: (commit: GHCommit, path: RepositoryPath) -> T,
): T {
    val key = DigestUtils.sha1Hex("${commit.shA1}-$path")
    val file = Path.of(System.getProperty("java.io.tmpdir")).resolve("vk-input").resolve("$key.json")

    try {
        if (Files.exists(file)) {
            log.info("Using cached value for $path.")
            val json = Files.readString(file, StandardCharsets.UTF_8)
            return mapper.readValue(json, T::class.java)
        }
    } catch (e: Exception) {
        log.warn("Failed to load and parse cached value for $path.", e)
    }

    val value = get(commit, path)

    file.createParentDirectories()
    Files.writeString(file, mapper.writeValueAsString(value), StandardCharsets.UTF_8)

    return value
}

/** Fetches the contents of a file for a generator input pulled from a GitHub repository. */
private fun getFile(
    commit: GHCommit,
    path: RepositoryPath,
): String = commit.owner.getFileContent(path.path, commit.shA1).readToString(commit)

/** Fetches the content of a file from a GitHub repository. */
private fun GHContent.readToString(commit: GHCommit): String {
    return if (size <= 1024 * 1024) {
        read().readAllBytes().decodeToString()
    } else {
        commit.owner.getBlob(sha).read().readAllBytes().decodeToString()
    }
}
