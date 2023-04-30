package com.backup7

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.compose.ui.Modifier
import java.io.File
import kotlin.system.exitProcess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

val defaultPadding = 16.dp

fun uiStartup() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Backup7",
        state = rememberWindowState(width = 800.dp, height = 600.dp),
        icon = painterResource("icon.png")
    ) {
        MaterialTheme {
            window()
        }
    }
}

@Composable
fun window() {
    val state = remember { mutableStateOf(AppState()) }
    when (state.value.state) {
        State.START -> startWindow(state)
        State.CONFIGURED -> configuredWindow(state)
        State.BACKUP_STARTED -> backupStartedWindow(state)
        State.BACKUP_DEST_FOUND -> backupDestFoundWindow(state)
        State.BACKUP_DEST_NOT_FOUND -> backupDestNotFoundWindow(state)
        State.BACKUP_ZIP_RUNNING -> backupZipRunningWindow(state)
        State.HELP_INFO -> helpInfoWindow(state)
        State.NOT_CONFIGURED -> notConfiguredWindow(state)
        State.BACKUP_DONE -> backupDoneWindow()
        State.FAIL -> failWindow(state)
    }
}

@Composable
fun failWindow(state: MutableState<AppState>) {
    BColumn {
        Text("Backup failed:")
        Output(state.value.failResult!!)
        BRow {
            Button(onClick = { state.value = AppState() }) {
                Text("Retry")
            }
            Button(onClick = { exitProcess(0) }) {
                Text("Terminate")
            }
            Button(onClick = { state.value = AppState(state = State.HELP_INFO) }) {
                Text("Show help information")
            }
        }
    }
}

@Composable
fun backupDoneWindow() {
    BColumn {
        Text("Your backup is successfully done")
        BRow {
            Button(onClick = { exitProcess(0) }) {
                Text("Terminate")
            }
        }
    }
}
@Composable
fun backupZipRunningWindow(state: MutableState<AppState>) {
    BColumn {
        Text("Backup from ${state.value.configResult!!.value().backupSource} to ${state.value.backupLocation!!.value()} starts.")
        Text("Files will be compressed and crypted with the password configured.")
        Text("This will take some time...")
        asyncCalculation(calcBody = {
            Service.run7zip(
                state.value.configResult!!.value(),
                state.value.backupLocation!!.value()
            )
        }) { runResult ->
            state.value = if (runResult.failed())
                AppState(state = State.FAIL, failResult = runResult.error())
            else
                state.value.copy(state =  State.BACKUP_DONE)
        }
    }
}

@Composable
fun backupDestFoundWindow(state: MutableState<AppState>) {
    BColumn {
        Text("Backup destination found: ${state.value.backupLocation!!.value()}")
        BRow {
            Button(onClick = { state.value = state.value.copy(state = State.BACKUP_ZIP_RUNNING) }) {
                Text("Start the backup")
            }
            Button(onClick = { exitProcess(0) }) {
                Text("Terminate")
            }
            Button(onClick = { state.value = AppState() }) {
                Text("Restart")
            }
        }
    }
}

@Composable
fun backupDestNotFoundWindow(state: MutableState<AppState>) {
    BColumn {
        Text("Backup cannot run: ${state.value.backupLocation!!.error()}")
        BRow {
            Button(onClick = { state.value = AppState() }) {
                Text("Retry")
            }
            Button(onClick = { exitProcess(0) }) {
                Text("Terminate")
            }
            Button(onClick = { state.value = AppState(state = State.HELP_INFO) }) {
                Text("Show help information")
            }
        }
    }
}

@Composable
fun configuredWindow(state: MutableState<AppState>) {
    BColumn {
        Text("Start an Update?")
        BRow {
            Button(onClick = { state.value = state.value.copy(state = State.BACKUP_STARTED) }) {
                Text("Yes!")
            }
            Button(onClick = { exitProcess(0) }) {
                Text("Not now, terminate")
            }
            Button(onClick = { state.value = AppState(state = State.HELP_INFO) }) {
                Text("Show help information")
            }
        }
    }
}

@Composable
fun notConfiguredWindow(state: MutableState<AppState>) {
    BColumn {
        Text("Program is not configured: ${state.value.configResult!!.error()}")
        BRow {
            Button(onClick = { state.value = AppState(state = State.HELP_INFO) }) {
                Text("Show help information")
            }
            Button(onClick = { exitProcess(0) }) {
                Text("Terminate")
            }
        }
    }
}

@Composable
fun startWindow(state: MutableState<AppState>) {
    BColumn {
        Text("Loading configuration...")
        asyncCalculation(Service::loadConfig) { configResult ->
            state.value = if (configResult.failed())
                AppState(state = State.NOT_CONFIGURED, configResult = configResult)
            else
                AppState(state = State.CONFIGURED, configResult = configResult)
        }
    }
}

@Composable
fun backupStartedWindow(state: MutableState<AppState>) {
    BColumn {
        Text("Searching the destinations to store the backup...")
        asyncCalculation(calcBody = {Service.findBackupLocation(state.value.configResult!!.value())}) { resultFile ->
            state.value = if (resultFile.failed())
                state.value.copy(state = State.BACKUP_DEST_NOT_FOUND, backupLocation = resultFile)
            else
                state.value.copy(state = State.BACKUP_DEST_FOUND, backupLocation = resultFile)
        }
    }
}

@Composable
fun helpInfoWindow(state: MutableState<AppState>) {
    BColumn {
        Text("This program will copy a password protected 7zip file to a backup location created from the contend of a source directory")
        val f = Service.configFile()
        Text("You must configurate this program by creating a file")
        Output(f.canonicalPath)
        Text("This file must contain configurations similar to this:")
        Output("password=JSa7az2y6qHG8XXvwac2\nsource=Z:\\backupsource\npath7zip=C:\\Program Files\\7-Zip\\7z.exe\nlocations=Z:\\backup1;Z:\\backup2")
        Text("Note that the password, source and locations for backup must not contain spaces or special chars")
        Text("Locations for backup may be multiple seperated by ';'")
        BRow {
            Button(onClick = { state.value = AppState() }) {
                Text("Restart?")
            }
            Button(onClick = { exitProcess(0) }) {
                Text("Terminate")
            }
        }
    }
}

@Composable
private fun Output(value: String) =
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        modifier = Modifier.fillMaxWidth()
    )

@Composable
private fun BColumn(content: @Composable ColumnScope.() -> Unit) =
    Column(
        modifier = Modifier.fillMaxSize().padding(defaultPadding),
        verticalArrangement = Arrangement.spacedBy(defaultPadding),
        content = content
    )

@Composable
private fun BRow(content: @Composable RowScope.() -> Unit) =
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(defaultPadding),
        content = content
    )

@Composable
fun <T> asyncCalculation(calcBody: () -> T, resultBody: (T) -> Unit) {
    val composeScope = rememberCoroutineScope()
    CoroutineScope(Dispatchers.IO).launch {
        val result = calcBody()
        composeScope.launch {
            resultBody(result)
        }
    }
}

data class AppState(
    val state: State = State.START,
    val configResult: Result<Config>? = null,
    val backupLocation: Result<File>? = null,
    val failResult: String? = null,
)

enum class State {
    START,
    CONFIGURED,
    BACKUP_STARTED,
    BACKUP_DEST_NOT_FOUND,
    BACKUP_DEST_FOUND,
    BACKUP_ZIP_RUNNING,
    BACKUP_DONE,
    FAIL,
    HELP_INFO,
    NOT_CONFIGURED,
}