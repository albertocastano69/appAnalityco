package co.tecno.sersoluciones.analityco.dependencyInjection

import dagger.Module
import dagger.Subcomponent
import javax.inject.Scope

// The "subcomponents" attribute in the @Module annotation tells Dagger what
// Subcomponents are children of the Component this module is included in.
@Module(subcomponents = [ChatListComponent::class])
class SubcomponentsModule {}

// Definition of a custom scope called ActivityScope
@Scope
@Retention(value = AnnotationRetention.RUNTIME)
annotation class ActivityScope

@ActivityScope
@Subcomponent
interface ChatListComponent {

    // Factory that is used to create instances of this subcomponent
    @Subcomponent.Factory
    interface Factory {
        fun create(): ChatListComponent
    }

//    fun inject(activity: ChatListActivity)

}