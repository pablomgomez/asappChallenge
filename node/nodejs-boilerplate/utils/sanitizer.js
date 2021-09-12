
const INVALID_SEQUENCES = ["'--" ]


/**
 * returns true if the parameter does not contain an special sequence that can cause a 
 * DB issue
 */
module.exports.checkStringSanity = (str) => {
    let presentInvalidChars = INVALID_SEQUENCES.map(sequence => {
        return (str.includes(sequence))
    }).filter( item => item == true)

    return presentInvalidChars.length == 0   
}



module.exports.checkPasswordRules = (password) => {
    
    let approvesLenght = (password.length >= 5) && (password.length <= 20)
    let approveOneUppercase  = /[A-Z]{1,}/.test(password)
    let approveOneLowercase = /[a-z]{1,}/.test(password)
    let approveOneNumber = /[0-9]{1,}/.test(password)
    let approveOneSpecial = /(?=.*[!@#$%^&*])/.test(password)

    let response = {
        pass: approvesLenght && approveOneUppercase && approveOneLowercase && approveOneNumber && approveOneSpecial,
        reason: ""
    }

    if (!approvesLenght) {
        response.reason = "Password length must be between 5 and 20 characters."
    }

    if (!approveOneUppercase ) {
        response.reason += "Password must contain at least one uppercase letter."
    }

    if(!approveOneLowercase) {
        response.reason += "Password must contain at least one lowercase letter." 
    }

    if (!approveOneNumber) {
        response.reason += "Password must contain at least one number."
    }
    
    if(!approveOneSpecial) {
        response.reason += " Password must contain at least one of the following special characters (!, @, #, $, %, ^, &, *)"
    }

    return response

}

module.exports.checkUsernameRules = (username) => {
    let approvesLenght = (username.length >= 5) && (username.length <= 20)

    let response = {
        pass: approvesLenght
    }

    if (!approvesLenght) {
        response.reason = "Username length must be between 5 and 20 characters."
    }

    return response

}