package mai.project.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import mai.project.core.utils.CoroutineContextProvider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object CoreModule {

    @Provides
    @Singleton
    fun provideCoroutineContextProvider(): CoroutineContextProvider = object : CoroutineContextProvider {
        override val io = Dispatchers.IO
        override val main: CoroutineDispatcher = Dispatchers.Main
        override val default = Dispatchers.Default
    }
}