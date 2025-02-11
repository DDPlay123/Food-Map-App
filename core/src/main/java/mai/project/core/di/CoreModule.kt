package mai.project.core.di

import android.content.Context
import android.location.LocationManager
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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

    @Provides
    @Singleton
    fun provideFusedLocationProviderClient(
        @ApplicationContext context: Context
    ) = LocationServices.getFusedLocationProviderClient(context)

    @Provides
    @Singleton
    fun provideLocationManager(
        @ApplicationContext context: Context
    ) = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
}