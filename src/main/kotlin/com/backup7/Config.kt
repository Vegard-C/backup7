package com.backup7

import java.io.File

data class Config (
    val backupSource: File,
    val password: String,
    val program7zip: File,
    val backupLocations: List<File>,
)