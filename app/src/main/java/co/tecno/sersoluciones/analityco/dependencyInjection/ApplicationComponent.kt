package co.tecno.sersoluciones.analityco.dependencyInjection

import co.tecno.sersoluciones.analityco.*
import co.tecno.sersoluciones.analityco.createCar.CompanyFragmentCar
import co.tecno.sersoluciones.analityco.individualContract.*
import co.tecno.sersoluciones.analityco.fragments.InfoFragment
import co.tecno.sersoluciones.analityco.fragments.RegisterListFragment
import co.tecno.sersoluciones.analityco.fragments.SettingFragment
import co.tecno.sersoluciones.analityco.nav.CreatePersonalActivity
import co.tecno.sersoluciones.analityco.services.UpdateDBService
import co.tecno.sersoluciones.analityco.ui.createPersonal.*
import co.tecno.sersoluciones.analityco.worker.BackupWorker
import co.tecno.sersoluciones.analityco.worker.FetchPositionWorker
import co.tecno.sersoluciones.analityco.worker.SendMsgWorker
import co.tecno.sersoluciones.analityco.worker.SendReportWorker
import dagger.Component
import dagger.android.AndroidInjectionModule
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AndroidInjectionModule::class,
        AppModule::class,
        NetworkModule::class,
        SubcomponentsModule::class]
)
interface ApplicationComponent {

    fun inject(activity: EnrollmentActivity)
    fun inject(activity: CheckReqsPersonalActivity)
    fun inject(activity: CreatePersonalActivity)
    fun inject(activity: CreateCarActivity)
    fun inject(activity: CreateIndividualContractActivity)
    fun inject(activity: LoginActivity)
    fun inject(fragment: CompanyFragment)
    fun inject(fragment: CompanyFragmentCar)
    fun inject(fragment: JoinPersonalNavContractFragment)
    fun inject(fragment: SelectNavContractFragment)
    fun inject(fragment: ContractTypeFragment)
    fun inject(fragment: ContractTypeFragmentCar)
    fun inject(fragment: PlaceToJobFragment)
    fun inject(fragment: ProjectFragment)
    fun inject(fragment: ProjectFragmentCar)
    fun inject(fragment: SettingFragment)
    fun inject(fragment: InfoFragment)
    fun inject(fragment: ScanFragmnet)
    fun inject(fragment: RegisterListFragment)
    fun inject(fragment: ScanIndivualContractFragment)
    fun inject(fragment: PersonalFragment)
    fun inject(fragment: DetailsPersonalFragment)
    fun inject(fragment: DetailsPersonalIndivudalContractFragment)
    fun inject(fragment: IinitiateOrderFragment)
    fun inject(fragment: DataForEmployeeFragment)
    fun inject(fragment: RequestOrderFragment)
    fun inject(fragment: DataForContractFragment)
    fun inject(fragment: RequirementIndividualContractFragment)
    fun inject(fragment: DocumentForIndividualContractFragment)
    fun inject(fragment: SelectNavEmployerFragment)
    fun inject(fragment: RequerimentsFragment)
    fun inject(fragment: EmployerContract)
    fun inject(fragment: PersonalIndividualContractFragment)
    fun inject(activity: SurveyDetailsActivity)
    fun injectService(service: UpdateDBService)
//    fun inject(fragment: ChatListFragment)

    fun injectIntoWorker(worker: SendMsgWorker)
    fun injectIntoWorker(worker: SendReportWorker)
    fun injectIntoWorker(worker: FetchPositionWorker)
    fun injectIntoWorker(worker: BackupWorker)

    // This function exposes the ChatListComponent Factory out of the graph so consumers
    // can use it to obtain new instances of ChatListComponent
    fun chatListComponent(): ChatListComponent.Factory
}