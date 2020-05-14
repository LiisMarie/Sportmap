package ee.taltech.likutt.iti0213_2019s_hw02.classes

class User(var email: String,
          var password: String,
          var firstName: String,
          var lastName: String) {

    override fun toString(): String {
        return "email: $email, password: $password, firstName: $firstName, " + "lastName: $lastName"
    }
}