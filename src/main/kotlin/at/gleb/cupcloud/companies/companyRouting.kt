package at.gleb.cupcloud.companies

import at.gleb.cupcloud.auth.AUTH_JWT
import at.gleb.cupcloud.auth.user
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.java.KoinJavaComponent

private val interactor: CompanyInteractor by KoinJavaComponent.inject(CompanyInteractor::class.java)

fun Application.companyRouting() {
    routing {
        route("/companies") {
            authenticate(AUTH_JWT) {
                get {
                    call.respond(interactor.getCompanies(call.user()))
                }

                get("/{id}") {
                    val companyId = call.parameters["id"]!!
                    call.respond(interactor.getCompanyById(call.user(), companyId))
                }

                post<CompanyInput> {
                    call.respond(interactor.createCompany(call.user(), it))
                }

                put<CompanyInput>("/{id}") {
                    val companyId = call.parameters["id"]!!
                    call.respond(interactor.editCompany(call.user(), it, companyId))
                }

                delete("/{id}") {
                    val companyId = call.parameters["id"]!!
                    call.respond(interactor.archiveCompany(call.user(), companyId))
                }
            }
        }
    }

}

data class CompanyInput(
    val name: String
)