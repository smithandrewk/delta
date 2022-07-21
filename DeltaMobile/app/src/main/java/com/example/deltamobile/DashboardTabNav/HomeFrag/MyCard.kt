package com.example.deltamobile.DashboardTabNav.HomeFrag

// names for values to be sent by intents
//
val intent__MYCARD_TITLE = "MYCARD_TITLE"
val intent__MYCARD_DESCRIPTION = "MYCARD_DESCRIPTION"
val intent__MYCARD_DATE = "MYCARD_DATE"
val intent__MYCARD_IMG_RES = "MYCARD_IMG_RES"

// names for actions for onclicks
//
val action__DETAIL_CARD = "DETAIL_CARD"
val action__DELETE_CARD = "DELETE_CARD"

data class MyCard(
    val imgResource:Int,
    val cardTitle:String,
    val cardDate:String,
    val cardDescription:String,
    )
