package com.necdetzr.onboarding

import com.necdetzr.onboarding.model.OnboardingVisual

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.necdetzr.onboarding.components.OnboardingDeviceIllustration
import com.necdetzr.onboarding.components.OnboardingHistoryIllustration
import com.necdetzr.onboarding.components.OnboardingRadarIllustration
import com.necdetzr.onboarding.model.OnboardingContent


@Composable
internal fun OnboardingPage(
    content: OnboardingContent,
) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
    ) {
        if (maxWidth > maxHeight) {
            OnboardingWidePage(content = content)
        } else {
            OnboardingCompactPage(content = content)
        }
    }
}
@Composable
private fun OnboardingVisualContent(
    visual: OnboardingVisual,
    modifier: Modifier = Modifier,
) {
    when (visual) {
        OnboardingVisual.RADAR -> {
            OnboardingRadarIllustration(modifier)
        }

        OnboardingVisual.DEVICE_DETAILS -> {
            OnboardingDeviceIllustration(modifier)
        }

        OnboardingVisual.HISTORY -> {
            OnboardingHistoryIllustration(modifier)
        }
    }
}
@Composable
private fun OnboardingCompactPage(
    content: OnboardingContent,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        OnboardingVisualContent(
            visual = content.visual,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        )

        OnboardingCopy(content = content)
    }
}

@Composable
private fun OnboardingWidePage(
    content: OnboardingContent,
) {
    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(32.dp),
    ) {
        OnboardingVisualContent(
            visual = content.visual,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        )

        OnboardingCopy(
            content = content,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun OnboardingCopy(
    content: OnboardingContent,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(content.titleRes),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )

        Text(
            text = stringResource(content.descriptionRes),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
