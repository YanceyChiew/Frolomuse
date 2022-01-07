package com.frolo.muse.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frolo.muse.arch.EventLiveData
import com.frolo.muse.arch.SingleLiveEvent
import com.frolo.muse.arch.call
import com.frolo.player.Player
import com.frolo.muse.interactor.billing.PremiumManager
import com.frolo.muse.interactor.feature.FeaturesUseCase
import com.frolo.muse.interactor.firebase.SyncFirebaseMessagingTokenUseCase
import com.frolo.muse.interactor.media.TransferPlaylistsUseCase
import com.frolo.muse.interactor.media.shortcut.NavigateToMediaUseCase
import com.frolo.muse.interactor.player.OpenAudioSourceUseCase
import com.frolo.muse.interactor.player.RestorePlayerStateUseCase
import com.frolo.muse.interactor.rate.RateUseCase
import com.frolo.muse.logger.*
import com.frolo.music.model.Media
import com.frolo.muse.permission.PermissionChecker
import com.frolo.muse.repository.AppearancePreferences
import com.frolo.muse.repository.RemoteConfigRepository
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.base.BaseViewModel
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import javax.inject.Inject


/**
 * The main view model associated to all screens in the app.
 *
 * The main purpose of this view model is:
 * 1) aks to rate the app;
 * 2) check the RES permission;
 * 3) handle the player connections.
 *
 * P.S. RES stands for Read-External-Storage.
 */
