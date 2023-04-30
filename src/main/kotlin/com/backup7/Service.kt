package com.backup7

import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object Service {
    fun configFile(): File = File(File(System.getProperty("user.home")), "backup7.configuration")
    fun loadConfig(): Result<Config> {
        try {
            val configFile = configFile()
            if (configFile.exists()) {
                val lines = configFile.readLines()
                val keyMap = lines.mapNotNull { line ->
                    if (line.isEmpty()) null
                    else {
                        val assignPos = line.indexOf('=')
                        if (assignPos <= 1) return Result("Wrong line in configuration file $configFile: '$line'")
                        val key = line.substring(0, assignPos)
                        val value = line.substring(assignPos + 1)
                        key to value
                    }
                }.toMap()
                val backupSource = File(
                    keyMap.get("source")
                        ?: return Result("No configuration for source found in configuration file $configFile")
                )
                if (!backupSource.exists() && !backupSource.isDirectory) return Result("Wrong source in configuration file $configFile. File $backupSource is not a directory")
                val password = keyMap.get("password")
                    ?: return Result("No configuration for password found in configuration file $configFile")
                val exe7zip = File(
                    keyMap.get("path7zip")
                        ?: return Result("No configuration for path7zip found in configuration file $configFile")
                )
                if (!exe7zip.exists()) return Result("Wrong path7zip in configuration file $configFile. File $exe7zip doesn't exist")
                val locations = keyMap.get("locations")
                    ?: return Result("No configuration for locations found in configuration file $configFile")
                return Result(
                    Config(
                        backupSource = backupSource,
                        password = password,
                        program7zip = exe7zip,
                        backupLocations = locations.split(';').map { File(it) }
                    )
                )
            } else {
                return Result("No Configuration file found: $configFile")
            }
        } catch (e: Exception) {
            return Result(e.toString())
        }
    }

    fun findBackupLocation(config: Config): Result<File> {
        return try {
            val f = config.backupLocations.firstOrNull { it.exists() && it.isDirectory }
            if (f == null) Result("No backup location found (configured: ${config.backupLocations})")
            else Result(f)
        } catch (e: Exception) {
            Result(e.toString())
        }
    }

    fun run7zip(config: Config, destination: File): Result<File> {
        return try {
            val sourceDir = config.backupSource.canonicalPath
            val now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))
            val dest = File(destination, "backup-$now.7z")
            if (dest.exists()) throw IllegalStateException("Destination-File $dest already exists")
            val cmd = listOf(
                """"${config.program7zip.canonicalPath}"""",
                "a",
                dest.canonicalPath,
                "-p${config.password}",
                "-mhe=on",
                sourceDir
            )
            val rc =ProcessBuilder(*cmd.toTypedArray())
                .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                .redirectError(ProcessBuilder.Redirect.DISCARD)
                .start()
                .waitFor()
            if (rc != 0) return Result("Failed creating the backup with RC=$rc. Try running the Command '${cmd.joinToString(" ")}' by hand.")
            else Result(dest)
        } catch (e: Exception) {
            Result(e.toString())
        }
    }
}