package com.migrely.app.attack.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.migrely.app.attack.ui.R
import com.migrely.app.attack.ui.mapper.AttackTimeUiFormatter
import com.migrely.app.attack.ui.model.AttackDetailsUiModel
import com.migrely.app.designsystem.icon.MigrelyIcons

/**
 * The medications taken during the attack, oldest first, each removable with undo, and "Add medication", which opens
 * "Medication taken" for this attack (BRD ATK-F5, D34). Archived medications keep their name (EC-MED-01).
 */
@Composable
internal fun MedicationsSection(
    details: AttackDetailsUiModel,
    timeUiFormatter: AttackTimeUiFormatter,
    actions: AttackDetailsActions
) {
    Column {
        SectionTitle(stringResource(R.string.section_medications))
        if (details.intakes.isEmpty()) {
            Text(
                text = stringResource(R.string.medication_none),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
        details.intakes.forEach { intake ->
            val what = listOfNotNull(intake.medicationName, intake.dose).joinToString(" ")
            val time = timeUiFormatter.formatWithinAttack(
                intake.takenAtEpochMillis,
                details.startEpochMillis,
                intake.zoneId
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
            ) {
                Text(
                    text = stringResource(R.string.medication_row, what, time),
                    style = MaterialTheme.typography.bodyLarge.copy(fontFeatureSettings = "tnum"),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp)
                )
                IconButton(onClick = { actions.onRemoveIntake(intake) }) {
                    Icon(
                        MigrelyIcons.Close,
                        contentDescription = stringResource(R.string.medication_remove, what)
                    )
                }
            }
        }
        OutlinedButton(
            onClick = actions.onAddMedication,
            modifier = Modifier
                .padding(top = 8.dp)
                .heightIn(min = 48.dp)
        ) {
            Icon(MigrelyIcons.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Text(
                text = stringResource(R.string.medication_add),
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}
