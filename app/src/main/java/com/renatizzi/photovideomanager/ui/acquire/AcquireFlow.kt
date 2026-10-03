package com.renatizzi.photovideomanager.ui.acquire

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.renatizzi.photovideomanager.R

enum class AcquireFlowStep {
    CENSUS,
    COPY,
}

@Composable
fun AcquireFlowHeader(
    step: AcquireFlowStep,
    onStep: (AcquireFlowStep) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = step == AcquireFlowStep.CENSUS,
                onClick = { onStep(AcquireFlowStep.CENSUS) },
                label = { Text(stringResource(R.string.acquire_step_census)) },
                modifier = Modifier.weight(1f),
            )
            FilterChip(
                selected = step == AcquireFlowStep.COPY,
                onClick = { onStep(AcquireFlowStep.COPY) },
                label = { Text(stringResource(R.string.acquire_step_copy)) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
fun AcquireFlowScaffold(
    step: AcquireFlowStep,
    onStep: (AcquireFlowStep) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier.fillMaxSize()) {
        AcquireFlowHeader(step = step, onStep = onStep)
        content()
    }
}
