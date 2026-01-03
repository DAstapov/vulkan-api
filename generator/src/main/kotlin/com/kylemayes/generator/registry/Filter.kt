package com.kylemayes.generator.registry

/** Filters out entities that are not supported by `vulkanalia`. */
fun Registry.filterRegistry(): Registry {
    val filteredVersions = versions.filterVersions()
    val vulkanCoreCommands = filteredVersions.values
        .flatMap { it.requireList }
        .flatMap { it.commands }
        .toSet()
    val vulkanCoreTypes = filteredVersions.values
        .flatMap { it.requireList }
        .flatMap { it.types.map { n -> n.intern() } }
        .toSet()
    val vulkanCoreValues = filteredVersions.values
        .flatMap { it.requireList }
        .flatMap { it.values.map { n -> n.name } }
        .toSet()
    val vulkanCoreIdentifiers = vulkanCoreCommands + vulkanCoreTypes + vulkanCoreValues

    val requiredExtensions =
        setOf(
            "VK_KHR_surface",
            "VK_KHR_swapchain",
            "VK_KHR_xcb_surface",
            "VK_KHR_xlib_surface",
            "VK_KHR_wayland_surface",
            "VK_KHR_unified_image_layouts",
            "VK_EXT_debug_utils",
            "VK_EXT_blend_operation_advanced",
            "VK_EXT_shader_object",
        )

    val filteredExtensions = extensions.filterExtension(requiredExtensions)
    val extensionsCommands = filteredExtensions.values
        .flatMap { it.requireList }
        .filter { it.depends.isEmpty() || requiredExtensions.contains(it.depends) }
        .flatMap { it.commands }
        .toSet()
    val extensionsTypes = filteredExtensions.values
        .flatMap { it.requireList }
        .flatMap { it.types.map { n -> n.intern() } }
        .toSet()
    val extensionsValues = filteredExtensions.values
        .flatMap { it.requireList }
        .flatMap { it.values.map { n -> n.name } }
        .toSet()
    val extensionsIdentifiers = extensionsCommands + extensionsTypes + extensionsValues

    val identifiers = vulkanCoreIdentifiers + extensionsIdentifiers

    return copy(
        aliases = aliases.filterEntities(identifiers),
        basetypes = basetypes.filterEntities(identifiers),
        bitmasks = bitmasks.filterEntities(identifiers).filterBitmasks(),
        constants = constants.filterEntities(identifiers),
        commands = commands.filterEntities(identifiers).filterCommands(),
        commandAliases = commandAliases.filterIdentifiers(identifiers),
        enums = enums.filterEntities(identifiers).filterEnums(),
        extensions = filteredExtensions,
        functions = functions.filterEntities(identifiers),
        handles = handles.filterEntities(identifiers),
        structs = structs.filterEntities(identifiers).filterStructures(),
        unions = unions.filterEntities(identifiers),
        versions = filteredVersions,
    )
}

private fun Map<Identifier, Version>.filterVersions() =
    filter { it.value.isVulkanApi() }

private fun Map<Identifier, Extension>.filterExtension(names: Set<String>) =
    filter { names.contains(it.value.name.value) }

private fun <T : Entity> Map<Identifier, T>.filterEntities(identifiers: Set<Identifier>) =
    filter { identifiers.contains(it.key) }

private fun Map<Identifier, Identifier>.filterIdentifiers(identifiers: Set<Identifier>) =
    filter { identifiers.contains(it.key) }

private fun Map<Identifier, Bitmask>.filterBitmasks() =
    filterChildren({ it.bitflags }, { e, c -> e.copy(bitflags = c.toMutableList()) })

private fun Map<Identifier, Command>.filterCommands() =
    filterChildren({ it.params }, { e, c -> e.copy(params = c) })

private fun Map<Identifier, Enum>.filterEnums() =
    filterChildren({ it.variants }, { e, c -> e.copy(variants = c.toMutableList()) })

private fun Map<Identifier, Structure>.filterStructures() =
    filterChildren({ it.members }, { e, c -> e.copy(members = c) })

private fun <T : Entity, C : Entity> Map<Identifier, T>.filterChildren(
    get: (T) -> List<C>,
    set: (T, List<C>) -> T,
) = mapValues {
    val children = get(it.value)
    set(it.value, children.filter { c -> c.isVulkanApi() })
}
