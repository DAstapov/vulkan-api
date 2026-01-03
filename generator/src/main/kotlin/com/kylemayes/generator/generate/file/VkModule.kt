// SPDX-License-Identifier: Apache-2.0

package com.kylemayes.generator.generate.file

import com.kylemayes.generator.registry.Registry

fun Registry.generateVkModule() =
    """
mod bitmasks;
mod commands;
mod constants;
mod enums;
mod extensions;
mod functions;
mod handles;
mod macros;
mod result_enums;
mod structs;
mod typedefs;
mod unions;

pub use bitmasks::*;
pub use commands::*;
pub use constants::*;
pub use enums::*;
pub use extensions::*;
pub use functions::*;
pub use handles::*;
pub use macros::*;
pub use result_enums::*;
pub use structs::*;
pub use typedefs::*;
pub use unions::*;
    """
