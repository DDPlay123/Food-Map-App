package mai.project.foodmap.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import mai.project.foodmap.data.repositoryImpl.GeocodeRepoImpl
import mai.project.foodmap.data.repositoryImpl.PlaceImpl
import mai.project.foodmap.data.repositoryImpl.PreferenceRepoImpl
import mai.project.foodmap.data.repositoryImpl.UserRepoImpl
import mai.project.foodmap.domain.repository.GeocodeRepo
import mai.project.foodmap.domain.repository.PlaceRepo
import mai.project.foodmap.domain.repository.PreferenceRepo
import mai.project.foodmap.domain.repository.UserRepo
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class RepoModule {

    @Binds
    @Singleton
    abstract fun bindUserRepo(impl: UserRepoImpl): UserRepo

    @Binds
    @Singleton
    abstract fun bindPreferenceRepo(impl: PreferenceRepoImpl): PreferenceRepo

    @Binds
    @Singleton
    abstract fun bindPlaceRepo(impl: PlaceImpl): PlaceRepo

    @Binds
    @Singleton
    abstract fun bindGeocodeRepo(impl: GeocodeRepoImpl): GeocodeRepo
}