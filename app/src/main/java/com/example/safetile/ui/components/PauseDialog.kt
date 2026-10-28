package com.example.safetile.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

private enum class ConfirmAction {
    NONE,
    RESTART,
    EXIT
}

@Composable
fun PauseDialog(
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onExit: () -> Unit
) {
    var confirmState by remember { mutableStateOf(ConfirmAction.NONE) }

    when (confirmState) {
        ConfirmAction.NONE -> {
            Dialog(
                onDismissRequest = onResume,
                properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false)
            ) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "GAME PAUSED",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = onResume,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(text = "RESUME", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }

                        OutlinedButton(
                            onClick = { confirmState = ConfirmAction.RESTART },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(text = "RESTART", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }

                        TextButton(
                            onClick = { confirmState = ConfirmAction.EXIT },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                        ) {
                            Text(
                                text = "EXIT TO HOME",
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }

        ConfirmAction.RESTART -> {
            AlertDialog(
                onDismissRequest = { confirmState = ConfirmAction.NONE },
                title = { Text(text = "Restart this game?", fontWeight = FontWeight.Bold) },
                text = { Text("Current round progress and score will be lost.") },
                confirmButton = {
                    Button(
                        onClick = {
                            confirmState = ConfirmAction.NONE
                            onRestart()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("RESTART")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { confirmState = ConfirmAction.NONE }) {
                        Text("CANCEL")
                    }
                }
            )
        }

        ConfirmAction.EXIT -> {
            AlertDialog(
                onDismissRequest = { confirmState = ConfirmAction.NONE },
                title = { Text(text = "Exit game?", fontWeight = FontWeight.Bold) },
                text = { Text("Current game progress will be lost. Return to home?") },
                confirmButton = {
                    Button(
                        onClick = {
                            confirmState = ConfirmAction.NONE
                            onExit()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("EXIT")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { confirmState = ConfirmAction.NONE }) {
                        Text("CANCEL")
                    }
                }
            )
        }
    }
}
