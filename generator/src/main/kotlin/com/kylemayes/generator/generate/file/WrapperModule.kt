// SPDX-License-Identifier: Apache-2.0

package com.kylemayes.generator.generate.file

import com.kylemayes.generator.registry.Registry

fun Registry.generateWrapperModule() =
    """
mod builders;
mod command_structs;
mod extension_traits;
mod version_traits;

pub use builders::*;
pub use command_structs::*;
pub use extension_traits::*;
pub use version_traits::*;
    """
