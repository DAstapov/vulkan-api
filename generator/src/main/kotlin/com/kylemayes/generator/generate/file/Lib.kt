// SPDX-License-Identifier: Apache-2.0

package com.kylemayes.generator.generate.file

import com.kylemayes.generator.registry.Registry

fun Registry.generateLib() =
    """
#[macro_use]
mod bitfields;

pub mod vk;
pub mod wrapper;

/// The result of a executing a fallible Vulkan command.
pub type VkResult<T> = core::result::Result<T, vk::ErrorCode>;
/// The result of a executing a fallible Vulkan command with multiple success codes.
pub type VkSuccessResult<T> = core::result::Result<(T, vk::SuccessCode), vk::ErrorCode>;
    """
