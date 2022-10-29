package com.side.project.foodmap.data.local

class User(
    val name: String,
    val imagePath:String = ""
) {
    constructor(): this("","")
}