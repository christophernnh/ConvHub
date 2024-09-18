package edu.bluejack23_2.convhub.data.model

import java.util.Date

data class User(
    val dob: Date = Date(),
    val username: String = "",
    val id: String = "",
    val email: String = "",
    val picture: String = "",
    val jobs: List<String> = listOf(),
    val preferredFields : List<String> = listOf(),
) {
    // No-argument constructor for Firestore
    constructor() : this(Date(), "", "", "", "", listOf(), listOf())
}
