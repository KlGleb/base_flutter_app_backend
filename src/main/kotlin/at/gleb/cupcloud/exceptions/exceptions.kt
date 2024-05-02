package at.gleb.cupcloud.exceptions

import io.ktor.http.*

data class MyExceptionVo(
    val msg: String?,
    val code: String,
)

fun MyException.toVo() = MyExceptionVo(msg = message, code = errorCode)

open class MyException(
    val errorMessage: String,
    val errorCode: String,
    val httpStatusCode: HttpStatusCode = HttpStatusCode.InternalServerError
) : Exception()

object ErrorCodes {
    const val UNKNOWN = "unknown"
    const val WRONG_CREDENTIALS = "wrong_credentials"
    const val EMAIL_ALREADY_REGISTERED = "email_is_already_registered"
    const val USER_NOT_FOUND = "user_not_found"
    const val CODE_NOT_EXISTS = "code_not_exists"
    const val EMAIL_NOT_REGISTERED = "email_not_registered"
    const val NO_PERMISSION = "no_permission"
    const val WRONG_TOKEN = "wrong_token"
    const val WRONG_PASSWORD = "wrong_password"
    const val NO_ACCESS_TOKEN = "no_access_token"
    const val WRONG_CODE = "wrong_code"
    const val CODE_EXPIRED = "code_expired"
    const val TOO_MANY_EMAIL_SENT = "too_many_email_sent"
    const val TOO_MANY_TRIES = "too_many_tries"
    const val NOT_FOUND  = "not_found"
}


class UnknownException(msg: String? = null) : MyException(msg ?: "Unknown error occurred", ErrorCodes.UNKNOWN)


class WrongCredentialsException() :
    MyException("Wrong credentials", ErrorCodes.WRONG_CREDENTIALS, HttpStatusCode.Unauthorized)

class ValidationException(message: String, code: String) : MyException(message, code, HttpStatusCode.BadRequest)
class EmailIsAlreadyRegisteredException(message: String) :
    MyException(message, ErrorCodes.EMAIL_ALREADY_REGISTERED, HttpStatusCode.Unauthorized)

class UserNotFountException(message: String = "User not found") :
    MyException(message, ErrorCodes.USER_NOT_FOUND, HttpStatusCode.NotFound)

class CodeNotExistException : MyException("The code does not exist or has expired", ErrorCodes.CODE_NOT_EXISTS)
class EmailNotRegistered : MyException("User with this email is not registered ", ErrorCodes.EMAIL_NOT_REGISTERED)

class NoPermission :
    MyException("You don't have an access to modify this object", ErrorCodes.NO_PERMISSION, HttpStatusCode.Forbidden)

//HttpStatusCode.Unauthorized
class TokenNotExist :
    MyException("Access token has expired or don't exist", ErrorCodes.WRONG_TOKEN, HttpStatusCode.Unauthorized)

class IncorrectPassword : MyException("Incorrect password", ErrorCodes.WRONG_PASSWORD, HttpStatusCode.Unauthorized)
class Unauthorised :
    MyException("You must provide 'accessToken' header", ErrorCodes.NO_ACCESS_TOKEN, HttpStatusCode.Unauthorized)


class TooManyEmailsSent : MyException(
    "You have sent too many emails today",
    ErrorCodes.TOO_MANY_EMAIL_SENT,
    HttpStatusCode.Unauthorized
)


class WrongCode : MyException(
    "Wrong code",
    ErrorCodes.WRONG_CODE,
    HttpStatusCode.BadRequest
)

class CodeExpired : MyException(
    "Code expired",
    ErrorCodes.CODE_EXPIRED,
    HttpStatusCode.Unauthorized
)

class TooManyTries : MyException(
    "Too many tries",
    ErrorCodes.TOO_MANY_TRIES,
    HttpStatusCode.Unauthorized
)

class NotFound : MyException(
    "Object not found",
    ErrorCodes.NOT_FOUND,
    HttpStatusCode.NotFound
)



