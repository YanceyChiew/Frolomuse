package com.frolo.muse.ui.main.editor.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.frolo.muse.di.AppComponent
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.model.media.Playlist
import com.frolo.muse.repository.PlaylistRepository
import com.frolo.muse.rx.SchedulerProvider
import javax.inject.Inject


class PlaylistEditorVMFactory constructor(
        appComponent: AppComponent,
        private val playlist: Playlist
): ViewModelProvider.Factory {

    @Inject
    internal lateinit var schedulerProvider: SchedulerProvider
    @Inject
    internal lateinit var repository: PlaylistRepository
    @Inject
    internal lateinit var eventLogger: EventLogger

    init {
        appComponent.inject(this)
    }

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return PlaylistEditorViewModel(
                schedulerProvider,
                repository,
                eventLogger,
                playlist
        ) as T
    }

}