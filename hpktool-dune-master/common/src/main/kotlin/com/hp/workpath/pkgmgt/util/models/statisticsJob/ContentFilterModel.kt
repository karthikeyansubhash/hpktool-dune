package com.hp.workpath.pkgmgt.util.models.statisticsJob

import com.hp.ext.types.base.ContentFilter
import com.hp.ext.types.base.ContentFilterString

class ContentFilterModel {
    private val filterStrings = mutableListOf<String>()

    init {
        filterStrings.addAll(DEFAULT_STATISTICS_FILTERS)
    }

    fun from(fromType: ContentFilter) {
        filterStrings.addAll(DEFAULT_STATISTICS_FILTERS)
    }

    fun to(): ContentFilter {
        val contentFilter = ContentFilter()
        filterStrings.forEach { filterStr ->
            contentFilter.add(ContentFilterString(filterStr))
        }
        return contentFilter
    }

    companion object {
        private val DEFAULT_STATISTICS_FILTERS = listOf(
            "(lastSequenceNumberProcessed, lastSequenceNumberNotified, missingSequenceNumbers)",
            "jobDetails/(sequenceNumber, jobId)"
        )
    }
}