class MainViewModel @Inject constructor(
    private val rateUseCase: RateUseCase,
    private val restorePlayerStateUseCase: RestorePlayerStateUseCase,
    private val openAudioSourceUseCase: OpenAudioSourceUseCase,
    private val navigateToMediaUseCase: NavigateToMediaUseCase,
    private val syncFirebaseMessagingTokenUseCase: SyncFirebaseMessagingTokenUseCase,
    private val transferPlaylistsUseCase: TransferPlaylistsUseCase,
    private val featuresUseCase: FeaturesUseCase,
    private val premiumManager: PremiumManager,
    private val schedulerProvider: SchedulerProvider,
    private val permissionChecker: PermissionChecker,
    private val appearancePreferences: AppearancePreferences,
    private val remoteConfigRepository: RemoteConfigRepository,
    private val eventLogger: EventLogger
): BaseViewModel(eventLogger) {

    // Internal
    @Volatile
    private var _player: Player? = null

    @Volatile
    private var _pendingReadStoragePermissionResult: Boolean = false

    private var _pendingAudioSourceIntent: String? = null

    private var askToRateDisposable: Disposable? = null

    private val _askRESPermissionsEvent = EventLiveData<Unit>()
    val askRESPermissionsEvent: LiveData<Unit> get() = _askRESPermissionsEvent

    private val _explainNeedForRESPermissionEvent = EventLiveData<Unit>()
    val explainNeedForRESPermissionEvent: LiveData<Unit> get() = _explainNeedForRESPermissionEvent

    private val _openPermissionSettingsEvent = EventLiveData<Unit>()
    val openPermissionSettingsEvent: LiveData<Unit> get() = _openPermissionSettingsEvent

    private val _askToRateEvent = SingleLiveEvent<Unit>()
    val askToRateEvent: LiveData<Unit> get() = _askToRateEvent

    val isSnowfallEnabled: LiveData<Boolean> by lazy {
        MutableLiveData<Boolean>().apply {
            val source1 = remoteConfigRepository.isSnowfallFeatureEnabled().toFlowable()
            val source2 = appearancePreferences.isSnowfallEnabled()
            val combined = Flowable.combineLatest(source1, source2) { isFeatureEnabled, isLocallyEnabled ->
                isFeatureEnabled && isLocallyEnabled
            }
            combined.distinctUntilChanged()
                .observeOn(schedulerProvider.main())
                .subscribeFor { isEnabled ->
                    value = isEnabled
                }
        }
    }

    private fun tryRestorePlayerStateIfNeeded() {
        val player: Player = _player ?: return

        if (permissionChecker.isQueryMediaContentPermissionGranted) {
            restorePlayerStateUseCase
                .restorePlayerStateIfNeeded(player)
                .observeOn(schedulerProvider.main())
                .subscribe(
                    { /* stub */ },
                    { err ->
                        if (err is SecurityException) {
                            tryAskRESPermission()
                        }
                    }
                )
                .save()
        } else {
            tryAskRESPermission()
        }
    }

    private fun tryHandlePendingAudioSourceIntentIfNeeded() {
        val source: String = _pendingAudioSourceIntent ?: return
        val player: Player = _player ?: return

        if (permissionChecker.isQueryMediaContentPermissionGranted) {
            openAudioSourceUseCase.openAudioSource(player, source)
                .observeOn(schedulerProvider.main())
                .subscribeFor { _pendingAudioSourceIntent = null }
        } else {
            tryAskRESPermission()
        }
    }

    private fun tryTransferPlaylistsIfNecessary() {
        transferPlaylistsUseCase.transferPlaylistsIfNecessary()
            .observeOn(schedulerProvider.main())
            .subscribeFor {  }
    }

    /**
     * The view model must call this method to ask the RES permission.
     * It also checks if it's still pending for the result of RES permission asked earlier.
     */
    private fun tryAskRESPermission() {
        if (!_pendingReadStoragePermissionResult) {
            _askRESPermissionsEvent.call()
            _pendingReadStoragePermissionResult = true
        }
    }

    fun onFirstCreate() {
        // Syncing Firebase CM
        syncFirebaseMessagingTokenUseCase.sync()
            .observeOn(schedulerProvider.main())
            .subscribeFor {  }
        // Syncing features
        featuresUseCase.sync()
            .observeOn(schedulerProvider.main())
            .subscribeFor {  }
        // Syncing premium state
        premiumManager.sync()
            .observeOn(schedulerProvider.main())
            .subscribeFor {  }
        // Transfer playlists if necessary
        if (permissionChecker.isQueryMediaContentPermissionGranted) {
            tryTransferPlaylistsIfNecessary()
        }
    }

    fun onStart() {
        if (permissionChecker.isQueryMediaContentPermissionGranted) {
            // TODO: restore the player state, if needed
        } else {
            tryAskRESPermission()
        }
    }

    fun onResume() {
        rateUseCase
            .checkIfRateNeeded()
            .observeOn(schedulerProvider.main())
            .doOnSubscribe { d ->
                askToRateDisposable?.dispose()
                askToRateDisposable = d
            }
            .subscribeFor { needRate ->
                if (needRate) {
                    _askToRateEvent.call()
                }
            }
    }

    fun onPause() {
        askToRateDisposable?.dispose()
    }

    fun onStop() {
        // no actions
    }

    //region Rate dialog

    fun onRateDialogAnswerYes() {
        rateUseCase.rate()
        eventLogger.logRateDialogAnswered(RATE_DIALOG_ANSWER_YES)
    }

    fun onRateDialogAnswerNo() {
        rateUseCase.dismissRate()
        eventLogger.logRateDialogAnswered(RATE_DIALOG_ANSWER_NO)
    }

    fun onRateDialogAnswerRemindLater() {
        rateUseCase.askLater()
        eventLogger.logRateDialogAnswered(RATE_DIALOG_ANSWER_REMIND_LATER)
    }

    fun onCancelledRateDialog() {
        rateUseCase.cancelRate()
        eventLogger.logRateDialogCancelled()
    }

    //endregion


    //region Player

    fun onPlayerConnected(player: Player) {
        _player = player
        tryRestorePlayerStateIfNeeded()
    }

    fun onPlayerDisconnected() {
        _player = null
    }

    //endregion


    //region Read Storage Permission

    fun onRESPermissionGranted() {
        _pendingReadStoragePermissionResult = false
        tryRestorePlayerStateIfNeeded()
        tryHandlePendingAudioSourceIntentIfNeeded()
        tryTransferPlaylistsIfNecessary()
    }

    fun onRESPermissionDenied() {
        _pendingReadStoragePermissionResult = false
        _explainNeedForRESPermissionEvent.call()
    }

    fun onAgreedWithRESPermissionExplanation() {
        if (permissionChecker.shouldRequestMediaPermissionInSettings()) {
            _openPermissionSettingsEvent.call()
        } else {
            tryAskRESPermission()
        }
    }

    fun onDeniedRESPermissionExplanation() {
        // TODO: do we need to respect this choice and don't ask the RES permission while this view model is alive?
    }

    //endregion

    fun onNavigateToMediaIntent(@Media.Kind kindOfMedia: Int, mediaId: Long) {
        navigateToMediaUseCase.navigate(kindOfMedia, mediaId)
            .observeOn(schedulerProvider.main())
            .subscribeFor {  }
    }

    fun onOpenAudioSourceIntent(source: String) {
        _pendingAudioSourceIntent = source
        tryHandlePendingAudioSourceIntentIfNeeded()
    }

    override fun onCleared() {
        super.onCleared()
        askToRateDisposable?.dispose()
    }
}