package com.backup7

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
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
        title = "Compose for Desktop",
        state = rememberWindowState(width = 1000.dp, height = 700.dp),
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
        State.BACKUP_STARTED -> backupStartedWindow(state)
        State.HELP_INFO -> helpInfoWindow(state)
        else -> Text("Fail")
    }
}

@Composable
fun startWindow(state: MutableState<AppState>) {
    Column(
        modifier = Modifier.fillMaxSize().padding(defaultPadding),
        verticalArrangement = Arrangement.spacedBy(defaultPadding),
    ) {
        Text("Start an Update?")
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(defaultPadding)
        ) {
            Button(onClick = {state.value = AppState(state = State.BACKUP_STARTED) }) {
                Text("Yes!")
            }
            Button(onClick = { exitProcess(0) }) {
                Text("Not now")
            }
            Button(onClick = { state.value = AppState(state = State.HELP_INFO) }) {
                Text("Show help information")
            }
        }
    }
}

@Composable
fun backupStartedWindow(state: MutableState<AppState>) {
    Column(
        modifier = Modifier.fillMaxSize().padding(defaultPadding),
        verticalArrangement = Arrangement.spacedBy(defaultPadding),
    ) {
        Text("Searching the destination to store the backup... Please wait.")
        asyncCalculation (::findBackupDestination) {file ->
            state.value = if (file == null)
                AppState(state = State.BACKUP_DEST_NOT_FOUND)
            else
                AppState(state = State.BACKUP_DEST_FOUND, backupLocation = file)
        }
    }
}
@Composable
fun helpInfoWindow(state: MutableState<AppState>) {
    Column(
        modifier = Modifier.fillMaxSize().padding(defaultPadding),
        verticalArrangement = Arrangement.spacedBy(defaultPadding),
    ) {
        Text("Showing help infos...")
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(defaultPadding)
        ) {
            Button(onClick = {state.value = AppState() }) {
                Text("Restart?")
            }
            Button(onClick = { exitProcess(0) }) {
                Text("Terminate")
            }
        }
    }
}

fun findBackupDestination(): File? {
    Thread.sleep(5000L)
    return null
}

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

data class AppState (
    val state: State = State.START,
    val backupLocation: File? = null,
)

enum class State {
    START,
    BACKUP_STARTED,
    BACKUP_DEST_NOT_FOUND,
    BACKUP_DEST_FOUND,
    BACKUP_ZIP_RUNNING,
    BACKUP_COPY_RUNNING,
    BACKUP_DONE,
    HELP_INFO
}