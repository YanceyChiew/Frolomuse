package com.frolo.muse.interactor.media.get

import com.frolo.muse.model.Library
import com.frolo.muse.model.media.Genre
import com.frolo.muse.model.media.Song
import com.frolo.muse.repository.GenreChunkRepository
import com.frolo.muse.repository.Preferences
import com.frolo.muse.rx.SchedulerProvider
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.Flowable


class GetGenreSongsUseCase @AssistedInject constructor(
        schedulerProvider: SchedulerProvider,
        private val repository: GenreChunkRepository,
        preferences: Preferences,
        @Assisted private val genre: Genre
): GetSectionedMediaUseCase<Song>(Library.GENRE, schedulerProvider, repository, preferences) {

    override fun getSortedCollection(sortOrder: String): Flowable<List<Song>> {
        return repository.getSongsFromGenre(genre, sortOrder)
    }

    @AssistedInject.Factory
    interface Factory {
        fun create(genre: Genre): GetGenreSongsUseCase
    }

}