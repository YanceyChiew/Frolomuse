package com.frolo.muse.di.modules

import android.content.Context
import com.frolo.muse.ActivityWatcher
import com.frolo.muse.FrolomuseApp
import com.frolo.muse.billing.TrialManager
import com.frolo.muse.billing.TrialManagerImpl
import com.frolo.muse.di.ApplicationScope
import com.frolo.muse.di.ExecutorQualifier
import com.frolo.muse.di.impl.misc.MainExecutor
import com.frolo.muse.di.impl.network.NetworkHelperImpl
import com.frolo.muse.di.impl.permission.PermissionCheckerImpl
import com.frolo.muse.di.impl.rx.SchedulerProviderImpl
import com.frolo.muse.engine.journals.AndroidLogPlayerJournal
import com.frolo.muse.engine.journals.CompositePlayerJournal
import com.frolo.muse.engine.journals.StoredInMemoryPlayerJournal
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.logger.EventLoggerFactory
import com.frolo.muse.network.NetworkHelper
import com.frolo.muse.permission.PermissionChecker
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.player.PlayerJournal
import dagger.Module
import dagger.Provides
import java.util.concurrent.Executor
import java.util.concurrent.Executors


@Module
class MiscModule constructor(private val isDebug: Boolean) {

    @ApplicationScope
    @Provides
    fun provideSchedulers(): SchedulerProvider {
        return SchedulerProviderImpl()
    }

    @Provides
    @ApplicationScope
    fun provideEventLogger(context: Context): EventLogger {
        if (isDebug) {
            // We do not want to send any analytics for debug builds.
            // Debug builds are for developer, so Console logger is on.
            return EventLoggerFactory.createConsole()
        }

        // Analytics is tracked using Firebase (release builds only)
        return EventLoggerFactory.createFirebase(context)
    }

    @ApplicationScope
    @Provides
    @ExecutorQualifier(ExecutorQualifier.Type.MAIN)
    fun provideMainExecutor(): Executor {
        return MainExecutor()
    }

    @ApplicationScope
    @Provides
    @ExecutorQualifier(ExecutorQualifier.Type.QUERY)
    fun provideQueryExecutor(): Executor {
        return Executors.newCachedThreadPool()
    }

    @Provides
    fun provideActivityWatcher(app: FrolomuseApp): ActivityWatcher = app

    @ApplicationScope
    @Provides
    fun providePermissionChecker(context: Context, activityWatcher: ActivityWatcher): PermissionChecker {
        return PermissionCheckerImpl(context, activityWatcher)
    }

    @Provides
    @ApplicationScope
    fun provideTrialManager(context: Context): TrialManager {
        return TrialManagerImpl(context)
    }

    @ApplicationScope
    @Provides
    fun providePlayerJournal(): PlayerJournal {
        if (isDebug) {
            val journals = listOf(
                AndroidLogPlayerJournal("FrolomusePlayerJournal"),
                StoredInMemoryPlayerJournal()
            )
            return CompositePlayerJournal(journals)
        }

        return PlayerJournal.EMPTY
    }

    @ApplicationScope
    @Provides
    fun provideNetworkHelper(context: Context): NetworkHelper {
        return NetworkHelperImpl(context)
    }

}