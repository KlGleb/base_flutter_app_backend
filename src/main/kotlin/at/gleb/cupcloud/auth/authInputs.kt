package at.gleb.cupcloud.auth


data class LoginInput(val email: String, val password: String)
data class RegisterInput(val email: String, val password: String)
data class ResetPasswordInput(val code: String, val newPassword: String)