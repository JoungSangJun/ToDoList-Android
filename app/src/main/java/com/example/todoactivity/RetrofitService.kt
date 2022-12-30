package com.example.todoactivity

import retrofit2.Call
import retrofit2.http.*

class User(
    val username: String, val token: String, val id: Int
)

class ToDo(
    val id: Int,
    val content: String,
    val is_complete: Boolean,
    val created: String
)

interface RetrofitService {

    @GET("to-do/search/")
    fun searchTodoList(
        @HeaderMap headers: Map<String, String>,
        @Query("keyword") keyword: String
    ): Call<ArrayList<ToDo>>

    @PUT("to-do/complete/{todoId}")
    fun changeToDoComplete(
        @HeaderMap headers: Map<String, String>,
        @Path("todoId") todoId: Int
    ): Call<Any>

    @GET("to-do/")
    fun getToDoList(
        @HeaderMap headers: Map<String, String>
    ): Call<ArrayList<ToDo>>

    @POST("user/login/")
    @FormUrlEncoded
    fun instaLogin(
        @FieldMap params: HashMap<String, Any>
    ): Call<User>

    @POST("to-do/")
    @FormUrlEncoded
    fun makeTodo(
        @HeaderMap headers: Map<String, String>,
        @FieldMap params: HashMap<String, Any>
    ): Call<Any>

}