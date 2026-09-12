package com.necdetzr.onboarding

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.necdetzr.designsystem.icons.BleIcons
import com.necdetzr.onboarding.model.OnboardingContent
import com.necdetzr.onboarding.model.OnboardingVisual
import kotlinx.coroutines.launch

@Composable
internal fun OnboardingScreen(
    onFinishClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pages = rememberOnboardingPages()
    val pagerState = rememberPagerState(pageCount = pages::size)
    val coroutineScope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == pages.lastIndex

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(onboardingBackground())
            .safeDrawingPadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
        ) {
            OnboardingHeader(onSkipClick = onFinishClick)

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
            ) { page ->
                OnboardingPage(content = pages[page])
            }

            OnboardingFooter(
                currentPage = pagerState.currentPage,
                pageCount = pages.size,
                isLastPage = isLastPage,
                onClick = {
                    if (isLastPage) {
                        onFinishClick()
                    } else {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(
                                pagerState.currentPage + 1
                            )
                        }
                    }
                },
            )
        }
    }
}

@Composable
private fun OnboardingHeader(
    onSkipClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.feature_onboarding_app_name),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimary,
        )

        Spacer(Modifier.weight(1f))

        TextButton(onClick = onSkipClick) {
            Text(
               text = stringResource(R.string.feature_onboarding_skip),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
private fun OnboardingFooter(
    currentPage: Int,
    pageCount: Int,
    isLastPage: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        OnboardingPageIndicator(
            currentPage = currentPage,
            pageCount = pageCount,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )

        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp),
            shape = RoundedCornerShape(18.dp),
        ) {
                Text(
                    text = stringResource(
                        if (isLastPage) {
                            R.string.feature_onboarding_get_started
                        } else {
                            R.string.feature_onboarding_next
                        }
                    ),
                    style = MaterialTheme.typography.titleMedium
                )
        }
    }
}

@Composable
private fun OnboardingPageIndicator(
    currentPage: Int,
    pageCount: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        repeat(pageCount) { page ->
            val indicatorWidth by animateDpAsState(
                targetValue = if (page == currentPage) 28.dp else 8.dp,
                label = "pageIndicatorWidth",
            )

            Box(
                modifier = Modifier
                    .width(indicatorWidth)
                    .height(8.dp)
                    .background(
                        color = if (page == currentPage) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outlineVariant
                        },
                        shape = CircleShape,
                    ),
            )
        }
    }
}

@Composable
private fun onboardingBackground(): Brush {
    return Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.background,
        )
    )
}

@Composable
private fun rememberOnboardingPages(): List<OnboardingContent> {
    return remember {
        listOf(
            OnboardingContent(
                titleRes = R.string.feature_onboarding_scan_title,
                descriptionRes = R.string.feature_onboarding_scan_description,
                visual = OnboardingVisual.RADAR,
            ),
            OnboardingContent(
                titleRes = R.string.feature_onboarding_details_title,
                descriptionRes = R.string.feature_onboarding_details_description,
                visual = OnboardingVisual.DEVICE_DETAILS,
            ),
            OnboardingContent(
                titleRes = R.string.feature_onboarding_history_title,
                descriptionRes = R.string.feature_onboarding_history_description,
                visual = OnboardingVisual.HISTORY,
            ),
        )
    }
}
